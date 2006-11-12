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
package wicket.util.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * High level API for processing file uploads.
 * </p>
 * 
 * <p>
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by <a
 * href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * 
 * <p>
 * How the data for individual parts is stored is determined by the factory used
 * to create them; a given part may be in memory, on disk, or somewhere else.
 * </p>
 * 
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 * @author Sean C. Sullivan
 */
public abstract class FileUploadBase
{

	// ---------------------------------------------------------- Class methods


	/**
	 * <p>
	 * Utility method that determines whether the request contains multipart
	 * content.
	 * </p>
	 * 
	 * <p>
	 * <strong>NOTE:</strong>This method will be moved to the
	 * <code>ServletFileUpload</code> class after the FileUpload 1.1 release.
	 * Unfortunately, since this method is static, it is not possible to provide
	 * its replacement until this method is removed.
	 * </p>
	 * 
	 * @param ctx
	 *            The request context to be evaluated. Must be non-null.
	 * 
	 * @return <code>true</code> if the request is multipart;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isMultipartContent(RequestContext ctx)
	{
		String contentType = ctx.getContentType();
		if (contentType == null)
		{
			return false;
		}
		if (contentType.toLowerCase().startsWith(MULTIPART))
		{
			return true;
		}
		return false;
	}

	// ----------------------------------------------------- Manifest constants


	/**
	 * HTTP content type header name.
	 */
	public static final String CONTENT_TYPE = "Content-type";


	/**
	 * HTTP content disposition header name.
	 */
	public static final String CONTENT_DISPOSITION = "Content-disposition";


	/**
	 * Content-disposition value for form data.
	 */
	public static final String FORM_DATA = "form-data";


	/**
	 * Content-disposition value for file attachment.
	 */
	public static final String ATTACHMENT = "attachment";


	/**
	 * Part of HTTP content type header.
	 */
	public static final String MULTIPART = "multipart/";


	/**
	 * HTTP content type header for multipart forms.
	 */
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";


	/**
	 * HTTP content type header for multiple uploads.
	 */
	public static final String MULTIPART_MIXED = "multipart/mixed";


	/**
	 * The maximum length of a single header line that will be parsed (1024
	 * bytes).
	 */
	public static final int MAX_HEADER_SIZE = 1024;


	// ----------------------------------------------------------- Data members


	/**
	 * The maximum size permitted for an uploaded file. A value of -1 indicates
	 * no maximum.
	 */
	private long sizeMax = -1;


	/**
	 * The content encoding to use when reading part headers.
	 */
	private String headerEncoding;


	// ----------------------------------------------------- Property accessors


	/**
	 * Returns the factory class used when creating file items.
	 * 
	 * @return The factory class for new file items.
	 */
	public abstract FileItemFactory getFileItemFactory();


	/**
	 * Sets the factory class to use when creating file items.
	 * 
	 * @param factory
	 *            The factory class for new file items.
	 */
	public abstract void setFileItemFactory(FileItemFactory factory);


	/**
	 * Returns the maximum allowed upload size.
	 * 
	 * @return The maximum allowed size, in bytes.
	 * 
	 * @see #setSizeMax(long)
	 * 
	 */
	public long getSizeMax()
	{
		return sizeMax;
	}


	/**
	 * Sets the maximum allowed upload size. If negative, there is no maximum.
	 * 
	 * @param sizeMax
	 *            The maximum allowed size, in bytes, or -1 for no maximum.
	 * 
	 * @see #getSizeMax()
	 * 
	 */
	public void setSizeMax(long sizeMax)
	{
		this.sizeMax = sizeMax;
	}


	/**
	 * Retrieves the character encoding used when reading the headers of an
	 * individual part. When not specified, or <code>null</code>, the
	 * platform default encoding is used.
	 * 
	 * @return The encoding used to read part headers.
	 */
	public String getHeaderEncoding()
	{
		return headerEncoding;
	}


	/**
	 * Specifies the character encoding to be used when reading the headers of
	 * individual parts. When not specified, or <code>null</code>, the
	 * platform default encoding is used.
	 * 
	 * @param encoding
	 *            The encoding used to read part headers.
	 */
	public void setHeaderEncoding(String encoding)
	{
		headerEncoding = encoding;
	}


	// --------------------------------------------------------- Public methods

