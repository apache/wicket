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
 * The id of an upload field, the context of its uploads and the size of each of its files at most,
 * which the field signs and its script sends along with every request, the only limit of the field
 * the server holds each request to: every file is an upload of its own, so the size of the files
 * chosen at once and their number are checked in the browser only. The signature also covers when
 * the grant expires, which is sent along, and who it was made for,
 * {@link ResumableUploadResourceReference#getCurrentOwner()}, which is not: the server puts in the
 * owner of each request when it checks the signature, so the grant of one owner never works for
 * another, and no id of a session ends up in the page.
 *
 * @param uploadFieldId
 *            the unique id of the field
 * @param context
 *            the part of the application the uploads belong to, see
 *            {@link ResumableUploadToResourceField#setUploadContext(String)}, {@code null} for none
 * @param fileMaxSize
 *            the size of a file at most, in bytes
 * @param expires
 *            when the grant expires, in milliseconds since the epoch, {@link #NEVER} for never
 */
record UploadGrant(String uploadFieldId, String context, long fileMaxSize, long expires)
{
	/** The query parameter of when the grant expires. */
	static final String EXPIRES = "grantExpires";

	/** The query parameter of the context of the uploads. */
	static final String CONTEXT = "uploadContext";

	/** When a grant that does not expire expires. */
	static final long NEVER = -1;

	/** Keeps the tokens of these fields apart from those of other uploads. */
	private static final String PURPOSE = "resumable-upload";

	/** The largest number a script holds exactly, {@code Number.MAX_SAFE_INTEGER}. */
	static final long MAX_SAFE_INTEGER = (1L << 53) - 1;

	/**
	 * @return the grant of a field, its size no larger than a script holds exactly: it sends it
	 *         back as it got it
	 */
	static UploadGrant of(String uploadFieldId, String context, Bytes fileMaxSize, Instant expires)
	{
		return new UploadGrant(uploadFieldId, context,
			Math.min(fileMaxSize.bytes(), MAX_SAFE_INTEGER),
			expires != null ? expires.toEpochMilli() : NEVER);
	}

	/**
	 * @param now
	 *            the current time
	 * @return whether the grant has expired
	 */
	boolean isExpired(Instant now)
	{
		return expires != NEVER && now.toEpochMilli() >= expires;
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
	 * @return the query of the grant, as the script of the field writes it
	 */
	String query(IUploadGrantSigner signer, String owner)
	{
		return AbstractFileUploadResource.UPLOAD_ID + '=' + encoded(uploadFieldId) + '&' +
			AbstractFileUploadResource.FILE_MAX_SIZE + '=' + fileMaxSize + '&' +
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
			fileMaxSize + '\n' + expires + '\n' + (owner != null ? owner : "");
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
				parameters.getParameterValue(AbstractFileUploadResource.FILE_MAX_SIZE).toLong(),
				parameters.getParameterValue(EXPIRES).toLong());
		}
		catch (StringValueConversionException e)
		{
			return null;
		}
		return signer.verify(grant.serialized(owner), token) ? grant : null;
	}
}
