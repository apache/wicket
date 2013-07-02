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
package org.apache.wicket.cdi;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;

/**
 * CdiWicketFilter is a Cdi Enabled version of WicketFilter. It uses the Managed Version of
 * {@link CdiWebApplicationFactory} therefore the WebApplication is also Managed via the Cdi container.
 *
 * @author jsarman
 */

public class CdiWicketFilter extends WicketFilter
{

	@Inject
	CdiWebApplicationFactory applicationFactory;

	public CdiWicketFilter()
	{
	}


	@Override
	protected IWebApplicationFactory getApplicationFactory()
	{
		return applicationFactory;
	}

}
