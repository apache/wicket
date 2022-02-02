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

import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

/**
 * A header response which defers all {@link AbstractJavaScriptReferenceHeaderItem}s.
 * <p>
 * To prevent any error because of possible dependencies to referenced JavaScript files
 * *all* {@link JavaScriptHeaderItem}s are replaced with suitable implementations that
 * delay any execution until all deferred {@link AbstractJavaScriptReferenceHeaderItem}s
 * have been loaded.
 * <p>
 * Note: This solution depends on the execution order of JavaScript in the browser:
 * The 'DOMContentLoaded' event has to be fired <me>after</em> all deferred JavaScript
 * resources have been loaded. This doesn't seem to be the case in all browsers, thus
 * this class should be considered experimental.
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
		if (RequestCycle.get().find(AjaxRequestTarget.class).isEmpty()) {
			while (item instanceof IWrappedHeaderItem) {
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
		}
		
		super.render(item);
	}

	/**
	 * A specialization that uses native "DOMContentLoaded" events without dependency to external JavaScript.
	 */
	private static class NativeOnDomContentLoadedHeaderItem extends OnDomReadyHeaderItem
	{
		private static final long serialVersionUID = 1L;

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
	 */
	private static class NativeOnLoadHeaderItem extends OnLoadHeaderItem
	{
		private static final long serialVersionUID = 1L;

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
