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
package org.apache.wicket.commons.fileupload2.impl;


import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.commons.fileupload2.FileItemHeaders;
import org.apache.wicket.commons.fileupload2.FileItemStream;
import org.apache.wicket.commons.fileupload2.FileUploadException;
import org.apache.wicket.commons.fileupload2.InvalidFileNameException;
import org.apache.wicket.commons.fileupload2.MultipartStream.ItemInputStream;
import org.apache.wicket.commons.fileupload2.disk.DiskFileItem;
import org.apache.wicket.commons.fileupload2.pub.FileUploadByteCountLimitException;
import org.apache.wicket.commons.fileupload2.util.LimitedInputStream;

/**
 * Default implementation of {@link FileItemStream}.
 */
public class FileItemStreamImpl implements FileItemStream {

    /**
     * The File Item iterator implementation.
     *
     * @see FileItemIteratorImpl
     */
    private final FileItemIteratorImpl fileItemIteratorImpl;

    /**
     * The file items content type.
     */
    private final String contentType;

    /**
     * The file items field name.
     */
    private final String fieldName;

    /**
     * The file items file name.
     */
    private final String fileName;

    /**
     * Whether the file item is a form field.
     */
    private final boolean formField;

    /**
     * The file items input stream.
     */
    private final InputStream inputStream;

    /**
     * The file items input stream closed flag.
     */
    private boolean inputStreamClosed;

    /**
     * The headers, if any.
     */
    private FileItemHeaders headers;

    /**
     * Creates a new instance.
     *
     * @param fileItemIterator The {@link FileItemIteratorImpl iterator}, which returned this file item.
     * @param fileName             The items file name, or null.
     * @param fieldName        The items field name.
     * @param contentType      The items content type, or null.
     * @param formField        Whether the item is a form field.
     * @param contentLength    The items content length, if known, or -1
     * @throws IOException         Creating the file item failed.
     * @throws FileUploadException Parsing the incoming data stream failed.
     */
    public FileItemStreamImpl(final FileItemIteratorImpl fileItemIterator, final String fileName, final String fieldName, final String contentType,
            final boolean formField, final long contentLength) throws FileUploadException, IOException {
        this.fileItemIteratorImpl = fileItemIterator;
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.formField = formField;
        final long fileSizeMax = fileItemIteratorImpl.getFileSizeMax();
        if (fileSizeMax != -1 && contentLength != -1 && contentLength > fileSizeMax) {
            throw new FileUploadByteCountLimitException(String.format("The field %s exceeds its maximum permitted size of %s bytes.", fieldName, fileSizeMax),
                    contentLength, fileSizeMax, fileName, fieldName);
        }
        // OK to construct stream now
        final ItemInputStream itemInputStream = fileItemIteratorImpl.getMultiPartStream().newInputStream();
        InputStream istream = itemInputStream;
        if (fileSizeMax != -1) {
            istream = new LimitedInputStream(istream, fileSizeMax) {
                @Override
                protected void raiseError(final long sizeMax, final long count) throws IOException {
                    itemInputStream.close(true);
                    throw new FileUploadByteCountLimitException(
                            String.format("The field %s exceeds its maximum permitted size of %s bytes.", fieldName, sizeMax), count, sizeMax, fileName,
                            fieldName);
                }
            };
        }
        this.inputStream = istream;
    }

    /**
     * Closes the file item.
     *
     * @throws IOException An I/O error occurred.
     */
    public void close() throws IOException {
        inputStream.close();
        inputStreamClosed = true;
    }

    /**
     * Gets the items content type, or null.
     *
     * @return Content type, if known, or null.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the items field name.
     *
     * @return Field name.
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the file item headers.
     *
     * @return The items header object
     */
    @Override
    public FileItemHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets the items file name.
     *
     * @return File name, if known, or null.
     * @throws InvalidFileNameException The file name contains a NUL character, which might be an indicator of a security attack. If you intend to use the file
     *                                  name anyways, catch the exception and use InvalidFileNameException#getName().
     */
    @Override
    public String getName() {
        return DiskFileItem.checkFileName(fileName);
    }

    /**
     * Tests whether this is a form field.
     *
     * @return True, if the item is a form field, otherwise false.
     */
    @Override
    public boolean isFormField() {
        return formField;
    }

    /**
     * Gets the input stream, which may be used to read the items contents.
     *
     * @return Opened input stream.
     * @throws IOException An I/O error occurred.
     */
    @Override
    public InputStream openStream() throws IOException {
        if (inputStreamClosed) {
            throw new FileItemStream.ItemSkippedException();
        }
        return inputStream;
    }

    /**
     * Sets the file item headers.
     *
     * @param headers The items header object
     */
    @Override
    public void setHeaders(final FileItemHeaders headers) {
        this.headers = headers;
    }

}
