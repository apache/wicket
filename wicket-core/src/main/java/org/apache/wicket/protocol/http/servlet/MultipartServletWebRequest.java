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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.upload.FileItem;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileUploadException;

/**
 * Servlet specific WebRequest subclass for multipart content uploads.
 * 
 * @author Matej Knopp
 */
public abstract class MultipartServletWebRequest extends ServletWebRequest
	implements
		IMultipartWebRequest
{
	/**
	 *  Maximum size of all uploaded files in bytes in a request.
	 */
	private Bytes maxSize;

	/**
	 *  Maximum size of file of upload in bytes (if there are more than one) in a request.
	 */
	private Bytes fileMaxSize;

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * @param filterPrefix
	 */
	public MultipartServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
	{
		super(httpServletRequest, filterPrefix);
	}

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * @param filterPrefix
	 * @param url
	 */
	public MultipartServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix,
		Url url)
	{
		super(httpServletRequest, filterPrefix, url);
	}

	/**
	 * Parses the multipart body of the request.
	 *
	 * @throws FileUploadException
	 * @since 6.18.0
	 */
	// this method should really be 'abstract' but it is not to preserve binary backward compatibility
	public void parseFileParts() throws FileUploadException
	{
	}

	@Override
	public ServletWebRequest cloneWithUrl(Url url)
	{
		return new MultipartServletWebRequest(getContainerRequest(), getFilterPrefix(), url)
		{
			@Override
			public void parseFileParts() throws FileUploadException
			{
				MultipartServletWebRequest.this.parseFileParts();
			}

			@Override
			public List<FileItem> getFile(String fieldName)
			{
				return MultipartServletWebRequest.this.getFile(fieldName);
			}

			@Override
			public Map<String, List<FileItem>> getFiles()
			{
				return MultipartServletWebRequest.this.getFiles();
			}

			@Override
			public IRequestParameters getPostParameters()
			{
				return MultipartServletWebRequest.this.getPostParameters();
			}
		};
	}

	public Bytes getMaxSize()
	{
		return maxSize;
	}

	public void setMaxSize(Bytes maxSize)
	{
		Args.notNull(maxSize, "maxSize");
		this.maxSize = maxSize;
	}

	public Bytes getFileMaxSize()
	{
		return fileMaxSize;
	}

	public void setFileMaxSize(Bytes fileMaxSize)
	{
		this.fileMaxSize = fileMaxSize;
	}
}
