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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.ajax.builtin.BasePage;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;


/**
 * @author Matej Knopp
 */
public class ModalWindowPage extends BasePage
{

	/**
	 */
	public ModalWindowPage()
	{
		final Label result;
		add(result = new Label("result", new PropertyModel<String>(this, "result")));
		result.setOutputMarkupId(true);

		/*
		 * First modal window
		 */

		final ModalWindow modal1;
		add(modal1 = new ModalWindow("modal1"));

		modal1.setCookieName("modal-1");

		modal1.setPageCreator(new ModalWindow.PageCreator()
		{
			@Override
			public Page createPage()
			{
				return new ModalContent1Page(ModalWindowPage.this.getPageReference(), modal1);
			}
		});
		modal1.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
		{
			@Override
			public void onClose(AjaxRequestTarget target)
			{
				target.add(result);
			}
		});
		modal1.setCloseButtonCallback(new ModalWindow.CloseButtonCallback()
		{
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget target)
			{
				setResult("Modal window 1 - close button");
				return true;
			}
		});

		add(new AjaxLink<Void>("showModal1")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				modal1.show(target);
			}
		});

		/*
		 * Second modal window
		 */

		final ModalWindow modal2;
		add(modal2 = new ModalWindow("modal2"));

		modal2.setContent(new ModalPanel1(modal2.getContentId()));
		modal2.setTitle("This is modal window with panel content.");
		modal2.setCookieName("modal-2");

		modal2.setCloseButtonCallback(new ModalWindow.CloseButtonCallback()
		{
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget target)
			{
				setResult("Modal window 2 - close button");
				return true;
			}
		});

		modal2.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
		{
			@Override
			public void onClose(AjaxRequestTarget target)
			{
				target.add(result);
			}
		});

		add(new AjaxLink<Void>("showModal2")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				modal2.show(target);
			}
		});
	}

	/**
	 * @return the result
	 */
	public String getResult()
	{
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result)
	{
		this.result = result;
	}

	private String result;

}
