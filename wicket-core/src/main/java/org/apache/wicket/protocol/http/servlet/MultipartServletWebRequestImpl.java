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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.upload.DiskFileItemFactory;
import org.apache.wicket.util.upload.FileItem;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadBase;
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
public class MultipartServletWebRequestImpl extends MultipartServletWebRequest
{
	/** Map of file items. */
	private final Map<String, List<FileItem>> files;

	/** Map of parameters. */
	private final ValueMap parameters;

	private final String upload;

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
	 * @param request
	 *            the servlet request
	 * @param filterPrefix
	 *            prefix to wicket filter mapping
	 * @param maxSize
	 *            the maximum size allowed for this request
	 * @param upload
	 *            upload identifier for {@link UploadInfo}
	 * @throws FileUploadException
	 *             Thrown if something goes wrong with upload
	 */
	public MultipartServletWebRequestImpl(HttpServletRequest request, String filterPrefix,
		Bytes maxSize, String upload) throws FileUploadException
	{
		this(request, filterPrefix, maxSize, upload, new DiskFileItemFactory(Application.get()
			.getResourceSettings()
			.getFileCleaner()));
	}

	/**
	 * Constructor
	 * 
	 * @param request
	 *            the servlet request
	 * @param filterPrefix
	 *            prefix to wicket filter mapping
	 * @param maxSize
	 *            the maximum size allowed for this request
	 * @param upload
	 *            upload identifier for {@link UploadInfo}
	 * @param factory
	 *            {@link DiskFileItemFactory} to use when creating file items used to represent
	 *            uploaded files
	 * @throws FileUploadException
	 *             Thrown if something goes wrong with upload
	 */
	public MultipartServletWebRequestImpl(HttpServletRequest request, String filterPrefix,
		Bytes maxSize, String upload, FileItemFactory factory) throws FileUploadException
	{
		super(request, filterPrefix);

		Args.notNull(maxSize, "maxSize");
		Args.notNull(upload, "upload");
		this.upload = upload;
		parameters = new ValueMap();
		files = new HashMap<String, List<FileItem>>();

		// Check that request is multipart
		final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart)
		{
			throw new IllegalStateException(
				"ServletRequest does not contain multipart content. One possible solution is to explicitly call Form.setMultipart(true), Wicket tries its best to auto-detect multipart forms but there are certain situation where it cannot.");
		}

		// Configure the factory here, if desired.
		ServletFileUpload fileUpload = new ServletFileUpload(factory);

		// The encoding that will be used to decode the string parameters
		// It should NOT be null at this point, but it may be
		// especially if the older Servlet API 2.2 is used
		String encoding = request.getCharacterEncoding();

		// The encoding can also be null when using multipart/form-data encoded forms.
		// In that case we use the [application-encoding] which we always demand using
		// the attribute 'accept-encoding' in wicket forms.
		if (encoding == null)
		{
			encoding = Application.get().getRequestCycleSettings().getResponseRequestEncoding();
		}

		// set encoding specifically when we found it
		if (encoding != null)
		{
			fileUpload.setHeaderEncoding(encoding);
		}

