/*
 * $Id$ $Revision$
 * $Date$
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
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.upload.FileUploadForm;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
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

	/** Upload folder we are working with. */
	private Folder uploadFolder;

	/** List of files, model for file table. */
	private final List files = new ArrayList();

	/** Reference to listview for easy access. */
	private FileListView fileListView;

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public UploadPage(final PageParameters parameters)
	{
		uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "WicketUploadTest");
		if (!uploadFolder.isDirectory())
		{
			uploadFolder.mkdir();
		}
		add(new UploadForm("upload", null, uploadFolder));
		add(new Label("dir", uploadFolder.getAbsolutePath()));
		files.addAll(Arrays.asList(uploadFolder.list()));
		fileListView = new FileListView("fileList", files);
		add(fileListView);
		add(new FeedbackPanel("feedback"));
	}

	/**
	 * Refresh file list.
	 */
	private void refreshFiles()
	{
		files.clear();
		files.addAll(Arrays.asList(uploadFolder.list()));
		fileListView.invalidateModel();
	}

	/**
	 * Form for uploads.
	 */
	private class UploadForm extends FileUploadForm
	{
		/** File name to upload to */
		private String filename;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 * @param validationErrorHandler
		 *            Error handler
		 * @param uploadFolder
		 *            Folder in which to save uploads
		 */
		public UploadForm(String name, IValidationFeedback validationErrorHandler,
				Folder uploadFolder)
		{
			super(name, validationErrorHandler, uploadFolder);
			add(new RequiredTextField("filename", this, "filename"));
		}
		
		/**
		 * @see wicket.markup.html.form.upload.FileUploadForm#onUpload(org.apache.commons.fileupload.FileItem)
		 */
		protected void onUpload(FileItem fileItem)
		{
			saveFile(fileItem, new File(uploadFolder, getFilename()));
		}

		/**
		 * @see wicket.markup.html.form.upload.AbstractUploadForm#onSubmit()
		 */
		protected void onSubmit()
		{
			super.onSubmit();
			refreshFiles();
		}
		
		/**
		 * @return Returns the filename.
		 */
		public String getFilename()
		{
			return filename;
		}
		
		/**
		 * @param filename The filename to set.
		 */
		public void setFilename(String filename)
		{
			this.filename = filename;
		}
	}

	/**
	 * table for files.
	 */
	private class FileListView extends ListView
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            component name
		 * @param object
		 *            file list
		 */
		public FileListView(String name, List object)
		{
			super(name, object);
		}

		/**
		 * @see ListView#populateItem(ListItem)
		 */
		protected void populateItem(ListItem listItem)
		{
			final String filename = (String)listItem.getModelObject();
			listItem.add(new Label("file", filename));
			listItem.add(new Link("delete")
			{

				public void onClick()
				{
					final File file = new File(uploadFolder, filename);
					log.info("Deleting " + file);
					Files.delete(file);
					refreshFiles();
				}
			});
		}
	}
}