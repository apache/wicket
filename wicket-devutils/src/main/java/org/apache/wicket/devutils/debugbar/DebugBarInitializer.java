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
package org.apache.wicket.devutils.debugbar;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;

/**
 * Debug bar module initializer
 * 
 * @author igor.vaynberg
 * 
 */
public class DebugBarInitializer implements IInitializer
{

	/** {@inheritDoc} */
	@Override
	public void init(final Application application)
	{
		if (application.getDebugSettings().isDevelopmentUtilitiesEnabled())
		{
			// register standard debug contributors
			DebugBar.registerContributor(VersionDebugContributor.DEBUG_BAR_CONTRIB, application);
			DebugBar.registerContributor(InspectorDebugPanel.DEBUG_BAR_CONTRIB, application);
			DebugBar.registerContributor(SessionSizeDebugPanel.DEBUG_BAR_CONTRIB, application);
			DebugBar.registerContributor(PageSizeDebugPanel.DEBUG_BAR_CONTRIB, application);
		}
	}

	@Override
	public String toString()
	{
		return "DevUtils DebugBar Initializer";
	}

	/** {@inheritDoc} */
	@Override
	public void destroy(final Application application)
	{
	}

}
