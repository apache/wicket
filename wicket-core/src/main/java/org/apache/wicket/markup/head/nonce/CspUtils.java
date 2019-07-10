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
package org.apache.wicket.markup.head.nonce;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.INonceStrategy;

import java.util.Optional;

/**
 * Provide some helpers for CSP security.
 * {@see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src#Unsafe_inline_script">https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src#Unsafe_inline_script</a>}
 */
public class CspUtils {

    /**
     * Get CSP nonce
     * @return Optional<String> Empty optional if strategy is not configured.
     */
    public static Optional<String> getNonce() {
        return getNonceStrategy().map(INonceStrategy::getNonce);
    }

    /**
     * Prepare CSP nonce html attribute string.
     * @return Optional<String> Empty optional if strategy is not configured.
     */
    public static Optional<String> getNonceAttribute() {
        return getNonce().map(s -> "nonce=\""+s+"\" ");
    }

    /**
     * Get configured nonce strategy.
     * @return Optional<String> Empty optional if not configured.
     */
    public static Optional<INonceStrategy> getNonceStrategy() {
        if (Application.exists()) {
            return Optional.ofNullable(Application.get().getSecuritySettings().getNonceStrategy());
        } else {
            return Optional.empty();
        }
    }

}
