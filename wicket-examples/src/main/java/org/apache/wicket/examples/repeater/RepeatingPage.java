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

import java.util.Iterator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;


/**
 * page that demonstrates a simple repeater view.
 * 
 * @author igor
 */
public class RepeatingPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public RepeatingPage()
	{
		Iterator<Contact> contacts = new ContactDataProvider().iterator(0, 10);

		RepeatingView repeating = new RepeatingView("repeating");
		add(repeating);

		int index = 0;
		while (contacts.hasNext())
		{
			AbstractItem item = new AbstractItem(repeating.newChildId());

			repeating.add(item);
			Contact contact = contacts.next();

			item.add(new ActionPanel("actions", new DetachableContactModel(contact)));
			item.add(new Label("contactid", String.valueOf(contact.getId())));
			item.add(new Label("firstname", contact.getFirstName()));
			item.add(new Label("lastname", contact.getLastName()));
			item.add(new Label("homephone", contact.getHomePhone()));
			item.add(new Label("cellphone", contact.getCellPhone()));

			final int idx = index;
			item.add(AttributeModifier.replace("class", new IModel<String>()
			{
				private static final long serialVersionUID = 1L;

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
