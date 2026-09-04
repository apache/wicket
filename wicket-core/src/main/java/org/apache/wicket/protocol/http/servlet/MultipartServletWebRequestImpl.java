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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.FileUploadFileCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletRequestContext;
import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.file.FileCleanerTrackerAdapter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;
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
	private final FileItemFactory fileItemFactory;

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
		this(request, filterPrefix, maxSize, upload,
			DiskFileItemFactory.builder()
					.setFileCleaningTracker(new FileCleanerTrackerAdapter(Application.get()
							.getResourceSettings()
							.getFileCleaner()))
					.get());
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

		Args.notNull(upload, "upload");
		this.upload = upload;
		this.fileItemFactory = factory;
		parameters = new ValueMap();
		files = new HashMap<>();

		// Check that request is multipart
		final boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
		if (!isMultipart)
		{
			throw new IllegalStateException(
				"ServletRequest does not contain multipart content. One possible solution is to explicitly call Form.setMultipart(true), Wicket tries its best to auto-detect multipart forms but there are certain situations where it cannot.");
		}

		setMaxSize(maxSize);
	}

	@Override
	public void parseFileParts() throws FileUploadException
	{
		HttpServletRequest request = getContainerRequest();

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

		AbstractFileUpload fileUpload = newFileUpload(encoding);

		List<FileItem> items;

		if (wantUploadProgressUpdates())
		{
			JakartaServletRequestContext ctx = new JakartaServletRequestContext(request)
			{
				@Override
				public InputStream getInputStream() throws IOException
				{
					return new CountingInputStream(super.getInputStream());
				}
			};
			totalBytes = request.getContentLength();

			onUploadStarted(totalBytes);
			try
			{
				items = fileUpload.parseRequest(ctx);
			}
			finally
			{
				onUploadCompleted();
			}
		}
		else
		{
			// try to parse the file uploads by using Apache Commons FileUpload APIs
			// because they are feature richer (e.g. progress updates, cleaner)
			items = fileUpload.parseRequest(new JakartaServletRequestContext(request));
			if (items.isEmpty())
			{
				// fallback to Servlet 3.0 APIs
				items = readServlet3Parts(request);
			}
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
				
				try 
				{
					if (encoding != null)
					{
						value = item.getString(Charset.forName(encoding));
					}
					else
					{
						value = item.getString();
					}
				}
				catch (IOException e)
				{
					throw new WicketRuntimeException(e);
				}

				addParameter(item.getFieldName(), value);
			}
			else
			{
				List<FileItem> fileItems = files.get(item.getFieldName());
				if (fileItems == null)
				{
					fileItems = new ArrayList<>();
					files.put(item.getFieldName(), fileItems);
				}
				// Add to file list
				fileItems.add(item);
			}
		}
	}

	/**
	 * Reads the uploads' parts by using Servlet 3.0 APIs.
	 *
	 * <strong>Note</strong>: By using Servlet 3.0 APIs the application won't be able to use
	 * upload progress updates.
	 * <p>
	 * The container has already parsed these parts, so the limits {@link #newFileUpload(String)}
	 * hands to commons-fileupload never had a chance to apply to them. They are applied here instead,
	 * with the same semantics and the same exception types, so that a request arriving along this path
	 * is accepted or rejected exactly as it would have been had commons-fileupload done the parsing.
	 * The checks use {@link Part#getSize()} and so complete before any part is read, which matters
	 * because {@link #parseFileParts()} materialises every form field in memory.
	 * <p>
	 * The per-file and file-count limits are the ones this path did not apply before. The aggregate
	 * {@code maxSize} is also checked, though commons-fileupload has normally rejected an oversized
	 * request already by comparing {@code Content-Length} against it before reading the body; the
	 * check below covers a request that declares no length at all.
	 *
	 * @param request
	 *              The http request with the upload data
	 * @return A list of {@link FileItem}s
	 * @throws FileUploadException
	 *              if the parts exceed the maximum upload size, the maximum size of a single file, or
	 *              the maximum number of files
	 */
	private List<FileItem> readServlet3Parts(HttpServletRequest request) throws FileUploadException
	{
		List<FileItem> itemsFromParts = new ArrayList<>();
		try
		{
			Collection<Part> parts = request.getParts();
			if (parts != null)
			{
				final long fileCountMax = getFileCountMax();
				final Bytes fileMaxSize = getFileMaxSize();
				final long maxSize = getMaxSize().bytes();
				long totalSize = 0;

				for (Part part : parts)
				{
					// a negative fileCountMax means unlimited, as in AbstractFileUpload
					if (fileCountMax >= 0 && itemsFromParts.size() >= fileCountMax)
					{
						throw new FileUploadFileCountLimitException(
							String.format("Request '%s' failed: Maximum file count %,d exceeded.",
								AbstractFileUpload.MULTIPART_FORM_DATA, fileCountMax),
							fileCountMax, itemsFromParts.size());
					}

					FileItem fileItem = new ServletPartFileItem(part);
					long size = fileItem.getSize();

					// commons-fileupload applies this to every part, form fields included
					if (fileMaxSize != null && size > fileMaxSize.bytes())
					{
						throw new FileUploadByteCountLimitException(
							String.format("The field %s exceeds its maximum permitted size of %s bytes.",
								fileItem.getFieldName(), fileMaxSize.bytes()),
							size, fileMaxSize.bytes(), fileItem.getName(), fileItem.getFieldName());
					}

					totalSize += size;
					if (totalSize > maxSize)
					{
						throw new FileUploadSizeException(String.format(
							"the request was rejected because its size (%s) exceeds the configured maximum (%s)",
							totalSize, maxSize), maxSize, totalSize);
					}

					itemsFromParts.add(fileItem);
				}
			}
		} catch (IOException | ServletException e)
		{
			throw new FileUploadException("An error occurred while reading the upload parts", e);
		}
		return itemsFromParts;
	}

	/**
	 * Factory method for creating new instances of AbstractFileUpload
	 *
	 * @param encoding
	 *            The encoding to use while reading the data
	 * @return A new instance of AbstractFileUpload
	 */
	protected AbstractFileUpload newFileUpload(String encoding) {
		// Configure the factory here, if desired.
		JakartaServletFileUpload fileUpload = new JakartaServletFileUpload(fileItemFactory);

		// set encoding specifically when we found it
		if (encoding != null)
		{
			Charset charset = Charset.forName(encoding);
			fileUpload.setHeaderCharset(charset);
		}

		fileUpload.setMaxSize(getMaxSize().bytes());

		Bytes fileMaxSize = getFileMaxSize();

		if (fileMaxSize != null)
		{
			fileUpload.setMaxFileSize(fileMaxSize.bytes());
		}

		fileUpload.setMaxFileCount(getFileCountMax());
		fileUpload.setMaxPartHeaderSize(getDefaultMaximumPartHeaderSize());

		return fileUpload;
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

		String[] newVal;

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
		Map<String, List<StringValue>> res = new HashMap<>();
		for (Map.Entry<String, Object> entry : parameters.entrySet())
		{
			String key = entry.getKey();
			String[] val = (String[])entry.getValue();
			if (val != null && val.length > 0)
			{
				List<StringValue> items = new ArrayList<>();
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
	 * Subclasses that want to receive upload notifications should return true. By default, it takes
	 * the value from {@link org.apache.wicket.settings.ApplicationSettings#isUploadProgressUpdatesEnabled()}.
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

		@Override
		public int read() throws IOException
		{
			int read = in.read();
			bytesUploaded += (read < 0) ? 0 : 1;
			onUploadUpdate(bytesUploaded, totalBytes);
			return read;
		}

		@Override
		public int read(byte[] b) throws IOException
		{
			int read = in.read(b);
			bytesUploaded += (read < 0) ? 0 : read;
			onUploadUpdate(bytesUploaded, totalBytes);
			return read;
		}

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
		// This request is already multipart, so there is nothing to create. The limits are enforced by
		// parseFileParts(), on both the commons-fileupload and the Servlet parts path, which is after
		// this method runs: Form#handleMultiPart() sets fileMaxSize and fileCountMax and only then
		// parses.
		return this;
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload, FileItemFactory factory)
			throws FileUploadException
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
