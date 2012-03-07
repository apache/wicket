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
package org.apache.wicket.extensions.markup.html.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;

/**
 * Interface used to represent a single tab in a TabbedPanel
 * 
 * @see org.apache.wicket.extensions.markup.html.tabs.TabbedPanel
 * @see org.apache.wicket.extensions.markup.html.tabs.AbstractTab
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface ITab extends IClusterable
{
	/**
	 * @return IModel used to represent the title of the tab. Must contain a string.
	 */
	IModel<String> getTitle();

	/**
	 * @param containerId
	 *            returned panel MUST have this id
	 * @return a container object (e.g. Panel or Fragment) that will be placed as the content of the
	 *         tab
	 */
	WebMarkupContainer getPanel(final String containerId);

	/**
	 * Returns whether this tab should be visible
	 * 
	 * @return whether this tab should be visible
	 */
	boolean isVisible();
}