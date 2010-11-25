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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileUploadException;

/**
 * Multipart request object that feeds the upload info into session
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
class MultipartRequest extends MultipartServletWebRequest
{
	/**
	 * @param req
	 * @param maxSize
	 * @throws FileUploadException
	 */
	public MultipartRequest(HttpServletRequest req, Bytes maxSize) throws FileUploadException
	{
		super(req, maxSize);
		if (req == null)
		{
			throw new IllegalStateException("req cannot be null");
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest#wantUploadProgressUpdates()
	 */
	@Override
	protected boolean wantUploadProgressUpdates()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest#onUploadStarted(int)
	 */
	@Override
	protected void onUploadStarted(int totalBytes)
	{
		UploadInfo info = new UploadInfo(totalBytes);

		HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest()).getHttpServletRequest();
		UploadWebRequest.setUploadInfo(request, info);
	}

	/**
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest#onUploadUpdate(int,
	 *      int)
	 */
	@Override
	protected void onUploadUpdate(int bytesUploaded, int total)
	{
		HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest()).getHttpServletRequest();
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
	 * @see org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest#onUploadCompleted()
	 */
	@Override
	protected void onUploadCompleted()
	{
		HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest()).getHttpServletRequest();
		UploadWebRequest.clearUploadInfo(request);
	}
}