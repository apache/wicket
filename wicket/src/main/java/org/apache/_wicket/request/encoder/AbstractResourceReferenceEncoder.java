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
package org.apache._wicket.request.encoder;

import java.util.Locale;

import org.apache._wicket.request.Url;
import org.apache._wicket.request.Url.QueryParameter;
import org.apache._wicket.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 * Base class for encoding and decoding {@link ResourceReference}s
 * 
 * @author Matej Knopp
 */
public abstract class AbstractResourceReferenceEncoder extends AbstractEncoder
{

	protected static class ResourceReferenceAttributes
	{
		protected Locale locale;
		protected String style;
	};
	
	protected static String encodeResourceReferenceAttributes(ResourceReferenceAttributes attributes)
	{
		if (attributes == null || (attributes.locale == null && attributes.style == null))
		{
			return null;
		}
		else
		{
			StringBuilder res = new StringBuilder();
			if (attributes.locale != null)
			{
				res.append(attributes.locale.toString());
			}
			if (!Strings.isEmpty(attributes.style))
			{
				res.append("-");
				res.append(attributes.style);
			}
			return res.toString();
		}
	};
	
	protected static ResourceReferenceAttributes decodeResourceReferenceAttributes(String attributes)
	{
		ResourceReferenceAttributes res = new ResourceReferenceAttributes();		
		if (!Strings.isEmpty(attributes))
		{
			String split[] = attributes.split("-", 2);
			res.locale = parseLocale(split[0]);
			if (split.length == 2)
			{
				res.style = split[1];
			}			
		}
		return res;
	}
	
	private static Locale parseLocale(String locale)
	{
		if (Strings.isEmpty(locale))
		{
			return null;
		}
		else
		{
			String parts[] = locale.toLowerCase().split("_", 3);
			if (parts.length == 1)
			{
				return new Locale(parts[0]);
			}
			else if (parts.length == 2)
			{
				return new Locale(parts[0], parts[1]);
			}
			else if (parts.length == 3)
			{
				return new Locale(parts[0], parts[1], parts[2]);	
			}
			else
			{
				return null;
			}
		}
	}	
	
	protected void encodeResourceReferenceAttributes(Url url, ResourceReference reference)
	{
		ResourceReferenceAttributes attributes = new ResourceReferenceAttributes();
		attributes.locale = reference.getLocale();
		attributes.style = reference.getStyle();
		String encoded = encodeResourceReferenceAttributes(attributes);
		if (!Strings.isEmpty(encoded))
		{
			url.getQueryParameters().add(new Url.QueryParameter(encoded, ""));
		}
	}
	
	protected ResourceReferenceAttributes getResourceReferenceAttributes(Url url)
	{
		if (url == null)
		{
			throw new IllegalStateException("Argument 'url' may not be null.");
		}
		if (url.getQueryParameters().size() > 0)
		{
			QueryParameter param = url.getQueryParameters().get(0);
			if (Strings.isEmpty(param.getValue()))
			{
				return decodeResourceReferenceAttributes(param.getName());
			}
		}
		return new ResourceReferenceAttributes();
	}
}
