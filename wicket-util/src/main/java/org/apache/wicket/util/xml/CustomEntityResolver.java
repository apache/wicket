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
package org.apache.wicket.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.wicket.util.lang.Args;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * entity resolver that tries to locate a document type definition (DTD) using a set of custom
 * entity resolvers
 * 
 * @author pete
 */
public class CustomEntityResolver implements EntityResolver
{
	private final Map<EntityKey, EntityLocator> entities = new HashMap<>(3);

	/**
	 * get default instances of custom entity resolver with preloaded well-known entities
	 * 
	 * @return instance of resolver
	 */
	public static CustomEntityResolver getPreloaded()
	{
		CustomEntityResolver resolver = new CustomEntityResolver();

		resolver.put(new EntityKey("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
			"http://java.sun.com/dtd/web-app_2_3.dtd"), new ServletApiEntityLocator(
			"web-app_2_3.dtd"));

		return resolver;
	}

	/**
	 * add custom entity resolver
	 * 
	 * @param key
	 *            key for lookup (contains id and url)
	 * @param locator
	 *            locator for looking up entity
	 */
	public void put(final EntityKey key, final EntityLocator locator)
	{
		Args.notNull(key, "key");
		Args.notNull(locator, "locator");
		entities.put(key, locator);
	}

	@Override
	public InputSource resolveEntity(final String id, final String url) throws SAXException,
		IOException
	{
		for (Map.Entry<EntityKey, EntityLocator> entry : entities.entrySet())
		{
			if (entry.getKey().id.equals(id) || entry.getKey().url.equals(url))
			{
				return entry.getValue().locateInputSource();
			}
		}

		return null;
	}

	/**
	 * key for entity
	 * <p/>
	 * consists of id + url
	 */
	public static class EntityKey
	{
		private final String id;
		private final String url;

		private EntityKey(final String id, final String url)
		{
			Args.notEmpty(id, "id");
			Args.notEmpty(url, "url");
			this.id = id;
			this.url = url;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof EntityKey))
			{
				return false;
			}

			EntityKey key = (EntityKey)o;

			if (!id.equals(key.id))
			{
				return false;
			}

			return url.equals(key.url);
		}

		@Override
		public int hashCode()
		{
			int result = id.hashCode();
			result = 31 * result + url.hashCode();
			return result;
		}
	}

	/**
	 * entity locator
	 * <p/>
	 * manages locating an entity
	 */
	public static interface EntityLocator
	{
		/**
		 * @return input source
		 * @throws SAXException
		 * @throws IOException
		 */
		InputSource locateInputSource() throws SAXException, IOException;
	}

	/**
	 * entity locator for resources inside servlet-api.jar
	 */
	public static class ServletApiEntityLocator implements EntityLocator
	{
		private final String name;

		private ServletApiEntityLocator(final String name)
		{
			this.name = name;
		}

		/**
		 * resolve servlet api resource, where e.g. 'web-app_2_3.dtd' is located
		 * 
		 * @return input source
		 */
		@Override
		public InputSource locateInputSource()
		{
			InputStream stream = Filter.class.getResourceAsStream("resources/" + name);

			if (stream == null)
			{
				return null;
			}

			return new InputSource(stream);
		}
	}
}
