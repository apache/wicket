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
import wicket.markup.html.form.upload.FileUploadForm;
import wicket.markup.html.form.upload.UploadModel;
import wicket.markup.html.form.upload.UploadTextField;
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
		add(new MultipleFilesUploadForm("upload", null, uploadFolder));
		add(new SimpleUploadForm("simpleUpload", null, uploadFolder));
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
	 * Form for uploads that just uses the original file name
	 * for the uploaded file.
	 */
	private class SimpleUploadForm extends FileUploadForm
	{
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
		public SimpleUploadForm(String name, IValidationFeedback validationErrorHandler,
				Folder uploadFolder)
		{
			super(name, validationErrorHandler, uploadFolder);
		}

		/**
		 * @see wicket.markup.html.form.upload.UploadForm#onSubmit()
		 */
		protected void onSubmit()
		{
			super.onSubmit();
			refreshFiles();
		}
	}
	
	/**
	 * Form for uploads that uploads multiple files and uses textfield
	 * for getting the names of uploads.
	 */
	private class MultipleFilesUploadForm extends FileUploadForm
	{
		private UploadModel model1;
		private UploadModel model2;

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
		public MultipleFilesUploadForm(String name, IValidationFeedback validationErrorHandler,
				Folder uploadFolder)
		{
			super(name, validationErrorHandler, uploadFolder);
			model1 = new UploadModel();
			model2 = new UploadModel();
			// first upload must be given
			add(new UploadTextField("name1", "upload1", model1, true));
			// second is optional
			add(new UploadTextField("name2", "upload2", model2, false));
		}

		/**
		 * @see wicket.markup.html.form.upload.UploadForm#onSubmit()
		 */
		protected void onSubmit()
		{
			// do not call super.submit as that will save the uploads using their
			// file names

			// first will allways be set if we come here
			saveFile(model1.getFile(), new File(uploadFolder, model1.getName()));
			FileItem file2 = model2.getFile();
			if(file2 != null && (file2.getSize() > 0)) // second might be set
			{
				// but if set, the name will allways be given
				saveFile(file2, new File(uploadFolder, model2.getName()));
			}
			refreshFiles();
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
					getRequestCycle().setRedirect(true);
				}
			});
		}
	}
}