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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * A demo panel which will be used to render mail template
 */
public class MailTemplatePanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the component id
	 * @param nameModel
	 *            the model that brings the customer's name
	 */
	public MailTemplatePanel(String id, IModel<String> nameModel)
	{
		super(id);

		add(new Label("name", nameModel));

		CharSequence relativeUrl = urlFor(new PackageResourceReference(MailTemplate.class,
			"resource.txt"), null);
		String href = getRequestCycle().getUrlRenderer().renderFullUrl(
			Url.parse(relativeUrl.toString()));
		ExternalLink downloadLink = new ExternalLink("downloadLink", href);
		add(downloadLink);
	}
}
