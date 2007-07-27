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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.cluster.pagestore.ClusteredDiskPageStore;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.ajax.builtin.tree.EditableTreeTablePage;
import org.apache.wicket.examples.ajax.builtin.tree.SimpleTreePage;
import org.apache.wicket.examples.ajax.builtin.tree.TreeTablePage;
import org.apache.wicket.markup.html.AjaxServerAndClientTimeFilter;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;
import org.apache.wicket.session.ISessionStore;


/**
 * Application object for the wicked ajax examples
 */
public class AjaxApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public AjaxApplication()
	{
	}

	/**
	 * @see org.apache.wicket.examples.WicketExampleApplication#init()
	 */
	protected void init()
	{
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		getDebugSettings().setAjaxDebugModeEnabled(true);
		mount(new HybridUrlCodingStrategy("tree/simple", SimpleTreePage.class));
		mount(new HybridUrlCodingStrategy("tree/table", TreeTablePage.class));
		mount(new HybridUrlCodingStrategy("tree/table/editable", EditableTreeTablePage.class));
		
	}
	
	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#newSessionStore()
	 */
	@Override
	protected ISessionStore newSessionStore()
	{
		return new SecondLevelCacheSessionStore(this, new ClusteredDiskPageStore());
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Index.class;
	}
}