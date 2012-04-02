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
package org.apache.wicket.request.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.core.util.resource.WebExternalResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource served from a file relative to the context root.
 * 
 * @author almaw
 */
public class ContextRelativeResource extends AbstractResource implements IStaticCacheableResource
{
	private static final String CACHE_PREFIX = "context-relative:/";
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ContextRelativeResource.class);

	private final String path;

	/**
	 * Construct.
	 * 
	 * @param pathRelativeToContextRoot
	 */
	public ContextRelativeResource(String pathRelativeToContextRoot)
	{
		if (pathRelativeToContextRoot == null)
		{
			throw new IllegalArgumentException("Cannot have null path for ContextRelativeResource.");
		}

		// Make sure there is a leading '/'.
		if (!pathRelativeToContextRoot.startsWith("/"))
		{
			pathRelativeToContextRoot = "/" + pathRelativeToContextRoot;
		}
		path = pathRelativeToContextRoot;
	}
	
	@Override
	public Serializable getCacheKey()
	{
		return CACHE_PREFIX + path;
	}

	@Override
	public IResourceStream getCacheableResourceStream()
	{
		return new WebExternalResourceStream(path);
	}
	
	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes)
	{
		final ResourceResponse resourceResponse = new ResourceResponse();

		final WebExternalResourceStream webExternalResourceStream =
			new WebExternalResourceStream(path);
		resourceResponse.setContentType(webExternalResourceStream.getContentType());
		resourceResponse.setLastModified(webExternalResourceStream.lastModifiedTime());
		resourceResponse.setFileName(path);
		resourceResponse.setWriteCallback(new WriteCallback()
		{
			@Override
			public void writeData(final Attributes attributes)
			{
				InputStream inputStream = null;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					inputStream = webExternalResourceStream.getInputStream();
					Streams.copy(inputStream, baos);
					attributes.getResponse().write(baos.toByteArray());
				}
				catch (ResourceStreamNotFoundException rsnfx)
				{
					throw new WicketRuntimeException(rsnfx);
				}
				catch (IOException iox)
				{
					throw new WicketRuntimeException(iox);
				}
				finally
				{
					IOUtils.closeQuietly(inputStream);
					IOUtils.closeQuietly(baos);
				}
			}
		});

		return resourceResponse;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContextRelativeResource other = (ContextRelativeResource)obj;
		if (path == null)
		{
			if (other.path != null)
				return false;
		}
		else if (!path.equals(other.path))
			return false;
		return true;
	}
}
