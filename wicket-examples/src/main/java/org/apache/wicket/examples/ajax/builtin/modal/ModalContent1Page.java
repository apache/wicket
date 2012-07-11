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
package org.apache.wicket.examples.ajax.builtin.modal;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.WebPage;


/**
 * @author Matej Knopp
 * 
 */
public class ModalContent1Page extends WebPage
{

	/**
	 * 
	 * @param modalWindowPage
	 * @param window
	 */
	public ModalContent1Page(final PageReference modalWindowPage, final ModalWindow window)
	{
		add(new AjaxLink<Void>("closeOK")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (modalWindowPage != null)
					((ModalWindowPage)modalWindowPage.getPage()).setResult("Modal window 1 - close link OK");
				window.close(target);
			}
		});

		add(new AjaxLink<Void>("closeCancel")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (modalWindowPage != null)
					((ModalWindowPage)modalWindowPage.getPage()).setResult("Modal window 1 - close link Cancel");
				window.close(target);
			}
		});

		add(new DateTimeField("dateTimeField"));

		final ModalWindow modal;
		add(modal = new ModalWindow("modal"));

		modal.setCookieName("modal window 2");

		modal.setResizable(false);
		modal.setInitialWidth(30);
		modal.setInitialHeight(15);
		modal.setWidthUnit("em");
		modal.setHeightUnit("em");

		modal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);

		modal.setPageCreator(new ModalWindow.PageCreator()
		{
			@Override
			public Page createPage()
			{
				return new ModalContent2Page(modal);
			}
		});

		modal.setCloseButtonCallback(new ModalWindow.CloseButtonCallback()
		{
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget target)
			{
				target.appendJavaScript("alert('You can\\'t close this modal window using close button."
					+ " Use the link inside the window instead.');");
				return false;
			}
		});

		add(new AjaxLink<Void>("open")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				modal.show(target);
			}
		});

	}
}
