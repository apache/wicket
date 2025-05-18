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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.http.fileupload.ProgressListener;
import org.apache.tomcat.util.http.fileupload.ProgressListenerFactory;
import org.apache.wicket.Application;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link ProgressListenerFactory} that allows reporting upload progress but uses tomcat native multipart machinery.
 */
public class TomcatUploadProgressListenerFactory implements ProgressListenerFactory
{
	/**
	 * Interface used to generate upload IDs. These IDs connect Wicket UI with tomcat progress reporting
	 */
	public interface IUploadIdGenerator
	{
		/**
		 * @return The unique ID for the upload.
		 */
		String newUploadId();
	}

	private static class AppUploadIdGenerator implements IUploadIdGenerator
	{

		private static final AppUploadIdGenerator instance = new AppUploadIdGenerator();

		public static AppUploadIdGenerator getInstance()
		{
			return instance;
		}

		private final AtomicLong counter = new AtomicLong();

		private AppUploadIdGenerator()
		{
		}

		@Override
		public String newUploadId() {
			return "upload-" + counter.incrementAndGet();
		}
	}

	/**
	 * Progress listener to be called by Tomcat to report multipart (file upload) progress.-
	 */
	public static class WicketProgressListener implements ProgressListener
	{

		private final String uploadId;
		private final HttpServletRequest servletRequest;
		private final long totalBytes;

		private WicketProgressListener(String uploadId, HttpServletRequest servletRequest)
		{
			Args.notEmpty(uploadId, "uploadId");
			this.uploadId = uploadId;
			Args.notNull(servletRequest, "servletRequest");
			this.servletRequest = servletRequest;
			this.totalBytes = servletRequest.getContentLength();
		}

		@Override
		public void uploadStarted()
		{
			MultipartServletWebRequestImpl.onUploadStarted(servletRequest, this.uploadId, this.totalBytes);
		}

		@Override
		public void update(long pBytesRead, long pContentLength, int pItems)
		{
			MultipartServletWebRequestImpl.onUploadUpdate(servletRequest, uploadId, pBytesRead, pContentLength);
		}

		@Override
		public void uploadFinished()
		{
			MultipartServletWebRequestImpl.onUploadCompleted(servletRequest, this.uploadId);
		}
	}

	private static IUploadIdGenerator iUploadIdGenerator = AppUploadIdGenerator.getInstance();


	public TomcatUploadProgressListenerFactory()
	{
		// constructor for reflection-based instantiation
	}


	@Override
	public ProgressListener newProgressListener(HttpServletRequest servletRequest) {
		// there is no need to check if we are in multipart request
		// we are because tomcat will only call this in the context of a
		// multipart request.
		if (wantUploadProgressUpdates())
		{
			// we extract the uploadId from the request
			Url url = Url.parse(servletRequest.getRequestURL() + "?" + servletRequest.getQueryString());
			Optional<Url.QueryParameter> queryParameter = url.getQueryParameters().stream().filter(
					queryParameter1 -> queryParameter1.getName().equals("uploadId")).findFirst();
			if (queryParameter.isPresent())
			{
				String uploadId = queryParameter.get().getValue();
				return new WicketProgressListener(uploadId, servletRequest);
			}
		}
		return null;
	}

	protected boolean wantUploadProgressUpdates()
	{
		return Application.get().getApplicationSettings().isUploadProgressUpdatesEnabled();
	}

	public static String  getUploadId() {
		if (Application.get().getApplicationSettings().isUseTomcatNativeFileUpload())
		{
			return iUploadIdGenerator.newUploadId();
		}
		return null;
	}

	/**
	 * Allows setting the {@link IUploadIdGenerator}
	 *
	 * @param iUploadIdGenerator {@link IUploadIdGenerator}
	 */
	public static void setUploadIdGenerator(IUploadIdGenerator iUploadIdGenerator)
	{
		Args.notNull(iUploadIdGenerator, "iUploadIdGenerator");
		TomcatUploadProgressListenerFactory.iUploadIdGenerator = iUploadIdGenerator;
	}
}
