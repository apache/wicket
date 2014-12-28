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

import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * The import url reference header item is used to import html files with the link tag
 * 
 * @see org.apache.wicket.markup.head.CssUrlReferenceHeaderItem
 * @author Tobias Soloschenko
 *
 */
public class ImportUrlReferenceHeaderItem extends CssUrlReferenceHeaderItem
{

	/**
	 * Creates a new {@code ImportUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the resource to import
	 * @param media
	 *            the media type for this import ("print", "screen", etc.)
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public ImportUrlReferenceHeaderItem(String url, String media, String condition)
	{
		super(url, media, condition);
	}

	@Override
	public void render(Response response)
	{
		internalRenderCSSReference(response,
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()), getMedia(),
			getCondition(), true);
	}

	@Override
	public String toString()
	{
		return "ImportUrlReferenceHeaderItem(" + getUrl() + ")";
	}

}
