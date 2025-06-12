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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload;
import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.value.ValueMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * Servlet-specific WebRequest subclass for multipart content uploads. Aimed to be used with tomcat 11+. This in
 * combination with {@link TomcatUploadProgressListenerFactory} and the setting {@link org.apache.wicket.settings.ApplicationSettings#setUseTomcatNativeFileUpload(boolean)}
 * allows to use tomcat's native multipart processing with progress reporting.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Cameron Braid
 * @author Ate Douma
 * @author Igor Vaynberg (ivaynberg)
 * @author Ernesto Reinaldo Barreiro (reiern70)
 */
public class TomcatNativeMultipartServletWebRequestImpl extends MultipartServletWebRequest
{
	/** Map of file items. */
	private final Map<String, List<FileItem>> files;

	/** Map of parameters. */
	private final ValueMap parameters;

	/**
	 * Constructor
	 *
	 * @param request
	 *            the servlet request
	 * @param filterPrefix
	 *            prefix to wicket filter mapping
	 * @param maxSize
	 *            the maximum size allowed for this request
	 */
	public TomcatNativeMultipartServletWebRequestImpl(HttpServletRequest request, String filterPrefix,
													  Bytes maxSize)
	{
		super(request, filterPrefix);

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


		List<FileItem> items = readServletParts(request);

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
						value = item.getString(Charset.forName(encoding));
					}
					catch (IOException e)
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
					fileItems = new ArrayList<>();
					files.put(item.getFieldName(), fileItems);
				}
				// Add to file list
				fileItems.add(item);
			}
		}
	}

	/**
	 * Reads the uploads' parts by using Servlet APIs. This is meant to be used with tomcat 11+;
	 *
	 * <strong>Note</strong>: Mind that in to get file upload with prpgres working you need to:
	 *
	 * 1) register a {@link TomcatUploadProgressListenerFactory}
	 * 2) set to true {@link org.apache.wicket.settings.ApplicationSettings#setUseTomcatNativeFileUpload}
	 *
	 * @param request
	 *              The http request with the upload data
	 * @return A list of {@link FileItem}s
	 * @throws FileUploadException
	 */
	private List<FileItem> readServletParts(HttpServletRequest request) throws FileUploadException
	{
		List<FileItem> itemsFromParts = new ArrayList<>();
		try
		{
			Collection<Part> parts = request.getParts();
			if (parts != null)
			{
				for (Part part : parts)
				{
					FileItem fileItem = new ServletPartFileItem(part) {
						@Override
						public ServletPartFileItem write(Path path) throws IOException {
							// we need to override this because supper method only uses file name and file is
							// not stored.
							getPart().write(path.toFile().getAbsolutePath());
							return this;
						}
					};
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


	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload)
		throws FileUploadException
	{
		// FIXME mgrigorov: Why these checks are made here ?!
		// Why they are not done also at org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl.newMultipartWebRequest(org.apache.wicket.util.lang.Bytes, java.lang.String, org.apache.wicket.util.upload.FileItemFactory)() ?
		// Why there is no check that the summary of all files' sizes is less than the set maxSize ?
		// Setting a breakpoint here never breaks with the standard upload examples.

		Bytes fileMaxSize = getFileMaxSize();
		for (Map.Entry<String, List<FileItem>> entry : files.entrySet())
		{
			List<FileItem> fileItems = entry.getValue();
			for (FileItem fileItem : fileItems)
			{
				if (fileMaxSize != null && fileItem.getSize() > fileMaxSize.bytes())
				{
					String fieldName = entry.getKey();
					FileUploadException fslex = new FileUploadByteCountLimitException("The field '" +
							fieldName + "' exceeds its maximum permitted size of '" +
							maxSize + "' characters.", fileItem.getSize(), fileMaxSize.bytes(), fileItem.getName(), fieldName);
					throw fslex;
				}
			}
		}
		return this;
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload, FileItemFactory factory)
			throws FileUploadException
	{
		return this;
	}

	private static final String SESSION_KEY = TomcatNativeMultipartServletWebRequestImpl.class.getName();

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