		fileUpload.setSizeMax(maxSize.bytes());

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
			items = fileUpload.parseRequest(ctx);
			onUploadCompleted();

		}
		else
		{
			items = fileUpload.parseRequest(request);
		}

		// Loop through items
		for (final FileItem item : items)
		{
			// Get next item
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
				List<FileItem> fileItems = files.get(item.getFieldName());
				if (fileItems == null)
				{
					fileItems = new ArrayList<FileItem>();
					files.put(item.getFieldName(), fileItems);
				}
				// Add to file list
				fileItems.add(item);
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
	@Override
	public Map<String, List<FileItem>> getFiles()
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
	@Override
	public List<FileItem> getFile(final String fieldName)
	{
		return files.get(fieldName);
	}

	@Override
	protected Map<String, List<StringValue>> generatePostParameters()
	{
		Map<String, List<StringValue>> res = new HashMap<String, List<StringValue>>();
		for (String key : parameters.keySet())
		{
			String[] val = (String[])parameters.get(key);
			if (val != null && val.length > 0)
			{
				List<StringValue> items = new ArrayList<StringValue>();
				for (String s : val)
				{
					items.add(StringValue.valueOf(s));
				}
				res.put(key, items);
			}
		}
		return res;
	}

	/**
	 * Subclasses that want to receive upload notifications should return true. By default it takes
	 * the value from {@link IApplicationSettings#isUploadProgressUpdatesEnabled()}.
	 * 
	 * @return true if upload status update event should be invoked
	 */
	protected boolean wantUploadProgressUpdates()
	{
		return Application.get().getApplicationSettings().isUploadProgressUpdatesEnabled();
	}

	/**
	 * Upload start callback
	 * 
	 * @param totalBytes
	 */
	protected void onUploadStarted(int totalBytes)
	{
		UploadInfo info = new UploadInfo(totalBytes);

		setUploadInfo(getContainerRequest(), upload, info);
	}

	/**
	 * Upload status update callback
	 * 
	 * @param bytesUploaded
	 * @param total
	 */
	protected void onUploadUpdate(int bytesUploaded, int total)
	{
		HttpServletRequest request = getContainerRequest();
		UploadInfo info = getUploadInfo(request, upload);
		if (info == null)
		{
			throw new IllegalStateException(
				"could not find UploadInfo object in session which should have been set when uploaded started");
		}
		info.setBytesUploaded(bytesUploaded);

		setUploadInfo(request, upload, info);
	}

	/**
	 * Upload completed callback
	 */
	protected void onUploadCompleted()
	{
		clearUploadInfo(getContainerRequest(), upload);
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

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload)
		throws FileUploadException
	{
		for (Map.Entry<String, List<FileItem>> entry : files.entrySet())
		{
			List<FileItem> fileItems = entry.getValue();
			for (FileItem fileItem : fileItems)
			{
				if (fileItem.getSize() > maxSize.bytes())
				{
					String fieldName = entry.getKey();
					FileUploadException fslex = new FileUploadBase.FileSizeLimitExceededException("The field " +
							fieldName + " exceeds its maximum permitted " + " size of " +
							maxSize + " characters.", fileItem.getSize(), maxSize.bytes());
					throw fslex;
				}
			}
		}
		return this;
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload,
		FileItemFactory factory) throws FileUploadException
	{
		return this;
	}

	private static final String SESSION_KEY = MultipartServletWebRequestImpl.class.getName();

	private static String getSessionKey(String upload)
	{
		return SESSION_KEY + ":" + upload;
	}

	/**
	 * Retrieves {@link UploadInfo} from session, null if not found.
	 * 
	 * @param req
	 *            http servlet request, not null
	 * @param upload
	 *            upload identifier
	 * @return {@link UploadInfo} object from session, or null if not found
	 */
	public static UploadInfo getUploadInfo(final HttpServletRequest req, String upload)
	{
		Args.notNull(req, "req");
		return (UploadInfo)req.getSession().getAttribute(getSessionKey(upload));
	}

	/**
	 * Sets the {@link UploadInfo} object into session.
	 * 
	 * @param req
	 *            http servlet request, not null
	 * @param upload
	 *            upload identifier
	 * @param uploadInfo
	 *            {@link UploadInfo} object to be put into session, not null
	 */
	public static void setUploadInfo(final HttpServletRequest req, String upload,
		final UploadInfo uploadInfo)
	{
		Args.notNull(req, "req");
		Args.notNull(upload, "upload");
		Args.notNull(uploadInfo, "uploadInfo");
		req.getSession().setAttribute(getSessionKey(upload), uploadInfo);
	}

	/**
	 * Clears the {@link UploadInfo} object from session if one exists.
	 * 
	 * @param req
	 *            http servlet request, not null
	 * @param upload
	 *            upload identifier
	 */
	public static void clearUploadInfo(final HttpServletRequest req, String upload)
	{
		Args.notNull(req, "req");
		Args.notNull(upload, "upload");
		req.getSession().removeAttribute(getSessionKey(upload));
	}
}