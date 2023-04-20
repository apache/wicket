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

/**
 * Signals that a request contains more files than the specified limit.
 */
public class FileUploadFileCountLimitException extends FileUploadSizeException {

    private static final long serialVersionUID = 2;

    /**
     * Constructs an instance.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param limit   The limit that was exceeded.
     * @param actual  The actual value.
     */
    public FileUploadFileCountLimitException(final String message, final long limit, final long actual) {
        super(message, limit, actual);
    }

}
