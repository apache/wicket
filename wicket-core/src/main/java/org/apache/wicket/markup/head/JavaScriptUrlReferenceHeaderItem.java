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
package org.apache.wicket.markup.head;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

/**
 * {@link HeaderItem} for script tags that are rendered using a fixed URL, for example resources
 * from an external site or context relative urls.
 * 
 * @author papegaaij
 */
public class JavaScriptUrlReferenceHeaderItem extends AbstractJavaScriptReferenceHeaderItem
{
	private final String url;

	/**
	 * Creates a new {@code JavaScriptUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 */
	public JavaScriptUrlReferenceHeaderItem(String url, String id)
	{
		this.url = url;
		setId(id);
	}

	/**
	 * @return context-relative url of the the javascript resource
	 */
	public String getUrl()
	{
		return url;
	}

	@Override
	public void render(Response response)
	{
		internalRenderJavaScriptReference(response,
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()));
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		String url = UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get());
		if (Strings.isEmpty(getId()))
			return Collections.singletonList("javascript-" + url);
		else
			return Arrays.asList("javascript-" + getId(), "javascript-" + url);
	}

	@Override
	public String toString()
	{
		return "JavaScriptUrlReferenceHeaderItem(" + getUrl() + ")";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		JavaScriptUrlReferenceHeaderItem that = (JavaScriptUrlReferenceHeaderItem) o;
		return Objects.equals(url, that.url);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), url);
	}
}
