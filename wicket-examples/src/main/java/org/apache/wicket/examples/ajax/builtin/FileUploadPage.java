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

import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
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
	private Label selectedFileInfo;
	private String fileInfo;

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
		form.add(text = new TextField<>("text", Model.of()));
		text.add(StringValidator.minimumLength(2));

		// create the file upload field
		form.add(file = new FileUploadField("file"));
		file.add(new FileUploadField.OnFileSelectedBehavior()
		{
			@Override
			protected void onFileSelected(AjaxRequestTarget target, String fileName, Long fileSize, Date lastModified, String mimeType)
			{
				Bytes bytes = Bytes.bytes(fileSize);
				fileInfo = "File " + fileName + " (with size " + bytes + ") was selected at client side. "
						+ "File was last modified at: " + lastModified
						+ " and is of type " + mimeType +
						". It has not been uploaded yet. ";
				if (bytes.greaterThan(form.getMaxSize())) {
					fileInfo += " File exceeds max allowed size.";
				} else {
					fileInfo += " You can click on buttons bellow in order to upload it.";
				}
				target.add(selectedFileInfo);
			}
		});

		add(selectedFileInfo = new Label("selectedFileInfo", (IModel<String>) () -> fileInfo) {
			@Override
			protected void onAfterRender() {
				super.onAfterRender();
				fileInfo = null;
			}
		});
		selectedFileInfo.setOutputMarkupId(true);
		form.add(selectedFileInfo);

		form.add(new Label("max", form::getMaxSize));

		form.add(new UploadProgressBar("progress", form, file));

		// create the ajax button used to submit the form
		form.add(new AjaxButton("ajaxSubmit")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Need to trigger submit to initiate progressbar. 
			 */
			@Override
			protected boolean shouldTriggerJavaScriptSubmitEvent()
			{
				return true;
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				info("This request was processed using AJAX");

				// ajax-update the feedback panel
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				// update feedback to display errors
				target.add(feedback);
			}

		});
		
		WebMarkupContainer drop = new WebMarkupContainer("drop");
		drop.add(new AjaxFileDropBehavior() {
			protected void onFileUpload(AjaxRequestTarget target, List<FileUpload> files) {
			    
				// display uploaded info
				if (files == null || files.isEmpty())
				{
					info("No file uploaded");
				}
				else
				{
				    for (FileUpload file : files) {
				    	info("File-Name: " + file.getClientFileName() + " File-Size: " +
				    		Bytes.bytes(file.getSize()).toString());
				    }
				}
				
				target.add(feedback);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, FileUploadException fux)
			{
				info(fux.getMessage());
				
				target.add(feedback);				
			}
		});
		add(drop);
	}
}
