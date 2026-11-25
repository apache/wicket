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
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableSupplier;

/**
 * Signs the grants of the resumable uploads with HMAC-SHA256, the signature in URL-safe Base64,
 * and checks them in constant time.
 * <p>
 * The key decides how long a grant holds. One the application keeps, from a file, a property or
 * a vault for example, lets a page go on with the uploads of its owner, see
 * {@link ResumableUploadResourceReference#getCurrentOwner()}, after the session that rendered it
 * ended, and after a restart of the application or on another node of a cluster too:
 *
 * <pre>
 * new ResumableUploadResourceReference(fileManager)
 * 	.setGrantSigner(new HmacGrantSigner(() -&gt; key));
 * </pre>
 *
 * {@link #random()} makes a key of its own, which the application keeps in memory: it holds beyond
 * the sessions, but not beyond a restart. {@link #inSession()} keeps a key in each HTTP session,
 * which ends with it.
 *
 * @since 10.13.0
 */
public class HmacGrantSigner implements IUploadGrantSigner
{
	private static final long serialVersionUID = 1L;

	/** The length of a key at least, in bytes: that of the hash of HMAC-SHA256. */
	public static final int MIN_KEY_LENGTH = 32;

	private static final String ALGORITHM = "HmacSHA256";

	private final IKeys keys;

	/**
	 * @param key
	 *            gives the key, of {@link #MIN_KEY_LENGTH} bytes at least, whenever a grant is
	 *            signed or checked
	 */
	public HmacGrantSigner(SerializableSupplier<byte[]> key)
	{
		this(new SuppliedKeys(Args.notNull(key, "key")));
	}

	private HmacGrantSigner(IKeys keys)
	{
		this.keys = keys;
	}

	/**
	 * @return a signer with a random key of its own, which the application keeps in memory: its
	 *         grants hold beyond the HTTP sessions, but not beyond a restart of the application,
	 *         nor on another node of a cluster
	 */
	public static HmacGrantSigner random()
	{
		byte[] key = newKey();
		return new HmacGrantSigner(() -> key.clone());
	}

	/**
	 * @return a signer whose key is a random one of the HTTP session, made once the session signs
	 *         a grant: its grants end with the session
	 */
	public static HmacGrantSigner inSession()
	{
		return new HmacGrantSigner(new SessionKeys());
	}

	@Override
	public String sign(String text)
	{
		Args.notNull(text, "text");
		return Base64.getUrlEncoder().withoutPadding().encodeToString(mac(keys.toSign(), text));
	}

	@Override
	public boolean verify(String text, String signature)
	{
		if (text == null || signature == null)
		{
			return false;
		}
		byte[] key = keys.toVerify();
		if (key == null)
		{
			return false;
		}
		byte[] given;
		try
		{
			given = Base64.getUrlDecoder().decode(signature);
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}
		return MessageDigest.isEqual(mac(key, text), given);
	}

	private static byte[] mac(byte[] key, String text)
	{
		if (key == null || key.length < MIN_KEY_LENGTH)
		{
			throw new IllegalStateException("The key of an " + HmacGrantSigner.class.getSimpleName() +
				" has to have " + MIN_KEY_LENGTH + " bytes at least");
		}
		try
		{
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(key, ALGORITHM));
			return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
		}
		catch (GeneralSecurityException e)
		{
			throw new WicketRuntimeException("Unable to sign the grant of an upload", e);
		}
	}

	private static byte[] newKey()
	{
		byte[] key = new byte[MIN_KEY_LENGTH];
		new SecureRandom().nextBytes(key);
		return key;
	}

	/**
	 * Where the keys come from.
	 */
	private interface IKeys extends IClusterable
	{
		/**
		 * @return the key to sign with
		 */
		byte[] toSign();

		/**
		 * @return the key to check with, {@code null} for none
		 */
		byte[] toVerify();
	}

	/**
	 * The key a supplier gives.
	 */
	private record SuppliedKeys(SerializableSupplier<byte[]> key) implements IKeys
	{
		@Override
		public byte[] toSign()
		{
			return key.get();
		}

		@Override
		public byte[] toVerify()
		{
			return key.get();
		}
	}

	/**
	 * The key of the current session, made when it first signs: a session that never rendered a
	 * grant has none to check one with.
	 */
	private static final class SessionKeys implements IKeys
	{
		private static final long serialVersionUID = 1L;

		private static final MetaDataKey<byte[]> KEY = new MetaDataKey<>()
		{
			private static final long serialVersionUID = 1L;
		};

		@Override
		public byte[] toSign()
		{
			Session session = Session.get();
			// two pages rendered at once make one key, else the second would refuse the first
			synchronized (session)
			{
				byte[] key = session.getMetaData(KEY);
				if (key == null)
				{
					key = newKey();
					session.setMetaData(KEY, key);
				}
				return key;
			}
		}

		@Override
		public byte[] toVerify()
		{
			return Session.get().getMetaData(KEY);
		}
	}
}
