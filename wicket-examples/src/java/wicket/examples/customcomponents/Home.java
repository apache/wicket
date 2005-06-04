/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.customcomponents;

import java.util.Date;

import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.datepicker.DatePicker;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.TextField;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.Model;

/**
 * This example's Home page.
 *
 * @author Eelco Hillenius
 */
public class Home extends WicketExamplePage
{
	/**
	 * Constructor.
	 */
	public Home()
	{
		final Book book = new Book();
		BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(book);
		setModel(model);

		add(new OnChangeTextField("title"));
		add(new OnChangeTextField("author"));
		add(new Label("booksumm", new Model()
		{
			public Object getObject(wicket.Component component)
			{
				return book.getTitle() + " by " + book.getAuthor();
			};
		}));

		TextField dateField1 = new TextField("date1", new Model(new Date()), Date.class);
		add(dateField1);
		add(new DatePicker("datePicker1", dateField1));

		TextField dateField2 = new TextField("date2", new Model(new Date()), Date.class);
		add(dateField2);
		add(new DatePicker("datePicker2", dateField2, DatePicker.BUTTON_ICON_2));

		TextField dateField3 = new TextField("date3", new Model(new Date()), Date.class);
		add(dateField3);
		add(new DatePicker("datePicker3", dateField2, DatePicker.BUTTON_ICON_3));
	}
}