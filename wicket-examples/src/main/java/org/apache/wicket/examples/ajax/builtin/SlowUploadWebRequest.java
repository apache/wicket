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
package org.apache.wicket.examples.ajax.builtin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.util.lang.Bytes;

/**
 * A request whose file uploads take {@link #DURATION}, whatever their size, so that the
 * {@link org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar} of the
 * {@link FileUploadPage} has some progress to show on a local server. Uploads are slow unless
 * the user turned it off for the session with {@link #setSlow(boolean)}.
 */
public class SlowUploadWebRequest extends ServletWebRequest
{
	/**
	 * How long a slow upload takes.
	 */
	public static final Duration DURATION = Duration.ofSeconds(5);

	private static final MetaDataKey<Boolean> FAST = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Constructor.
	 *
	 * @param httpServletRequest
	 *            the servlet request
	 * @param filterPrefix
	 *            the prefix of the Wicket filter mapping
	 */
	public SlowUploadWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
	{
		super(httpServletRequest, filterPrefix);
	}

	/**
	 * @return whether the uploads of the current session are slowed down, which they are unless
	 *         turned off
	 */
	public static boolean isSlow()
	{
		return !Session.exists() || !Boolean.TRUE.equals(Session.get().getMetaData(FAST));
	}

	/**
	 * Turns the slowing down of uploads on or off for the current session.
	 *
	 * @param slow
	 *            whether the uploads are slowed down
	 */
	public static void setSlow(boolean slow)
	{
		Session.get().setMetaData(FAST, slow ? null : Boolean.TRUE);
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload)
		throws FileUploadException
	{
		if (isSlow())
		{
			return new SlowMultipartWebRequest(getContainerRequest(), getFilterPrefix(), maxSize,
				upload);
		}
		return super.newMultipartWebRequest(maxSize, upload);
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload,
		FileItemFactory factory) throws FileUploadException
	{
		if (isSlow())
		{
			return new SlowMultipartWebRequest(getContainerRequest(), getFilterPrefix(), maxSize,
				upload, factory);
		}
		return super.newMultipartWebRequest(maxSize, upload, factory);
	}

	/**
	 * Waits after every chunk read until the upload has taken its share of
	 * {@link SlowUploadWebRequest#DURATION}.
	 */
	static class SlowMultipartWebRequest extends MultipartServletWebRequestImpl
	{
		private long started;

		SlowMultipartWebRequest(HttpServletRequest request, String filterPrefix, Bytes maxSize,
			String upload) throws FileUploadException
		{
			super(request, filterPrefix, maxSize, upload);
		}

		SlowMultipartWebRequest(HttpServletRequest request, String filterPrefix, Bytes maxSize,
			String upload, FileItemFactory factory) throws FileUploadException
		{
			super(request, filterPrefix, maxSize, upload, factory);
		}

		@Override
		protected void onUploadStarted(int totalBytes)
		{
			started = System.nanoTime();
			super.onUploadStarted(totalBytes);
		}

		@Override
		protected void onUploadUpdate(int bytesUploaded, int total)
		{
			super.onUploadUpdate(bytesUploaded, total);

			long due = started + (long)((double)DURATION.toNanos() * bytesUploaded / Math.max(total, 1));
			long wait = due - System.nanoTime();
			if (wait > 0)
			{
				try
				{
					TimeUnit.NANOSECONDS.sleep(wait);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
