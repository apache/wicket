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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.crypt.ICrypt;

/**
 * Signs the grants of the resumable uploads by encrypting them with the
 * {@link org.apache.wicket.settings.SecuritySettings#getCryptFactory() crypt} of the application:
 * the browser sends the text back, and the encrypted text is taken if it decrypts to that. The
 * key is the crypt factory's, by default one in the HTTP session.
 * <p>
 * This is only as strong as the crypt: it has to be authenticated, so that a changed text does
 * not decrypt to anything, which the crypt of Wicket is. It is the signer of
 * {@link ResumableUploadResourceReference} by default.
 *
 * @since 11.1.0
 */
public class CryptFactoryGrantSigner implements IUploadGrantSigner
{
	private static final long serialVersionUID = 1L;

	@Override
	public String sign(String text)
	{
		return crypt().encryptUrlSafe(text);
	}

	@Override
	public boolean verify(String text, String signature)
	{
		if (text == null || signature == null)
		{
			return false;
		}
		String decrypted;
		try
		{
			decrypted = crypt().decryptUrlSafe(signature);
		}
		catch (RuntimeException e)
		{
			return false;
		}
		return decrypted != null && MessageDigest.isEqual(
			text.getBytes(StandardCharsets.UTF_8), decrypted.getBytes(StandardCharsets.UTF_8));
	}

	private static ICrypt crypt()
	{
		return Application.get().getSecuritySettings().getCryptFactory().newCrypt();
	}
}
