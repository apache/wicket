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
package org.apache.wicket.markup.html.form.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * Model for file uploads. Objects of this class should not be kept between requests, and should
 * therefore be marked as <code>transient</code> if they become a property of an IModel.
 * 
 * @author Jonathan Locke
 */
public class FileUpload implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final FileItem item;

	private transient List<InputStream> inputStreamsToClose;

	/**
	 * Constructor
	 * 
	 * @param item
	 *            The uploaded file item
	 */
	public FileUpload(final FileItem item)
	{
		Args.notNull(item, "item");
		this.item = item;
	}

	/**
	 * Close the streams which has been opened when getting the InputStream using
	 * {@link #getInputStream()}. All the input streams are closed at the end of the request. This
	 * is done when the FileUploadField, which is associated with this FileUpload is detached.
	 * <p>
	 * If an exception is thrown when closing the input streams, we ignore it, because the stream
	 * might have been closed already.
	 */
	public final void closeStreams()
	{
		if (inputStreamsToClose != null)
		{
			for (InputStream inputStream : inputStreamsToClose)
			{
				IOUtils.closeQuietly(inputStream);
			}

			// Reset the list
			inputStreamsToClose = null;
		}
	}

	/**
	 * Deletes temp file from disk
	 */
	public void delete()
	{
		item.delete();
	}

	/**
	 * @return Uploaded file as an array of bytes
	 */
	public byte[] getBytes()
	{
		return item.get();
	}

	/**
	 * Get the MD5 checksum.
	 * 
	 * @param algorithm
	 *            the digest algorithm, e.g. MD5, SHA-1, SHA-256, SHA-512
	 * 
	 * @return The cryptographic digest of the file
	 */
	public byte[] getDigest(String algorithm)
	{
		try
		{
			Args.notEmpty(algorithm, "algorithm");
			MessageDigest digest = java.security.MessageDigest.getInstance(algorithm);

			if (item.isInMemory())
			{
				digest.update(getBytes());
				return digest.digest();
			}

			InputStream in = null;

			try
			{
				in = item.getInputStream();
				byte[] buf = new byte[Math.min((int)item.getSize(), 4096 * 10)];
				int len;
				while (-1 != (len = in.read(buf)))
				{
					digest.update(buf, 0, len);
				}
				return digest.digest();
			}
			catch (IOException ex)
			{
				throw new WicketRuntimeException("Error while reading input data for " + algorithm +
					" checksum", ex);
			}
			finally
			{
				IOUtils.closeQuietly(in);
			}
		}
		catch (NoSuchAlgorithmException ex)
		{
			String error = String.format(
				"Your java runtime does not support digest algorithm [%s]. "
					+ "Please see java.security.MessageDigest.getInstance(\"%s\")", algorithm,
				algorithm);

			throw new WicketRuntimeException(error, ex);
		}
	}

	/**
	 * Get the MD5 checksum.
	 * 
	 * @return The MD5 checksum of the file
	 */
	public byte[] getMD5()
	{
		return getDigest("MD5");
	}

	/**
	 * @since 1.2
	 * @return name of uploaded client side file
	 */
	public String getClientFileName()
	{
		String name = item.getName();

		// when uploading from localhost some browsers will specify the entire path, we strip it
		// down to just the file name
		name = Strings.lastPathComponent(name, '/');
		name = Strings.lastPathComponent(name, '\\');

		return name;
	}

	/**
	 * @return Content type for upload
	 */
	public String getContentType()
	{
		return item.getContentType();
	}

	/**
	 * Get an input stream for the file uploaded. Use this input stream if you can't use
	 * {@link #writeTo(File)} for persisting the uploaded file. This can be if you need to react
	 * upon the content of the file or need to persist it elsewhere, i.e. a database or external
	 * filesystem.
	 * <p>
	 * <b>PLEASE NOTE!</b><br>
	 * The InputStream return will be closed be Wicket at the end of the request. If you need it
	 * across a request you need to hold on to this FileUpload instead.
	 * 
	 * @return Input stream with file contents.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException
	{
		if (inputStreamsToClose == null)
		{
			inputStreamsToClose = new ArrayList<InputStream>();
		}

		InputStream is = item.getInputStream();
		inputStreamsToClose.add(is);

		return is;
	}

	/**
	 * @return The upload's size
	 */
	public long getSize()
	{
		return item.getSize();
	}

	/**
	 * Saves this file upload to a given file on the server side.
	 * 
	 * @param file
	 *            The file
	 * @throws Exception
	 */
	public void writeTo(final File file) throws Exception
	{
		item.write(file);
	}

	/**
	 * Convenience method that copies the input stream returned by {@link #getInputStream()} into a
	 * temporary file.
	 * <p>
	 * Only use this if you actually need a {@link File} to work with, in all other cases use
	 * {@link #getInputStream()} or {@link #getBytes()}
	 * 
	 * @since 1.2
	 * 
	 * @return temporary file containing the contents of the uploaded file
	 * @throws Exception
	 */
	public final File writeToTempFile() throws Exception
	{
		String sessionId = Session.exists() ? Session.get().getId() : "";
		String tempFileName = sessionId + "_" + RequestCycle.get().getStartTime();
		File temp = File.createTempFile(tempFileName, Files.cleanupFilename(item.getFieldName()));
		writeTo(temp);
		return temp;
	}
}
