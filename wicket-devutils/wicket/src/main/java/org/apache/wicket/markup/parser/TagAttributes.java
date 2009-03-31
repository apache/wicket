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
package org.apache.wicket.markup.parser;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TagAttributes extends ValueMap
{
	/** Log. */
	static final Logger log = LoggerFactory.getLogger(TagAttributes.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs empty <code>ValueMap</code>.
	 */
	public TagAttributes()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            the <code>ValueMap</code> to copy
	 */
	public TagAttributes(final Map map)
	{
		super();
		putAll(map);
	}

	/**
	 * @see org.apache.wicket.util.value.ValueMap#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public final Object put(String key, Object value)
	{
		checkIdAttribute(key);
		return super.put(key, value);
	}

	/**
	 * @param key
	 */
	private void checkIdAttribute(String key)
	{
		if ((key != null) && (key.equalsIgnoreCase("id")))
		{
			log.warn("WARNING: Please use component.setMarkupId(String) to change the tag's 'id' attribute.");
		}
	}

	/**
	 * Modifying the 'id' attribute should be made via Component.setMarkupId(). But the markup
	 * parser must still be able to add the 'id' attribute without warning.
	 * 
	 * @param key
	 * @param value
	 * @return The old value
	 */
	public final Object putInternal(String key, Object value)
	{
		return super.put(key, value);
	}

	/**
	 * @see org.apache.wicket.util.value.ValueMap#putAll(java.util.Map)
	 */
	@Override
	public final void putAll(Map map)
	{
		Iterator<?> iter = map.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String)iter.next();
			checkIdAttribute(key);
		}

		super.putAll(map);
	}
}