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
package org.apache.wicket.request.mapper.mount;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.StringValue;

/**
 * {@link org.apache.wicket.request.IRequestMapper} that can mount requests onto urls. TODO docs and unit test
 *
 * @author igor.vaynberg
 * @deprecated Will be removed in Wicket 8.0
 */
@Deprecated
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
	public MountMapper(final String mountPath, final IMountedRequestMapper mapper)
	{
		Args.notEmpty(mountPath, "mountPath");
		Args.notNull(mapper, "mapper");

		mountSegments = getMountSegments(mountPath);
		this.mapper = mapper;
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param mapper
	 */
	public MountMapper(final String mountPath, final IRequestMapper mapper)
	{
		Args.notEmpty(mountPath, "mountPath");
		Args.notNull(mapper, "mapper");

		mountSegments = getMountSegments(mountPath);
		this.mapper = new UnmountedMapperAdapter(mapper);
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param handler
	 */
	public MountMapper(final String mountPath, final IRequestHandler handler)
	{
		Args.notEmpty(mountPath, "mountPath");
		Args.notNull(handler, "handler");

		mountSegments = getMountSegments(mountPath);
		mapper = new UnmountedRequestHandlerAdapter(handler);
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(final Request request)
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
	 *            a {@link org.apache.wicket.request.Request} with the all mount segments - mount ones and the ones for the
	 *            delegated mapper
	 * @return a {@link org.apache.wicket.request.Request} with {@link org.apache.wicket.request.Url} without the mount segments
	 */
	private Request dismountRequest(final Request request)
	{
		Url dismountedUrl = new Url(request.getUrl());
		dismountedUrl.removeLeadingSegments(mountSegments.length);
		return request.cloneWithUrl(dismountedUrl);
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	@Override
	public final IRequestHandler mapRequest(final Request request)
	{
		final Url url = request.getUrl();

		if ((url.getSegments().size() >= mountSegments.length) && urlStartsWith(url, mountSegments))
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
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public Url mapHandler(final IRequestHandler handler)
	{
		Mount mount = mapper.mapHandler(handler);
		if (mount == null)
		{
			return null;
		}

		Checks.notNull(mount.getUrl(), "Mount's Url should not be null");
		Checks.notNull(mount.getMountParameters(), "Mount's parameters should not be null");

		for (int i = mountSegments.length; i > 0; i--)
		{
			String segment = mountSegments[i - 1];
			String placeholder = getPlaceholder(segment);
			String replacement = segment;

			if (placeholder != null)
			{
				replacement = mount.getMountParameters().getValue(placeholder).toString();
				Checks.notNull(replacement, "Cannot find a value for placeholder '%s'.",
						placeholder);
			}

			mount.getUrl().getSegments().add(0, replacement);
		}

		return mount.getUrl();
	}
}
