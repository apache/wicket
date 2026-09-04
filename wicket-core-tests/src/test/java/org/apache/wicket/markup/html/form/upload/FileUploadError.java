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
package org.apache.wicket.markup.html.form.upload;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Homepage
 */
public class FileUploadError extends WebPage
{
	private static final long serialVersionUID = 1L;
	static final String THIS_VALUE_SHOULD_THROW_EXCEPTION = "test ex";

	/**
	 */
	public FileUploadError()
	{
		add(new Label("message",
			"If you see this message wicket is properly configured and running."));

		Form<?> form = new Form<Object>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				super.onSubmit();
				setResponsePage(FileUploadError.class);
			}
		};

		add(form);

		// inputField
		TextField<String> inputField = new TextField<String>("textField", new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void setObject(String value)
			{
				if (THIS_VALUE_SHOULD_THROW_EXCEPTION.equals(value))
				{
					throw new RuntimeException("Special value: " +
						THIS_VALUE_SHOULD_THROW_EXCEPTION);
				}
				super.setObject(value);
			}
		});

		inputField.add(new StringValidator(3, 10));
		inputField.setRequired(true);
		form.add(inputField);

		// file upload
		form.add(new FileUploadField("fileUpload", new ListModel<FileUpload>()));
		// feedback
		form.add(new FeedbackPanel("feedback"));
	}
}
