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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.head.INonceStrategy;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * {@inheritDoc}
 */
public abstract class AbstractNonceStrategy implements INonceStrategy {

    private static final int NONCE_LENGTH = 24;

    MetaDataKey<String> NONCE_KEY = new MetaDataKey<String>() {};

    private static final SecureRandom RND = new SecureRandom();

    private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder();

    protected String generateNonce() {
        byte[] randomBytes = new byte[NONCE_LENGTH];
        RND.nextBytes(randomBytes);
        return BASE_64_ENCODER.encodeToString(randomBytes);
    }

}
