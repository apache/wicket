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
package org.apache.wicket.core.util.crypt;

/**
 * A factory producing {@link ICrypt} instances. The factory owns the <em>key source</em> (e.g. a
 * per-session key or a global application key); the encryption scheme and decryption whitelist
 * come from the application's
 * {@link org.apache.wicket.settings.SecuritySettings crypto settings}.
 */
public interface ICryptFactory
{
	/**
	 * @return a (possibly cached) {@link ICrypt} bound to this factory's key
	 */
	ICrypt newCrypt();
}
