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
package org.apache.wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadException;

/**
 * Multipart request object that feeds the upload info into session
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
class MultipartRequest extends MultipartServletWebRequestImpl
{


	public MultipartRequest(final HttpServletRequest request, final String filterPrefix,
		final Bytes maxSize, final FileItemFactory factory) throws FileUploadException
	{
		super(request, filterPrefix, maxSize, factory);
	}

	public MultipartRequest(final HttpServletRequest request, final String filterPrefix,
		final Bytes maxSize) throws FileUploadException
	{
		super(request, filterPrefix, maxSize);
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl#wantUploadProgressUpdates()
	 */
	@Override
	protected boolean wantUploadProgressUpdates()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl#onUploadStarted(int)
	 */
	@Override
	protected void onUploadStarted(final int totalBytes)
	{
		UploadInfo info = new UploadInfo(totalBytes);

		UploadWebRequest.setUploadInfo(getContainerRequest(), info);
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl#onUploadUpdate(int,
	 *      int)
	 */
	@Override
	protected void onUploadUpdate(final int bytesUploaded, final int total)
	{
		HttpServletRequest request = getContainerRequest();
		UploadInfo info = UploadWebRequest.getUploadInfo(request);
		if (info == null)
		{
			throw new IllegalStateException(
				"could not find UploadInfo object in session which should have been set when uploaded started");
		}
		info.setBytesUploaded(bytesUploaded);

		UploadWebRequest.setUploadInfo(request, info);
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl#onUploadCompleted()
	 */
	@Override
	protected void onUploadCompleted()
	{
		UploadWebRequest.clearUploadInfo(getContainerRequest());
	}
}