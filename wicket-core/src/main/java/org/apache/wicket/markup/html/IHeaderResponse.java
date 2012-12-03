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
package org.apache.wicket.markup.html;

import java.io.Closeable;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Interface that is used to render header elements (usually javascript and CSS references).
 * 
 * Implementation of this interface is responsible for filtering duplicate contributions (so that
 * for example the same javascript is not loaded twice) during the same request.
 * 
 * @author Matej Knopp
 */
public interface IHeaderResponse extends Closeable
{
	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 */
	public void renderJavaScriptReference(ResourceReference reference);

	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 */
	public void renderJavaScriptReference(ResourceReference reference, String id);

	/**
	 * Writes a javascript reference with query parameters, if the specified reference hasn't been
	 * rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the javascript resource
	 * @param pageParameters
	 *            the parameters for this Javascript resource reference
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 */
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id);

	/**
	 * Writes a javascript reference with query parameters, if the specified reference hasn't been
	 * rendered yet.
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
	 */
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer);

	/**
	 * Writes a javascript reference with query parameters, if the specified reference hasn't been
	 * rendered yet.
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
	 */
	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer, String charset);

	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 */
	public void renderJavaScriptReference(String url);

	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 */
	public void renderJavaScriptReference(String url, String id);

	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param url
	 *            context-relative url of the the javascript resource
	 * @param id
	 *            id that will be used to filter duplicate reference (it's still filtered by URL
	 *            too)
	 * @param defer
	 *            specifies that the execution of a script should be deferred (delayed) until after
	 *            the page has been loaded.
	 */
	public void renderJavaScriptReference(String url, String id, boolean defer);

	/**
	 * Writes a javascript reference, if the specified reference hasn't been rendered yet.
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
	 */
	public void renderJavaScriptReference(String url, String id, boolean defer, String charset);

	/**
	 * Renders javascript code to the response, if the javascript has not already been rendered.
	 * 
	 * the necessary surrounding <code>script</code> tags will be added to the output.
	 * 
	 * @param javascript
	 *            javascript content to be rendered.
	 * 
	 * @param id
	 *            unique id for the javascript element. This can be null, however in that case the
	 *            ajax header contribution can't detect duplicate script fragments.
	 */
	public void renderJavaScript(CharSequence javascript, String id);

	/**
	 * Renders CSS code to the response, if the CSS has not already been rendered.
	 * 
	 * the necessary surrounding &lt;style&gt; tags will be added to the output.
	 * 
	 * @param css
	 *            css content to be rendered.
	 * 
	 * @param id
	 *            unique id for the &lt;style&gt; element. This can be <code>null</code>, however in
	 *            that case the ajax header contribution can't detect duplicate CSS fragments.
	 */
	void renderCSS(CharSequence css, String id);

	/**
	 * Writes a CSS reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 */
	public void renderCSSReference(ResourceReference reference);

	/**
	 * Writes a CSS reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 */
	public void renderCSSReference(String url);

	/**
	 * Writes a CSS reference, if the specified reference hasn't been rendered yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 */
	public void renderCSSReference(ResourceReference reference, String media);

	/**
	 * Writes a CSS reference with query parameters, if the specified reference hasn't been rendered
	 * yet.
	 * 
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 * @param pageParameters
	 *            the parameters for this CSS resource reference
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 */
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media);

	/**
	 * Writes a conditional IE comment with a CSS reference with query parameters, if the specified
	 * reference hasn't been rendered yet.
	 *
	 * <strong>Warning</strong>: the conditional comments don't work when injected dynamically
	 * with JavaScript (i.e. in Ajax response). An alternative solution is to use user agent sniffing
	 * at the server side:
	 * <code><pre>
	 * public void renderHead(IHeaderResponse response) {
	 *   WebClientInfo clientInfo = (WebClientInfo) getSession().getClientInfo();
	 *   ClientProperties properties = clientInfo.getProperties();
	 *   if (properties.isBrowserInternetExplorer() && properties.getBrowserVersionMajor() >= 8) {
	 *     response.renderCSSReference(new PackageResourceReference(MyPage.class, "my-conditional.css" ));
	 *   }
	 * }
	 * </pre></code>
	 *
	 * @param reference
	 *            resource reference pointing to the CSS resource
	 * @param pageParameters
	 *            the parameters for this CSS resource reference
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media, String condition);

	/**
	 * Writes a link to a CSS resource, if the specified url hasn't been rendered yet.
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 */
	public void renderCSSReference(String url, String media);

	/**
	 * Writes a conditional IE comment for a link to a CSS resource, if the specified url hasn't
	 * been rendered yet.
	 *
	 * <strong>Warning</strong>: the conditional comments don't work when injected dynamically
	 * with JavaScript (i.e. in Ajax response). An alternative solution is to use user agent sniffing
	 * at the server side:
	 * <code><pre>
	 * public void renderHead(IHeaderResponse response) {
	 *   WebClientInfo clientInfo = (WebClientInfo) getSession().getClientInfo();
	 *   ClientProperties properties = clientInfo.getProperties();
	 *   if (properties.isBrowserInternetExplorer() && properties.getBrowserVersionMajor() >= 8) {
	 *     response.renderCSSReference(new PackageResourceReference(MyPage.class, "my-conditional.css" ));
	 *   }
	 * }
	 * </pre></code>
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public void renderCSSReference(String url, String media, String condition);


	/**
	 * Renders an arbitrary string to the header. The string is only rendered if the same string
	 * hasn't been rendered before.
	 * <p>
	 * Note: This method is kind of dangerous as users are able to write to the output whatever they
	 * like.
	 * 
	 * @param string
	 *            string to be rendered to head
	 */
	public void renderString(CharSequence string);

	/**
	 * Marks the given object as rendered. The object can be anything (string, resource reference,
	 * etc...). The purpose of this function is to allow user to manually keep track of rendered
	 * items. This can be useful for items that are expensive to generate (like interpolated text).
	 * 
	 * @param object
	 *            object to be marked as rendered.
	 */
	public void markRendered(Object object);

	/**
	 * Returns whether the given object has been marked as rendered.
	 * <ul>
	 * <li>Methods <code>renderJavaScriptReference</code> and <code>renderCSSReference</code> mark
	 * the specified {@link ResourceReference} as rendered.
	 * <li>Method <code>renderJavaScript</code> marks List of two elements (first is javascript body
	 * CharSequence and second is id) as rendered.
	 * <li>Method <code>renderString</code> marks the whole string as rendered.
	 * <li>Method <code>markRendered</code> can be used to mark an arbitrary object as rendered
	 * </ul>
	 * 
	 * @param object
	 *            Object that is queried to be rendered
	 * @return Whether the object has been marked as rendered during the request
	 */
	public boolean wasRendered(Object object);

	/**
	 * Returns the response that can be used to write arbitrary text to the head section.
	 * <p>
	 * Note: This method is kind of dangerous as users are able to write to the output whatever they
	 * like.
	 * 
	 * @return Response
	 */
	public Response getResponse();

	/**
	 * Renders javascript that is executed right after the DOM is built, before external resources
	 * (e.g. images) are loaded.
	 * 
	 * @param javascript
	 */
	public void renderOnDomReadyJavaScript(String javascript);

	/**
	 * Renders javascript that is executed after the entire page is loaded.
	 * 
	 * @param javascript
	 */
	public void renderOnLoadJavaScript(String javascript);

	/**
	 * Renders javascript that is executed after the given event happens on specified target
	 * 
	 * @param target
	 * @param event
	 * @param javascript
	 */
	public void renderOnEventJavaScript(String target, String event, String javascript);

	/**
	 * Mark Header rendering is completed and subsequent usage will be ignored. If some kind of
	 * buffering is used internally, this action will mark that the contents has to be flushed out.
	 */
	public void close();

	/**
	 * @return if header rendering is completed and subsequent usage will be ignored
	 */
	boolean isClosed();
}
