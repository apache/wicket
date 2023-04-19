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
package org.apache.wicket.commons.fileupload2.pub;

import org.apache.wicket.commons.fileupload2.FileUploadException;

/**
 * Signals that a request is not a multipart request.
 */
public class FileUploadContentTypeException extends FileUploadException {

    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = 2;

    /**
     * The guilty content type.
     */
    private String contentType;

    /**
     * Constructs an instance with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param contentType The guilty content type.
     */
    public FileUploadContentTypeException(final String message, final String contentType) {
        super(message);
        this.contentType = contentType;
    }

    /**
     * Constructs an instance with the specified detail message and cause.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     * @param cause the original cause
     */
    public FileUploadContentTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public String getContentType() {
        return contentType;
    }
}
