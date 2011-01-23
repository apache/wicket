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
package org.apache.wicket.devutils.inspector;

import org.apache.wicket.Application;
import org.apache.wicket.devutils.DevUtilsPanel;
import org.apache.wicket.markup.html.basic.Label;

/**
 * A Wicket panel that shows interesting information about a given Wicket session.
 * 
 * @author Jonathan Locke
 */
public final class ApplicationView extends DevUtilsPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            Component id
	 * @param application
	 *            The application to view
	 */
	public ApplicationView(final String id, final Application application)
	{
		super(id);

		// Basic attributes
		add(new Label("name", application.getName()));
		add(new Label("componentUseCheck", "" +
			application.getDebugSettings().getComponentUseCheck()));
		add(new Label("compressWhitespace", "" +
			application.getMarkupSettings().getCompressWhitespace()));
		add(new Label("stripComments", "" + application.getMarkupSettings().getStripComments()));
		add(new Label("stripWicketTags", "" + application.getMarkupSettings().getStripWicketTags()));
		add(new Label("bufferResponse", "" +
			application.getRequestCycleSettings().getBufferResponse()));
		add(new Label("resourcePollFrequency", "" +
			application.getResourceSettings().getResourcePollFrequency()));
		add(new Label("versionPages", "" + application.getPageSettings().getVersionPagesByDefault()));
	}
}
