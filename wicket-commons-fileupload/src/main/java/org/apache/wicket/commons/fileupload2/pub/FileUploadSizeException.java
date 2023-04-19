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
 * Signals that a requests permitted size is exceeded.
 */
public class FileUploadSizeException extends FileUploadException {

    /**
     * Serial version UID, being used, if serialized.
     */
    private static final long serialVersionUID = 2;

    /**
     * The actual size of the request.
     */
    private final long actual;

    /**
     * The maximum permitted size of the request.
     */
    private final long permitted;

    /**
     * Constructs an instance.
     *
     * @param message   The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param permitted The requests size limit.
     * @param actual    The actual values for the request.
     */
    public FileUploadSizeException(final String message, final long permitted, final long actual) {
        super(message);
        this.permitted = permitted;
        this.actual = actual;
    }

    /**
     * Gets the actual size of the request.
     *
     * @return The actual size of the request.
     */
    public long getActualSize() {
        return actual;
    }

    /**
     * Gets the limit size of the request.
     *
     * @return The limit size of the request.
     */
    public long getPermitted() {
        return permitted;
    }

}
