/*
 * $Id$ $Revision:
 * 1.11 $ $Date$
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
package wicket.markup.html.form.upload;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import wicket.WicketRuntimeException;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.util.file.Files;
import wicket.util.file.Folder;
import wicket.util.string.Strings;
import wicket.util.thread.Lock;

/**
 * Form that uploads files and writes them to the file system. It uses a
 * conflict handler that is called when a file with the same name exists in the
 * same directory when trying to save an uploaded file.
 * 
 * @author Eelco Hillenius
 */
public class FileUploadForm extends UploadForm
{
	/**
	 * This conflict handler renames the file using an ascending number until it
	 * finds a name that it not used yet. Names are of form:
	 * {simple-filename}({number}).{ext}. e.g.: myfile.gif, myfile(1).gif and
	 * myfile(2).gif
	 */
	public final static IFileConflictResolver NUMBERING_FILE_CONFLICT_RESOLVER = new IFileConflictResolver()
	{
		/**
		 * @see wicket.markup.html.form.upload.FileUploadForm.IFileConflictResolver#resolveConflict(java.io.File)
		 */
		public File resolveConflict(final File file)
		{
			final String path = Strings.beforeLast(file.getPath(), '.');
			final String extension = Strings.afterLast(file.getPath(), '.');
			for (int i = 1;; i++)
			{
				final File newFile = new File(path + "(" + i + ")." + extension);
				if (!newFile.exists())
				{
					return newFile;
				}
			}
		}
	};

	/**
	 * This conflict handler tries to delete the current file and returns the
	 * given file. Hence, the current file will be overwritten by the upload
	 * file.
	 */
	public final static IFileConflictResolver OVERWRITING_FILE_CONFLICT_RESOLVER = new IFileConflictResolver()
	{
		/**
		 * @see wicket.markup.html.form.upload.FileUploadForm.IFileConflictResolver#resolveConflict(java.io.File)
		 */
		public File resolveConflict(final File file)
		{
			// Try one more time to delete the file
			if (!Files.delete(file))
			{
				throw new IllegalStateException("Unable to overwrite " + file.getAbsolutePath());
			}
			return file;
		}
	};

	/** Lock used to serialize file conflict resolutions */
	private static final Lock conflictResolverLock = new Lock();

	/** Serial Version ID */
	private static final long serialVersionUID = 6615560494113373735L;

	/**
	 * Conflict handler that will be called when a file with the same name
	 * already exists in the same directory when trying to save an uploaded
	 * file.
	 */
	private IFileConflictResolver fileConflictResolver = NUMBERING_FILE_CONFLICT_RESOLVER;

	/** The directory where the uploaded files should be put. */
	private Folder uploadFolder;

	/**
	 * Interface for handlers that will be called when a file with the same name
	 * already exists in the same directory when trying to save an uploaded
	 * file.
	 */
	public static interface IFileConflictResolver
	{
		/**
		 * Get the file that should be used to save the upload to.
		 * 
		 * @param file
		 *            The current, already existing file.
		 * @return The file that should be used to save the upload to
		 */
		public File resolveConflict(File file);
	}

	/**
	 * Constructor; uses NUMBER_FILE_CONFLICT_HANDLER to handle conflicts with
	 * existing files.
	 * 
	 * @param name
	 *            Component name
	 */
	public FileUploadForm(final String name)
	{
		super(name);
	}

	/**
	 * Constructor; uses NUMBER_FILE_CONFLICT_HANDLER to handle conflicts with
	 * existing files.
	 * 
	 * @param name
	 *            Component name
	 * @param validationErrorHandler
	 *            Error handler for validation errors
	 */
	public FileUploadForm(final String name, final IValidationFeedback validationErrorHandler)
	{
		super(name, validationErrorHandler);
	}

	/**
	 * @return Returns the fileConflictResolver.
	 */
	public IFileConflictResolver getFileConflictResolver()
	{
		return fileConflictResolver;
	}

	/**
	 * @return Returns the uploadFolder.
	 */
	public Folder getUploadFolder()
	{		
		// If no upload folder
		if (uploadFolder == null)
		{
			// set a default
			uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
		}
		
		// Ensure that folder exists
		if (!uploadFolder.isDirectory())
		{
			if (!uploadFolder.mkdir())
			{
				throw new WicketRuntimeException("Unable to create upload folder " + uploadFolder);
			}
		}		
		return uploadFolder;
	}

	/**
	 * @param fileConflictResolver
	 *            The fileConflictResolver to set.
	 */
	public void setFileConflictResolver(IFileConflictResolver fileConflictResolver)
	{
		this.fileConflictResolver = fileConflictResolver;
	}

	/**
	 * @param uploadFolder
	 *            The uploadFolder to set.
	 */
	public void setUploadFolder(Folder uploadFolder)
	{
		this.uploadFolder = uploadFolder;
	}

	/**
	 * Saves each uploaded file by calling saveFiles().
	 * 
	 * @see wicket.markup.html.form.upload.UploadForm#onSubmit()
	 */
	protected void onSubmit()
	{
		saveFiles();
	}

	/**
	 * Saves the uploaded file to disk.
	 * 
	 * @param item
	 *            the upload item
	 * @param file
	 *            the target file
	 */
	protected final void saveFile(final FileItem item, final File file)
	{
		File newFile = file;
		try
		{
			// We need to hold this lock because more than one thread could be
			// uploading to the same file and both threads might run into the
			// same conflict simultaneously. If both threads were to resolve the
			// same conflict in the same way, they might end up writing to the
			// same file, so we serialize conflict resolution so that only one
			// conflict is resolved at a time.
			synchronized (conflictResolverLock)
			{
				if (newFile.exists())
				{
					newFile = fileConflictResolver.resolveConflict(file);
				}

				// By creating the file while we still hold the
				// conflictResovlerLock monitor, we ensure that any other
				// threads waiting to resolve the same conflict will take
				// a higher conflict resolution number.
				newFile.createNewFile();
			}
			item.write(newFile);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Could not save upload to " + newFile, e);
		}
	}

	/**
	 * Loops through all uploaded files and saves each file to the set upload
	 * folder using the current conflict resolver.
	 */
	protected void saveFiles()
	{
		// The submit was valid and some form subclass implementation of
		// onSubmit() called super.onSubmit()
		final Map files = ((MultipartWebRequest)getRequest()).getFiles();
		for (final Iterator iterator = files.values().iterator(); iterator.hasNext();)
		{
			final FileItem fileItem = (FileItem)iterator.next();
			long sizeInBytes = fileItem.getSize();

			// Only process when there's anything uploaded at all
			if (sizeInBytes > 0)
			{
				// Save the upload to disk
				saveFile(fileItem, new File(getUploadFolder(), fileItem.getName()));
			}
		}
	}
}
