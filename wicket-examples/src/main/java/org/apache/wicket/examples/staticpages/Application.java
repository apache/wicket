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

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Examples for serving static files
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class Application extends WebApplication
{
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Home.class;
	}

	@Override
	protected void init()
	{
		super.init();

		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		// TODO NG
// // Hello World as a Static Page
// mount(new URIRequestTargetUrlCodingStrategy("/docs")
// {
// @Override
// public IRequestHandler decode(ObsoleteRequestParameters requestParameters)
// {
// String path = "/staticpages/" + getURI(requestParameters);
// return new WebExternalResourceRequestTarget(path);
// }
// });
//
// // Hello World as a Static Page with XSLT layout
// mount(new URIRequestTargetUrlCodingStrategy("/xsldocs")
// {
// @Override
// public IRequestHandler decode(ObsoleteRequestParameters requestParameters)
// {
// String path = "/staticpages/" + getURI(requestParameters);
// IResourceStream xslStream = new PackageResourceStream(Application.class,
// "layout.xsl");
// IResourceStream docStream = new WebExternalResourceStream(path);
// return new ResourceStreamRequestHandler(
// new XSLTResourceStream(xslStream, docStream));
// }
// });
//
// // All requests to bookmarkable page "Page" will be captured, and the
// // "Sent" page is shown instead
// mount(new CapturingBookmarkablePageRequestTargetUrlCodingStrategy("/capturedpage",
// EmailPage.class, Sent.class));
	}
}
