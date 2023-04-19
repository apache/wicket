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
 * Signals that a file size exceeds the configured maximum.
 */
public class FileUploadByteCountLimitException extends FileUploadSizeException {

    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = 2;

    /**
     * File name of the item, which caused the exception.
     */
    private final String fileName;

    /**
     * Field name of the item, which caused the exception.
     */
    private final String fieldName;

    /**
     * Constructs an instance with the specified detail message, and actual and permitted sizes.
     *
     * @param message   The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param actual    The actual request size.
     * @param permitted The maximum permitted request size.
     * @param fileName  File name of the item, which caused the exception.
     * @param fieldName Field name of the item, which caused the exception.
     */
    public FileUploadByteCountLimitException(final String message, final long actual, final long permitted, final String fileName, final String fieldName) {
        super(message, permitted, actual);
        this.fileName = fieldName;
        this.fieldName = fieldName;
    }

    /**
     * Gets the field name of the item, which caused the exception.
     *
     * @return Field name, if known, or null.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the file name of the item, which caused the exception.
     *
     * @return File name, if known, or null.
     */
    public String getFileName() {
        return fileName;
    }

}
