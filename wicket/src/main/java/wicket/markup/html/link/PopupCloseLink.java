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
package wicket.markup.html.link;

import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.IModel;

/**
 * Closes a popup window and cleans up any related session page map for the
 * popup.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 */
public class PopupCloseLink<T> extends Link<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * ClosePopupPage closes the popup window.
	 */
	public static final class ClosePopupPage extends WebPage
	{
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 */
	public PopupCloseLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 * @param object
	 */
	public PopupCloseLink(MarkupContainer parent, String id, IModel<T> object)
	{
		super(parent, id, object);
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		// Remove the popup's page map from the session
		getPage().getPageMap().remove();

		// Web page closes window using javascript code in PopupCloseLink$1.html
		setResponsePage(new ClosePopupPage());
	}
}
