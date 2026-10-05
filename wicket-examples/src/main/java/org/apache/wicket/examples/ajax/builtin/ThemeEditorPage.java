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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.clipboard.ClipboardCopyBehavior;
import org.apache.wicket.extensions.markup.html.collapsible.CollapsiblePanel;
import org.apache.wicket.extensions.markup.html.progress.ProgressBar;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabsStyle;
import org.apache.wicket.extensions.markup.html.tabs.TabsStyleBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.string.Strings;

import com.github.openjson.JSONObject;

/**
 * Edits the colors of a theme of wicket-extensions in the browser: a picker per theme property, a
 * preview of themed components following every change, and the CSS class defining the theme, to
 * copy into an application's style sheet.
 */
public class ThemeEditorPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * The properties a theme sets, in the order of wicket-theme.css.
	 */
	static final List<String> PROPERTIES = List.of("primary", "primary-dark", "on-primary",
		"accent", "text", "surface", "surface-alt", "hover", "selected", "highlight", "toolbar",
		"border", "progress", "progress-dark", "track", "veil", "color-scheme");

	private final WebMarkupContainer preview;

	private final ModalDialog dialog;

	/**
	 * Constructor.
	 */
	public ThemeEditorPage()
	{
		preview = newThemedContainer("preview");
		add(preview);
		add(newThemeChoice("theme", preview));

		Form<Void> editor = new Form<>("editor");
		editor.setOutputMarkupId(true);
		add(editor);
		editor.add(new ListView<>("properties", PROPERTIES)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item)
			{
				String property = item.getModelObject();
				item.add(new Label("name", "--wicket-theme-" + property));
				item.add(new Label("description",
					new ResourceModel("property." + property)));
				item.add(newInput("input", property));
			}
		});
		editor.add(new WebMarkupContainer("copy"));

		preview.add(new ProgressBar("bar", Model.of(62)));
		preview.add(new ProgressBar("indeterminate", Model.of((Integer)null)));
		preview.add(new CollapsiblePanel("collapsible", new ResourceModel("collapsible.title"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String id)
			{
				return new Label(id, new ResourceModel("collapsible.body"));
			}
		}.setExpanded(true));
		preview.add(newTabs("tabs"));
		preview.add(new AutoCompleteTextField<String>("country", Model.of(""))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<String> getChoices(String input)
			{
				return countries(input);
			}
		});

		dialog = new ModalDialog("dialog");
		dialog.add(new DefaultTheme());
		dialog.closeOnEscape();
		preview.add(dialog);
		preview.add(new AjaxLink<Void>("openDialog")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				dialog.open(new DialogContent(ModalDialog.CONTENT_ID), target);
			}
		});
	}

	@Override
	protected void onThemeChanged(AjaxRequestTarget target)
	{
		target.appendJavaScript("Wicket.ThemeEditor.load();");
	}

	private Component newInput(String id, String property)
	{
		Fragment input;
		if ("veil".equals(property))
		{
			input = new Fragment(id, "text", this);
		}
		else if ("color-scheme".equals(property))
		{
			input = new Fragment(id, "scheme", this);
		}
		else
		{
			input = new Fragment(id, "color", this);
		}
		WebMarkupContainer field = new WebMarkupContainer("field");
		field.add(AttributeModifier.replace("data-theme-property", "--wicket-theme-" + property));
		field.add(AttributeModifier.replace("aria-label", "--wicket-theme-" + property));
		input.add(field);
		return input;
	}

	private static Component newTabs(String id)
	{
		List<ITab> tabs = new ArrayList<>();
		for (String title : List.of("First", "Second", "Third"))
		{
			tabs.add(new AbstractTab(Model.of(title))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public WebMarkupContainer getPanel(String panelId)
				{
					return new TabContent(panelId, title);
				}
			});
		}
		return new AjaxTabbedPanel<>(id, tabs).add(new TabsStyleBehavior(TabsStyle.TABS));
	}

	private static Iterator<String> countries(String input)
	{
		if (Strings.isEmpty(input))
		{
			return Collections.emptyIterator();
		}
		List<String> choices = new ArrayList<>();
		for (Locale locale : Locale.getAvailableLocales())
		{
			String country = locale.getDisplayCountry(Locale.ENGLISH);
			if (choices.contains(country) == false &&
				country.toUpperCase(Locale.ROOT).startsWith(input.toUpperCase(Locale.ROOT)))
			{
				choices.add(country);
			}
		}
		Collections.sort(choices);
		return choices.stream().limit(8).iterator();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		ClipboardCopyBehavior.renderHeadItems(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(ThemeEditorPage.class, "ThemeEditorPage.css")));
		response.render(JavaScriptHeaderItem.forReference(
			new JavaScriptResourceReference(ThemeEditorPage.class, "ThemeEditorPage.js")));
		JSONObject config = new JSONObject();
		config.put("editor", get("editor").getMarkupId());
		config.put("preview", preview.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript("Wicket.ThemeEditor.init(" + config + ");"));
	}

	/**
	 * The content of a tab of the preview.
	 */
	private static class TabContent extends Panel
	{
		private static final long serialVersionUID = 1L;

		TabContent(String id, String title)
		{
			super(id);
			add(new Label("text", title));
		}
	}

	/**
	 * The content of the dialog of the preview.
	 */
	private class DialogContent extends Panel
	{
		private static final long serialVersionUID = 1L;

		DialogContent(String id)
		{
			super(id);
			add(new AjaxLink<Void>("close")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					dialog.close(target);
				}
			});
		}
	}
}
