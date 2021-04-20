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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileDescription;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FilesSelectedBehavior;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
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

	private static class SingleFileUploadSamplePanel extends Panel {

		private final FileUploadField file;
		private final TextField<String> text;
		private final Label selectedFileInfo;
		private final AjaxButton ajaxSubmit;
		private final Button submit;
		private String fileInfo;

		public SingleFileUploadSamplePanel(String id) {
			super(id);

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

			// create a submit button
			form.add(submit = new Button("submit"));
			submit.setOutputMarkupId(true);

			// create the ajax button used to submit the form
			form.add(ajaxSubmit = new AjaxButton("ajaxSubmit")
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
			ajaxSubmit.setOutputMarkupId(true);

			file.add(FilesSelectedBehavior.onSelected(
					(AjaxRequestTarget target, List<FileDescription> fileDescriptions)->
					{
						FileDescription fileDescription = fileDescriptions.get(0);
						Bytes bytes = Bytes.bytes(fileDescription.getFileSize());
						fileInfo = "File " + fileDescription.getFileName() +
								" (with size " + bytes + ") was selected at client side. "
								+ "File was last modified at: " + fileDescription.getLastModified()
								+ " and is of type " + fileDescription.getMimeType() +
								". It has not been uploaded yet. ";
						if (bytes.greaterThan(form.getMaxSize()))
						{
							fileInfo += " File exceeds max allowed size.";
							// disable buttons as file is not valid
							submit.setEnabled(false);
							ajaxSubmit.setEnabled(false);
						}
						else
						{
							fileInfo += " You can click on buttons bellow in order to upload it.";
							// enable buttons as file is valid
							submit.setEnabled(true);
							ajaxSubmit.setEnabled(true);
						}
						target.add(selectedFileInfo, submit, ajaxSubmit);
					}));
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

	private static class MultipleFileUploadsSamplePanel extends Panel {

		private static class DataProvider extends SortableDataProvider<FileDescription, String> {

			private List<FileDescription> fileDescriptions;

			public void setFileDescriptions(List<FileDescription> fileDescriptions) {
				this.fileDescriptions = new ArrayList<>(fileDescriptions);
			}

			@Override
			public Iterator<? extends FileDescription> iterator(long first, long count) {
				if (this.fileDescriptions == null) {
					return EmptyIterator.emptyIterator();
				}
				return fileDescriptions.listIterator();
			}

			@Override
			public long size() {
				if (this.fileDescriptions == null) {
					return 0L;
				}
				return this.fileDescriptions.size();
			}

			@Override
			public IModel<FileDescription> model(FileDescription object) {
				return Model.of(object);
			}

			@Override
			public void detach() {
				super.detach();
				this.fileDescriptions = null;
			}
		}

		private final FileUploadField file;
		private final TextField<String> text;
		private DataProvider dataProvider;
		private AjaxFallbackDefaultDataTable<FileDescription, String> selectedFileInfo;
		private final AjaxButton ajaxSubmit;
		private final Button submit;

		public MultipleFileUploadsSamplePanel(String id) {
			super(id);

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

			List<IColumn<FileDescription, String>> columns = new ArrayList<>();
			columns.add(new PropertyColumn<>(Model.of("File Name"), "fileName"));
			columns.add(new PropertyColumn<>(Model.of("Size"), "fileSize"));
			columns.add(new PropertyColumn<>(Model.of("Last Modified"), "lastModified"));
			columns.add(new PropertyColumn<>(Model.of("MIME Type"), "mimeType"));
			selectedFileInfo = new AjaxFallbackDefaultDataTable<FileDescription, String>("selectedFileInfo", columns, dataProvider = new DataProvider(), 100) {
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(dataProvider.size() > 0);
				}
			};
			form.add(selectedFileInfo);
			selectedFileInfo.setOutputMarkupPlaceholderTag(true);

			// create a textfield to demo non-file content
			form.add(text = new TextField<>("text", Model.of()));
			text.add(StringValidator.minimumLength(2));

			// create the file upload field
			form.add(file = new FileUploadField("file"));

			form.add(new Label("max", form::getMaxSize));

			form.add(new UploadProgressBar("progress", form, file));

			// create a submit button
			form.add(submit = new Button("submit"));
			submit.setOutputMarkupId(true);

			// create the ajax button used to submit the form
			form.add(ajaxSubmit = new AjaxButton("ajaxSubmit")
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
			ajaxSubmit.setOutputMarkupId(true);
			file.add(FilesSelectedBehavior.onSelected(
					(AjaxRequestTarget target, List<FileDescription> fileDescriptions) ->
			{
				dataProvider.setFileDescriptions(fileDescriptions);
				Bytes bytes = Bytes.bytes(fileDescriptions.stream().mapToLong(FileDescription::getFileSize).sum());
				if (bytes.greaterThan(form.getMaxSize()))
				{
					form.error("Total file size exceeds max allowed size.");
					// disable buttons as file is not valid
					submit.setEnabled(false);
					ajaxSubmit.setEnabled(false);
				}
				else
				{
					form.info("You can click on buttons bellow in order to upload selected files.");
					// enable buttons as file is valid
					submit.setEnabled(true);
					ajaxSubmit.setEnabled(true);
				}
				target.add(selectedFileInfo, submit, ajaxSubmit, feedback);
			}));
		}
	}
	/**
	 * Constructor
	 */
	public FileUploadPage()
	{
		// sample of a single uploaded file.
		add(new SingleFileUploadSamplePanel("singleFileUpload"));
		add(new MultipleFileUploadsSamplePanel("multipleFileUpload"));


	}
}
