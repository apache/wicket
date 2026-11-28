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
package org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable;

import org.apache.wicket.util.io.IClusterable;

/**
 * Signs the grants of the resumable uploads, the id of an upload field and the limits of its
 * uploads that the browser sends back with every request, and checks what the browser sends.
 * <p>
 * Where its key lives decides how long a page can go on with its uploads: a key in the HTTP session
 * ends with the session, so an upload that outlives it, see
 * {@link ResumableUploadResourceReference#getCurrentOwner()}, is continued from a page rendered
 * afterwards; a key of the application, see {@link HmacGrantSigner}, lets a page rendered in an
 * earlier session go on with the uploads of the same owner.
 *
 * @see ResumableUploadResourceReference#setGrantSigner(IUploadGrantSigner)
 * @since 12.0.0
 */
public interface IUploadGrantSigner extends IClusterable
{
	/**
	 * Signs a text.
	 *
	 * @param text
	 *            the text
	 * @return the signature, safe in a URL
	 */
	String sign(String text);

	/**
	 * Checks the signature of a text. The text and the signature come from the browser, and so
	 * can be anything.
	 *
	 * @param text
	 *            the text
	 * @param signature
	 *            the signature the browser sent
	 * @return whether the signature is that of the text, made with this signer's key
	 */
	boolean verify(String text, String signature);

	/**
	 * Tells whether the key ends with the HTTP session: then a grant holds as long as the session
	 * that rendered it, and does not expire by default, see
	 * {@link ResumableUploadResourceReference#getGrantExpiry()}.
	 *
	 * @return whether the key ends with the session, {@code false} by default
	 */
	default boolean isBoundToSession()
	{
		return false;
	}
}
