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
package org.apache.wicket.ajax.form;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * @author shuraa
 *
 */
public class AjaxFormValidatingBehaviorTestPage extends WebPage
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public AjaxFormValidatingBehaviorTestPage()
	{
		super();
		addFormWithOrdinalFeedbackPanel();
		addFormWithHiddenFeedbackPanel();
	}

	private void addFormWithOrdinalFeedbackPanel()
	{
		Form<Void> form = new Form<>("form1");
		form.add(new AjaxFormValidatingBehavior("blur"));
		form.add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(form)).setOutputMarkupId(true));
		add(form);

		TextField<String> input = new TextField<>("input", new Model<String>());
		input.setRequired(true);
		form.add(input);
	}
	
	private void addFormWithHiddenFeedbackPanel()
	{
		Form<Void> form = new Form<>("form2");
		form.add(new AjaxFormValidatingBehavior("blur"));
		form.add(new HiddenFeedbackPanel("feedback", form));
		add(form);

		TextField<String> input = new TextField<>("input", new Model<String>());
		input.setRequired(true);
		form.add(input);
	}

	/**
	 * Feedback panel that initially hidden.
	 */
	public static final class HiddenFeedbackPanel extends FeedbackPanel
	{

		private static final long serialVersionUID = 1L;

		/**
		 * @param id
		 * @param markupContainer 
		 */
		public HiddenFeedbackPanel(String id, MarkupContainer markupContainer)
		{
			super(id, new ContainerFeedbackMessageFilter(markupContainer));
			setOutputMarkupPlaceholderTag(true);
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();
			setVisible(anyMessage());
		}

	}

}
