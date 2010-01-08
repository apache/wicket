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
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.ajax.builtin.modal.ModalWindowPage;
import org.apache.wicket.examples.ajax.builtin.tree.EditableTreeTablePage;
import org.apache.wicket.examples.ajax.builtin.tree.SimpleTreePage;
import org.apache.wicket.examples.ajax.builtin.tree.TreeTablePage;


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
		super.init();

		getResourceSettings().setThrowExceptionOnMissingResource(false);

		// TODO (NG)
		// getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());

		getDebugSettings().setAjaxDebugModeEnabled(true);

		mountBookmarkablePage("autocomplete", AutoCompletePage.class);
		mountBookmarkablePage("choice", ChoicePage.class);
		mountBookmarkablePage("clock", ClockPage.class);
		mountBookmarkablePage("editable-label", EditableLabelPage.class);
		mountBookmarkablePage("effects", EffectsPage.class);
		mountBookmarkablePage("form", FormPage.class);
		mountBookmarkablePage("guest-book", GuestBook.class);
		mountBookmarkablePage("lazy-loading", LazyLoadingPage.class);
		mountBookmarkablePage("links", LinksPage.class);
		mountBookmarkablePage("modal-window", ModalWindowPage.class);
		mountBookmarkablePage("on-change-ajax-behavior", OnChangeAjaxBehaviorPage.class);
		mountBookmarkablePage("pageables", PageablesPage.class);
		mountBookmarkablePage("ratings", RatingsPage.class);
		mountBookmarkablePage("tabbed-panel", TabbedPanelPage.class);
		mountBookmarkablePage("todo-list", TodoList.class);
		mountBookmarkablePage("world-clock", WorldClockPage.class);
		mountBookmarkablePage("tree/simple", SimpleTreePage.class);
		mountBookmarkablePage("tree/table", TreeTablePage.class);
		mountBookmarkablePage("tree/table/editable", EditableTreeTablePage.class);
		mountBookmarkablePage("upload", FileUploadPage.class);

	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Index.class;
	}
}