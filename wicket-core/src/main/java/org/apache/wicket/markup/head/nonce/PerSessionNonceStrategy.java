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

import org.apache.wicket.Session;
import org.apache.wicket.util.lang.Args;

/**
 * Per session Nonce generation strategy.
 * Generates a new nonce for each new session.
 */
public class PerSessionNonceStrategy extends AbstractNonceStrategy {

    @Override
    public String getNonce() {
        Session session = Session.get();
        String nonce = session.getMetaData(NONCE_KEY);
        if (nonce == null) {
            nonce = generateNonce();
            saveNonce(session, nonce);
        }
        return nonce;
    }

    @Override
    public void invalidate() {
        saveNonce(Session.get(), generateNonce());
    }

    private void saveNonce(Session session, String nonce) {
        Args.notNull(session, "session");
        Args.notNull(nonce, "csp");
        session.setMetaData(NONCE_KEY, nonce);
    }

}