	/**
	 * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
	 * compliant <code>multipart/form-data</code> stream.
	 * 
	 * @param ctx
	 *            The context for the request to be parsed.
	 * 
	 * @return A list of <code>FileItem</code> instances parsed from the
	 *         request, in the order that they were transmitted.
	 * 
	 * @exception FileUploadException
	 *                if there are problems reading/parsing the request or
	 *                storing files.
	 */
	public List /* FileItem */<FileItem> parseRequest(RequestContext ctx)
			throws FileUploadException
	{
		if (ctx == null)
		{
			throw new IllegalArgumentException("ctx parameter cannot be null");
		}

		ArrayList<FileItem> items = new ArrayList<FileItem>();
		String contentType = ctx.getContentType();

		if ((null == contentType) || (!contentType.toLowerCase().startsWith(MULTIPART)))
		{
			throw new InvalidContentTypeException("the request doesn't contain a "
					+ MULTIPART_FORM_DATA + " or " + MULTIPART_MIXED
					+ " stream, content type header is " + contentType);
		}
		int requestSize = ctx.getContentLength();

		if (requestSize == -1)
		{
			throw new UnknownSizeException("the request was rejected because its size is unknown");
		}

		if (sizeMax >= 0 && requestSize > sizeMax)
		{
			throw new SizeLimitExceededException("the request was rejected because "
					+ "its size exceeds allowed range");
		}

		try
		{
			byte[] boundary = getBoundary(contentType);
			if (boundary == null)
			{
				throw new FileUploadException("the request was rejected because "
						+ "no multipart boundary was found");
			}

			InputStream input = ctx.getInputStream();

			MultipartFormInputStream multi = new MultipartFormInputStream(input, boundary);
			multi.setHeaderEncoding(headerEncoding);

			boolean nextPart = multi.skipPreamble();

			// Don't allow a header larger than this size (to prevent DOS
			// attacks)
			final int maxHeaderBytes = 65536;
			while (nextPart)
			{
				Map<String, String> headers = parseHeaders(multi.readHeaders(maxHeaderBytes));
				String fieldName = getFieldName(headers);
				if (fieldName != null)
				{
					String subContentType = getHeader(headers, CONTENT_TYPE);
					if (subContentType != null
							&& subContentType.toLowerCase().startsWith(MULTIPART_MIXED))
					{
						// Multiple files.
						byte[] subBoundary = getBoundary(subContentType);
						multi.setBoundary(subBoundary);
						boolean nextSubPart = multi.skipPreamble();
						while (nextSubPart)
						{
							headers = parseHeaders(multi.readHeaders(maxHeaderBytes));
							if (getFileName(headers) != null)
							{
								FileItem item = createItem(headers, false);
								OutputStream os = item.getOutputStream();
								try
								{
									multi.readBodyData(os);
								}
								finally
								{
									os.close();
								}
								items.add(item);
							}
							else
							{
								// Ignore anything but files inside
								// multipart/mixed.
								multi.discardBodyData();
							}
							nextSubPart = multi.readBoundary();
						}
						multi.setBoundary(boundary);
					}
					else
					{
						FileItem item = createItem(headers, getFileName(headers) == null);
						OutputStream os = item.getOutputStream();
						try
						{
							multi.readBodyData(os);
						}
						finally
						{
							os.close();
						}
						items.add(item);
					}
				}
				else
				{
					// Skip this part.
					multi.discardBodyData();
				}
				nextPart = multi.readBoundary();
			}
		}
		catch (IOException e)
		{
			throw new FileUploadException("Processing of " + MULTIPART_FORM_DATA
					+ " request failed. " + e.getMessage());
		}

		return items;
	}


	// ------------------------------------------------------ Protected methods


	/**
	 * Retrieves the boundary from the <code>Content-type</code> header.
	 * 
	 * @param contentType
	 *            The value of the content type header from which to extract the
	 *            boundary value.
	 * 
	 * @return The boundary, as a byte array.
	 */
	protected byte[] getBoundary(String contentType)
	{
		ParameterParser parser = new ParameterParser();
		parser.setLowerCaseNames(true);
		// Parameter parser can handle null input
		Map params = parser.parse(contentType, ';');
		String boundaryStr = (String)params.get("boundary");

		if (boundaryStr == null)
		{
			return null;
		}
		byte[] boundary;
		try
		{
			boundary = boundaryStr.getBytes("ISO-8859-1");
		}
		catch (UnsupportedEncodingException e)
		{
			boundary = boundaryStr.getBytes();
		}
		return boundary;
	}


	/**
	 * Retrieves the file name from the <code>Content-disposition</code>
	 * header.
	 * 
	 * @param headers
	 *            A <code>Map</code> containing the HTTP request headers.
	 * 
	 * @return The file name for the current <code>encapsulation</code>.
	 */
	protected String getFileName(Map /* String, String */<String, String> headers)
	{
		String fileName = null;
		String cd = getHeader(headers, CONTENT_DISPOSITION);
		if (cd.startsWith(FORM_DATA) || cd.startsWith(ATTACHMENT))
		{
			ParameterParser parser = new ParameterParser();
			parser.setLowerCaseNames(true);
			// Parameter parser can handle null input
			Map params = parser.parse(cd, ';');
			if (params.containsKey("filename"))
			{
				fileName = (String)params.get("filename");
				if (fileName != null)
				{
					fileName = fileName.trim();
					int index = fileName.lastIndexOf('\\');
					if (index == -1)
					{
						index = fileName.lastIndexOf('/');
					}
					if (index != -1)
					{
						fileName = fileName.substring(index + 1);
					}
				}
				else
				{
					// Even if there is no value, the parameter is present, so
					// we return an empty file name rather than no file name.
					fileName = "";
				}
			}
		}
		return fileName;
	}


