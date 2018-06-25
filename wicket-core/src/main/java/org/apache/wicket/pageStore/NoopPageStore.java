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
package org.apache.wicket.pageStore;

import org.apache.wicket.page.IManageablePage;

/**
 * A non-storage of pages.
 */
public class NoopPageStore implements IPageStore
{
	@Override
	public boolean supportsVersioning()
	{
		return false;
	}

	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		return true;
	}
	
	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		return null;
	}
}