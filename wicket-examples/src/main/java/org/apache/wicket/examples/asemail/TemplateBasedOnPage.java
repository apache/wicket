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
package org.apache.wicket.examples.asemail;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * A page which acts a template for mails which should be send to the customers.
 */
public class TemplateBasedOnPage extends WebPage
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            the current page parameters
	 */
	public TemplateBasedOnPage(final PageParameters parameters)
	{
		super(parameters);

		add(new Label("name", parameters.get("name").toString("Unknown")));

		CharSequence relativeUrl = urlFor(new PackageResourceReference(MailTemplate.class,
			"resource.txt"), null);
		String href = getRequestCycle().getUrlRenderer().renderFullUrl(
			Url.parse(relativeUrl.toString()));
		ExternalLink downloadLink = new ExternalLink("downloadLink", href);
		add(downloadLink);
	}
}
