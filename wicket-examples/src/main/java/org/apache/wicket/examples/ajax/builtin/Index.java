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

import java.util.List;

import org.apache.wicket.examples.AjaxEngineSelector;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.ajax.builtin.modal.ModalDialogPage;
import org.apache.wicket.examples.homepage.HomePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * Wicket ajax example index page
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public class Index extends BasePage
{
	private static final List<Class<? extends BasePage>> EXAMPLES = List.of(
		AutoCompletePage.class, ChoicePage.class, ClockPage.class, EditableLabelPage.class,
		EffectsPage.class, FormPage.class, GuestBook.class, LazyLoadingPage.class,
		LinksPage.class, FileUploadPage.class, ModalDialogPage.class,
		OnChangeAjaxBehaviorPage.class, PageablesPage.class, RatingsPage.class,
		TabbedPanelPage.class, TodoList.class, WorldClockPage.class, AjaxDownloadPage.class);

	/**
	 * Constructor.
	 */
	public Index()
	{
		IModel<List<Class<? extends BasePage>>> examples = () -> EXAMPLES.stream()
			.filter(Index::isAvailable)
			.toList();

		add(new ListView<>("examples", examples)
		{
			@Override
			protected void populateItem(ListItem<Class<? extends BasePage>> item)
			{
				Class<? extends BasePage> page = item.getModelObject();
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", page);
				link.add(new Label("title", new ResourceModel(titleKey(page))));
				item.add(link);
				item.add(new Label("description",
					new ResourceModel(page.getSimpleName() + ".description")));
			}
		});
	}

	private static boolean isAvailable(Class<? extends BasePage> page)
	{
		// EffectsPage needs jQuery UI, which the plain JavaScript Ajax engine does not load
		return page != EffectsPage.class ||
			AjaxEngineSelector.getEffectiveEngine() == AjaxEngineSelector.Engine.JQUERY;
	}

	@Override
	protected Class<? extends WicketExamplePage> getBackPage() {
		return HomePage.class;
	}

}