/*
 * $Id: RepeatingPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) $
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
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * page that demonstrates a simple repeater view.
 * 
 * @see wicket.extensions.markup.html.repeater.RepeatingView
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

		RepeatingView repeating = new RepeatingView(this, "repeating");

		int index = 0;
		while (contacts.hasNext())
		{
			WebMarkupContainer item = new WebMarkupContainer(repeating, repeating.newChildId());
			Contact contact = (Contact)contacts.next();

			new ActionPanel(item, "actions", new DetachableContactModel(contact));
			new Label(item, "contactid", String.valueOf(contact.getId()));
			new Label(item, "firstname", contact.getFirstName());
			new Label(item, "lastname", contact.getLastName());
			new Label(item, "homephone", contact.getHomePhone());
			new Label(item, "cellphone", contact.getCellPhone());

			final int idx = index;
			item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>()
			{
				@Override
				public String getObject()
				{
					return (idx % 2 == 1) ? "even" : "odd";
				}
			}));

			index++;
		}
	}
}
