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
import org.apache.wicket.response.filter.AjaxServerAndClientTimeFilter;


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

		getApplicationSettings().setUploadProgressUpdatesEnabled(true);

		getResourceSettings().setThrowExceptionOnMissingResource(false);

		getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());

		getDebugSettings().setAjaxDebugModeEnabled(true);

		mountPage("autocomplete", AutoCompletePage.class);
		mountPage("choice", ChoicePage.class);
		mountPage("clock", ClockPage.class);
		mountPage("editable-label", EditableLabelPage.class);
		mountPage("effects", EffectsPage.class);
		mountPage("form", FormPage.class);
		mountPage("guest-book", GuestBook.class);
		mountPage("lazy-loading", LazyLoadingPage.class);
		mountPage("links", LinksPage.class);
		mountPage("modal-window", ModalWindowPage.class);
		mountPage("on-change-ajax-behavior", OnChangeAjaxBehaviorPage.class);
		mountPage("pageables", PageablesPage.class);
		mountPage("ratings", RatingsPage.class);
		mountPage("tabbed-panel", TabbedPanelPage.class);
		mountPage("todo-list", TodoList.class);
		mountPage("world-clock", WorldClockPage.class);
		mountPage("upload", FileUploadPage.class);

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