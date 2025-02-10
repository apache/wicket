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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.Part;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory.AbstractFileItemBuilder;
import org.apache.commons.fileupload2.core.FileItemHeaders;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * An adapter of Servlet 3.0 {@link Part} to Apache Commons FileUpload's {@link FileItem}
 */
class ServletPartFileItem implements FileItem
{
	/**
	 * The adapted part
	 */
	private final Part part;
	private final boolean isFormField;

	/**
	 * Constructor
	 */
	ServletPartFileItem(@Nonnull Part part)
	{
		Args.notNull(part, "part");
		this.part = part;

		String contentType = part.getContentType();
		this.isFormField = contentType == null;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return part.getInputStream();
	}

	@Override
	public String getContentType()
	{
		return part.getContentType();
	}

	@Override
	public String getName()
	{
		return getFileName(part);
	}

	private String getFileName(Part part)
	{
		String contentDisposition = part.getHeader(AbstractResource.CONTENT_DISPOSITION_HEADER_NAME);
		for (String cd : Strings.split(contentDisposition, ';'))
		{
			if (cd.trim().startsWith("filename"))
			{
				return cd.substring(cd.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}

	@Override
	public boolean isInMemory()
	{
		return true;
	}

	@Override
	public long getSize()
	{
		return part.getSize();
	}

	@Override
	public byte[] get()
	{
		try
		{
			return IOUtils.toByteArray(getInputStream());
		}
		catch (IOException iox)
		{
			throw new WicketRuntimeException("Could not read upload's part input stream", iox);
		}
	}

	@Override
	public String getString(Charset toCharset) throws IOException
	{
		byte[] bytes = get();
		return new String(bytes, toCharset);
	}

	@Override
	public String getString()
	{
		try
		{
			return getString(StandardCharsets.UTF_8);
		}
		catch (IOException uex)
		{
			throw new WicketRuntimeException("UTF-8 must be supported", uex);
		}
	}

	@Override
	public ServletPartFileItem write(Path path) throws IOException
	{
		part.write(path.toFile().getName());
		return this;
	}

	@Override
	public ServletPartFileItem delete()
	{
		try
		{
			part.delete();
		}
		catch (IOException iox)
		{
			throw new WicketRuntimeException("A problem occurred while deleting an upload part", iox);
		}
		return this;
	}

	@Override
	public String getFieldName()
	{
		return part.getName();
	}

	@Override
	public ServletPartFileItem setFieldName(String name)
	{
		throw new UnsupportedOperationException("setFieldName");
	}

	@Override
	public boolean isFormField()
	{
		return isFormField;
	}

	@Override
	public ServletPartFileItem setFormField(boolean state)
	{
		throw new UnsupportedOperationException("setFormField");
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		throw new UnsupportedOperationException("getOutputStream");
	}

	@Override
	public FileItemHeaders getHeaders()
	{
		FileItemHeaders fileItemHeaders = AbstractFileItemBuilder.newFileItemHeaders();
		for (String headerName : part.getHeaderNames())
		{
			Collection<String> headerValues = part.getHeaders(headerName);
			for (String headerValue : headerValues)
			{
				fileItemHeaders.addHeader(headerName, headerValue);
			}
		}
		return fileItemHeaders;
	}

	@Override
	public ServletPartFileItem setHeaders(FileItemHeaders headers)
	{
		throw new UnsupportedOperationException("setHeaders");
	}
}
