/*
 * $Id$
 * $Revision$ $Date$
 *
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;

import wicket.WicketRuntimeException;
import wicket.markup.html.form.validation.IValidationFeedback;

/**
 * Form that uploads files and writes them to the file system. It uses a
 * conflict handler that is called when a file with the same name exists in the
 * same directory when trying to save an uploaded file.
 *
 * @author Eelco Hillenius
 */
public class FileUploadForm extends AbstractUploadForm
{

	/**
	 * This conflict handler renames the file using an ascending number until it
	 * finds a name that it not used yet. Names are of form:
	 * {simple-filename}({number}).{ext}. e.g.: myfile.gif, myfile(1).gif and
	 * myfile(2).gif
	 */
	public final static FileExistsConflictHandler NUMBER_FILE_CONFLICT_HANDLER = new FileExistsConflictHandler()
	{
		/**
		 * @see wicket.markup.html.form.upload.FileUploadForm.FileExistsConflictHandler#getFileForSaving(java.io.File)
		 */
		public File getFileForSaving(final File uploadFile)
		{
			final File targetDirectory = uploadFile.getParentFile();
			final String fileName = uploadFile.getName();
			final int extloc = fileName.lastIndexOf('.');
			final String ext = fileName.substring((extloc + 1), fileName.length());
			File newFile;
			int i = 1;
			while (true)
			{
				File testFile = new File(targetDirectory, (fileName + "(" + i + ")." + ext));
				if (testFile.exists())
				{
					i++;
				}
				else
				{
					newFile = testFile;
					break;
				}
			}
			return newFile;
		}
	};

	/**
	 * This conflict handler tries to delete the current file and returns the
	 * given file handler. Hence, the current file will be overwritten by the
	 * upload file.
	 */
	public final static FileExistsConflictHandler OVERWRITE_FILE_CONFLICT_HANDLER = new FileExistsConflictHandler()
	{
		/**
		 * @see wicket.markup.html.form.upload.FileUploadForm.FileExistsConflictHandler#getFileForSaving(java.io.File)
		 */
		public File getFileForSaving(final File uploadFile)
		{
			if (!uploadFile.delete()) // delete current file
			{
				// fix for java/win bug
				// see:
				// http://forum.java.sun.com/thread.jsp?forum=4&thread=158689&tstart=0&trange=15
				System.gc();
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
				}
				if (!uploadFile.delete())
				{
					throw new IllegalStateException("unable to delete old file "
							+ uploadFile.getAbsolutePath());
				}
			}
			return uploadFile;
		}
	};

	/**
	 * give the resource a new, numbered, name when a resource with the same
	 * name exists.
	 */
	private final static int MODE_NUMBER = 1;

	/** overwrite existing resources. */
	private final static int MODE_OVERWRITE = 0;
	/** Serial Version ID */
	private static final long serialVersionUID = 6615560494113373735L;

	/**
	 * conflict handler that will be called when a file with the same name
	 * already exists in the same directory when trying to save an uploaded
	 * file.
	 */
	private FileExistsConflictHandler fileExistsConflictHandler;

	private String fileName = null;

	/** the directory where the uploaded files should be put. */
	private final File targetDirectory;

	/** the current upload mode. */
	private int uploadMode = MODE_NUMBER;

	/**
	 * Interface for handlers that will be called when a file with the same name
	 * already exists in the same directory when trying to save an uploaded
	 * file.
	 */
	public static interface FileExistsConflictHandler
	{
		/**
		 * Get the file handle that should be used to save the upload to.
		 *
		 * @param uploadFile
		 *            the current, allready existing file.
		 * @return the file that should be used to save the upload to
		 */
		File getFileForSaving(File uploadFile);
	}

	/**
	 * Construct; uses NUMBER_FILE_CONFLICT_HANDLER as the
	 * fileExistsConflictHandler.
	 *
	 * @param name
	 *            component name
	 * @param validationErrorHandler
	 *            error handler for validations
	 * @param targetDirectory
	 *            the directory where the uploaded files should be put
	 */
	public FileUploadForm(String name, IValidationFeedback validationErrorHandler,
			File targetDirectory)
	{
		this(name, validationErrorHandler, targetDirectory, NUMBER_FILE_CONFLICT_HANDLER);
	}

	/**
	 * Construct.
	 *
	 * @param name
	 *            component name
	 * @param validationErrorHandler
	 *            error handler for validations
	 * @param targetDirectory
	 *            the directory where the uploaded files should be put
	 * @param fileExistsConflictHandler
	 *            conflict handler that will be called when a file with the same
	 *            name already exists in the same directory when trying to save
	 *            an uploaded file
	 */
	public FileUploadForm(String name, IValidationFeedback validationErrorHandler,
			File targetDirectory, FileExistsConflictHandler fileExistsConflictHandler)
	{
		super(name, validationErrorHandler);
		this.targetDirectory = targetDirectory;
		this.fileExistsConflictHandler = fileExistsConflictHandler;
	}

	/**
	 * Processes a form field.
	 *
	 * @param item
	 *            a file item
	 * @see wicket.markup.html.form.upload.AbstractUploadForm#processFormField(org.apache.commons.fileupload.FileItem)
	 */
	protected final void processFormField(FileItem item)
	{
		this.fileName = item.getString();
	}

	/**
	 * Process an upload item.
	 *
	 * @param item
	 *            upload item (item.isFormField() == false)
	 * @see wicket.markup.html.form.upload.AbstractUploadForm#processUploadedFile(org.apache.commons.fileupload.FileItem)
	 */
	protected final void processUploadedFile(FileItem item)
	{
		if (item == null)
		{
			throw new IllegalArgumentException("No file");
		}
		String originalName = item.getName();
		int extloc = originalName.lastIndexOf('.');
		String ext = originalName.substring((extloc + 1), originalName.length());
		if (fileName == null)
		{
			fileName = originalName.substring(0, extloc);
		}

		if (fileName == null || fileName.trim().equals(""))
		{
			throw new WicketRuntimeException("No file name given");
		}

		File targetFile = new File(targetDirectory, fileName + "." + ext);
		saveFile(item, targetFile);
	}

	/**
	 * Saves the uploaded file to disk.
	 *
	 * @param item
	 *            the upload item
	 * @param targetFile
	 *            the target file
	 */
	private final void saveFile(final FileItem item, final File targetFile)
	{
		try
		{
			item.write(targetFile.exists()
					? fileExistsConflictHandler.getFileForSaving(targetFile)
					: targetFile);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
	}
}