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
package org.apache.wicket.devutils.diskstore;

import org.apache.wicket.Component;
import org.apache.wicket.devutils.diskstore.browser.BrowserPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A page that shows the attributes (id, name, size) of the pages stored in the data stores.
 */
public class DiskStoreBrowserPage extends WebPage
{

	/**
	 * Construct.
	 * 
	 * @param parameters
	 *            the request parameters
	 */
	public DiskStoreBrowserPage(final PageParameters parameters)
	{
		super(parameters);

		Component tree;
// tree = new LabelTree("tree", new PageWindowModel(sessionId, dataStore));
		tree = new BrowserPanel("tree");
		add(tree);
	}

	@Override
	public boolean isVersioned()
	{
		return false;
	}
}
