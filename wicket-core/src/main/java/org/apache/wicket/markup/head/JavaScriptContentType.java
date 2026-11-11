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

import org.apache.wicket.util.io.IClusterable;

/**
 * To be used to define the "type" attribute of the script tag written
 * by a {@link JavaScriptHeaderItem}.
 * Also defines constants for <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/script/type#value">browser-processed</a>
 * types:
 * <UL>
 *     <LI>"text/javascript" ({@link JavaScriptContentType#TEXT_JAVASCRIPT})</LI>
 *     <LI>"importmap" ({@link JavaScriptContentType#IMPORT_MAP})</LI>
 *     <LI>"module" ({@link JavaScriptContentType#MODULE})</LI>
 *     <LI>"speculationrules" ({@link JavaScriptContentType#SPECULATION_RULES})</LI>
 * </UL>
 */
@FunctionalInterface
public interface JavaScriptContentType extends IClusterable
{
    String getType();

    /**
     * Defined content types as described by
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/script/type#value">browser-processed</a>
     */
	JavaScriptContentType TEXT_JAVASCRIPT = () -> "text/javascript";
    JavaScriptContentType IMPORT_MAP = () -> "importmap";
    JavaScriptContentType MODULE = () -> "module";
    JavaScriptContentType SPECULATION_RULES = () -> "speculationrules";
}
