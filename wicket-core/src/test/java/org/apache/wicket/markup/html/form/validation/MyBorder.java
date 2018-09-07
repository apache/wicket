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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 *
 */
public class MyBorder extends Border
{
	private static final long serialVersionUID = 1L;
	private Form<Void> form;

	boolean hitOnSubmit = false;
	boolean hitOnError = false;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public MyBorder(String id)
	{
		super(id);

		form = new Form<Void>("form2");
		form.setOutputMarkupId(true);
		addToBorder(form);

		form.add(new FeedbackPanel("feedback"));

		form.add(new AjaxSubmitLink("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				info("onSubmit");
				hitOnSubmit = true;
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				error("onError");
				hitOnError = true;
				target.add(form);
			}
		});
	}

	@Override
	protected void onBeforeRender()
	{
		hitOnSubmit = false;
		hitOnError = false;

		super.onBeforeRender();
		form.add(getBodyContainer());
	}
}
