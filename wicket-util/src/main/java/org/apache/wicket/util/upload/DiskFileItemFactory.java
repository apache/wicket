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
package org.apache.wicket.util.upload;

import java.io.File;

import org.apache.wicket.util.file.FileCleaningTracker;
import org.apache.wicket.util.file.IFileCleaner;


/**
 * <p>
 * The default {@link org.apache.wicket.util.upload.FileItemFactory} implementation. This
 * implementation creates {@link org.apache.wicket.util.upload.FileItem} instances which keep their
 * content either in memory, for smaller items, or in a temporary file on disk, for larger items.
 * The size threshold, above which content will be stored on disk, is configurable, as is the
 * directory in which temporary files will be created.
 * </p>
 * 
 * <p>
 * If not otherwise configured, the default configuration values are as follows:
 * <ul>
 * <li>Size threshold is 10KB.</li>
 * <li>Repository is the system default temp directory, as returned by
 * <code>System.getProperty("java.io.tmpdir")</code>.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * When using the <code>DiskFileItemFactory</code>, then you should consider the following:
 * Temporary files are automatically deleted as soon as they are no longer needed. (More precisely,
 * when the corresponding instance of {@link java.io.File} is garbage collected.) Cleaning up those
 * files is done by an instance of {@link FileCleaningTracker}, and an associated thread. In a
 * complex environment, for example in a web application, you should consider terminating this
 * thread, for example, when your web application ends. See the section on "Resource cleanup" in the
 * users guide of commons-fileupload.
 * </p>
 * 
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 */
public class DiskFileItemFactory implements FileItemFactory
{

	// ----------------------------------------------------- Manifest constants


	/**
	 * The default threshold above which uploads will be stored on disk.
	 */
	public static final int DEFAULT_SIZE_THRESHOLD = 10240;


	// ----------------------------------------------------- Instance Variables


	/**
	 * The directory in which uploaded files will be stored, if stored on disk.
	 */
	private File repository;


	/**
	 * The threshold above which uploads will be stored on disk.
	 */
	private int sizeThreshold = DEFAULT_SIZE_THRESHOLD;

	private final IFileCleaner fileUploadCleaner;

	// ----------------------------------------------------------- Constructors


	/**
	 * Constructs an unconfigured instance of this class. The resulting factory may be configured by
	 * calling the appropriate setter methods.
	 * 
	 * @param fileUploadCleaner
	 */
	public DiskFileItemFactory(final IFileCleaner fileUploadCleaner)
	{
		this(DEFAULT_SIZE_THRESHOLD, null, fileUploadCleaner);
	}


	/**
	 * Constructs a preconfigured instance of this class.
	 * 
	 * @param sizeThreshold
	 *            The threshold, in bytes, below which items will be retained in memory and above
	 *            which they will be stored as a file.
	 * @param repository
	 *            The data repository, which is the directory in which files will be created, should
	 *            the item size exceed the threshold.
	 * @param fileUploadCleaner
	 */
	public DiskFileItemFactory(final int sizeThreshold, final File repository,
		final IFileCleaner fileUploadCleaner)
	{
		this.sizeThreshold = sizeThreshold;
		this.repository = repository;
		this.fileUploadCleaner = fileUploadCleaner;
	}

	// ------------------------------------------------------------- Properties


	/**
	 * Returns the directory used to temporarily store files that are larger than the configured
	 * size threshold.
	 * 
	 * @return The directory in which temporary files will be located.
	 * 
	 * @see #setRepository(java.io.File)
	 * 
	 */
	public File getRepository()
	{
		return repository;
	}


	/**
	 * Sets the directory used to temporarily store files that are larger than the configured size
	 * threshold.
	 * 
	 * @param repository
	 *            The directory in which temporary files will be located.
	 * 
	 * @see #getRepository()
	 * 
	 */
	public void setRepository(final File repository)
	{
		this.repository = repository;
	}


	/**
	 * Returns the size threshold beyond which files are written directly to disk. The default value
	 * is 10240 bytes.
	 * 
	 * @return The size threshold, in bytes.
	 * 
	 * @see #setSizeThreshold(int)
	 */
	public int getSizeThreshold()
	{
		return sizeThreshold;
	}


	/**
	 * Sets the size threshold beyond which files are written directly to disk.
	 * 
	 * @param sizeThreshold
	 *            The size threshold, in bytes.
	 * 
	 * @see #getSizeThreshold()
	 * 
	 */
	public void setSizeThreshold(final int sizeThreshold)
	{
		this.sizeThreshold = sizeThreshold;
	}


	// --------------------------------------------------------- Public Methods

	/**
	 * Create a new {@link org.apache.wicket.util.upload.DiskFileItem} instance from the supplied
	 * parameters and the local factory configuration.
	 * 
	 * @param fieldName
	 *            The name of the form field.
	 * @param contentType
	 *            The content type of the form field.
	 * @param isFormField
	 *            <code>true</code> if this is a plain form field; <code>false</code> otherwise.
	 * @param fileName
	 *            The name of the uploaded file, if any, as supplied by the browser or other client.
	 * 
	 * @return The newly created file item.
	 */
	@Override
	public FileItem createItem(final String fieldName, final String contentType,
		final boolean isFormField, final String fileName)
	{
		return new DiskFileItem(fieldName, contentType, isFormField, fileName, sizeThreshold,
			repository, fileUploadCleaner);
	}

}
