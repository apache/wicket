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
import wicket.markup.html.basic.Label;
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

		add(new DatePicker("datePicker", new Model(new Date())));
	}
}