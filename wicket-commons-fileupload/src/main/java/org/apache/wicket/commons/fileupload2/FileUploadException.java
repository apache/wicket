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
package org.apache.wicket.commons.fileupload2;

import java.io.IOException;

/**
 * Signals errors encountered while processing the request.
 */
public class FileUploadException extends IOException {

    /**
     * Serial version UID, being used, if the exception is serialized.
     */
    private static final long serialVersionUID = 2;

    /**
     * Constructs a new instance.
     */
    public FileUploadException() {
    }

    /**
     * Constructs an instance with a given detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public FileUploadException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance with the given detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is permitted, and indicates that the cause
     *                is nonexistent or unknown.)
     */
    public FileUploadException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
