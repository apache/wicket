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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.TestPage1;
import org.apache.wicket.ajaxng.request.AjaxNGUrlCodingStrategy;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.ajax.builtin.modal.ModalWindowPage;
import org.apache.wicket.examples.ajax.builtin.tree.EditableTreeTablePage;
import org.apache.wicket.examples.ajax.builtin.tree.SimpleTreePage;
import org.apache.wicket.examples.ajax.builtin.tree.TreeTablePage;
import org.apache.wicket.markup.html.AjaxServerAndClientTimeFilter;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;


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
	@Override
	protected void init()
	{
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
		getDebugSettings().setAjaxDebugModeEnabled(true);

		mount(new HybridUrlCodingStrategy("autocomplete", AutoCompletePage.class));
		mount(new HybridUrlCodingStrategy("choice", ChoicePage.class));
		mount(new HybridUrlCodingStrategy("clock", ClockPage.class));
		mount(new HybridUrlCodingStrategy("editable-label", EditableLabelPage.class));
		mount(new HybridUrlCodingStrategy("effects", EffectsPage.class));
		mount(new HybridUrlCodingStrategy("form", FormPage.class));
		mount(new HybridUrlCodingStrategy("guest-book", GuestBook.class));
		mount(new HybridUrlCodingStrategy("lazy-loading", LazyLoadingPage.class));
		mount(new HybridUrlCodingStrategy("links", LinksPage.class));
		mount(new HybridUrlCodingStrategy("modal-window", ModalWindowPage.class));
		mount(new HybridUrlCodingStrategy("on-change-ajax-behavior", OnChangeAjaxBehaviorPage.class));
		mount(new HybridUrlCodingStrategy("pageables", PageablesPage.class));
		mount(new HybridUrlCodingStrategy("ratings", RatingsPage.class));
		mount(new HybridUrlCodingStrategy("tabbed-panel", TabbedPanelPage.class));
		mount(new HybridUrlCodingStrategy("todo-list", TodoList.class));
		mount(new HybridUrlCodingStrategy("world-clock", WorldClockPage.class));
		mount(new HybridUrlCodingStrategy("tree/simple", SimpleTreePage.class));
		mount(new HybridUrlCodingStrategy("tree/table", TreeTablePage.class));
		mount(new HybridUrlCodingStrategy("tree/table/editable", EditableTreeTablePage.class));
		
		mount(new HybridUrlCodingStrategy("/test/page1", TestPage1.class));
		
		mount(new AjaxNGUrlCodingStrategy("wicket-ajax-ng"));
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class< ? extends Page> getHomePage()
	{
		return Index.class;
	}
}