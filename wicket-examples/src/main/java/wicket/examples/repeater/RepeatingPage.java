/*
 * $Id$ $Revision:
 * 4054 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.repeater;

import java.util.Iterator;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.repeater.RepeatingView;
import wicket.model.AbstractReadOnlyModel;

/**
 * page that demonstrates a simple repeater view.
 * 
 * @author igor
 */
public class RepeatingPage extends BasePage
{
	/**
	 * Constructor
	 */
	public RepeatingPage()
	{
		Iterator contacts = new ContactDataProvider().iterator(0, 10);

		RepeatingView repeating = new RepeatingView("repeating");
		add(repeating);

		int index = 0;
		while (contacts.hasNext())
		{
			WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
			repeating.add(item);
			Contact contact = (Contact)contacts.next();

			item.add(new ActionPanel("actions", new DetachableContactModel(contact)));
			item.add(new Label("contactid", String.valueOf(contact.getId())));
			item.add(new Label("firstname", contact.getFirstName()));
			item.add(new Label("lastname", contact.getLastName()));
			item.add(new Label("homephone", contact.getHomePhone()));
			item.add(new Label("cellphone", contact.getCellPhone()));

			final int idx = index;
			item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
			{
				public Object getObject(Component component)
				{
					return (idx % 2 == 1) ? "even" : "odd";
				}
			}));

			index++;
		}
	}
}