	/**
	 * Retrieves the field name from the <code>Content-disposition</code>
	 * header.
	 * 
	 * @param headers
	 *            A <code>Map</code> containing the HTTP request headers.
	 * 
	 * @return The field name for the current <code>encapsulation</code>.
	 */
	protected String getFieldName(Map /* String, String */<String, String> headers)
	{
		String fieldName = null;
		String cd = getHeader(headers, CONTENT_DISPOSITION);
		if (cd != null && cd.startsWith(FORM_DATA))
		{

			ParameterParser parser = new ParameterParser();
			parser.setLowerCaseNames(true);
			// Parameter parser can handle null input
			Map params = parser.parse(cd, ';');
			fieldName = (String)params.get("name");
			if (fieldName != null)
			{
				fieldName = fieldName.trim();
			}
		}
		return fieldName;
	}


	/**
	 * Creates a new {@link FileItem} instance.
	 * 
	 * @param headers
	 *            A <code>Map</code> containing the HTTP request headers.
	 * @param isFormField
	 *            Whether or not this item is a form field, as opposed to a
	 *            file.
	 * 
	 * @return A newly created <code>FileItem</code> instance.
	 */
	protected FileItem createItem(Map /* String, String */<String, String> headers,
			boolean isFormField)
	{
		return getFileItemFactory().createItem(getFieldName(headers),
				getHeader(headers, CONTENT_TYPE), isFormField, getFileName(headers));
	}


	/**
	 * <p>
	 * Parses the <code>header-part</code> and returns as key/value pairs.
	 * 
	 * <p>
	 * If there are multiple headers of the same names, the name will map to a
	 * comma-separated list containing the values.
	 * 
	 * @param headerPart
	 *            The <code>header-part</code> of the current
	 *            <code>encapsulation</code>.
	 * 
	 * @return A <code>Map</code> containing the parsed HTTP request headers.
	 */
	protected Map /* String, String */<String, String> parseHeaders(String headerPart)
	{
		Map<String, String> headers = new HashMap<String, String>();
		char[] buffer = new char[MAX_HEADER_SIZE];
		boolean done = false;
		int j = 0;
		int i;
		String header, headerName, headerValue;
		try
		{
			while (!done)
			{
				i = 0;
				// Copy a single line of characters into the buffer,
				// omitting trailing CRLF.
				while (i < 2 || buffer[i - 2] != '\r' || buffer[i - 1] != '\n')
				{
					buffer[i++] = headerPart.charAt(j++);
				}
				header = new String(buffer, 0, i - 2);
				if (header.equals(""))
				{
					done = true;
				}
				else
				{
					if (header.indexOf(':') == -1)
					{
						// This header line is malformed, skip it.
						continue;
					}
					headerName = header.substring(0, header.indexOf(':')).trim().toLowerCase();
					headerValue = header.substring(header.indexOf(':') + 1).trim();
					if (getHeader(headers, headerName) != null)
					{
						// More that one heder of that name exists,
						// append to the list.
						headers.put(headerName, getHeader(headers, headerName) + ',' + headerValue);
					}
					else
					{
						headers.put(headerName, headerValue);
					}
				}
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			// Headers were malformed. continue with all that was
			// parsed.
		}
		return headers;
	}


	/**
	 * Returns the header with the specified name from the supplied map. The
	 * header lookup is case-insensitive.
	 * 
	 * @param headers
	 *            A <code>Map</code> containing the HTTP request headers.
	 * @param name
	 *            The name of the header to return.
	 * 
	 * @return The value of specified header, or a comma-separated list if there
	 *         were multiple headers of that name.
	 */
	protected final String getHeader(Map /* String, String */<String, String> headers, String name)
	{
		return headers.get(name.toLowerCase());
	}


	/**
	 * Thrown to indicate that the request is not a multipart request.
	 */
	public static class InvalidContentTypeException extends FileUploadException
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a <code>InvalidContentTypeException</code> with no
		 * detail message.
		 */
		public InvalidContentTypeException()
		{
			super();
		}

		/**
		 * Constructs an <code>InvalidContentTypeException</code> with the
		 * specified detail message.
		 * 
		 * @param message
		 *            The detail message.
		 */
		public InvalidContentTypeException(String message)
		{
			super(message);
		}
	}


	/**
	 * Thrown to indicate that the request size is not specified.
	 */
	public static class UnknownSizeException extends FileUploadException
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a <code>UnknownSizeException</code> with no detail
		 * message.
		 */
		public UnknownSizeException()
		{
			super();
		}

		/**
		 * Constructs an <code>UnknownSizeException</code> with the specified
		 * detail message.
		 * 
		 * @param message
		 *            The detail message.
		 */
		public UnknownSizeException(String message)
		{
			super(message);
		}
	}


	/**
	 * Thrown to indicate that the request size exceeds the configured maximum.
	 */
	public static class SizeLimitExceededException extends FileUploadException
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a <code>SizeExceededException</code> with no detail
		 * message.
		 */
		public SizeLimitExceededException()
		{
			super();
		}

		/**
		 * Constructs an <code>SizeExceededException</code> with the specified
		 * detail message.
		 * 
		 * @param message
		 *            The detail message.
		 */
		public SizeLimitExceededException(String message)
		{
			super(message);
		}
	}

}
