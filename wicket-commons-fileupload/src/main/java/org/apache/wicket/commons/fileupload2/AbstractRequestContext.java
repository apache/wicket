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

import java.util.function.Function;
import java.util.function.LongSupplier;

public abstract class AbstractRequestContext implements RequestContext {

    /**
     * Supplies the content length default.
     */
    private final LongSupplier contentLengthDefault;

    /**
     * Supplies the content length string.
     */
    private final Function<String, String> contentLengthString;

    /**
     * Constructs a new instance.
     *
     * @param contentLengthString  How to get the content length string.
     * @param contentLengthDefault How to get the content length default.
     */
    protected AbstractRequestContext(final Function<String, String> contentLengthString, final LongSupplier contentLengthDefault) {
        super();
        this.contentLengthString = contentLengthString;
        this.contentLengthDefault = contentLengthDefault;
    }

    /**
     * Gets the content length of the request.
     *
     * @return The content length of the request.
     * @since 1.3
     */
    @Override
    public long getContentLength() {
        try {
            return Long.parseLong(contentLengthString.apply(AbstractFileUpload.CONTENT_LENGTH));
        } catch (final NumberFormatException e) {
            return contentLengthDefault.getAsLong();
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return String.format("%s [ContentLength=%s, ContentType=%s]", getClass().getSimpleName(), getContentLength(), getContentType());
    }

}
