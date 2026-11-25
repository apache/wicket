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
package org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.wicket.markup.html.form.upload.resource.AbstractFileUploadResource;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValueConversionException;

/**
 * The id of an upload field and the limits of its uploads, which the field signs and its script
 * sends along with every request, as the query parameters of
 * {@link org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField} are. The
 * signature also covers when the grant expires, which is sent along, and who it was made for,
 * {@link ResumableUploadResourceReference#getCurrentOwner()}, which is not: the server puts in the
 * owner of each request when it checks the signature, so the grant of one owner never works for
 * another, and no id of a session ends up in the page.
 *
 * @param uploadFieldId
 *            the unique id of the field
 * @param context
 *            the part of the application the uploads belong to, see
 *            {@link ResumableUploadToResourceField#setUploadContext(String)}, {@code null} for none
 * @param maxSize
 *            the size of the files sent at once at most, in bytes
 * @param fileMaxSize
 *            the size of a file at most, in bytes, -1 for none
 * @param fileCountMax
 *            the number of files sent at once at most, -1 for any
 * @param expires
 *            when the grant expires, in milliseconds since the epoch
 */
record UploadGrant(String uploadFieldId, String context, long maxSize, long fileMaxSize,
	long fileCountMax, long expires)
{
	/** The query parameter of when the grant expires. */
	static final String EXPIRES = "grantExpires";

	/** The query parameter of the context of the uploads. */
	static final String CONTEXT = "uploadContext";

	/** Keeps the tokens of these fields apart from those of other uploads. */
	private static final String PURPOSE = "resumable-upload";

	/** The largest number a script holds exactly, {@code Number.MAX_SAFE_INTEGER}. */
	private static final long MAX_SAFE_INTEGER = (1L << 53) - 1;

	/**
	 * @return the grant of a field, its sizes no larger than a script holds exactly: it sends them
	 *         back as it got them
	 */
	static UploadGrant of(String uploadFieldId, String context, Bytes maxSize, Bytes fileMaxSize,
		long fileCountMax, Instant expires)
	{
		return new UploadGrant(uploadFieldId, context, Math.min(maxSize.bytes(), MAX_SAFE_INTEGER),
			fileMaxSize != null ? Math.min(fileMaxSize.bytes(), MAX_SAFE_INTEGER) : -1,
			fileCountMax, expires.toEpochMilli());
	}

	/**
	 * @return the size of a file at most, in bytes
	 */
	long maxFileSize()
	{
		return fileMaxSize >= 0 ? Math.min(maxSize, fileMaxSize) : maxSize;
	}

	/**
	 * @param now
	 *            the current time
	 * @return whether the grant has expired
	 */
	boolean isExpired(Instant now)
	{
		return now.toEpochMilli() >= expires;
	}

	/**
	 * @param signer
	 *            the signer of the grants
	 * @param owner
	 *            whom the grant is for, {@code null} for nobody
	 * @return the grant signed, for the browser to send it back
	 */
	String token(IUploadGrantSigner signer, String owner)
	{
		return signer.sign(serialized(owner));
	}

	/**
	 * @param signer
	 *            the signer of the grants
	 * @param owner
	 *            whom the grant is for, {@code null} for nobody
	 * @return the query of the grant, as the script of
	 *         {@link org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField}
	 *         writes it, with when it expires
	 */
	String query(IUploadGrantSigner signer, String owner)
	{
		return AbstractFileUploadResource.UPLOAD_ID + '=' + encoded(uploadFieldId) + '&' +
			AbstractFileUploadResource.MAX_SIZE + '=' + maxSize + '&' +
			AbstractFileUploadResource.FILE_MAX_SIZE + '=' + fileMaxSize + '&' +
			AbstractFileUploadResource.FILE_COUNT_MAX + '=' + fileCountMax + '&' +
			AbstractFileUploadResource.UPLOAD_TOKEN + '=' + encoded(token(signer, owner)) + '&' +
			EXPIRES + '=' + expires + (context != null ? '&' + CONTEXT + '=' + encoded(context) : "");
	}

	/**
	 * @return whether a text can be a line of the signed grant: one with a line break could stand
	 *         for others
	 */
	static boolean isLine(String text)
	{
		return text == null || (text.indexOf('\n') < 0 && text.indexOf('\r') < 0);
	}

	private static String encoded(String value)
	{
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String serialized(String owner)
	{
		return PURPOSE + '\n' + uploadFieldId + '\n' + (context != null ? context : "") + '\n' +
			maxSize + '\n' + fileMaxSize + '\n' +
			fileCountMax + '\n' + expires + '\n' + (owner != null ? owner : "");
	}

	/**
	 * @param parameters
	 *            the query parameters of a request
	 * @param signer
	 *            the signer of the grants
	 * @param owner
	 *            the owner of the request
	 * @return the grant the parameters carry, {@code null} when they carry none, or one whose
	 *         token was not made for them and the owner with the signer's key; whether it expired
	 *         is not checked
	 */
	static UploadGrant of(IRequestParameters parameters, IUploadGrantSigner signer, String owner)
	{
		String token = parameters.getParameterValue(AbstractFileUploadResource.UPLOAD_TOKEN)
			.toOptionalString();
		String uploadFieldId = parameters.getParameterValue(AbstractFileUploadResource.UPLOAD_ID)
			.toOptionalString();
		String context = parameters.getParameterValue(CONTEXT).toOptionalString();
		if (token == null || uploadFieldId == null || !isLine(uploadFieldId) || !isLine(context))
		{
			return null;
		}
		UploadGrant grant;
		try
		{
			grant = new UploadGrant(uploadFieldId, context,
				parameters.getParameterValue(AbstractFileUploadResource.MAX_SIZE).toLong(),
				parameters.getParameterValue(AbstractFileUploadResource.FILE_MAX_SIZE).toLong(-1),
				parameters.getParameterValue(AbstractFileUploadResource.FILE_COUNT_MAX).toLong(-1),
				parameters.getParameterValue(EXPIRES).toLong());
		}
		catch (StringValueConversionException e)
		{
			return null;
		}
		return signer.verify(grant.serialized(owner), token) ? grant : null;
	}
}
