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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.JavaScriptLibrarySettings;
import org.apache.wicket.util.string.Strings;

/**
 * {@link HeaderItem} for scripts that need to be executed after the entire page is loaded.
 *
 * @author papegaaij
 */
public class OnLoadHeaderItem extends HeaderItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a {@link OnLoadHeaderItem} for the script.
	 *
	 * @param javaScript
	 *            The script to execute on the load event.
	 *
	 * @return A newly created {@link OnLoadHeaderItem}.
	 */
	public static OnLoadHeaderItem forScript(CharSequence javaScript)
	{
		return new OnLoadHeaderItem(javaScript);
	}

	private final CharSequence javaScript;

	/**
	 * Constructor.
	 *
	 * The JavaScript should be provided by overloaded #getJavaScript
	 */
	public OnLoadHeaderItem()
	{
		this(null);
	}

	/**
	 * Construct.
	 *
	 * @param javaScript
	 *            The script to execute on the load event
	 */
	public OnLoadHeaderItem(CharSequence javaScript)
	{
		this.javaScript = javaScript;
	}

	/**
	 * @return the script that gets executed after the entire is loaded.
	 */
	public CharSequence getJavaScript()
	{
		return javaScript;
	}


	@Override
	public void render(Response response)
	{
		CharSequence js = getJavaScript();
		if (Strings.isEmpty(js) == false)
		{
			JavaScriptUtils.writeJavaScript(response,
				"(function() {var f = function() {" + js + ";};\nif ('complete' === document.readyState) f(); else window.addEventListener('load', f);})();");
		}
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Collections.singletonList("javascript-load-" + getJavaScript());
	}

	@Override
	public String toString()
	{
		return String.format("OnLoadHeaderItem('%s')", getJavaScript());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OnLoadHeaderItem that = (OnLoadHeaderItem) o;
		return Objects.equals(javaScript, that.javaScript);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(javaScript);
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		JavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
		ResourceReference wicketAjaxReference = ajaxSettings.getWicketAjaxReference();
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(wicketAjaxReference));
		return dependencies;
	}
}
