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
package org.apache.wicket.examples.staticpages;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.URIRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.PackageResourceStream;
import org.apache.wicket.util.resource.WebExternalResourceStream;
import org.apache.wicket.util.resource.XSLTResourceStream;
import org.apache.wicket.util.value.ValueMap;

/**
 * Examples for serving static files
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class Application extends WebApplication
{
	@Override
	public Class getHomePage()
	{
		return Home.class;
	}

	@Override
	protected void init()
	{
		// Hello World as a Static Page
		mount(new URIRequestTargetUrlCodingStrategy("/docs")
		{
			@Override
			public IRequestTarget decode(RequestParameters requestParameters)
			{
				String path = "/staticpages/" + getURI(requestParameters);
				return new WebExternalResourceRequestTarget(path);
			}
		});

		// Hello World as a Static Page with XSLT layout
		mount(new URIRequestTargetUrlCodingStrategy("/xsldocs")
		{
			@Override
			public IRequestTarget decode(RequestParameters requestParameters)
			{
				String path = "/staticpages/" + getURI(requestParameters);
				IResourceStream xslStream = new PackageResourceStream(Application.class,
						"layout.xsl");
				IResourceStream docStream = new WebExternalResourceStream(path);
				return new ResourceStreamRequestTarget(new XSLTResourceStream(xslStream, docStream));
			}
		});

		// Passing URI to a Wicket page
		mount(new URIRequestTargetUrlCodingStrategy("/pages")
		{
			@Override
			public IRequestTarget decode(RequestParameters requestParameters)
			{
				final ValueMap requestParams = decodeParameters(requestParameters);
				PageParameters params = new PageParameters();
				params.put("uri", requestParams.get(URI));
				return new BookmarkablePageRequestTarget(Page.class, params) {
					/**
					 * @see org.apache.wicket.request.target.component.BookmarkablePageRequestTarget#respond(org.apache.wicket.RequestCycle)
					 */
					@Override
					public void respond(RequestCycle requestCycle)
					{
						if (requestParams.getString("email") != null) {
							final StringResponse emailResponse = new StringResponse();
							final WebResponse originalResponse = (WebResponse)RequestCycle.get().getResponse();
							RequestCycle.get().setResponse(emailResponse);
							super.respond(requestCycle);
							// Here send the email instead of dumping it to stdout!
							System.out.println(emailResponse.toString());
							RequestCycle.get().setResponse(originalResponse);
							RequestCycle.get().setRequestTarget(new BookmarkablePageRequestTarget(Sent.class));
						} else {
							super.respond(requestCycle);
						}
					}
				};
			}
		});
	}
}
