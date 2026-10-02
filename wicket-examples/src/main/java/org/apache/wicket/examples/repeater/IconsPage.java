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
package org.apache.wicket.examples.repeater;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IconBasedRowAction;
import org.apache.wicket.extensions.markup.html.clipboard.ClipboardCopyBehavior;
import org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.extensions.markup.html.icon.SvgIcon;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * Shows the icons wicket-extensions offers as {@link IIcon}s, filtered by name: the built-in
 * icons of {@link IconBasedRowAction.Icon}, {@link SvgIcon} and {@link FontAwesomeIcon}. A click
 * on an icon copies its Java constant to the clipboard.
 */
public class IconsPage extends ExamplePage
{
	private static final long serialVersionUID = 1L;

	private static final int MAX_SHOWN = 300;

	/**
	 * The sets of icons.
	 */
	enum IconSet
	{
		SVG(SvgIcon.class, SvgIcon.values()),

		FONT_AWESOME(FontAwesomeIcon.class, FontAwesomeIcon.values()),

		BUILT_IN(IconBasedRowAction.Icon.class, IconBasedRowAction.Icon.values());

		private final Class<?> type;

		private final List<IIcon> icons;

		IconSet(Class<?> type, IIcon[] icons)
		{
			this.type = type;
			this.icons = Arrays.asList(icons);
		}

		/**
		 * @return the icons of the set whose name contains the filter
		 */
		List<IIcon> matching(String filter)
		{
			String term = filter == null ? "" : filter.trim().toUpperCase(Locale.ROOT)
				.replace('-', '_').replace(' ', '_');
			return icons.stream().filter(icon -> ((Enum<?>)icon).name().contains(term)).toList();
		}

		/**
		 * @return the Java constant of an icon, such as {@code SvgIcon.HOUSE}
		 */
		String constantOf(IIcon icon)
		{
			String outer = type.getEnclosingClass() != null
				? type.getEnclosingClass().getSimpleName() + "." : "";
			return outer + type.getSimpleName() + "." + ((Enum<?>)icon).name();
		}
	}

	private IconSet set = IconSet.SVG;

	private String filter;

	/**
	 * Constructor.
	 */
	public IconsPage()
	{
		WebMarkupContainer results = new WebMarkupContainer("results");
		results.setOutputMarkupId(true);
		add(results);

		Form<Void> form = new Form<>("form");
		add(form);
		form.add(new DropDownChoice<>("set", new PropertyModel<>(this, "set"),
			List.of(IconSet.values()), new EnumChoiceRenderer<>(this))
			.setRequired(true)
			.add(new AjaxFormComponentUpdatingBehavior("change")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.add(results);
				}
			}));
		form.add(new TextField<>("filter", new PropertyModel<String>(this, "filter"))
			.add(new OnChangeAjaxBehavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.add(results);
				}
			}));

		results.add(new Label("count", new StringResourceModel("count", this)
			.setParameters((IModel<Integer>)() -> matching().size(),
				(IModel<Integer>)() -> Math.min(MAX_SHOWN, matching().size()))));
		results.add(new ListView<>("icons", () -> matching().stream().limit(MAX_SHOWN).toList())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<IIcon> item)
			{
				IIcon icon = item.getModelObject();
				String constant = set.constantOf(icon);
				item.add(new ClipboardCopyBehavior(Model.of(constant)));
				item.add(AttributeModifier.replace("title", constant));
				item.add(new Label("icon", icon.getMarkup()).setEscapeModelStrings(false));
				item.add(new Label("name", ((Enum<?>)icon).name()));
			}
		});
	}

	private List<IIcon> matching()
	{
		return set.matching(filter);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(FontAwesomeResourceReference.styleSheet());
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(IconsPage.class, "IconsPage.css")));
	}
}
