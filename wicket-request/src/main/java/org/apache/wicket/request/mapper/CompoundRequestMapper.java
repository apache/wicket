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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread safe compound {@link IRequestMapper}. The mappers are searched depending on their
 * compatibility score and the orders they were registered. If two or more {@link IRequestMapper}s
 * have the same compatibility score, the last registered mapper has highest priority.
 * 
 * @author igor.vaynberg
 * @author Matej Knopp
 */
public class CompoundRequestMapper implements ICompoundRequestMapper
{
	private static final Logger LOG = LoggerFactory.getLogger(CompoundRequestMapper.class);

	/**
	 * 
	 */
	static class MapperWithScore implements Comparable<MapperWithScore>
	{
		private final IRequestMapper mapper;
		private final int compatibilityScore;

		public MapperWithScore(final IRequestMapper mapper, final int compatibilityScore)
		{
			this.mapper = mapper;
			this.compatibilityScore = compatibilityScore;
		}

		@Override
		public int compareTo(final MapperWithScore o)
		{
			return (compatibilityScore < o.compatibilityScore ? 1
				: (compatibilityScore > o.compatibilityScore ? -1 : 0));
		}

		public IRequestMapper getMapper()
		{
			return mapper;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (!(o instanceof MapperWithScore))
				return false;

			MapperWithScore that = (MapperWithScore)o;

			if (compatibilityScore != that.compatibilityScore)
				return false;
			return mapper.equals(that.mapper);
		}

		@Override
		public int hashCode()
		{
			int result = mapper.hashCode();
			result = 31 * result + compatibilityScore;
			return result;
		}

		@Override
		public String toString()
		{
			return "Mapper: " + mapper.getClass().getName() + "; Score: " + compatibilityScore;
		}
	}

	private final List<IRequestMapper> mappers = new CopyOnWriteArrayList<IRequestMapper>();

	@Override
	public CompoundRequestMapper add(final IRequestMapper mapper)
	{
		mappers.add(0, mapper);
		return this;
	}

	@Override
	public CompoundRequestMapper remove(final IRequestMapper mapper)
	{
		mappers.remove(mapper);
		return this;
	}

	/**
	 * Searches the registered {@link IRequestMapper}s to find one that can map the {@link Request}.
	 * Each registered {@link IRequestMapper} is asked to provide its compatibility score. Then the
	 * mappers are asked to map the request in order depending on the provided compatibility
	 * score.
	 * <p>
	 * The mapper with highest compatibility score which can map the request is returned.
	 * 
	 * @param request
	 * @return RequestHandler for the request or <code>null</code> if no mapper for the request is
	 *         found.
	 */
	@Override
	public IRequestHandler mapRequest(final Request request)
	{
		List<MapperWithScore> list = new ArrayList<MapperWithScore>(mappers.size());

		for (IRequestMapper mapper : mappers)
		{
			int score = mapper.getCompatibilityScore(request);
			list.add(new MapperWithScore(mapper, score));
		}

		Collections.sort(list);

		if (LOG.isDebugEnabled())
		{
			logMappers(list, request.getUrl().toString());
		}

		for (MapperWithScore mapperWithScore : list)
		{
			IRequestMapper mapper = mapperWithScore.getMapper();
			IRequestHandler handler = mapper.mapRequest(request);
			if (handler != null)
			{
				return handler;
			}
		}

		return null;
	}

	/**
	 * Logs all mappers with a positive compatibility score
	 *
	 * @param mappersWithScores
	 *      the list of all mappers
	 * @param url
	 *      the url to match by these mappers
	 */
	private void logMappers(final List<MapperWithScore> mappersWithScores, final String url)
	{
		final List<MapperWithScore> compatibleMappers = new ArrayList<MapperWithScore>();
		for (MapperWithScore mapperWithScore : mappersWithScores)
		{
			if (mapperWithScore.compatibilityScore > 0)
			{
				compatibleMappers.add(mapperWithScore);
			}
		}
		if (compatibleMappers.size() == 0)
		{
			LOG.debug("No compatible mapper found for URL '{}'", url);
		}
		else if (compatibleMappers.size() == 1)
		{
			LOG.debug("One compatible mapper found for URL '{}' -> '{}'", url, compatibleMappers.get(0));
		}
		else
		{
			LOG.debug("Multiple compatible mappers found for URL '{}'", url);
			for (MapperWithScore compatibleMapper : compatibleMappers)
			{
		        LOG.debug(" * {}", compatibleMapper);
			}
		}
	}

	/**
	 * Searches the registered {@link IRequestMapper}s to find one that can map the
	 * {@link IRequestHandler}. Each registered {@link IRequestMapper} is asked to map the
	 * {@link IRequestHandler} until a mapper which can map the {@link IRequestHandler} is found or
	 * no more mappers are left.
	 * <p>
	 * The mappers are searched in reverse order as they have been registered. More recently
	 * registered mappers have bigger priority.
	 * 
	 * @param handler
	 * @return Url for the handler or <code>null</code> if no mapper for the handler is found.
	 */
	@Override
	public Url mapHandler(final IRequestHandler handler)
	{
		for (IRequestMapper mapper : mappers)
		{
			Url url = mapper.mapHandler(handler);
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
	@Override
	public int getCompatibilityScore(final Request request)
	{
		int score = Integer.MIN_VALUE;
		for (IRequestMapper mapper : mappers)
		{
			score = Math.max(score, mapper.getCompatibilityScore(request));
		}
		return score;
	}

	@Override
	public Iterator<IRequestMapper> iterator()
	{
		return mappers.iterator();
	}

	@Override
	public void unmount(String path)
	{
		final Url url = Url.parse(path);
		final Request request = createRequest(url);

		for (IRequestMapper mapper : this)
		{
			if (mapper.mapRequest(request) != null)
			{
				remove(mapper);
			}
		}
	}

	int size()
	{
		return mappers.size();
	}

	Request createRequest(final Url url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}

			@Override
			public Url getClientUrl()
			{
				return url;
			}

			@Override
			public Charset getCharset()
			{
				return null;
			}
		};
	}
}
