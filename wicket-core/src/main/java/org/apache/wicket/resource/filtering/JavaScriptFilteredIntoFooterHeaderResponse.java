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
package org.apache.wicket.resource.filtering;

import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * A header response that creates two buckets. The header bucket will contain all references to CSS.
 * The other bucket will contain all JavaScript, and you will need to add a
 * HeaderResponseFilteredResponseContainer to the footer of your page (typically just before the end
 * body tag) to render the JavaScript.
 * 
 * @author Jeremy Thomerson
 */
public final class JavaScriptFilteredIntoFooterHeaderResponse extends
	HeaderResponseContainerFilteringHeaderResponse
{
	private static final String HEADER_FILTER_NAME = "headerBucket";

	/**
	 * Construct.
	 * 
	 * @param response
	 *            the response you are wrapping
	 * @param footerBucketName
	 *            the name of the bucket that you will use for your footer container (see the class
	 *            javadocs for a reminder about putting this container in your footer)
	 */
	public JavaScriptFilteredIntoFooterHeaderResponse(IHeaderResponse response,
		String footerBucketName)
	{
		super(response, HEADER_FILTER_NAME, null);
		setFilters(createFilters(footerBucketName));
	}

	private IHeaderResponseFilter[] createFilters(String footerBucketName)
	{
		IHeaderResponseFilter footer = createFooterFilter(footerBucketName);
		IHeaderResponseFilter header = createHeaderFilter(HEADER_FILTER_NAME, footer);
		return new IHeaderResponseFilter[] { header, footer };
	}

	private IHeaderResponseFilter createFooterFilter(String footerBucketName)
	{
		return new JavaScriptAcceptingHeaderResponseFilter(footerBucketName);
	}

	private IHeaderResponseFilter createHeaderFilter(String headerFilterName, IHeaderResponseFilter footerFilter)
	{
		return new OppositeHeaderResponseFilter(headerFilterName, footerFilter);
	}

}
