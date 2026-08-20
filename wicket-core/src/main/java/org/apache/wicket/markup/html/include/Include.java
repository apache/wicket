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
package org.apache.wicket.markup.html.include;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;

import org.apache.wicket.IGenericComponent;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.lang.Args;


/**
 * <p>
 * Component that includes/ renders the import result of an URL, much like JSP include.
 * </p>
 * <p>
 * Use this to integrate non-Wicket locations in your page. <strong>This component is NOT meant for
 * integrating more Wicket sources as a means of quick and dirty page composition. Use Panels,
 * Borders and (Markup)inheritance for page composition instead.</strong>
 * </p>
 * <p>
 * You can feed this component the URL directly, or use a model that should deliver a valid URL. You
 * can both use absolute (e.g. http://www.theserverside.com/) and relative (e.g. mydir/mypage.html)
 * urls. This component will try to resolve relative urls to resources in the same webapplication.
 * </p>
 * <p>
 * The following example shows how to integrate a header and footer, coming from a plain HTML source
 * on the same server is integrated using this component. The files footer.html and header.html
 * would be located in the web application root directory
 * </p>
 * <p>
 * Java:
 * 
 * <pre>
 *   ...
 * 	add(new Include(&quot;header&quot;, &quot;header.html&quot;));
 * 	add(new Include(&quot;footer&quot;, &quot;footer.html&quot;));
 *   ...
 * </pre>
 * 
 * Html:
 * 
 * <pre>
 *   ...
 * 	&lt;div&gt;
 * 	 &lt;div wicket:id=&quot;header&quot;&gt;header comes here&lt;/div&gt;
 * 	 &lt;div&gt;I am the body!&lt;/div&gt;
 * 	 &lt;div wicket:id=&quot;footer&quot;&gt;footer comes here&lt;/div&gt;
 * 	&lt;/div&gt;
 *   ...
 * </pre>
 * 
 * </p>
 * <p>
 * <strong>This component is deprecated for security reasons and cannot be made safe.</strong> It
 * connects to whatever URL its model resolves to and writes the response into the page body
 * without escaping it, so the model value decides three separate things at once: what the server
 * connects to, what ends up in the page, and how much of it is read. Where anything in the request
 * can influence that value, a caller reaches {@code file:}, {@code jar:} and {@code ftp:} URLs as
 * well as hosts only the server can see, places arbitrary markup and script in the application's
 * own origin, and pins a render thread on a read that has neither a size limit nor a timeout.
 * </p>
 * <p>
 * None of that is a defect in the implementation. Fetching an arbitrary URL and splicing its raw
 * content into a page is what this component is for, and restricting the scheme, the host, the size
 * or the escaping would leave nothing of it, so there is no replacement and no configuration that
 * makes it safe. Applications using it for page composition &mdash; the reason most reach for it
 * &mdash; should use Panels, Borders and markup inheritance as described above; for including
 * remote content there is nothing to migrate to. See {@code SECURITY.md} for the scope this places
 * the component in.
 * </p>
 * 
 * @author Eelco Hillenius
 * @deprecated no replacement; see above. Fetching a URL and rendering its content unescaped cannot
 *             be made safe, so this component is removed in Wicket 11.
 */
@Deprecated
public class Include extends WebComponent implements IGenericComponent<String, Include>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 */
	public Include(final String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            the model
	 */
	public Include(String id, IModel<String> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param modelObject
	 *            the model object (will be wrapped in a model)
	 */
	public Include(String id, String modelObject)
	{
		super(id, new Model<>(modelObject));
	}

	/**
	 * Imports the contents of the url of the model object.
	 * 
	 * @return the imported contents
	 */
	protected String importAsString()
	{
		// gets the model object: should provide us with either an absolute or a
		// relative url
		String url = getModelObject();

		if (UrlUtils.isRelative(url))
		{
			return importRelativeUrl(url);
		}
		else
		{
			return importAbsoluteUrl(url);
		}
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		String content = importAsString();
		replaceComponentTagBody(markupStream, openTag, content);
	}

	/**
	 * Imports from a relative url.
	 * 
	 * @param url
	 *            the url to import
	 * @return the imported url's contents
	 */
	private String importRelativeUrl(String url)
	{
		Args.notEmpty(url, "url");

		if (url.charAt(0) != '/')
		{
			url = '/' + url;
		}

		try
		{
			ServletContext servletContext = getWebApplication().getServletContext();
			URL resource = servletContext.getResource(url);
			return importUrl(resource);
		} catch (MalformedURLException mux)
		{
			throw new WicketRuntimeException(mux);
		}
	}

	/**
	 * Imports from an absolute url.
	 * 
	 * @param url
	 *            the url to import
	 * @return the imported url's contents
	 */
	private String importAbsoluteUrl(CharSequence url)
	{
		try
		{
			return importUrl(new URL(url.toString()));
		}
		catch (MalformedURLException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * 
	 * @return The charset of the text to be retrieved and included
	 */
	public Charset getCharset()
	{
		return null;
	}

	/**
	 * Imports the contents from the given url.
	 * 
	 * @param url
	 *            the url
	 * @return the imported contents
	 */
	private String importUrl(URL url)
	{
		return ResourceUtil.readString(new UrlResourceStream(url), getCharset());
	}
}
