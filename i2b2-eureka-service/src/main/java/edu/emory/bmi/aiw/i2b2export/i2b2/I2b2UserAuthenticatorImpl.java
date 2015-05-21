package edu.emory.bmi.aiw.i2b2export.i2b2;

/*
 * #%L
 * i2b2 Export Service
 * %%
 * Copyright (C) 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import edu.emory.bmi.aiw.i2b2export.comm.I2b2AuthMetadata;
import edu.emory.bmi.aiw.i2b2export.config.I2b2EurekaServicesProperties;
import edu.emory.bmi.aiw.i2b2export.xml.I2b2ExportServiceXmlException;
import edu.emory.bmi.aiw.i2b2export.xml.XmlUtil;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the i2b2 user authentication interface. It authenticates i2b2 users by filling out the i2b2 user
 * authentication XML template and sending it to the i2b2 service defined in the application's properties file (see:
 * {@link I2b2CommUtil}).
 *
 * @author Michel Mansour
 * @since 1.0
 */
public final class I2b2UserAuthenticatorImpl implements I2b2UserAuthenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(I2b2UserAuthenticatorImpl.class);

	private final Configuration config;
	private final I2b2EurekaServicesProperties properties;
	private final I2b2XmlPostSupport i2b2XmlPostSupport;

	/**
	 * Default no-arg constructor.
	 */
	@Inject
	public I2b2UserAuthenticatorImpl(I2b2EurekaServicesProperties inProperties,
			I2b2XmlPostSupport inI2b2XmlPostSupport) {
		this.config = new Configuration();
		this.config.setClassForTemplateLoading(this.getClass(), "/");
		this.config.setObjectWrapper(new DefaultObjectWrapper());
		this.i2b2XmlPostSupport = inI2b2XmlPostSupport;
		this.properties = inProperties;
	}

	@Override
	public boolean authenticateUser(I2b2AuthMetadata authMetadata) throws I2b2ExportServiceXmlException {
		try {
			LOGGER.debug("Attempting to authenticate i2b2 user: {} with password node: {} in domain {} for project {}",
					new String[]{
							authMetadata.getUsername(), authMetadata.getPasswordNode(),
							authMetadata.getDomain(), authMetadata.getProjectId()});

			Template tmpl = this.config.getTemplate(I2b2CommUtil.TEMPLATES_DIR + "/i2b2_user_auth.ftl");
			StringWriter writer = new StringWriter();

			DateFormat sdf = new SimpleDateFormat(I2b2CommUtil.I2B2_DATE_FMT);
			Date now = new Date();
			String messageId = this.i2b2XmlPostSupport.generateMessageId();

			Map<String, Object> params = new HashMap<>();
			params.put("redirectHost", this.properties.getI2b2ServiceHostUrl());
			params.put("domain", authMetadata.getDomain());
			params.put("username", authMetadata.getUsername());
			params.put("passwordNode", authMetadata.getPasswordNode());
			params.put("messageId", messageId);
			params.put("messageDatetime", sdf.format(now));
			params.put("projectId", authMetadata.getProjectId());

			tmpl.process(params, writer);
			Document respXml = this.i2b2XmlPostSupport.postXmlToI2b2(writer.toString());

			String status = (String) XmlUtil.evalXPath(respXml,
					"//response_header/result_status/status/@type",
					XPathConstants.STRING);

			LOGGER.debug("Received authentication status: {}", status);

			return "DONE".equalsIgnoreCase(status);
		} catch (IOException | XPathExpressionException | SAXException | ParserConfigurationException | TemplateException e) {
			throw new I2b2ExportServiceXmlException(e);
		}

	}
}