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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.IFileCleaner;
import org.apache.wicket.util.io.DeferredFileOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The default implementation of the {@link org.apache.wicket.util.upload.FileItem FileItem}
 * interface.
 * 
 * <p>
 * After retrieving an instance of this class from a
 * {@link org.apache.wicket.util.upload.DiskFileUpload DiskFileUpload} instance (see
 * {@link org.apache.wicket.util.upload.DiskFileUpload #parseRequest(javax.servlet.http.HttpServletRequest)}
 * ), you may either request all contents of file at once using {@link #get()} or request an
 * {@link java.io.InputStream InputStream} with {@link #getInputStream()} and process the file
 * without attempting to load it into memory, which may come handy with large files.
 * 
 * <p>
 * When using the <code>DiskFileItemFactory</code>, then you should consider the following:
 * Temporary files are automatically deleted as soon as they are no longer needed. (More precisely,
 * when the corresponding instance of {@link java.io.File} is garbage collected.) This is done by
 * the so-called reaper thread, which is started automatically when the class
 * {@link org.apache.commons.io.FileCleaner} is loaded. It might make sense to terminate that
 * thread, for example, if your web application ends. See the section on "Resource cleanup" in the
 * users guide of commons-fileupload.
 * </p>
 * 
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@apache.org">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 */
public class DiskFileItem implements FileItem, FileItemHeadersSupport
{
	private static final Logger log = LoggerFactory.getLogger(DiskFileItem.class);

	/**
	 * The UID to use when serializing this instance.
	 */
	private static final long serialVersionUID = 2237570099615271025L;

	/**
	 * Default content charset to be used when no explicit charset parameter is provided by the
	 * sender. Media subtypes of the "text" type are defined to have a default charset value of
	 * "ISO-8859-1" when received via HTTP.
	 */
	public static final String DEFAULT_CHARSET = "ISO-8859-1";

	// ----------------------------------------------------------- Data members

	/**
	 * UID used in unique file name generation.
	 */
	private static final String UID = UUID.randomUUID()
		.toString()
		.replace(':', '_')
		.replace('-', '_');

	/**
	 * Random counter used in unique identifier generation.
	 */
	private static final Random counter = new Random();

	/**
	 * The name of the form field as provided by the browser.
	 */
	private String fieldName;

	/**
	 * The content type passed by the browser, or <code>null</code> if not defined.
	 */
	private final String contentType;

	/**
	 * Whether or not this item is a simple form field.
	 */
	private boolean isFormField;

	/**
	 * The original filename in the user's filesystem.
	 */
	private final String fileName;

	/**
	 * The size of the item, in bytes. This is used to cache the size when a file item is moved from
	 * its original location.
	 */
	private long size = -1;

	/**
	 * The threshold above which uploads will be stored on disk.
	 */
	private final int sizeThreshold;

	/**
	 * The directory in which uploaded files will be stored, if stored on disk.
	 */
	private final File repository;

	/**
	 * Cached contents of the file.
	 */
	private byte[] cachedContent;

	/**
	 * Output stream for this item.
	 */
	private transient DeferredFileOutputStream dfos;

	/**
	 * The temporary file to use.
	 */
	private transient File tempFile;

	/**
	 * File to allow for serialization of the content of this item.
	 */
	private File dfosFile;

	/**
	 * The file items headers.
	 */
	private FileItemHeaders headers;

	/**
	 * This is transient because it is needed only for the upload request lifetime to add this file
	 * item in the tracker. After that the cleaner is not needed anymore.
	 */
	private transient final IFileCleaner fileUploadCleaner;

	/**
	 * Constructs a new <code>DiskFileItem</code> instance.
	 * 
	 * @param fieldName
	 *            The name of the form field.
	 * @param contentType
	 *            The content type passed by the browser or <code>null</code> if not specified.
	 * @param isFormField
	 *            Whether or not this item is a plain form field, as opposed to a file upload.
	 * @param fileName
	 *            The original filename in the user's filesystem, or <code>null</code> if not
	 *            specified.
	 * @param sizeThreshold
	 *            The threshold, in bytes, below which items will be retained in memory and above
	 *            which they will be stored as a file.
	 * @param repository
	 *            The data repository, which is the directory in which files will be created, should
	 *            the item size exceed the threshold.
	 * @param fileUploadCleaner
	 */
	public DiskFileItem(final String fieldName, final String contentType,
		final boolean isFormField, final String fileName, final int sizeThreshold,
		final File repository, final IFileCleaner fileUploadCleaner)
	{
		this.fieldName = fieldName;
		this.contentType = contentType;
		this.isFormField = isFormField;
		this.fileName = fileName;
		this.sizeThreshold = sizeThreshold;
		this.repository = repository;
		this.fileUploadCleaner = fileUploadCleaner;
	}

	/**
	 * Returns an {@link java.io.InputStream InputStream} that can be used to retrieve the contents
	 * of the file.
	 * 
	 * @return An {@link java.io.InputStream InputStream} that can be used to retrieve the contents
	 *         of the file.
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	@Override
	public InputStream getInputStream() throws IOException
	{
		if (!isInMemory())
		{
			return new FileInputStream(dfos.getFile());
		}

		if (cachedContent == null)
		{
			cachedContent = dfos.getData();
		}
		return new ByteArrayInputStream(cachedContent);
	}

	/**
	 * Returns the content type passed by the agent or <code>null</code> if not defined.
	 * 
	 * @return The content type passed by the agent or <code>null</code> if not defined.
	 */
	@Override
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * Returns the content charset passed by the agent or <code>null</code> if not defined.
	 * 
	 * @return The content charset passed by the agent or <code>null</code> if not defined.
	 */
	public String getCharSet()
	{
		ParameterParser parser = new ParameterParser();
		parser.setLowerCaseNames(true);
		// Parameter parser can handle null input
		Map<?, ?> params = parser.parse(getContentType(), ';');
		return (String)params.get("charset");
	}

	/**
	 * Returns the original filename in the client's filesystem.
	 * 
	 * @return The original filename in the client's filesystem.
	 */
	@Override
	public String getName()
	{
		return fileName;
	}

	/**
	 * Provides a hint as to whether or not the file contents will be read from memory.
	 * 
	 * @return <code>true</code> if the file contents will be read from memory; <code>false</code>
	 *         otherwise.
	 */
	@Override
	public boolean isInMemory()
	{
		if (cachedContent != null)
		{
			return true;
		}
		return dfos.isInMemory();
	}

	/**
	 * Returns the size of the file.
	 * 
	 * @return The size of the file, in bytes.
	 */
	@Override
	public long getSize()
	{
		if (size >= 0)
		{
			return size;
		}
		else if (cachedContent != null)
		{
			return cachedContent.length;
		}
		else if (dfos.isInMemory())
		{
			return dfos.getData().length;
		}
		else
		{
			return dfos.getFile().length();
		}
	}

	/**
	 * Returns the contents of the file as an array of bytes. If the contents of the file were not
	 * yet cached in memory, they will be loaded from the disk storage and cached.
	 * 
	 * @return The contents of the file as an array of bytes.
	 */
	@Override
	public byte[] get()
	{
		if (isInMemory())
		{
			if (cachedContent == null)
			{
				cachedContent = dfos.getData();
			}
			return cachedContent;
		}

		File file = dfos.getFile();

		try
		{
			return Files.readBytes(file);
		}
		catch (IOException e)
		{
			log.debug("failed to read content of file: " + file.getAbsolutePath(), e);
			return null;
		}
	}


	/**
	 * Returns the contents of the file as a String, using the specified encoding. This method uses
	 * {@link #get()} to retrieve the contents of the file.
	 * 
	 * @param charset
	 *            The charset to use.
	 * 
	 * @return The contents of the file, as a string.
	 * 
	 * @throws UnsupportedEncodingException
	 *             if the requested character encoding is not available.
	 */
	@Override
	public String getString(final String charset) throws UnsupportedEncodingException
	{
		return new String(get(), charset);
	}

	/**
	 * Returns the contents of the file as a String, using the default character encoding. This
	 * method uses {@link #get()} to retrieve the contents of the file.
	 * 
	 * @return The contents of the file, as a string.
	 * 
	 * @todo Consider making this method throw UnsupportedEncodingException.
	 */
	@Override
	public String getString()
	{
		byte[] rawdata = get();
		String charset = getCharSet();
		if (charset == null)
		{
			charset = DEFAULT_CHARSET;
		}
		try
		{
			return new String(rawdata, charset);
		}
		catch (UnsupportedEncodingException e)
		{
			return new String(rawdata);
		}
	}


	/**
	 * A convenience method to write an uploaded item to disk. The client code is not concerned with
	 * whether or not the item is stored in memory, or on disk in a temporary location. They just
	 * want to write the uploaded item to a file.
	 * <p>
	 * This implementation first attempts to rename the uploaded item to the specified destination
	 * file, if the item was originally written to disk. Otherwise, the data will be copied to the
	 * specified file.
	 * <p>
	 * This method is only guaranteed to work <em>once</em>, the first time it is invoked for a
	 * particular item. This is because, in the event that the method renames a temporary file, that
	 * file will no longer be available to copy or rename again at a later time.
	 * 
	 * @param file
	 *            The <code>File</code> into which the uploaded item should be stored.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	public void write(final File file) throws IOException
	{
		if (isInMemory())
		{
			FileOutputStream fout = new FileOutputStream(file);

			try
			{
				fout.write(get());
			}
			finally
			{
				fout.close();
			}
		}
		else
		{
			File outputFile = getStoreLocation();
			Checks.notNull(outputFile,
				"for a non-memory upload the file location must not be empty");

			// Save the length of the file
			size = outputFile.length();
			/*
			 * The uploaded file is being stored on disk in a temporary location so move it to the
			 * desired file.
			 */
			if (!outputFile.renameTo(file))
			{
				BufferedInputStream in = null;
				BufferedOutputStream out = null;
				try
				{
					in = new BufferedInputStream(new FileInputStream(outputFile));
					out = new BufferedOutputStream(new FileOutputStream(file));
					Streams.copy(in, out);
				}
				finally
				{
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
		}
	}

	/**
	 * Deletes the underlying storage for a file item, including deleting any associated temporary
	 * disk file. Although this storage will be deleted automatically when the <code>FileItem</code>
	 * instance is garbage collected, this method can be used to ensure that this is done at an
	 * earlier time, thus preserving system resources.
	 */
	@Override
	public void delete()
	{
		cachedContent = null;
		File outputFile = getStoreLocation();
		if ((outputFile != null) && outputFile.exists())
		{
			if (Files.remove(outputFile) == false)
			{
				log.error("failed to delete file: " + outputFile.getAbsolutePath());
			}
		}
	}


	/**
	 * Returns the name of the field in the multipart form corresponding to this file item.
	 * 
	 * @return The name of the form field.
	 * 
	 * @see #setFieldName(java.lang.String)
	 * 
	 */
	@Override
	public String getFieldName()
	{
		return fieldName;
	}

	/**
	 * Sets the field name used to reference this file item.
	 * 
	 * @param fieldName
	 *            The name of the form field.
	 * 
	 * @see #getFieldName()
	 * 
	 */
	@Override
	public void setFieldName(final String fieldName)
	{
		this.fieldName = fieldName;
	}

	/**
	 * Determines whether or not a <code>FileItem</code> instance represents a simple form field.
	 * 
	 * @return <code>true</code> if the instance represents a simple form field; <code>false</code>
	 *         if it represents an uploaded file.
	 * 
	 * @see #setFormField(boolean)
	 * 
	 */
	@Override
	public boolean isFormField()
	{
		return isFormField;
	}


	/**
	 * Specifies whether or not a <code>FileItem</code> instance represents a simple form field.
	 * 
	 * @param state
	 *            <code>true</code> if the instance represents a simple form field;
	 *            <code>false</code> if it represents an uploaded file.
	 * 
	 * @see #isFormField()
	 * 
	 */
	@Override
	public void setFormField(final boolean state)
	{
		isFormField = state;
	}


	/**
	 * Returns an {@link java.io.OutputStream OutputStream} that can be used for storing the
	 * contents of the file.
	 * 
	 * @return An {@link java.io.OutputStream OutputStream} that can be used for storing the
	 *         contensts of the file.
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	@Override
	public OutputStream getOutputStream() throws IOException
	{
		if (dfos == null)
		{
			dfos = new DeferredFileOutputStream(sizeThreshold,
				new DeferredFileOutputStream.FileFactory()
				{
					@Override
					public File createFile()
					{
						return getTempFile();
					}
				});
		}
		return dfos;
	}


	// --------------------------------------------------------- Public methods


	/**
	 * Returns the {@link java.io.File} object for the <code>FileItem</code>'s data's temporary
	 * location on the disk. Note that for <code>FileItem</code>s that have their data stored in
	 * memory, this method will return <code>null</code>. When handling large files, you can use
	 * {@link java.io.File#renameTo(java.io.File)} to move the file to new location without copying
	 * the data, if the source and destination locations reside within the same logical volume.
	 * 
	 * @return The data file, or <code>null</code> if the data is stored in memory.
	 */
	public File getStoreLocation()
	{
		return dfos == null ? null : dfos.getFile();
	}


	// ------------------------------------------------------ Protected methods


	/**
	 * Removes the file contents from the temporary storage.
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize(); // currently empty but there for safer refactoring

		File outputFile = dfos.getFile();

		if ((outputFile != null) && outputFile.exists())
		{
			if (Files.remove(outputFile) == false)
			{
				log.error("failed to delete file: " + outputFile.getAbsolutePath());
			}
		}
	}


	/**
	 * Creates and returns a {@link java.io.File File} representing a uniquely named temporary file
	 * in the configured repository path. The lifetime of the file is tied to the lifetime of the
	 * <code>FileItem</code> instance; the file will be deleted when the instance is garbage
	 * collected.
	 * 
	 * @return The {@link java.io.File File} to be used for temporary storage.
	 */
	protected File getTempFile()
	{
		if (tempFile == null)
		{
			File tempDir = repository;
			if (tempDir == null)
			{
				String systemTmp = null;
				try
				{
					systemTmp = System.getProperty("java.io.tmpdir");
				}
				catch (SecurityException e)
				{
					throw new RuntimeException(
						"Reading property java.io.tmpdir is not allowed"
							+ " for the current security settings. The repository location needs to be"
							+ " set manually, or upgrade permissions to allow reading the tmpdir property.");
				}
				tempDir = new File(systemTmp);
			}

			try
			{
				do
				{
					String tempFileName = "upload_" + UID + "_" + getUniqueId() + ".tmp";
					tempFile = new File(tempDir, tempFileName);
				}
				while (!tempFile.createNewFile());
			}
			catch (IOException e)
			{
				throw new RuntimeException("Could not create the temp file for upload: " +
					tempFile.getAbsolutePath(), e);
			}

			if (fileUploadCleaner != null)
			{
				fileUploadCleaner.track(tempFile, this);
			}
		}
		return tempFile;
	}

	// -------------------------------------------------------- Private methods


	/**
	 * Returns an identifier that is unique within the class loader used to load this class, but
	 * does not have random-like appearance.
	 * 
	 * @return A String with the non-random looking instance identifier.
	 */
	private static String getUniqueId()
	{
		final int limit = 100000000;
		int current;
		synchronized (DiskFileItem.class)
		{
			current = counter.nextInt();
		}
		String id = Integer.toString(current);

		// If you manage to get more than 100 million of ids, you'll
		// start getting ids longer than 8 characters.
		if (current < limit)
		{
			id = ("00000000" + id).substring(id.length());
		}
		return id;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "name=" + getName() + ", StoreLocation=" + String.valueOf(getStoreLocation()) +
			", size=" + getSize() + "bytes, " + "isFormField=" + isFormField() + ", FieldName=" +
			getFieldName();
	}


	// -------------------------------------------------- Serialization methods


	/**
	 * Writes the state of this object during serialization.
	 * 
	 * @param out
	 *            The stream to which the state should be written.
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException
	{
		// Read the data
		if (dfos.isInMemory())
		{
			cachedContent = get();
		}
		else
		{
			cachedContent = null;
			dfosFile = dfos.getFile();
		}

		// write out values
		out.defaultWriteObject();
	}

	/**
	 * Reads the state of this object during deserialization.
	 * 
	 * @param in
	 *            The stream from which the state should be read.
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 * @throws ClassNotFoundException
	 *             if class cannot be found.
	 */
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		// read values
		in.defaultReadObject();

		OutputStream output = getOutputStream();
		if (cachedContent != null)
		{
			output.write(cachedContent);
		}
		else
		{
			FileInputStream input = new FileInputStream(dfosFile);
			Streams.copy(input, output);
			Files.remove(dfosFile);
			dfosFile = null;
		}
		output.close();

		cachedContent = null;
	}

	/**
	 * Returns the file item headers.
	 * 
	 * @return The file items headers.
	 */
	@Override
	public FileItemHeaders getHeaders()
	{
		return headers;
	}

	/**
	 * Sets the file item headers.
	 * 
	 * @param pHeaders
	 *            The file items headers.
	 */
	@Override
	public void setHeaders(final FileItemHeaders pHeaders)
	{
		headers = pHeaders;
	}
}
