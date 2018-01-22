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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.AbstractJavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.page.PartialPageUpdate;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;

/**
 * A header response that defers all {@link AbstractJavaScriptReferenceHeaderItem}s.
 * <p>
 * To prevent any error because of possible dependencies to referenced JavaScript files
 * *all* {@link JavaScriptHeaderItem}s are replaced with suitable implementations that
 * delay any execution until {@link AbstractJavaScriptReferenceHeaderItem}s have been loaded.
 * 
 * @author svenmeier
+ */
public class JavaScriptDeferHeaderResponse extends DecoratingHeaderResponse
{
	/**
	 * Decorate the given response.
	 * 
	 * @param response
	 */
	public JavaScriptDeferHeaderResponse(IHeaderResponse response)
	{
		super(response);
	}
	
	@Override
	public void render(HeaderItem item)
	{
		if (item instanceof IWrappedHeaderItem) {
			item = ((IWrappedHeaderItem)item).getWrapped();
		}

		if (item instanceof AbstractJavaScriptReferenceHeaderItem) {
			((AbstractJavaScriptReferenceHeaderItem)item).setDefer(true);
		} else if (item instanceof JavaScriptContentHeaderItem) {
			item = new NativeOnDomContentLoadedHeaderItem(((JavaScriptContentHeaderItem)item).getJavaScript());
		} else if (item instanceof OnDomReadyHeaderItem) {
			item = new NativeOnDomContentLoadedHeaderItem(((OnDomReadyHeaderItem)item).getJavaScript());
		} else if (item instanceof OnLoadHeaderItem) {
			item = new NativeOnLoadHeaderItem(((OnLoadHeaderItem)item).getJavaScript());
		}
		
		super.render(item);
	}

	/**
	 * A specialization that uses native "DOMContentLoaded" events without dependency to external JavaScript.
	 * <p>
	 * For Ajax requests we utilize the fact, that {@link PartialPageUpdate} renders {@link #getJavaScript()} only,
	 * thus executing the JavaScript directly without any event registration.
	 */
	private class NativeOnDomContentLoadedHeaderItem extends OnDomReadyHeaderItem
	{
		/**
		 * Construct.
		 *
		 * @param javaScript
		 */
		public NativeOnDomContentLoadedHeaderItem(CharSequence javaScript)
		{
			super(javaScript);
		}

		/**
		 * Overriden to use native {@code addEventListener('DOMContentLoaded')} instead.
		 */
		@Override
		public void render(Response response)
		{
			CharSequence js = getJavaScript();
			if (Strings.isEmpty(js) == false)
			{
				JavaScriptUtils.writeJavaScript(response, "document.addEventListener('DOMContentLoaded', function() { " + js + "; });");
			}
		}
	}
	
	/**
	 * A specialization that uses native "load" events without dependency to external JavaScript 
	 * <p>
	 * For Ajax requests we utilize the fact, that {@link PartialPageUpdate} renders {@link #getJavaScript()} only,
	 * thus executing the JavaScript directly without any event registration.
	 */
	private class NativeOnLoadHeaderItem extends OnLoadHeaderItem
	{

		/**
		 * Construct.
		 *
		 * @param javaScript
		 */
		public NativeOnLoadHeaderItem(CharSequence javaScript)
		{
			super(javaScript);
		}

		/**
		 * Overriden to use native {@code addEventListener('load')} instead.
		 */
		@Override
		public void render(Response response)
		{
			CharSequence js = getJavaScript();
			if (Strings.isEmpty(js) == false)
			{
				JavaScriptUtils.writeJavaScript(response, "window.addEventListener('load', function() { " + js + "; });");
			}
		}
	}	
} 
