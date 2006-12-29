/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.protocol.http.portlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.RequestCycle;
import wicket.request.compound.IResponseStrategy;

/**
 * @author Janne Hietam&auml;ki
 * 
 * IResponseStrategy which do not really render the page, but sets the needed
 * parameters as portlet render parameters
 */
public class PortletActionRequestResponseStrategy implements IResponseStrategy
{

	/** Logging object */
	private static final Logger log = LoggerFactory.getLogger(PortletActionRequestResponseStrategy.class);

	/*
	 * @see wicket.request.compound.IResponseStrategy#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		PortletRequestCodingStrategy strategy = (PortletRequestCodingStrategy)requestCycle
				.getProcessor().getRequestCodingStrategy();
		strategy.setRenderParameters((PortletRequestCycle)requestCycle, requestCycle
				.getRequestTarget());
	}

}
