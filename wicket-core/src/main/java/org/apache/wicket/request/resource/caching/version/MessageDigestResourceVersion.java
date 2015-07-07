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
package org.apache.wicket.request.resource.caching.version;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * computes the message digest of a {@link org.apache.wicket.request.resource.caching.IStaticCacheableResource} 
 * and uses it as a version string
 * <p/>
 * you can use any message digest algorithm that can be retrieved 
 * by Java Cryptography Architecture (JCA) on your current platform.
 * Check <a href="http://download.oracle.com/javase/1.5.0/docs/guide/security/CryptoSpec.html#AppA">here</a>
 * for more information on possible algorithms.
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class MessageDigestResourceVersion implements IResourceVersion
{
	private static final Logger log = LoggerFactory.getLogger(MessageDigestResourceVersion.class);

	private static final String DEFAULT_ALGORITHM = "MD5";
	private static final int DEFAULT_BUFFER_BYTES = 8192; // needed for javadoc {@value ..}
	private static final Bytes DEFAULT_BUFFER_SIZE = Bytes.bytes(DEFAULT_BUFFER_BYTES);

	/**
	 * A valid pattern is a sequence of digits and upper cased English letters A-F
	 */
	private static final Pattern DIGEST_PATTERN = Pattern.compile("[0-9A-F]+");

	/** 
	 * message digest algorithm for computing hashes 
	 */
	private final String algorithm;

	/** 
	 * buffer size for computing the digest 
	 */
	private final Bytes bufferSize;

	/**
	 * create an instance of the message digest 
	 * resource version provider using algorithm {@value #DEFAULT_ALGORITHM}
	 * 
	 * @see #MessageDigestResourceVersion(String) 
	 * @see #MessageDigestResourceVersion(String, org.apache.wicket.util.lang.Bytes)
	 */
	public MessageDigestResourceVersion()
	{
		this(DEFAULT_ALGORITHM, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * create an instance of the message digest resource version provider 
	 * using the specified algorithm. The algorithm name must be one
	 * that can be retrieved by Java Cryptography Architecture (JCA) 
	 * using {@link MessageDigest#getInstance(String)}. For digest computation
	 * an internal buffer of up to {@value #DEFAULT_BUFFER_BYTES}
	 * bytes will be used.
	 *
	 * @param algorithm
	 *            digest algorithm
	 *
	 * @see #MessageDigestResourceVersion()
	 * @see #MessageDigestResourceVersion(String, org.apache.wicket.util.lang.Bytes)
	 */
	public MessageDigestResourceVersion(String algorithm)
	{
		this(algorithm, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * create an instance of the message digest resource version provider 
	 * using the specified algorithm. The algorithm name must be one
	 * that can be retrieved by Java Cryptography Architecture (JCA) 
	 * using {@link MessageDigest#getInstance(String)}. For digest computation
	 * an internal buffer with a maximum size specified by parameter 
	 * <code>bufferSize</code> will be used. 
	 *
	 * @param algorithm
	 *            digest algorithm
	 * @param bufferSize
	 *            maximum size for internal buffer            
	 */
	public MessageDigestResourceVersion(String algorithm, Bytes bufferSize)
	{
		this.algorithm = Args.notEmpty(algorithm, "algorithm");
		this.bufferSize = Args.notNull(bufferSize, "bufferSize");
	}

	@Override
	public String getVersion(IStaticCacheableResource resource)
	{
		IResourceStream stream = resource.getResourceStream();

		// if resource stream can not be found do not cache
		if (stream == null)
		{
			return null;
		}

		try
		{
			final InputStream inputStream = stream.getInputStream();

			try
			{
				// get binary hash
				final byte[] hash = computeDigest(inputStream);

				// convert to hexadecimal
				return Strings.toHexString(hash);
			}
			finally
			{
				IOUtils.close(stream);
			}
		}
		catch (IOException e)
		{
			log.warn("unable to compute hash for " + resource, e);
			return null;
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.warn("unable to locate resource for " + resource, e);
			return null;
		}
	}

	@Override
	public Pattern getVersionPattern()
	{
		return DIGEST_PATTERN;
	}

	/**
	 * get instance of message digest provider from JCA
	 * 
	 * @return message digest provider
	 */
	protected MessageDigest getMessageDigest()
	{
		try
		{
			return MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new WicketRuntimeException("message digest " + algorithm + " not found", e);
		}
	}

	/**
	 * compute digest for resource stream
	 * 
	 * @param inputStream
	 *            input stream to compute message digest for
	 * 
	 * @return binary message digest
	 * 
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	protected byte[] computeDigest(InputStream inputStream) throws IOException
	{
		final MessageDigest digest = getMessageDigest();

		// get actual buffer size
		final int bufferLen = (int)Math.min(Integer.MAX_VALUE, bufferSize.bytes());

		// allocate read buffer
		final byte[] buf = new byte[bufferLen];
		int len;

		// read stream and update message digest
		while ((len = inputStream.read(buf)) != -1)
		{
			digest.update(buf, 0, len);
		}
		// finish message digest and return hash
		return digest.digest();
	}
}
