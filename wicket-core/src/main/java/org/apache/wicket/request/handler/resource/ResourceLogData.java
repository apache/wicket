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
package org.apache.wicket.request.handler.resource;

import java.util.Locale;

import org.apache.wicket.request.ILogData;

/**
 * Contains logging data related to resources requests.
 * 
 * @author Emond Papegaaij
 */
public abstract class ResourceLogData implements ILogData
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private final String locale;
	private final String style;
	private final String variation;

	/**
	 * Construct.
	 * 
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public ResourceLogData(String name, Locale locale, String style, String variation)
	{
		this.name = name;
		this.locale = locale == null ? null : locale.toString();
		this.style = style;
		this.variation = variation;
	}

	/**
	 * @return (file)name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return locale
	 */
	public final String getLocale()
	{
		return locale;
	}

	/**
	 * @return style
	 */
	public final String getStyle()
	{
		return style;
	}

	/**
	 * @return variation
	 */
	public final String getVariation()
	{
		return variation;
	}

	protected void fillToString(StringBuilder sb)
	{
		sb.append("name=");
		sb.append(getName());
		sb.append(",locale=");
		sb.append(getLocale());
		sb.append(",style=");
		sb.append(getStyle());
		sb.append(",variation=");
		sb.append(getVariation());
	}
}
