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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemHeaders;

/**
 * The file of a resumable upload all of whose bytes arrived, as the
 * {@link org.apache.wicket.markup.html.form.upload.FileUpload} an
 * {@link org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager} stores.
 */
final class ReceivedFileItem implements FileItem<ReceivedFileItem>
{
	/** The name of the form field the file is reported under. */
	static final String FIELD_NAME = "WICKET-FILE-UPLOAD";

	private final ResumableUpload upload;

	private FileItemHeaders headers;

	ReceivedFileItem(ResumableUpload upload)
	{
		this.upload = upload;
	}

	@Override
	public ReceivedFileItem delete() throws IOException
	{
		Files.deleteIfExists(upload.getFile());
		return this;
	}

	@Override
	public byte[] get() throws IOException
	{
		return Files.readAllBytes(upload.getFile());
	}

	@Override
	public String getContentType()
	{
		return upload.getContentType();
	}

	@Override
	public String getFieldName()
	{
		return FIELD_NAME;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return Files.newInputStream(upload.getFile());
	}

	@Override
	public String getName()
	{
		return upload.getClientFileName();
	}

	@Override
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException("The file of an upload is read only");
	}

	@Override
	public long getSize()
	{
		return upload.getLength();
	}

	@Override
	public String getString() throws IOException
	{
		return getString(StandardCharsets.UTF_8);
	}

	@Override
	public String getString(Charset toCharset) throws IOException
	{
		return new String(get(), toCharset);
	}

	@Override
	public boolean isFormField()
	{
		return false;
	}

	@Override
	public boolean isInMemory()
	{
		return false;
	}

	@Override
	public ReceivedFileItem setFieldName(String name)
	{
		throw new UnsupportedOperationException("The field name of an upload is fixed");
	}

	@Override
	public ReceivedFileItem setFormField(boolean state)
	{
		throw new UnsupportedOperationException("An upload is a file");
	}

	/**
	 * Moves the file rather than copying it: the upload has no use for it afterwards.
	 */
	@Override
	public ReceivedFileItem write(Path file) throws IOException
	{
		Files.move(upload.getFile(), file, StandardCopyOption.REPLACE_EXISTING);
		return this;
	}

	@Override
	public FileItemHeaders getHeaders()
	{
		return headers;
	}

	@Override
	public ReceivedFileItem setHeaders(FileItemHeaders headers)
	{
		this.headers = headers;
		return this;
	}
}
