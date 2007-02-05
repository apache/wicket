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
package wicket.extensions.yui.calendar;

import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.datetime.markup.html.form.DateTextField;
import wicket.datetime.util.StyleDateConverter;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebComponent;
import wicket.markup.html.panel.Panel;

/**
 * Pops up a YUI calendar component so that the user can select a date. On
 * selection, the date is set in the passed in
 * {@link DateTextField date text field} and the popup is closed again.
 * 
 * @author eelcohillenius
 */
public class CalendarPopup extends Panel {

	/**
	 * Simple img component.
	 */
	private final class Icon extends WebComponent {

		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component id
		 */
		public Icon(String id) {
			super(id);
			setOutputMarkupId(true);
		}

		/**
		 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
		 */
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			tag.put("src", getIconUrl());
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Construct. Contributes packaged dependencies.
	 * 
	 * @param id
	 *            The component id
	 * @param target
	 *            The component that recieves the calendar selections. Bound to
	 *            instances of {@link DateTextField} as this component depends
	 *            on the Joda based {@link StyleDateConverter} being used. Must
	 *            be not null
	 * @throws IllegalArgumentException
	 *             if the target component is null
	 */
	public CalendarPopup(String id, DateTextField target) {
		this(id, target, true);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param target
	 *            The component that recieves the calendar selections. Bound to
	 *            instances of {@link DateTextField} as this component depends
	 *            on the Joda based {@link StyleDateConverter} being used. Must
	 *            be not null.
	 * @param contributeDependencies
	 *            Whether to contribute the packaged dependencies. Pass false in
	 *            case you want to include the dependencies manually in your own
	 *            page, e.g. when you want to keep them in your web application
	 *            dir.
	 * @throws IllegalArgumentException
	 *             if the target component is null
	 */
	public CalendarPopup(String id, final DateTextField target,
			boolean contributeDependencies) {

		super(id);

		if (target == null) {
			throw new IllegalArgumentException(
					"target component must be not null");
		}
		// in case this wasn't set yet, set it now
		target.setOutputMarkupId(true);

		// add the icon that triggers popping up the widget
		final Icon icon = new Icon("icon");
		add(icon);

		final AbstractCalendar calendar = new AbstractCalendar("cal",
				contributeDependencies) {

			private static final long serialVersionUID = 1L;

			protected void appendToInit(String markupId, String javascriptId,
					String javascriptWidgetId, StringBuffer b) {

				super.appendToInit(markupId, javascriptId, javascriptWidgetId,
						b);

				// not pretty to look at, but cheaper than using a template
				String iconId = icon.getMarkupId();
				// add a listener to the calendar widget that fills in the value
				// of the passed in date text field when a selection is made,
				// after which the widget is hidden again (it starts out hidden)
				b.append("  YAHOO.util.Event.addListener(\"");
				b.append(iconId);
				b.append("\", \"click\", ");
				b.append(javascriptWidgetId);
				b.append(".show, ");
				b.append(javascriptWidgetId);
				b.append(", true);\n");
				b.append("  function selectHandler(type, args, cal) {\n");
				b.append("    var selDateArray = args[0][0];\n");
				b.append("    var yr = selDateArray[0];\n");
				b.append("    var month = selDateArray[1];\n");
				b.append("    var dt = selDateArray[2];\n");
				String datePattern = target.getDateConverter().getDatePattern();
				b.append("    var val = '");
				// use the target component's pattern to fill in the date
				// it's quite rough (e.g. YY is still filled in as YYYY), but
				// should work without problems
				b.append(datePattern);
				b.append("'.replace(/d+/, dt).replace(/M+/, month)");
				b.append(".replace(/y+/, yr);\n    YAHOO.util.Dom.get(\"");
				b.append(target.getMarkupId());
				b.append("\").value = val;\n");
				b.append("    cal.hide();\n}");
				b.append("  ");
				b.append(javascriptWidgetId);
				b.append(".selectEvent.subscribe(selectHandler, ");
				b.append(javascriptWidgetId);
				b.append(");");
			}
		};
		add(calendar);
	}

	/**
	 * Gets the url for the popup button. Users can override
	 * 
	 * @return the url to use for the popup button/ icon
	 */
	protected CharSequence getIconUrl() {
		return RequestCycle.get().urlFor(
				new ResourceReference(CalendarPopup.class, "icon1.gif"));
	}
}
