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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class PageWithResourceLink extends WebPage
{
	public PageWithResourceLink()
	{
		this(new EmptyPanel("link"));
	}

	public PageWithResourceLink(ResourceReference reference)
	{
		this(new ResourceLink<Void>("link", reference));
	}

	public PageWithResourceLink(IResource resource)
	{
		this(new ResourceLink<Void>("link", resource));
	}
	
	private PageWithResourceLink(Component component) {
		add(component);
			
		setStatelessHint(true);
	}
}
