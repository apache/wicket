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

/**
 * Nonce generation strategy. Implementation should define how and when the nonce is generated.
 * <p>
 * Strategy Should be configured in the Application.
 * The nonce strategy itself is only responsible for nonce generation.
 *  {@link org.apache.wicket.core.util.string.JavaScriptUtils} and {@link org.apache.wicket.core.util.string.CssUtils}
 *  are responsible for nonce attributes in SCRIPT, LINK and STYLE tags
 * <p>
 * It is up to developers how to append CSP Headers to the response.
 * <p>
 * {@see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src#Unsafe_inline_script">https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src#Unsafe_inline_script</a>}
 */
public interface INonceStrategy {

    String getNonce();

    void invalidate();

}
