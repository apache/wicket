/*
 * $Id$
 * $Revision$ $Date$
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.markup.html.form.upload.UploadForm;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;
import wicket.util.file.Files;
import wicket.util.file.Folder;

/**
 * Upload example.
 * 
 * @author Eelco Hillenius
 */
public class UploadPage extends WicketExamplePage
{
	/** Log. */
	private static Log log = LogFactory.getLog(UploadPage.class);

	/** List of files, model for file table. */
	private List files = new ArrayList();

	/** Reference to listview for easy access. */
	private FileListView fileListView;

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
		// set upload folder to tempdir + 'wicket-uploads'.
		this.uploadFolder = uploadFolder = new Folder(
				System.getProperty("java.io.tmpdir"), "wicket-uploads");

		// Create feedback panels
		final FeedbackPanel simpleUploadFeedback = new FeedbackPanel("simpleUploadFeedback");
		final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");

		// Add uploadFeedback to the page itself
		add(uploadFeedback);

		// Add simple upload form, which is hooked up to its feedback panel by
		// virtue of that panel being nested in the form.
		final FileUploadForm simpleUploadForm = new FileUploadForm("simpleUpload");
		simpleUploadForm.add(simpleUploadFeedback);
		add(simpleUploadForm);

		// Add folder view
		add(new Label("dir", uploadFolder.getAbsolutePath()));
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
		fileListView = new FileListView("fileList", files);
		add(fileListView);
	}

	/**
	 * Refresh file list.
	 */
	private void refreshFiles()
	{
		files.clear();
		files.addAll(Arrays.asList(uploadFolder.listFiles()));
		fileListView.modelChangedStructure();
	}

	/**
	 * Check whether the file allready exists, and if so, try to delete it.
	 * @param newFile the file to check
	 */
	private void checkFileExists(File newFile)
	{
		if(newFile.exists())
		{
			// Try to delete the file
			if (!Files.delete(newFile))
			{
				throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Form for uploads.
	 */
	private class FileUploadForm extends UploadForm
	{
		/** model to put the reference to the uploaded file in. */
		private final Model fileModel = new Model();

		/**
		 * Construct.
		 * @param name Component name
		 */
		public FileUploadForm(String name)
		{
			super(name);

			// add one file input field
			add(new FileUploadField("fileInput", fileModel));
		}

		/**
		 * @see wicket.markup.html.form.upload.UploadForm#onSubmit()
		 */
		protected void onSubmit()
		{
			// get the uploaded file
			FileItem item = (FileItem)fileModel.getObject(this);

			if(item != null)
			{
				// the original file name
				String fileName = item.getName();
	
				// hack a bit to get at least an acceptable file name
				fileName = afterLast(fileName, ':');
				fileName = afterLast(fileName, '\\');
				fileName = afterLast(fileName, '/');
				log.info("uploaded item: " + fileName);
	
				// create a new file
				File newFile = new File(uploadFolder, fileName);
	
				// check new file, delete if it allready existed
				checkFileExists(newFile);
				try
				{
					newFile.createNewFile(); // create it
					item.write(newFile); // write the uploaded file to our new file
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Unable to write file");
				}
	
				// refresh the file list view
				refreshFiles();
			}
		}

		/**
		 * Gets the last part of the string after c or the string itself when c is
		 * not found.
		 * @param s the string
		 * @param c the char
		 * @return part of string
		 */
		private String afterLast(final String s, final char c)
		{
			final int index = s.lastIndexOf(c);
			if (index == -1)
			{
				return s;
			}
			return s.substring(index + 1);
		}
	}

	/**
	 * List view for files in upload folder.
	 */
	private class FileListView extends ListView
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 * @param files
		 *            The file list model
		 */
		public FileListView(String name, final List files)
		{
			super(name, files);
		}

		/**
		 * @see ListView#populateItem(ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			final File file = (File)listItem.getModelObject();
			listItem.add(new Label("file", file.getName()));
			listItem.add(new Link("delete")
			{
				public void onClick()
				{
					log.info("Deleting " + file);
					Files.delete(file);
					refreshFiles();
				}
			});
		}
	}
}