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
package org.apache.wicket.extensions.ajax.markup.html.modal.theme;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Default theme for {@link ModalDialog}.
 * <p>
 * Inside a {@link org.apache.wicket.extensions.theme.Theme theme} of wicket-extensions the dialog
 * takes the theme's colors: the overlay dims with the theme's veil, the dialog has its surface,
 * text and border colors and its color scheme, and content using the CSS classes
 * {@code modal-dialog-header}, {@code modal-dialog-body} and {@code modal-dialog-footer} gets a
 * header in the theme's primary colors, a padded body and a footer in the toolbar color. Outside a
 * theme the dialog looks as without one.
 * 
 * @author svenmeier
 */
public class DefaultTheme extends Behavior
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference CSS = new CssResourceReference(DefaultTheme.class, "theme.css");

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		tag.append("class", "dialog-theme-default", " ");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(CSS));
	}
}
