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
package org.apache.wicket.extensions.theme;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A color theme shared by the components of wicket-extensions.
 * <p>
 * A theme is a CSS class defining a set of custom properties (CSS variables) named
 * {@code --wicket-theme-*}, which the style sheets of the components read: for example
 * {@code --wicket-theme-primary} for the header of a table and the bar of a progress bar. Put on
 * an element with a {@link ThemeBehavior}, the theme applies to every themable component inside
 * it, so one theme on the page styles them all alike. Themes are switched by class only, so they
 * work under a strict Content Security Policy.
 * <p>
 * An application can define a theme of its own as a CSS class setting the same properties; the
 * properties are listed in {@link #CSS}.
 *
 * @since 11.0.0
 */
public enum Theme
{
	/** Dark blue headers on white and light blue rows. */
	DEFAULT("wicket-theme-default"),

	/** Bright blue. */
	BLUE("wicket-theme-blue"),

	/** Red. */
	RED("wicket-theme-red"),

	/** Grey. */
	GREY("wicket-theme-grey"),

	/** Green. */
	GREEN("wicket-theme-green"),

	/** Orange. */
	ORANGE("wicket-theme-orange"),

	/** Light blue, softer than {@link #BLUE}. */
	LIGHT_BLUE("wicket-theme-light-blue"),

	/** Light text on dark surfaces, with dark form controls. */
	DARK("wicket-theme-dark");

	/** The style sheet defining the themes. */
	public static final ResourceReference CSS = new CssResourceReference(Theme.class,
		"wicket-theme.css");

	private final String cssClass;

	Theme(String cssClass)
	{
		this.cssClass = cssClass;
	}

	/**
	 * @return the CSS class applying the theme
	 */
	public String getCssClass()
	{
		return cssClass;
	}
}
