/*
 * $Id: UploadPage.java 4619 2006-02-23 14:25:06 -0800 (Thu, 23 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-23 14:25:06 -0800 (Thu, 23 Feb
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.upload.FileUpload;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.util.file.Files;
import wicket.util.file.Folder;
import wicket.util.lang.Bytes;

/**
 * Upload example.
 * 
 * @author Eelco Hillenius
 */
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
		 * @param parent
		 *            The parent
		 * 
		 * @param name
		 *            Component name
		 * @param files
		 *            The file list model
		 */
		public FileListView(MarkupContainer parent, String name, final List<File> files)
		{
			super(parent, name, files);
		}

		/**
		 * @see ListView#populateItem(ListItem)
		 */
		@Override
		protected void populateItem(ListItem<File> listItem)
		{
			final File file = listItem.getModelObject();
			new Label(listItem, "file", file.getName());
			new Link(listItem, "delete")
			{
				@Override
				public void onClick()
				{
					Files.remove(file);
					refreshFiles();
					UploadPage.this.info("Deleted " + file);
				}
			};
		}
	}

	/**
	 * Form for uploads.
	 */
	private class FileUploadForm extends Form
	{
		private FileUploadField fileUploadField;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent
		 * 
		 * @param name
		 *            Component name
		 */
		public FileUploadForm(MarkupContainer parent, String name)
		{
			super(parent, name);

			// set this form to multipart mode (allways needed for uploads!)
			setMultiPart(true);

			// Add one file input field
			fileUploadField = new FileUploadField(this, "fileInput");

			// Set maximum size to 100K for demo purposes
			setMaxSize(Bytes.kilobytes(100));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
			final FileUpload upload = fileUploadField.getFileUpload();
			if (upload != null)
			{
				// Create a new file
				File newFile = new File(uploadFolder, upload.getClientFileName());

				// Check new file, delete if it allready existed
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
					throw new IllegalStateException("Unable to write file");
				}

				// refresh the file list view
				refreshFiles();
			}
		}
	}

	/** Log. */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(UploadPage.class);

	/** Reference to listview for easy access. */
	private FileListView fileListView;

	/** List of files, model for file table. */
	private List<File> files = new ArrayList<File>();

	/** Upload folder */
	private Folder uploadFolder;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public UploadPage(final PageParameters parameters)
	{
		// Set upload folder to tempdir + 'wicket-uploads'.
		this.uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");

		// Ensure folder exists
		uploadFolder.mkdirs();

		// Create feedback panels
		new FeedbackPanel(this, "uploadFeedback");

		// Add simple upload form, which is hooked up to its feedback panel by
		// virtue of that panel being nested in the form.
		new FileUploadForm(this, "simpleUpload");

		// Add folder view
		new Label(this, "dir", uploadFolder.getAbsolutePath());
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
		fileListView = new FileListView(this, "fileList", files);

		// Add upload form with ajax progress bar
		final FileUploadForm ajaxSimpleUploadForm = new FileUploadForm(this, "ajax-simpleUpload");
		new UploadProgressBar(ajaxSimpleUploadForm, "progress", ajaxSimpleUploadForm);

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

	/**
	 * Refresh file list.
	 */
	private void refreshFiles()
	{
		fileListView.modelChanging();
		files.clear();
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
	}
}