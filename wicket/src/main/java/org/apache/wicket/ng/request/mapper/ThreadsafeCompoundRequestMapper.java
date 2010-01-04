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
package org.apache.wicket.ng.request.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.Request;
import org.apache.wicket.ng.request.ICompoundRequestMapper;
import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.Url;


/**
 * Thread safe compound {@link IRequestMapper}. The mappers are searched depending on their
 * compatibility score and the orders they were registered. If two or more {@link IRequestMapper}s
 * have the same compatibility score, the last registered mapper has highest priority.
 * 
 * @author Matej Knopp
 */
public class ThreadsafeCompoundRequestMapper implements ICompoundRequestMapper
{
	/**
	 * 
	 */
	private static class EncoderWithSegmentsCount implements Comparable<EncoderWithSegmentsCount>
	{
		private final IRequestMapper mapper;
		private final int compatibilityScore;

		public EncoderWithSegmentsCount(IRequestMapper encoder, int compatibilityScore)
		{
			mapper = encoder;
			this.compatibilityScore = compatibilityScore;
		}

		public int compareTo(EncoderWithSegmentsCount o)
		{
			return o.compatibilityScore - compatibilityScore;
		}

		public IRequestMapper getMapper()
		{
			return mapper;
		}
	}

	private final List<IRequestMapper> mappers = new CopyOnWriteArrayList<IRequestMapper>();

	/**
	 * Construct.
	 */
	public ThreadsafeCompoundRequestMapper()
	{
	}

	/**
	 * @see org.apache.wicket.ng.request.ICompoundRequestMapper#register(org.apache.wicket.ng.request.IRequestMapper)
	 */
	public void register(IRequestMapper encoder)
	{
		mappers.add(0, encoder);
	}

	/**
	 * @see org.apache.wicket.ng.request.ICompoundRequestMapper#unregister(org.apache.wicket.ng.request.IRequestMapper)
	 */
	public void unregister(IRequestMapper encoder)
	{
		mappers.remove(encoder);
	}

	/**
	 * Searches the registered {@link IRequestMapper}s to find one that can decode the
	 * {@link Request}. Each registered {@link IRequestMapper} is asked to provide the matching
	 * segments count. Then the encoders are asked to decode the request in order depending on the
	 * provided segments count.
	 * <p>
	 * The encoder with highest matching segments count that can decode the request is returned.
	 * 
	 * @param request
	 * @return RequestHandler for the request or <code>null</code> if no encoder for the request is
	 *         found.
	 */
	public IRequestHandler mapRequest(Request request)
	{
		List<EncoderWithSegmentsCount> list = new ArrayList<EncoderWithSegmentsCount>(
			mappers.size());

		for (IRequestMapper encoder : mappers)
		{
			list.add(new EncoderWithSegmentsCount(encoder, encoder.getCompatibilityScore(request)));
		}

		Collections.sort(list);

		for (EncoderWithSegmentsCount encoder : list)
		{
			IRequestHandler handler = encoder.getMapper().mapRequest(request);
			if (handler != null)
			{
				return handler;
			}
		}

		return null;
	}

	/**
	 * Searches the registered {@link IRequestMapper}s to find one that can encode the
	 * {@link IRequestHandler}. Each registered {@link IRequestMapper} is asked to encode the
	 * {@link IRequestHandler} until an encoder that can encode the {@link IRequestHandler} is found
	 * or no more encoders are left.
	 * <p>
	 * The handlers are searched in reverse order as they have been registered. More recently
	 * registered handlers have bigger priority.
	 * 
	 * @param handler
	 * @return Url for the handler or <code>null</code> if no encoder for the handler is found.
	 */
	public Url mapHandler(IRequestHandler handler)
	{
		for (IRequestMapper encoder : mappers)
		{
			Url url = encoder.mapHandler(handler);
			if (url != null)
			{
				return url;
			}
		}
		return null;
	}

	/**
	 * The scope of the compound mapper is the highest score of the registered mappers.
	 * 
	 * {@inheritDoc}
	 */
	public int getCompatibilityScore(Request request)
	{
		int score = Integer.MIN_VALUE;
		for (IRequestMapper mapper : mappers)
		{
			score = Math.max(score, mapper.getCompatibilityScore(request));
		}
		return score;
	}
}
