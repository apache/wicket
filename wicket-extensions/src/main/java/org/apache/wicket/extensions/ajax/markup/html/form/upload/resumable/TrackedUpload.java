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

import java.time.Instant;

import org.apache.wicket.util.io.IClusterable;

/**
 * What an {@link IUploadsTracker} knows of an upload: the state of a {@link ResumableUpload} at an
 * event, which an application may keep, in its database for example, beyond the upload itself.
 * <p>
 * The name, the type, the date of the file and the place are reported by the browser and must not
 * be trusted.
 *
 * @param id
 *            the random name of the upload, the last segment of its URL
 * @param owner
 *            who the upload belongs to, see
 *            {@link ResumableUploadResourceReference#getCurrentOwner()}
 * @param context
 *            the part of the application the upload belongs to, see
 *            {@link ResumableUploadToResourceField#setUploadContext(String)}, {@code null} for none;
 *            signed with the grant of the field, unlike the place
 * @param uploadFieldId
 *            the id of the upload field the upload was created for
 * @param place
 *            the place of the field that created it, see
 *            {@link ResumableUploadToResourceField#getRememberedAs()}, {@code null} when the
 *            client did not tell
 * @param clientFileName
 *            the name of the file, see {@link ResumableUpload#getClientFileName()}
 * @param contentType
 *            the content type of the file, see {@link ResumableUpload#getContentType()}
 * @param lastModified
 *            when the file was last modified, in milliseconds since the epoch, 0 when the client
 *            did not tell
 * @param length
 *            the length of the file, in bytes
 * @param offset
 *            the number of bytes the server holds
 * @param created
 *            when the upload was created
 * @param touched
 *            when the browser last sent bytes, or created the upload
 * @param storedFieldId
 *            the id of the upload field the whole file was stored for, {@code null} before
 * @param storedName
 *            the name the file manager stored the whole file under, {@code null} before
 * @since 11.1.0
 */
public record TrackedUpload(String id, String owner, String context, String uploadFieldId,
	String place,
	String clientFileName, String contentType, long lastModified, long length, long offset,
	Instant created, Instant touched, String storedFieldId, String storedName)
	implements IClusterable
{
	/** The tus metadata the field sends the place of its upload in. */
	static final String PLACE = "place";

	/** The tus metadata the field sends the date of the file in. */
	static final String LAST_MODIFIED = "lastmodified";

	/** The longest place kept. */
	static final int LONGEST_PLACE = 1024;

	/**
	 * @return whether the server holds all the bytes of the file
	 */
	public boolean isComplete()
	{
		return offset >= length;
	}

	/**
	 * @return whether the whole file went to the file manager, under {@link #storedName()}
	 */
	public boolean isStored()
	{
		return storedName != null;
	}

	/**
	 * @return the state of an upload now
	 */
	static TrackedUpload of(ResumableUpload upload)
	{
		String place = upload.getMetadata().get(PLACE);
		if (place != null && (place.isBlank() || place.length() > LONGEST_PLACE))
		{
			place = null;
		}
		return new TrackedUpload(upload.getId(), upload.getOwner(), upload.getContext(),
			upload.getUploadFieldId(), place,
			upload.getClientFileName(), upload.getContentType(),
			lastModifiedOf(upload.getMetadata().get(LAST_MODIFIED)), upload.getLength(),
			upload.getOffset(), upload.getCreated(), upload.getTouched(), upload.getStoredFieldId(),
			upload.getStoredName());
	}

	private static long lastModifiedOf(String value)
	{
		long number = ResumableUploadResource.number(value);
		return Math.max(number, 0);
	}
}
