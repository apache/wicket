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

/**
 * A look for the tabs of a {@link TabbedPanel}, applied by a {@link TabsStyleBehavior}. Every
 * look takes its colors from the {@link org.apache.wicket.extensions.theme.Theme theme} the panel
 * is in, and has colors of its own outside a theme.
 *
 * @since 11.0.0
 */
public enum TabsStyle
{
	/** Tabs on top of the panel, the selected one joined to it. */
	TABS("wicket-tabs-tabs"),

	/** Rounded buttons, the selected one filled with the theme's primary color. */
	PILLS("wicket-tabs-pills"),

	/** Plain titles, the selected one underlined in the theme's primary color. */
	UNDERLINE("wicket-tabs-underline");

	private final String cssClass;

	TabsStyle(String cssClass)
	{
		this.cssClass = cssClass;
	}

	/**
	 * @return the CSS class putting the look on a tabbed panel
	 */
	public String getCssClass()
	{
		return cssClass;
	}
}
