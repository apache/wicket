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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A file a browser sends to a {@link ResumableUploadResourceReference} in chunks: its name and type
 * as the browser reported them, its length, and how many of its bytes the server holds. Once the
 * server holds all of them, the file goes to the
 * {@link org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager} of the reference
 * and the upload only remembers the name it was stored under.
 * <p>
 * The name, the type and the other metadata are reported by the browser and must not be trusted.
 *
 * @since 10.13.0
 */
public final class ResumableUpload
{
	/** The longest client file name kept. */
	static final int LONGEST_NAME = 255;

	private final String id;

	private final String owner;

	private final String context;

	private final boolean endingWithSession;

	private final String uploadFieldId;

	private final long length;

	private final Map<String, String> metadata;

	private final Path file;

	private final Instant created;

	/** Held by the request writing the file, or deleting it. */
	final ReentrantLock lock = new ReentrantLock();

	private volatile long offset;

	private volatile Instant touched;

	private volatile String storedName;

	private volatile String storedFieldId;

	/** Set once the upload is to be deleted as soon as no request writes it. */
	private volatile boolean ending;

	/** Set once the reference forgot the upload. */
	private volatile boolean forgotten;

	ResumableUpload(String id, String owner, String context, boolean endingWithSession,
		String uploadFieldId, long length, Map<String, String> metadata, Path file, Instant created)
	{
		this.id = id;
		this.owner = owner;
		this.context = context;
		this.endingWithSession = endingWithSession;
		this.uploadFieldId = uploadFieldId;
		this.length = length;
		this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
		this.file = file;
		this.created = created;
		touched = created;
	}

	/**
	 * @return the random name of the upload, the last segment of its URL
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return who the upload belongs to, see
	 *         {@link ResumableUploadResourceReference#getCurrentOwner()}
	 */
	String getOwner()
	{
		return owner;
	}

	/**
	 * @return the part of the application the upload belongs to, as the grant of its field tells,
	 *         {@code null} for none
	 */
	String getContext()
	{
		return context;
	}

	/**
	 * @return whether the upload belongs to the HTTP session that created it, and ends with it
	 */
	boolean isEndingWithSession()
	{
		return endingWithSession;
	}

	/**
	 * @return the unique id of the upload field the upload was created for
	 */
	public String getUploadFieldId()
	{
		return uploadFieldId;
	}

	/**
	 * @return the length of the file, in bytes
	 */
	public long getLength()
	{
		return length;
	}

	/**
	 * @return the number of bytes the server holds
	 */
	public long getOffset()
	{
		return offset;
	}

	void setOffset(long offset, Instant now)
	{
		this.offset = offset;
		touched = now;
	}

	/**
	 * @return whether the server holds all the bytes of the file
	 */
	public boolean isComplete()
	{
		return offset >= length;
	}

	/**
	 * @return whether the file went to the file manager
	 */
	boolean isStored()
	{
		return storedName != null;
	}

	/**
	 * @return the name the file manager stored the file under, {@code null} before
	 */
	String getStoredName()
	{
		return storedName;
	}

	/**
	 * @return the id of the upload field the file was stored for, {@code null} before
	 */
	String getStoredFieldId()
	{
		return storedFieldId;
	}

	void stored(String fieldId, String name, Instant now)
	{
		storedFieldId = fieldId;
		storedName = name;
		touched = now;
	}

	/**
	 * @return whether the server holds no bytes of the file: none arrived yet, or the file went to
	 *         the file manager
	 */
	boolean holdsNoBytes()
	{
		return offset == 0 || isStored();
	}

	/**
	 * Marks the upload to be deleted as soon as no request writes it: its session ended, or the
	 * application stops.
	 */
	void end()
	{
		ending = true;
	}

	/**
	 * @return whether the upload is to be deleted as soon as no request writes it
	 */
	boolean isEnding()
	{
		return ending;
	}

	/**
	 * Marks the upload as forgotten by the reference: no request goes on with it.
	 */
	void forget()
	{
		forgotten = true;
	}

	/**
	 * @return whether the reference forgot the upload
	 */
	boolean isForgotten()
	{
		return forgotten;
	}

	/**
	 * @return the pairs of {@code Upload-Metadata}, as the browser reported them:
	 *         {@code filename} and {@code filetype} among others
	 */
	public Map<String, String> getMetadata()
	{
		return metadata;
	}

	/**
	 * @return the name of the file as the browser reported it, without any folder, at most
	 *         {@value #LONGEST_NAME} characters; {@code upload} without one
	 */
	public String getClientFileName()
	{
		return clientFileNameOf(metadata.get("filename"));
	}

	/**
	 * @return the content type of the file as the browser reported it,
	 *         {@code application/octet-stream} without one
	 */
	public String getContentType()
	{
		String type = metadata.get("filetype");
		return type == null || type.isBlank() || type.length() > 255
			? "application/octet-stream" : type;
	}

	/**
	 * @return the file holding the bytes received so far, until it goes to the file manager
	 */
	Path getFile()
	{
		return file;
	}

	/**
	 * @return when the upload was created
	 */
	public Instant getCreated()
	{
		return created;
	}

	/**
	 * @return when the browser last sent bytes, or created the upload
	 */
	public Instant getTouched()
	{
		return touched;
	}

	/**
	 * Deletes the file of the bytes received.
	 *
	 * @return whether there was a file to delete
	 */
	boolean deleteFile()
	{
		try
		{
			return Files.deleteIfExists(file);
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * @param name
	 *            the name of a file as a browser reported it
	 * @return the name without any folder or control characters, at most {@value #LONGEST_NAME}
	 *         characters; {@code upload} when nothing is left
	 */
	static String clientFileNameOf(String name)
	{
		if (name == null)
		{
			return "upload";
		}
		String last = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
		StringBuilder clean = new StringBuilder();
		last.codePoints()
			.filter(c -> !Character.isISOControl(c))
			.forEach(clean::appendCodePoint);
		String result = clean.toString().strip();
		if (result.isEmpty() || result.equals(".") || result.equals(".."))
		{
			return "upload";
		}
		return result.length() > LONGEST_NAME ? result.substring(0, LONGEST_NAME) : result;
	}

	@Override
	public String toString()
	{
		return "ResumableUpload " + id + " (" + offset + " of " + length + " bytes)";
	}
}
