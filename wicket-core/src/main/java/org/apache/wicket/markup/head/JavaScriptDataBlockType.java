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
package org.apache.wicket.markup.head;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wicket.util.lang.Args;

/**
 * A <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/script/type#value">data block</a>
 * type (that is, any other value than a browser-processed value) for {@link JavaScriptContentHeaderItem}.
 */
public class JavaScriptDataBlockType implements JavaScriptContentType
{
    private static final Pattern TYPE_PREFIX_PATTERN = Pattern.compile("^[-!#$%&'*+.0-9A-Z^_`a-z{|}~]+/");
    private static final Set<String> JAVASCRIPT_MEDIA_TYPES = Set.of(
            "text/javascript",
            "application/javascript",
            "application/ecmascript",
            "application/x-ecmascript",
            "application/x-javascript",
            "text/ecmascript",
            "text/javascript1.0",
            "text/javascript1.1",
            "text/javascript1.2",
            "text/javascript1.3",
            "text/javascript1.4",
            "text/javascript1.5",
            "text/jscript",
            "text/livescript",
            "text/x-ecmascript",
            "text/x-javascript"
    );

    private final String type;

    /**
     * Create a new data block type with the given media type.
     * <p>
     * A rudimentary check is done to ensure <code>type</code> is a media type. It must start with a
     * <a href="https://www.rfc-editor.org/info/rfc9110/#media.type">token followed by a slash</a>. No check is done for
     * what follows the slash.
     * <p>
     * The type may also not be one of the JavaScript media types defined in
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types#textjavascript"><i>text/javacript</i>
     * on MDN</a>.
     *
     * @param type the media type of the data block.
     */
    public JavaScriptDataBlockType(String type)
    {
        Args.notNull(type, "type");
        Args.isTrue(TYPE_PREFIX_PATTERN.matcher(type).find(),
                "'type' must be a media type (that is: start with a type followed by a slash).");
        Args.isFalse(JAVASCRIPT_MEDIA_TYPES.contains(type.toLowerCase(Locale.ENGLISH)),
                "'type' may not be a JavaScript media type.");

        this.type = type;
    }

    @Override
    public String getType()
    {
        return type;
    }
}
