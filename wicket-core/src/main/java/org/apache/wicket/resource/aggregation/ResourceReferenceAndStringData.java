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
package org.apache.wicket.resource.aggregation;

import org.apache.wicket.request.resource.ResourceReference;

/**
 * A data holder built for the {@link AbstractResourceAggregatingHeaderResponse} that groups the
 * resource reference with a boolean representing whether it is css (or, if not, it is js), and the
 * string that was passed in to the responsible render*Reference method (for JS, this is ID, for
 * CSS, this is media).
 * 
 * It acts as a temporary data holder while the IHeaderContributors are being called so that at the
 * end of their traversal, we can render these references in an aggregated way, and still have the
 * appropriate data (i.e. was it CSS or JS) to render it properly.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceReferenceAndStringData
{
	private final ResourceReference reference;
	private final String string;
	private final boolean css;

	/**
	 * Construct with fields.
	 * 
	 * @param reference
	 * @param string
	 * @param css
	 */
	public ResourceReferenceAndStringData(ResourceReference reference, String string, boolean css)
	{
		super();
		this.reference = reference;
		this.string = string;
		this.css = css;
	}

	/**
	 * @return the resource reference that the user rendered
	 */
	public ResourceReference getReference()
	{
		return reference;
	}

	/**
	 * @return the string representing media (if this isCss()), or id (if not, meaning it's js)
	 */
	public String getString()
	{
		return string;
	}

	/**
	 * @return true if this is css, false if it's js
	 */
	public boolean isCss()
	{
		return css;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (css ? 1231 : 1237);
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceReferenceAndStringData other = (ResourceReferenceAndStringData)obj;
		if (css != other.css)
			return false;
		if (reference == null)
		{
			if (other.reference != null)
				return false;
		}
		else if (!reference.equals(other.reference))
			return false;
		if (string == null)
		{
			if (other.string != null)
				return false;
		}
		else if (!string.equals(other.string))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ResourceReferenceAndStringData [reference=" + reference + ", string=" + string +
			", css=" + css + "]";
	}
}