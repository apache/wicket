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
package org.apache.wicket.protocol.http.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.DiskFileItemFactory;
import org.apache.wicket.util.upload.FileItem;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadException;
import org.apache.wicket.util.upload.ServletFileUpload;
import org.apache.wicket.util.upload.ServletRequestContext;
import org.apache.wicket.util.value.ValueMap;


/**
 * Servlet specific WebRequest subclass for multipart content uploads.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Cameron Braid
 * @author Ate Douma
 * @author Igor Vaynberg (ivaynberg)
 */
public class MultipartServletWebRequest extends ServletWebRequest implements IMultipartWebRequest
{
	/** Map of file items. */
	private final Map<String, FileItem> files = new HashMap<String, FileItem>();

	/** Map of parameters. */
	private final ValueMap parameters = new ValueMap();


	/**
	 * total bytes uploaded (downloaded from server's pov) so far. used for upload notifications
	 */
	private int bytesUploaded;

	/** content length cache, used for upload notifications */
	private int totalBytes;


	/**
	 * Constructor.
	 * 
	 * This constructor will use {@link DiskFileItemFactory} to store uploads.
	 * 
	 * @param maxSize
	 *            the maximum size allowed for this request
	 * @param request
	 *            the servlet request
	 * @throws FileUploadException
	 *             Thrown if something goes wrong with upload
	 */
	public MultipartServletWebRequest(HttpServletRequest request, Bytes maxSize)
		throws FileUploadException
	{
		this(request, maxSize, new DiskFileItemFactory());
	}

	/**
	 * Constructor
	 * 
	 * @param maxSize
	 *            the maximum size allowed for this request
	 * @param request
	 *            the servlet request
	 * @param factory
	 *            {@link DiskFileItemFactory} to use when creating file items used to represent
	 *            uploaded files
	 * @throws FileUploadException
	 *             Thrown if something goes wrong with upload
	 */
	public MultipartServletWebRequest(HttpServletRequest request, Bytes maxSize,
		FileItemFactory factory) throws FileUploadException
	{
		super(request);

		if (maxSize == null)
		{
			throw new IllegalArgumentException("argument maxSize must be not null");
		}

		// Check that request is multipart
		final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart)
		{
			throw new IllegalStateException("ServletRequest does not contain multipart content");
		}


		// Configure the factory here, if desired.
		ServletFileUpload upload = new ServletFileUpload(factory);

		// The encoding that will be used to decode the string parameters
		// It should NOT be null at this point, but it may be
		// if the older Servlet API 2.2 is used
		String encoding = request.getCharacterEncoding();

		// set encoding specifically when we found it
		if (encoding != null)
		{
			upload.setHeaderEncoding(encoding);
		}

		upload.setSizeMax(maxSize.bytes());

		final List<FileItem> items;

		if (wantUploadProgressUpdates())
		{
			ServletRequestContext ctx = new ServletRequestContext(request)
			{
				@Override
				public InputStream getInputStream() throws IOException
				{
					return new CountingInputStream(super.getInputStream());
				}
			};
			totalBytes = request.getContentLength();

			onUploadStarted(totalBytes);
			items = upload.parseRequest(ctx);
			onUploadCompleted();

		}
		else
		{
			items = upload.parseRequest(request);
		}

		// Loop through items
		for (Iterator<FileItem> i = items.iterator(); i.hasNext();)
		{
			// Get next item
			final FileItem item = i.next();

			// If item is a form field
			if (item.isFormField())
			{
				// Set parameter value
				final String value;
				if (encoding != null)
				{
					try
					{
						value = item.getString(encoding);
					}
					catch (UnsupportedEncodingException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
				else
				{
					value = item.getString();
				}

				addParameter(item.getFieldName(), value);
			}
			else
			{
				// Add to file list
				files.put(item.getFieldName(), item);
			}
		}
	}

	/**
	 * Adds a parameter to the parameters value map
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 */
	private void addParameter(final String name, final String value)
	{
		final String[] currVal = (String[])parameters.get(name);

		String[] newVal = null;

		if (currVal != null)
		{
			newVal = new String[currVal.length + 1];
			System.arraycopy(currVal, 0, newVal, 0, currVal.length);
			newVal[currVal.length] = value;
		}
		else
		{
			newVal = new String[] { value };

		}

		parameters.put(name, newVal);
	}

	/**
	 * @return Returns the files.
	 */
	public Map<String, FileItem> getFiles()
	{
		return files;
	}

	/**
	 * Gets the file that was uploaded using the given field name.
	 * 
	 * @param fieldName
	 *            the field name that was used for the upload
	 * @return the upload with the given field name
	 */
	public FileItem getFile(final String fieldName)
	{
		return files.get(fieldName);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebRequest#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(final String key)
	{
		String[] val = (String[])parameters.get(key);
		return (val == null) ? null : val[0];
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebRequest#getParameterMap()
	 */
	@Override
	public Map getParameterMap()
	{
		return parameters;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebRequest#getParameters(java.lang.String)
	 */
	@Override
	public String[] getParameters(final String key)
	{
		return (String[])parameters.get(key);
	}

	/**
	 * Subclasses that want to receive upload notifications should return true
	 * 
	 * @return true if upload status update event should be invoked
	 */
	protected boolean wantUploadProgressUpdates()
	{
		return false;
	}

	/**
	 * Upload start callback
	 * 
	 * @param totalBytes
	 */
	protected void onUploadStarted(int totalBytes)
	{

	}

	/**
	 * Upload status update callback
	 * 
	 * @param bytesUploaded
	 * @param total
	 */
	protected void onUploadUpdate(int bytesUploaded, int total)
	{

	}

	/**
	 * Upload completed callback
	 */
	protected void onUploadCompleted()
	{

	}

	/**
	 * An {@link InputStream} that updates total number of bytes read
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private class CountingInputStream extends InputStream
	{

		private final InputStream in;

		/**
		 * Constructs a new CountingInputStream.
		 * 
		 * @param in
		 *            InputStream to delegate to
		 */
		public CountingInputStream(InputStream in)
		{
			this.in = in;
		}

		/**
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException
		{
			int read = in.read();
			bytesUploaded += (read < 0) ? 0 : 1;
			onUploadUpdate(bytesUploaded, totalBytes);
			return read;
		}

		/**
		 * @see java.io.InputStream#read(byte[])
		 */
		@Override
		public int read(byte[] b) throws IOException
		{
			int read = in.read(b);
			bytesUploaded += (read < 0) ? 0 : read;
			onUploadUpdate(bytesUploaded, totalBytes);
			return read;
		}

		/**
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			int read = in.read(b, off, len);
			bytesUploaded += (read < 0) ? 0 : read;
			onUploadUpdate(bytesUploaded, totalBytes);
			return read;
		}

	}

}