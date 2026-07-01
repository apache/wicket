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
package org.apache.wicket.ajax;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A resource reference for the JavaScript of the XML replacement method for markup.
 */
public class XmlReplacementMethodResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final XmlReplacementMethodResourceReference INSTANCE = new XmlReplacementMethodResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static XmlReplacementMethodResourceReference get()
	{
		return INSTANCE;
	}

	private XmlReplacementMethodResourceReference()
	{
		super(XmlReplacementMethodResourceReference.class, "res/js/xml-replacement-method.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final ResourceReference wicketAjaxJqueryReference;
		if (Application.exists())
		{
            wicketAjaxJqueryReference = Application.get().getJavaScriptLibrarySettings().getWicketAjaxReference();
		}
		else
		{
			wicketAjaxJqueryReference = WicketAjaxJQueryResourceReference.get();
		}
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(wicketAjaxJqueryReference));
		return dependencies;
	}
}
