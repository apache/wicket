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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.NullProvider;
import org.apache.wicket.util.lang.Args;

/**
 * Mapper that rewrites parent path segments ({@code ../}) with the specified string and viceversa.
 * 
 * @author igor.vaynberg
 */
public class ParentPathReferenceRewriter implements IRequestMapper
{
	private final IProvider<String> escapeSequence;
	private final IRequestMapper chain;

	/**
	 * Construct.
	 * 
	 * @param chain
	 *            chained request mapper
	 * 
	 * @param escapeSequence
	 */
	public ParentPathReferenceRewriter(final IRequestMapper chain,
		final IProvider<String> escapeSequence)
	{
		Args.notNull(chain, "chain");
		Args.notNull(escapeSequence, "relativePathPartEscapeSequence");
		this.escapeSequence = escapeSequence;
		this.chain = chain;
	}

	/**
	 * Construct.
	 * 
	 * @param chain
	 *            chained request mapper
	 */
	public ParentPathReferenceRewriter(final IRequestMapper chain)
	{
		this(chain, new NullProvider<String>());
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	@Override
	public IRequestHandler mapRequest(final Request request)
	{
		Url url = request.getUrl();

		if (escapeSequence.get() != null)
		{
			for (int i = 0; i < url.getSegments().size(); i++)
			{
				if (url.getSegments().get(i).equals(escapeSequence.get()))
				{
					url.getSegments().set(i, "..");
				}
			}
		}

		return chain.mapRequest(request.cloneWithUrl(url));
	}

	/** {@inheritDoc} */
	@Override
	public Url mapHandler(final IRequestHandler requestHandler)
	{
		Url url = chain.mapHandler(requestHandler);
		if ((url != null) && (escapeSequence.get() != null))
		{
			for (int i = 0; i < url.getSegments().size(); i++)
			{
				if ("..".equals(url.getSegments().get(i)))
				{
					url.getSegments().set(i, escapeSequence.get());
				}
			}
		}
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public int getCompatibilityScore(final Request request)
	{
		return chain.getCompatibilityScore(request);
	}
}
