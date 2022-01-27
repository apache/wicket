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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxNewWindowNotifyingBehavior;
import org.apache.wicket.application.IComponentInitializationListener;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.ajax.builtin.modal.ModalDialogPage;
import org.apache.wicket.markup.html.WebPage;


/**
 * Application object for the wicked ajax examples
 */
public class AjaxApplication extends WicketExampleApplication
{
	@Override
	protected void init()
	{
		super.init();

		getApplicationSettings().setUploadProgressUpdatesEnabled(true);

		getResourceSettings().setThrowExceptionOnMissingResource(false);

		getComponentInitializationListeners().add(new IComponentInitializationListener()
		{
			@Override
			public void onInitialize(Component component)
			{
				if (component instanceof WebPage) {
					component.add(new AjaxNewWindowNotifyingBehavior());
				}
			}
		});

		mountPage("autocomplete", AutoCompletePage.class);
		mountPage("choice", ChoicePage.class);
		mountPage("clock", ClockPage.class);
		mountPage("editable-label", EditableLabelPage.class);
		mountPage("effects", EffectsPage.class);
		mountPage("form", FormPage.class);
		mountPage("guest-book", GuestBook.class);
		mountPage("lazy-loading", LazyLoadingPage.class);
		mountPage("links", LinksPage.class);
		mountPage("modal-dialog", ModalDialogPage.class);
		mountPage("on-change-ajax-behavior", OnChangeAjaxBehaviorPage.class);
		mountPage("pageables", PageablesPage.class);
		mountPage("ratings", RatingsPage.class);
		mountPage("tabbed-panel", TabbedPanelPage.class);
		mountPage("todo-list", TodoList.class);
		mountPage("world-clock", WorldClockPage.class);
		mountPage("upload", FileUploadPage.class);
		mountPage("download", AjaxDownloadPage.class);

		mountResource("dynamic-text-file", AjaxDownloadPage.DynamicTextFileResource.instance);
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
