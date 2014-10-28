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
package org.apache.wicket.examples.upload;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upload example.
 * 
 * @author Eelco Hillenius
 */
@SuppressWarnings("serial")
public class UploadPage extends WicketExamplePage
{
	/**
	 * List view for files in upload folder.
	 */
	private class FileListView extends ListView<File>
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 * @param files
		 *            The file list model
		 */
		public FileListView(String name, final IModel<List<File>> files)
		{
			super(name, files);
		}

		/**
		 * @see ListView#populateItem(ListItem)
		 */
		@Override
		protected void populateItem(ListItem<File> listItem)
		{
			final File file = listItem.getModelObject();
			listItem.add(new Label("file", file.getName()));
			listItem.add(new Link<Void>("delete")
			{
				@Override
				public void onClick()
				{
					Files.remove(file);
					info("Deleted " + file);
				}
			});
		}
	}

	/**
	 * Form for uploads.
	 */
	private class FileUploadForm extends Form<Void>
	{
		FileUploadField fileUploadField;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 */
		public FileUploadForm(String name)
		{
			super(name);

			// set this form to multipart mode (always needed for uploads!)
			setMultiPart(true);

			// Add one file input field
			add(fileUploadField = new FileUploadField("fileInput"));

			// Set maximum size to 100K for demo purposes
			setMaxSize(Bytes.kilobytes(100));

			// Set maximum size per file to 90K for demo purposes
			setFileMaxSize(Bytes.kilobytes(90));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			final List<FileUpload> uploads = fileUploadField.getFileUploads();
			if (uploads != null)
			{
				for (FileUpload upload : uploads)
				{
					// Create a new file
					File newFile = new File(getUploadFolder(), upload.getClientFileName());

					// Check new file, delete if it already existed
					checkFileExists(newFile);
					try
					{
						// Save to new file
						newFile.createNewFile();
						upload.writeTo(newFile);

						UploadPage.this.info("saved file: " + upload.getClientFileName());
					}
					catch (Exception e)
					{
						throw new IllegalStateException("Unable to write file", e);
					}
				}
			}
		}
	}

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(UploadPage.class);

	/** Reference to listview for easy access. */
	private final FileListView fileListView;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public UploadPage(final PageParameters parameters)
	{
		Folder uploadFolder = getUploadFolder();

		// Create feedback panels
		final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");

		// Add uploadFeedback to the page itself
		add(uploadFeedback);

		// Add simple upload form, which is hooked up to its feedback panel by
		// virtue of that panel being nested in the form.
		final FileUploadForm simpleUploadForm = new FileUploadForm("simpleUpload");
		add(simpleUploadForm);

		// Add folder view
		add(new Label("dir", uploadFolder.getAbsolutePath()));
		fileListView = new FileListView("fileList", new LoadableDetachableModel<List<File>>()
		{
			@Override
			protected List<File> load()
			{
				return Arrays.asList(getUploadFolder().listFiles());
			}
		});
		add(fileListView);

		// Add upload form with progress bar
		final FileUploadForm progressUploadForm = new FileUploadForm("progressUpload");

		progressUploadForm.add(new UploadProgressBar("progress", progressUploadForm,
			progressUploadForm.fileUploadField));
		add(progressUploadForm);

		// Add upload form that uses HTML5 <input type="file" multiple />, so it can upload
		// more than one file in browsers which support "multiple" attribute
		final FileUploadForm html5UploadForm = new FileUploadForm("html5Upload");
		add(html5UploadForm);
	}

	/**
	 * Check whether the file allready exists, and if so, try to delete it.
	 * 
	 * @param newFile
	 *            the file to check
	 */
	private void checkFileExists(File newFile)
	{
		if (newFile.exists())
		{
			// Try to delete the file
			if (!Files.remove(newFile))
			{
				throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
			}
		}
	}

	private Folder getUploadFolder()
	{
		return ((UploadApplication)Application.get()).getUploadFolder();
	}
}
