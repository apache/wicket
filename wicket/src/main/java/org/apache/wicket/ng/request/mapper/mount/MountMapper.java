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
package org.apache.wicket.ng.request.mapper.mount;

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.Request;
import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.mapper.AbstractMapper;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.StringValue;

/**
 * {@link IRequestMapper} that can mount requests onto urls. TODO docs and unit test
 * 
 * @author igor.vaynberg
 */
public class MountMapper extends AbstractMapper
{
	private final String[] mountSegments;
	private final IMountedRequestMapper mapper;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param mapper
	 */
	public MountMapper(String mountPath, IMountedRequestMapper mapper)
	{
		Checks.argumentNotEmpty(mountPath, "mountPath");
		Checks.argumentNotNull(mapper, "mapper");

		mountSegments = getMountSegments(mountPath);
		this.mapper = mapper;
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param mapper
	 */
	public MountMapper(String mountPath, IRequestMapper mapper)
	{
		Checks.argumentNotEmpty(mountPath, "mountPath");
		Checks.argumentNotNull(mapper, "mapper");

		mountSegments = getMountSegments(mountPath);
		this.mapper = new UnmountedMapperAdapter(mapper);
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param handler
	 */
	public MountMapper(String mountPath, IRequestHandler handler)
	{
		Checks.argumentNotEmpty(mountPath, "mountPath");
		Checks.argumentNotNull(handler, "handler");

		mountSegments = getMountSegments(mountPath);
		mapper = new UnmountedRequestHandlerAdapter(handler);
	}

	/**
	 * @see org.apache.wicket.ng.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		if (urlStartsWith(request.getUrl(), mountSegments))
		{
			return mountSegments.length + mapper.getCompatibilityScore(dismountRequest(request));
		}
		else
		{
			return 0;
		}
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	private Request dismountRequest(Request request)
	{
		Url dismountedUrl = new Url(request.getUrl());
		dismountedUrl.removeLeadingSegments(mountSegments.length);
		return request.requestWithUrl(dismountedUrl);
	}

	/**
	 * @see org.apache.wicket.ng.request.IRequestMapper#mapRequest(org.apache.wicket.Request)
	 */
	public final IRequestHandler mapRequest(Request request)
	{
		final Url url = request.getUrl();

		if (url.getSegments().size() >= mountSegments.length && urlStartsWith(url, mountSegments))
		{
			MountParameters params = new MountParameters();
			for (int i = 0; i < mountSegments.length; i++)
			{
				String placeholder = getPlaceholder(mountSegments[i]);
				if (placeholder != null)
				{
					params.setValue(placeholder, StringValue.valueOf(url.getSegments().get(i)));
				}
			}

			return mapper.mapRequest(dismountRequest(request), params);
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.ng.request.IRequestMapper#mapHandler(org.apache.wicket.ng.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler handler)
	{
		Mount mount = mapper.mapHandler(handler);
		if (mount == null)
		{
			return null;
		}

		// TODO
		// Check.notNull(mount.getUrl());
		// Check.notNull(mount.getMountParameters());

		for (int i = mountSegments.length; i > 0; i--)
		{
			String segment = mountSegments[i - 1];
			String placeholder = getPlaceholder(segment);
			String replacement = segment;

			if (placeholder != null)
			{
				replacement = mount.getMountParameters().getValue(placeholder).toString();
				if (replacement == null)
				{
					throw new IllegalStateException();// TODO message
				}
			}

			mount.getUrl().getSegments().add(0, replacement);
		}

		return mount.getUrl();
	}
}
