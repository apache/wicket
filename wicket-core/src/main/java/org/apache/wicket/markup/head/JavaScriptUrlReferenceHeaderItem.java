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
public class JavaScriptUrlReferenceHeaderItem extends JavaScriptHeaderItem
{
	private final String url;
	private final String id;
	private final boolean defer;
	private final String charset;

	/**
	 * Creates a new {@code JavaScriptUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @param charset
	 *            a non null value specifies the charset attribute of the script tag
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public JavaScriptUrlReferenceHeaderItem(String url, String id, boolean defer, String charset,
		String condition)
	{
		super(condition);
		this.url = url;
		this.id = id;
		this.defer = defer;
		this.charset = charset;
	}

	/**
	 * @return context-relative url of the the javascript resource
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @return id that will be used to filter duplicate reference
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return if the execution of a script should be deferred (delayed) until after the page has
	 *         been loaded.
	 */
	public boolean isDefer()
	{
		return defer;
	}

	/**
	 * @return a non null value specifies the charset attribute of the script tag
	 */
	public String getCharset()
	{
		return charset;
	}


	@Override
	public void render(Response response)
	{
		internalRenderJavaScriptReference(response,
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()), getId(), isDefer(),
			getCharset(), getCondition());
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
	public int hashCode()
	{
		return getUrl().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof JavaScriptUrlReferenceHeaderItem)
			return ((JavaScriptUrlReferenceHeaderItem)obj).getUrl().equals(getUrl());
		return false;
	}
}
