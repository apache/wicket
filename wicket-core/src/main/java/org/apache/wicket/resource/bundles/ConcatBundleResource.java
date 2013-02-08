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
package org.apache.wicket.resource.bundles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.resource.ITextResourceCompressor;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@linkplain IResource resource} that concatenates several resources into one download. This
 * resource can only bundle {@link IStaticCacheableResource}s. The content type of the resource will
 * be that of the first resource that specifies its content type.
 * 
 * @author papegaaij
 */
public class ConcatBundleResource extends AbstractResource implements IStaticCacheableResource
{
	private static final Logger log = LoggerFactory.getLogger(ConcatBundleResource.class);

	private static final long serialVersionUID = 1L;

	private final List<? extends IReferenceHeaderItem> providedResources;

	private boolean cachingEnabled;

	/**
	 * An optional compressor that will be used to compress the bundle resources
	 */
	private ITextResourceCompressor compressor;

	/**
	 * Construct.
	 * 
	 * @param providedResources
	 */
	public ConcatBundleResource(List<? extends IReferenceHeaderItem> providedResources)
	{
		this.providedResources = Args.notNull(providedResources, "providedResources");
		cachingEnabled = true;
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		final ResourceResponse resourceResponse = new ResourceResponse();

		if (resourceResponse.dataNeedsToBeWritten(attributes))
		{
			try
			{
				List<IResourceStream> resources = collectResourceStreams();
				if (resources == null)
					return sendResourceError(resourceResponse, HttpServletResponse.SC_NOT_FOUND,
						"Unable to find resource");

				resourceResponse.setContentType(findContentType(resources));

				// add Last-Modified header (to support HEAD requests and If-Modified-Since)
				final Time lastModified = findLastModified(resources);

				if (lastModified != null)
					resourceResponse.setLastModified(lastModified);

				// read resource data
				final byte[] bytes = readAllResources(resources);

				// send Content-Length header
				resourceResponse.setContentLength(bytes.length);

				// send response body with resource data
				resourceResponse.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(bytes);
					}
				});
			}
			catch (IOException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to read resource stream");
			}
			catch (ResourceStreamNotFoundException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to open resource stream");
			}
		}

		return resourceResponse;
	}

	private List<IResourceStream> collectResourceStreams()
	{
		List<IResourceStream> ret = new ArrayList<IResourceStream>(providedResources.size());
		for (IReferenceHeaderItem curItem : providedResources)
		{
			IResourceStream stream = ((IStaticCacheableResource)curItem.getReference()
				.getResource()).getCacheableResourceStream();
			if (stream == null)
				return null;

			ret.add(stream);
		}
		return ret;
	}

	private String findContentType(List<IResourceStream> resources)
	{
		for (IResourceStream curStream : resources)
			if (curStream.getContentType() != null)
				return curStream.getContentType();
		return null;
	}

	private Time findLastModified(List<IResourceStream> resources)
	{
		Time ret = null;
		for (IResourceStream curStream : resources)
		{
			Time curLastModified = curStream.lastModifiedTime();
			if (ret == null || curLastModified.after(ret))
				ret = curLastModified;
		}
		return ret;
	}

	private byte[] readAllResources(List<IResourceStream> resources) throws IOException,
		ResourceStreamNotFoundException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (IResourceStream curStream : resources)
			IOUtils.copy(curStream.getInputStream(), output);

		byte[] bytes = output.toByteArray();

		if (getCompressor() != null)
		{
			String nonCompressed = new String(bytes, "UTF-8");
			bytes = getCompressor().compress(nonCompressed).getBytes("UTF-8");
		}

		return bytes;
	}

	private ResourceResponse sendResourceError(ResourceResponse resourceResponse, int errorCode,
		String errorMessage)
	{
		String msg = String.format("bundled resource: %s (status=%d)", errorMessage, errorCode);

		log.warn(msg);

		resourceResponse.setError(errorCode, errorMessage);
		return resourceResponse;
	}

	@Override
	public boolean isCachingEnabled()
	{
		return cachingEnabled;
	}

	public void setCachingEnabled(final boolean enabled)
	{
		cachingEnabled = enabled;
	}

	@Override
	public Serializable getCacheKey()
	{
		ArrayList<Serializable> key = new ArrayList<Serializable>(providedResources.size());
		for (IReferenceHeaderItem curItem : providedResources)
		{
			Serializable curKey = ((IStaticCacheableResource)curItem.getReference().getResource()).getCacheKey();
			if (curKey == null)
				return null;
			key.add(curKey);
		}
		return key;
	}

	@Override
	public IResourceStream getCacheableResourceStream()
	{
		List<IResourceStream> resources = collectResourceStreams();
		byte[] bytes = null;
		try
		{
			bytes = readAllResources(resources);
		}
		catch (IOException e)
		{
			return null;
		}
		catch (ResourceStreamNotFoundException e)
		{
			return null;
		}

		final String contentType = findContentType(resources);
		final Time lastModified = findLastModified(resources);
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		final long length = bytes.length;
		AbstractResourceStream ret = new AbstractResourceStream()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getInputStream() throws ResourceStreamNotFoundException
			{
				return inputStream;
			}

			@Override
			public Bytes length()
			{
				return Bytes.bytes(length);
			}

			@Override
			public String getContentType()
			{
				return contentType;
			}

			@Override
			public Time lastModifiedTime()
			{
				return lastModified;
			}

			@Override
			public void close() throws IOException
			{
				inputStream.close();
			}
		};
		return ret;
	}

	public void setCompressor(ITextResourceCompressor compressor)
	{
		this.compressor = compressor;
	}

	public ITextResourceCompressor getCompressor()
	{
		return compressor;
	}
}