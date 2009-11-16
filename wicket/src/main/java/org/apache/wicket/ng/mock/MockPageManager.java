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
package org.apache.wicket.ng.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.page.PageManagerContext;

/**
 * Simple {@link PageManager} used for testing.
 * 
 * @author Matej Knopp
 */
public class MockPageManager implements PageManager
{
	private final Map<Integer, ManageablePage> pages = new HashMap<Integer, ManageablePage>();

	/**
	 * Construct.
	 */
	public MockPageManager()
	{
	}

	public void commitRequest()
	{
	}

	public void destroy()
	{
	}

	public ManageablePage getPage(int id)
	{
		return pages.get(id);
	}

	public void newSessionCreated()
	{
	}

	public void sessionExpired(String sessionId)
	{
	}

	public void setContext(PageManagerContext context)
	{
	}

	public boolean supportsVersioning()
	{
		return false;
	}

	public void touchPage(ManageablePage page)
	{
		pages.put(page.getPageId(), page);
	}

}
