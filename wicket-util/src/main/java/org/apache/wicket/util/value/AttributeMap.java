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
package org.apache.wicket.util.value;

import java.util.Map;

/**
 * <code>ValueMap</code> for attributes.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public final class AttributeMap extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty <code>AttributeMap</code>.
	 */
	public AttributeMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            a <code>Map</code> to be copied
	 */
	public AttributeMap(final Map<String, Object> map)
	{
		super(map);
	}
}
