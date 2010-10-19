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
package org.apache.wicket.examples.niceurl;

import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.niceurl.mounted.Page3;
import org.apache.wicket.request.mapper.PackageMapper;
import org.apache.wicket.request.mapper.mount.MountMapper;
import org.apache.wicket.util.lang.PackageName;


/**
 * Application class for this example.
 * 
 * TODO NG
 * 
 * @author Eelco Hillenius
 */
public class NiceUrlApplication extends WicketExampleApplication
{
	/**
	 * Construct.
	 */
	public NiceUrlApplication()
	{
		super();
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Home.class;
	}

// private <C extends Page> void mountBookmarkablePageWithUrlCoding(String path, Class<C> pageClass)
// {
// mount(new QueryStringUrlCodingStrategy(path, pageClass));
// }

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		// mount single bookmarkable pages
		mountPage("/the/homepage/path", Home.class);
		mountPage("/a/nice/path/to/the/first/page", Page1.class);
		mountPage("/path/to/page2", Page2.class);
		mountPage("/path/to/page2qpencoded", Page2QP.class);

		// mount a whole package at once (all bookmarkable pages,
		// the relative class name will be part of the url

		// maybe not the neatest sight, but for package mounting it makes
		// sense to use one of the (important) classes in your package, so
		// that any refactoring (like a package rename) will automatically
		// be applied here.
		getRootRequestMapperAsCompound().add(
			new MountMapper("/my/mounted/package", new PackageMapper(
				PackageName.forClass(Page3.class))));
	}
	/**
	 * Sets up a request coding strategy that uses case-insensitive mounts
	 * 
	 * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycleProcessor() //
	 */
// @Override
// protected IRequestCycleProcessor newRequestCycleProcessor()
// {
// return new WebRequestCycleProcessor()
// {
// @Override
// protected IRequestCodingStrategy newRequestCodingStrategy()
// {
// WebRequestCodingStrategy.Settings stratSettings = new WebRequestCodingStrategy.Settings();
// stratSettings.setMountsCaseSensitive(false);
// return new WebRequestCodingStrategy(stratSettings);
// }
// };
// }
}
