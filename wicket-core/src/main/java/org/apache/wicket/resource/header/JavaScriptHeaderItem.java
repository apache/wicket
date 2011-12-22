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
package org.apache.wicket.resource.header;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.JavaScriptUtils;
import org.apache.wicket.util.string.Strings;

/**
 * Base class for all {@link HeaderItem}s that represent javascripts. This class mainly contains
 * factory methods.
 * 
 * @author papegaaij
 */
public abstract class JavaScriptHeaderItem extends HeaderItem
{
	/**
	 * Creates a {@link JavaScriptReferenceHeaderItem} for the given reference.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @return A newly created {@link JavaScriptReferenceHeaderItem} for the given reference.
	 */
	public static JavaScriptReferenceHeaderItem forReference(ResourceReference reference)
	{
		return new JavaScriptReferenceHeaderItem(reference, null, null, false, null);
	}

	/**
	 * Creates a {@link JavaScriptReferenceHeaderItem} for the given reference.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @return A newly created {@link JavaScriptReferenceHeaderItem} for the given reference.
	 */
	public static JavaScriptReferenceHeaderItem forReference(ResourceReference reference, String id)
	{
		return new JavaScriptReferenceHeaderItem(reference, null, id, false, null);
	}

	/**
	 * Creates a {@link JavaScriptReferenceHeaderItem} for the given reference.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param pageParameters
	 *            the parameters for this Javascript resource reference
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @return A newly created {@link JavaScriptReferenceHeaderItem} for the given reference.
	 */
	public static JavaScriptReferenceHeaderItem forReference(ResourceReference reference,
		PageParameters pageParameters, String id)
	{
		return new JavaScriptReferenceHeaderItem(reference, pageParameters, id, false, null);
	}

	/**
	 * Creates a {@link JavaScriptReferenceHeaderItem} for the given reference.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param pageParameters
	 *            the parameters for this Javascript resource reference
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @return A newly created {@link JavaScriptReferenceHeaderItem} for the given reference.
	 */
	public static JavaScriptReferenceHeaderItem forReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer)
	{
		return new JavaScriptReferenceHeaderItem(reference, pageParameters, id, defer, null);
	}

	/**
	 * Creates a {@link JavaScriptReferenceHeaderItem} for the given reference.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param pageParameters
	 *            the parameters for this Javascript resource reference
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @param charset
	 *            a non null value specifies the charset attribute of the script tag
	 * @return A newly created {@link JavaScriptReferenceHeaderItem} for the given reference.
	 */
	public static JavaScriptReferenceHeaderItem forReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer, String charset)
	{
		return new JavaScriptReferenceHeaderItem(reference, pageParameters, id, defer, charset);
	}

	/**
	 * Creates a {@link JavaScriptContentHeaderItem} for the given content.
	 * 
	 * @param javascript
	 *            javascript content to be rendered.
	 * @param id
	 *            unique id for the javascript element. This can be null, however in that case the
	 *            ajax header contribution can't detect duplicate script fragments.
	 * @return A newly created {@link JavaScriptContentHeaderItem} for the given content.
	 */
	public static JavaScriptContentHeaderItem forScript(CharSequence javascript, String id)
	{
		return new JavaScriptContentHeaderItem(javascript, id);
	}

	/**
	 * Creates a {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @return A newly created {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 */
	public static JavaScriptUrlReferenceHeaderItem forUrl(String url)
	{
		return new JavaScriptUrlReferenceHeaderItem(url, null, false, null);
	}

	/**
	 * Creates a {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @return A newly created {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 */
	public static JavaScriptUrlReferenceHeaderItem forUrl(String url, String id)
	{
		return new JavaScriptUrlReferenceHeaderItem(url, id, false, null);
	}

	/**
	 * Creates a {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 * @return A newly created {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 */
	public static JavaScriptUrlReferenceHeaderItem forUrl(String url, String id, boolean defer)
	{
		return new JavaScriptUrlReferenceHeaderItem(url, id, defer, null);
	}

	/**
	 * Creates a {@link JavaScriptUrlReferenceHeaderItem} for the given url.
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
	 * @return A newly created {@link JavaScriptUrlReferenceHeaderItem} for the given url.
	 */
	public static JavaScriptUrlReferenceHeaderItem forUrl(String url, String id, boolean defer,
		String charset)
	{
		return new JavaScriptUrlReferenceHeaderItem(url, id, defer, charset);
	}

	protected static void internalRenderJavaScriptReference(Response response, String url,
		String id, boolean defer, String charset)
	{
		if (Strings.isEmpty(url))
		{
			throw new IllegalArgumentException("url cannot be empty or null");
		}

		JavaScriptUtils.writeJavaScriptUrl(response, url, id, defer, charset);
	}
}
