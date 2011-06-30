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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * uses the message digest of a {@link PackageResource} as a version string
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public class MessageDigestResourceVersion implements IResourceVersion
{
	private static final Logger log = LoggerFactory.getLogger(MessageDigestResourceVersion.class);

	private static final String DEFAULT_ALGORITHM = "MD5";
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/** message digest algorithm for computing hashes */
	private final String algorithm;

	/** buffer size for computing the digest */
	private final int bufferSize;

	/**
	 * create an instance using {@value #DEFAULT_ALGORITHM}
	 */
	public MessageDigestResourceVersion()
	{
		this(DEFAULT_ALGORITHM, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * create an instance using an algorihm that can be 
	 * retrieved by Java Cryptography Architecture (JCA) 
	 * using {@link MessageDigest#getInstance(String)} and 
	 * using an fixed-size internal buffer for digest computation of
	 * {@value #DEFAULT_BUFFER_SIZE} bytes.
	 *
	 * @param algorithm
	 *           digest algorithm
	 */
	public MessageDigestResourceVersion(String algorithm)
	{
		this(algorithm, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * create an instance using an algorihm that can be 
	 * retrieved by Java Cryptography Architecture (JCA) 
	 * using {@link MessageDigest#getInstance(String)} and 
	 * using an specified internal buffer for digest computation.
	 *
	 * @param algorithm
	 *           digest algorithm
	 * @param bufferSize
	 *           internal buffer size for digest computation
	 */
	public MessageDigestResourceVersion(String algorithm, int bufferSize)
	{
		this.algorithm = Args.notEmpty(algorithm, "algorithm");
		this.bufferSize = bufferSize;
	}

	public String getVersion(PackageResourceReference resourceReference)
	{
		final PackageResourceReference.StreamInfo streamInfo = resourceReference.getCurrentStreamInfo();

		if (streamInfo == null)
		{
			log.debug("could not get stream info for " + resourceReference);
			return null;
		}

		try
		{
			// get binary hash
			final byte[] hash = computeDigest(streamInfo.stream);

			// convert to hexadecimal
			return Strings.toHexString(hash);
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.warn("resource stream not found for " + resourceReference);
			return null;
		}
		catch (IOException e)
		{
			log.warn("resource stream not be read for " + resourceReference, e);
			return null;
		}
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
	 * @param resourceStream
	 *           resource stream to compute message digest for
	 * 
	 * @return binary message digest
	 * 
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	protected byte[] computeDigest(IResourceStream resourceStream)
		throws ResourceStreamNotFoundException, IOException
	{
		final MessageDigest digest = getMessageDigest();
		final InputStream inputStream = resourceStream.getInputStream();

		try
		{
			final byte[] buf = new byte[bufferSize];
			int len;

			while ((len = inputStream.read(buf)) != -1)
			{
				digest.update(buf, 0, len);
			}
			return digest.digest();
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}
}
