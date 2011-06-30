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
package org.apache.wicket.request.resource.caching.version;

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * cache key for resource lookup
 * <p/>
 * this key will take the localized version of the resource as a key
 *
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class CacheResourceVersionKey implements Serializable
{
	/** resource scope */
	private final Class<?> scope;
	
	/** resource name */
	private final String name;
	
	/** current locale */
	private final Locale locale;

	/** current style */
	private final String style;
	
	/** current variation */
	private final String variation;

	/**
	 * create cache key from localized resource information 
	 * 
	 * @param resourceReference
	 *           base package resource reference
	 * @param streamInfo
	 *           current localized stream information     
	 */
	public CacheResourceVersionKey(PackageResourceReference resourceReference,
	                               PackageResourceReference.StreamInfo streamInfo)
	{
		Args.notNull(resourceReference, "resourceReference");
		Args.notNull(streamInfo, "streamInfo");
		
		this.scope = Args.notNull(resourceReference.getScope(), "resource scope");
		this.name = Args.notEmpty(resourceReference.getName(), "resource name");
		this.locale = streamInfo.locale;
		this.style = streamInfo.style;
		this.variation = streamInfo.variation;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof CacheResourceVersionKey))
			return false;

		final CacheResourceVersionKey that = (CacheResourceVersionKey)o;

		if (locale != null? !locale.equals(that.locale) : that.locale != null)
			return false;
		
		if (!name.equals(that.name))
			return false;
		
		if (!scope.equals(that.scope))
			return false;
		
		if (style != null? !style.equals(that.style) : that.style != null)
			return false;
		
		if (variation != null? !variation.equals(that.variation) : that.variation != null)
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = scope.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (locale != null? locale.hashCode() : 0);
		result = 31 * result + (style != null? style.hashCode() : 0);
		result = 31 * result + (variation != null? variation.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("CacheResourceVersionKey");
		sb.append("{scope=").append(scope);
		sb.append(", name='").append(name).append('\'');
		sb.append(", locale=").append(locale);
		sb.append(", style='").append(style).append('\'');
		sb.append(", variation='").append(variation).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
