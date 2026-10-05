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
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.AjaxFileDropBehavior;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.extensions.markup.html.icon.SvgIcon;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
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

	/**
	 * A form with a text field, a file field and an upload progress bar, whose submit buttons are
	 * enabled only while the file field holds files that can be uploaded.
	 */
	private abstract static class UploadSamplePanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		final Component feedback;

		final Form<?> form;

		final TextField<String> text;

		final FileUploadField file;

		final Button submit;

		final AjaxButton ajaxSubmit;

		private boolean uploadable;

		UploadSamplePanel(String id)
		{
			super(id);

			feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this))
				.setOutputMarkupId(true);
			add(feedback);

			form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					info("Text: " + text.getModelObject());
					List<FileUpload> uploads = file.getFileUploads();
					if (uploads.isEmpty())
					{
						info("No file uploaded");
					}
					for (FileUpload upload : uploads)
					{
						info("File-Name: " + upload.getClientFileName() + " File-Size: " +
							Bytes.bytes(upload.getSize()).toString());
					}
				}
			};
			form.setMaxSize(Bytes.megabytes(10));
			add(form);

			form.add(text = new TextField<>("text", Model.of()));
			text.add(StringValidator.minimumLength(2));

			form.add(file = new FileUploadField("file")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onBeforeRender()
				{
					// a rendered file field is empty
					uploadable = false;
					super.onBeforeRender();
				}
			});

			form.add(new Label("max", form::getMaxSize));

			form.add(new UploadProgressBar("progress", form, file));

			form.add(submit = new Button("submit")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure()
				{
					super.onConfigure();
					setEnabled(uploadable);
				}
			});
			submit.add(newIcon(SvgIcon.UPLOAD));
			submit.setOutputMarkupId(true);

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
				protected void onConfigure()
				{
					super.onConfigure();
					setEnabled(uploadable);
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					info("This request was processed using AJAX");

					target.add(feedback);
				}

				@Override
				protected void onError(AjaxRequestTarget target)
				{
					target.add(feedback);
				}
			});
			ajaxSubmit.add(newIcon(SvgIcon.CLOUD_ARROW_UP));
			ajaxSubmit.setOutputMarkupId(true);

			file.add(FilesSelectedBehavior.onSelected(
				(AjaxRequestTarget target, List<FileDescription> fileDescriptions) -> {
					Bytes size = Bytes.bytes(fileDescriptions.stream()
						.mapToLong(FileDescription::getFileSize)
						.sum());
					uploadable = !fileDescriptions.isEmpty() && !size.greaterThan(form.getMaxSize());
					onFilesSelected(target, fileDescriptions, uploadable);
					target.add(submit, ajaxSubmit);
				}));
		}

		private static Component newIcon(IIcon icon)
		{
			return new Label("icon", icon.getMarkup()).setEscapeModelStrings(false);
		}

		/**
		 * Tells the user about the files selected in the file field, which have not been uploaded
		 * yet.
		 *
		 * @param target
		 *            the request target
		 * @param fileDescriptions
		 *            the selected files
		 * @param uploadable
		 *            whether the files are within the maximum size, which enables the buttons
		 */
		abstract void onFilesSelected(AjaxRequestTarget target,
			List<FileDescription> fileDescriptions, boolean uploadable);
	}

	private static class SingleFileUploadSamplePanel extends UploadSamplePanel
	{
		private static final long serialVersionUID = 1L;

		private final Label selectedFileInfo;

		private String fileInfo;

		public SingleFileUploadSamplePanel(String id)
		{
			super(id);

			form.add(selectedFileInfo = new Label("selectedFileInfo", (IModel<String>) () -> fileInfo) {
				@Override
				protected void onAfterRender() {
					super.onAfterRender();
					fileInfo = null;
				}
			});
			selectedFileInfo.setOutputMarkupId(true);

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

		@Override
		void onFilesSelected(AjaxRequestTarget target, List<FileDescription> fileDescriptions,
			boolean uploadable)
		{
			FileDescription fileDescription = fileDescriptions.get(0);
			Bytes bytes = Bytes.bytes(fileDescription.getFileSize());
			fileInfo = "File " + fileDescription.getFileName() +
					" (with size " + bytes + ") was selected at client side. "
					+ "File was last modified at: " + fileDescription.getLastModified()
					+ " and is of type " + fileDescription.getMimeType() +
					". It has not been uploaded yet. ";
			if (uploadable)
			{
				fileInfo += " You can click on buttons bellow in order to upload it.";
			}
			else
			{
				fileInfo += " File exceeds max allowed size.";
			}
			target.add(selectedFileInfo);
		}
	}

	private static class MultipleFileUploadsSamplePanel extends UploadSamplePanel
	{
		private static final long serialVersionUID = 1L;

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

		private final DataProvider dataProvider;

		private final AjaxFallbackDefaultDataTable<FileDescription, String> selectedFileInfo;

		public MultipleFileUploadsSamplePanel(String id)
		{
			super(id);

			List<IColumn<FileDescription, String>> columns = new ArrayList<>();
			columns.add(new PropertyColumn<>(Model.of("File Name"), "fileName"));
			columns.add(new PropertyColumn<>(Model.of("Size"), "fileSize"));
			columns.add(new PropertyColumn<>(Model.of("Last Modified"), "lastModified"));
			columns.add(new PropertyColumn<>(Model.of("MIME Type"), "mimeType"));
			selectedFileInfo = new AjaxFallbackDefaultDataTable<>("selectedFileInfo", columns, dataProvider = new DataProvider(), 100) {
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(dataProvider.size() > 0);
				}
			};
			form.add(selectedFileInfo);
			selectedFileInfo.setOutputMarkupPlaceholderTag(true);
		}

		@Override
		void onFilesSelected(AjaxRequestTarget target, List<FileDescription> fileDescriptions,
			boolean uploadable)
		{
			dataProvider.setFileDescriptions(fileDescriptions);
			if (uploadable)
			{
				form.info("You can click on buttons bellow in order to upload selected files.");
			}
			else
			{
				form.error("Total file size exceeds max allowed size.");
			}
			target.add(selectedFileInfo, feedback);
		}
	}

	/**
	 * Constructor
	 */
	public FileUploadPage()
	{
		WebMarkupContainer themed = newThemedContainer("themed");
		add(themed);
		add(newThemeChoice("theme", themed));

		add(new AjaxCheckBox("slowUpload",
			LambdaModel.of(SlowUploadWebRequest::isSlow, SlowUploadWebRequest::setSlow))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
			}
		});

		themed.add(new SingleFileUploadSamplePanel("singleFileUpload"));
		themed.add(new MultipleFileUploadsSamplePanel("multipleFileUpload"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(FileUploadPage.class, "FileUploadPage.css")));
	}
}
