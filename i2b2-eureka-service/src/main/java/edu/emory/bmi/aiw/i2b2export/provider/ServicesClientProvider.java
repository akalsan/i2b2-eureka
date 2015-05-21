/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.emory.bmi.aiw.i2b2export.provider;

/*
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.emory.bmi.aiw.i2b2export.config.I2b2EurekaServicesProperties;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;

/**
 * 
 * @author hrathod
 */
@Singleton
public class ServicesClientProvider implements Provider<ServicesClient> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServicesClientProvider.class);
	private final ServicesClient client;

	@Inject
	public ServicesClientProvider(I2b2EurekaServicesProperties inProperties) {
		LOGGER.debug("service url = {}", inProperties.getServiceUrl());
		this.client = new ServicesClient(inProperties.getServiceUrl());
	}

	@Override
	public ServicesClient get() {
		return this.client;
	}

}
