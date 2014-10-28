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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;


/**
 * Upload example.
 * 
 * @author Eelco Hillenius
 */
public class MultiUploadPage extends WicketExamplePage
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
				private static final long serialVersionUID = 1L;

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
		// collection that will hold uploaded FileUpload objects
		private final Collection<FileUpload> uploads = new ArrayList<>();

		/**
		 * TODO
		 * 
		 * @return Collection
		 */
		public Collection<FileUpload> getUploads()
		{
			return uploads;
		}

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

			// Add one multi-file upload field
			add(new MultiFileUploadField("fileInput", new PropertyModel<Collection<FileUpload>>(
				this, "uploads"), 5, true));

			// Set maximum size to 100K for demo purposes
			setMaxSize(Bytes.kilobytes(100));
            setFileMaxSize(Bytes.kilobytes(30));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			for (FileUpload upload : uploads)
			{
				// Create a new file
				File newFile = new File(getUploadFolder(), upload.getClientFileName());

				// Check new file, delete if it allready existed
				checkFileExists(newFile);
				try
				{
					// Save to new file
					newFile.createNewFile();
					upload.writeTo(newFile);

					MultiUploadPage.this.info("saved file: " + upload.getClientFileName());
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Unable to write file");
				}
			}
		}
	}

	/** Reference to listview for easy access. */
	private final FileListView fileListView;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public MultiUploadPage(final PageParameters parameters)
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
