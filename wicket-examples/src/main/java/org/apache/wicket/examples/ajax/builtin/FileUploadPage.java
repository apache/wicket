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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Demos ajax handling of a multipart form
 * 
 * @author igor.vaynberg
 */
public class FileUploadPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private final FileUploadField file;
	private final TextField<String> text;

	/**
	 * Constructor
	 */
	public FileUploadPage()
	{

		// create a feedback panel
		final Component feedback = new FeedbackPanel("feedback").setOutputMarkupId(true);
		add(feedback);

		// create the form
		final Form<?> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
			 */
			@Override
			protected void onSubmit()
			{
				// display uploaded info
				info("Text: " + text.getModelObject());
				FileUpload upload = file.getFileUpload();
				if (upload == null)
				{
					info("No file uploaded");
				}
				else
				{
					info("File-Name: " + upload.getClientFileName() + " File-Size: " +
						Bytes.bytes(upload.getSize()).toString());
				}
			}
		};
		form.setMaxSize(Bytes.megabytes(1));
		add(form);

		// create a textfield to demo non-file content
		form.add(text = new TextField<>("text", new Model<String>()));
		text.add(StringValidator.minimumLength(2));

		// create the file upload field
		form.add(file = new FileUploadField("file"));

		form.add(new Label("max", new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return form.getMaxSize().toString();
			}
		}));

		form.add(new UploadProgressBar("progress", form, file));

		// create the ajax button used to submit the form
		form.add(new AjaxButton("ajaxSubmit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				info("This request was processed using AJAX");

				// ajax-update the feedback panel
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				// update feedback to display errors
				target.add(feedback);
			}

		});
	}
}
