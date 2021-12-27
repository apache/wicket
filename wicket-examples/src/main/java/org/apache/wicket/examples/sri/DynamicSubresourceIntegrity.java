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
package org.apache.wicket.examples.sri;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.markup.head.ISubresourceHeaderItem;
import org.apache.wicket.markup.head.filter.SubresourceHeaderResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic calculation of SRI for {@link IStaticCacheableResource}s.
 * 
 * @author svenmeier
 */
public class DynamicSubresourceIntegrity
{
	private static final Logger log = LoggerFactory.getLogger(DynamicSubresourceIntegrity.class);

	private Map<Serializable, String> cache = new HashMap<>();

	/**
	 * Wrap the given response
	 * 
	 * @param response
	 *            response to add SRI to
	 * @return wrapper
	 */
	public IHeaderResponse wrap(IHeaderResponse response)
	{
		return new SubresourceHeaderResponse(response)
		{
			@Override
			protected void configure(ISubresourceHeaderItem item)
			{
				String integrity = getIntegrity(item);
				if (integrity != null)
				{
					item.setIntegrity(integrity);
				}
			}
		};
	}

	public String getIntegrity(ISubresourceHeaderItem item)
	{
		if (item instanceof IReferenceHeaderItem)
		{
			ResourceReference reference = ((IReferenceHeaderItem)item).getReference();

			IResource resource = reference.getResource();
			if (resource instanceof IStaticCacheableResource)
			{
				IStaticCacheableResource cacheableResource = (IStaticCacheableResource)resource;
				
				return getIntegrity(reference, cacheableResource);
			}
		}

		return null;
	}

	private String getIntegrity(ResourceReference reference, IStaticCacheableResource cacheableResource)
	{
		String integrity = cache.get(cacheableResource.getCacheKey());
		if (integrity == null)
		{
			Url baseUrl = getBaseUrl(reference);
			try
			{
				byte[] bytes = getBytes(cacheableResource, baseUrl);

				integrity = "sha384-" + createHash(bytes);
				cache.put(cacheableResource.getCacheKey(), integrity);
			}
			catch (Exception ex)
			{
				log.error("cannot calculate integrity", ex);
			}
		}
		return integrity;
	}

	private Url getBaseUrl(ResourceReference reference)
	{
		RequestCycle cycle = RequestCycle.get();
		Url url = Url.parse(cycle.urlFor(reference, null));
		if (url.getSegments().get(0).equals("."))
		{
			// not sure why this is needed but leading dot must be removed,
			// otherwise relative urls will differ from the actually served css
			url.removeLeadingSegments(1);
		}
		
		return url;
	}

	/**
	 * Get bytes.
	 */
	protected byte[] getBytes(IStaticCacheableResource cacheableResource, Url baseUrl)
		throws IOException, ResourceStreamNotFoundException
	{
		byte[] bytes;

		// base url has to be adjusted for relative images in CSS
		RequestCycle cycle = RequestCycle.get();
		Url originalBaseUrl = cycle.getUrlRenderer().setBaseUrl(baseUrl);

		try (IResourceStream stream = cacheableResource.getResourceStream())
		{
			bytes = IOUtils.toByteArray(stream.getInputStream());
		}
		finally
		{
			cycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
		}

		return bytes;
	}

	/**
	 * Create the hash.
	 * 
	 * <pre>
	 * openssl dgst -sha384 -binary xy.js | openssl base64 -A
	 * </pre>
	 */
	protected String createHash(byte[] bytes) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-384");

		Encoder encoder = Base64.getEncoder();

		return encoder.encodeToString(digest.digest(bytes));
	}
}