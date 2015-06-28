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
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * An adapter of {@link Part} to {@link FileItem}
 */
class ServletPartFileItem implements FileItem
{
	/**
	 * The adapted part
	 */
	private final Part part;

	/**
	 * Constructor
	 */
	ServletPartFileItem(Part part)
	{
		Args.notNull(part, "part");
		this.part = part;
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
		String contentDisposition = part.getHeader("content-disposition");
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
	public String getString(String encoding) throws UnsupportedEncodingException
	{
		byte[] bytes = get();
		return new String(bytes, encoding);
	}

	@Override
	public String getString()
	{
		try
		{
			return getString("UTF-8");
		}
		catch (UnsupportedEncodingException uex)
		{
			throw new WicketRuntimeException("UTF-8 must be supported", uex);
		}
	}

	@Override
	public void write(File file) throws Exception
	{
		part.write(file.getName());
	}

	@Override
	public void delete()
	{
		try
		{
			part.delete();
		}
		catch (IOException iox)
		{
			throw new WicketRuntimeException("A problem occurred while deleting an upload part", iox);
		}
	}

	@Override
	public String getFieldName()
	{
		return part.getName();
	}

	@Override
	public void setFieldName(String name)
	{
		throw new UnsupportedOperationException("setFieldName");
	}

	@Override
	public boolean isFormField()
	{
		return false;
	}

	@Override
	public void setFormField(boolean state)
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
		FileItemHeadersImpl fileItemHeaders = new FileItemHeadersImpl();
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
	public void setHeaders(FileItemHeaders headers)
	{
		throw new UnsupportedOperationException("setHeaders");
	}
}
