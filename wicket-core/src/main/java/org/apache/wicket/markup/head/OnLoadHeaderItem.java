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

import org.apache.wicket.Application;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.core.util.string.JavaScriptUtils;

/**
 * {@link HeaderItem} for scripts that need to be executed after the entire page is loaded.
 * 
 * @author papegaaij
 */
public class OnLoadHeaderItem extends HeaderItem
{
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
	 * Construct.
	 * 
	 * @param javaScript
	 */
	public OnLoadHeaderItem(CharSequence javaScript)
	{
		this.javaScript = Args.notEmpty(javaScript, "javaScript");
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
		JavaScriptUtils.writeJavaScript(response, "Wicket.Event.add(window, \"load\", " +
			"function(event) { " + getJavaScript() + ";});");
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Collections.singletonList("javascript-load-" + getJavaScript());
	}

	@Override
	public String toString()
	{
		return "OnLoadHeaderItem(" + getJavaScript() + ")";
	}

	@Override
	public int hashCode()
	{
		return getJavaScript().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof OnLoadHeaderItem)
			return ((OnLoadHeaderItem)obj).getJavaScript().equals(getJavaScript());
		return false;
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		IJavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
		ResourceReference wicketEventReference = ajaxSettings.getWicketEventReference();
		return Collections.singletonList(JavaScriptHeaderItem.forReference(wicketEventReference));
	}
}
