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
package org.apache.wicket.queueing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.SlowTests;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTests.class)
public class ComponentQueueingPerformanceTest extends WicketTestCase
{
	private void run(Class<? extends Page> pageClass)
	{
		WicketTester tester = new WicketTester(new MockApplication());
		try
		{
			tester.startPage(pageClass);
		}
		finally
		{
			tester.destroy();
		}
	}

	@Test
	public void performance()
	{
		final int warmup = 30;
		final int performance = 60;

		tester.startPage(AddContactsPage.class);

		for (int i = 0; i < warmup; i++)
		{
			run(AddContactsPage.class);
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < performance; i++)
		{
			run(AddContactsPage.class);
		}
		long end = System.currentTimeMillis();
		long addDuration = end - start;

		for (int i = 0; i < warmup; i++)
		{
			run(QueueContactsPage.class);
		}
		start = System.currentTimeMillis();
		for (int i = 0; i < performance; i++)
		{
			run(QueueContactsPage.class);
		}
		end = System.currentTimeMillis();
		long queueDuration = end - start;


		System.out.println("add duration: " + addDuration + " queue duration: " + queueDuration);

	}


	@Test
	public void consistency()
	{
		tester.startPage(new QueueContactsPage());
		String queue = tester.getLastResponseAsString();
		tester.startPage(new AddContactsPage());
		String add = tester.getLastResponseAsString();
		assertEquals(queue, add);
	}

	private static class PhoneNumber
	{
		String id = UUID.randomUUID().toString();
		String areacode = "234";
		String prefix = "342";
		String suffix = "3423";
	}

	private static class Address
	{
		String id = UUID.randomUUID().toString();
		String street = "2343 Jsdfjsf St.";
		String city = "Ksdfjsfs";
		String state = "AS";
		String zipcode = "32434";
	}

	private static class Contact
	{
		String id = UUID.randomUUID().toString();
		String first = "Jlkjsf";
		String last = "Kjwieojkjf";
		Address address = new Address();
		PhoneNumber work = new PhoneNumber();
		PhoneNumber cell = new PhoneNumber();

	}

	static Store store = new Store();

	private static class Store
	{
		Map<String, PhoneNumber> phones = new HashMap<String, PhoneNumber>();
		Map<String, Address> addresses = new HashMap<String, Address>();
		Map<String, Contact> contacts = new HashMap<String, Contact>();

		public <T> T get(Class<T> clazz, String id)
		{
			if (PhoneNumber.class.equals(clazz))
			{
				return (T)phones.get(id);
			}
			else if (Address.class.equals(clazz))
			{
				return (T)addresses.get(id);
			}
			else if (Contact.class.equals(clazz))
			{
				return (T)contacts.get(id);
			}
			throw new RuntimeException();
		}

		public Store()
		{
			for (int i = 0; i < 250; i++)
			{
				Contact contact = new Contact();
				contacts.put(contact.id, contact);
			}
		}

	}

	private static class ContactModel extends LoadableDetachableModel<Contact>
	{
		private String id;

		public ContactModel(Contact contact)
		{
			super(contact);
			this.id = contact.id;
		}

		@Override
		protected Contact load()
		{
			return store.contacts.get(id);
		}

	}

	private static abstract class AbstractPhonePanel extends TestPanel
	{
		public AbstractPhonePanel(String id, IModel<PhoneNumber> phone)
		{
			super(id);
			setPanelMarkup("<wicket:panel><span wicket:id='areacode'></span> <span wicket:id='prefix'></span>-<span wicket:id='suffix'></span></wicket:panel>");
		}
	}

	private static class AddPhonePanel extends AbstractPhonePanel
	{
		public AddPhonePanel(String id, IModel<PhoneNumber> phone)
		{
			super(id, phone);
			add(new Label("areacode", new PropertyModel(phone, "areacode")));
			add(new Label("prefix", new PropertyModel(phone, "prefix")));
			add(new Label("suffix", new PropertyModel(phone, "suffix")));
		}
	}
	private static class QueuePhonePanel extends AbstractPhonePanel
	{
		public QueuePhonePanel(String id, IModel<PhoneNumber> phone)
		{
			super(id, phone);
			queue(new Label("areacode", new PropertyModel(phone, "areacode")));
			queue(new Label("prefix", new PropertyModel(phone, "prefix")));
			queue(new Label("suffix", new PropertyModel(phone, "suffix")));
		}
	}

	private static abstract class AbstractAddressPanel extends TestPanel
	{
		public AbstractAddressPanel(String id, IModel<Address> addr)
		{
			super(id);
			setPanelMarkup("<wicket:panel><span wicket:id='street'></span><br/><span wicket:id='city'></span>, <span wicket:id='state'></span> <span wicket:id='zipcode'></span></wicket:panel>");
		}
	}

	private static class AddAddressPanel extends AbstractAddressPanel
	{
		public AddAddressPanel(String id, IModel<Address> addr)
		{
			super(id, addr);
			add(new Label("street", new PropertyModel(addr, "street")));
			add(new Label("city", new PropertyModel(addr, "city")));
			add(new Label("state", new PropertyModel(addr, "state")));
			add(new Label("zipcode", new PropertyModel(addr, "zipcode")));
		}
	}
	private class QueueAddressPanel extends AbstractAddressPanel
	{
		public QueueAddressPanel(String id, IModel<Address> addr)
		{
			super(id, addr);
			queue(new Label("street", new PropertyModel(addr, "street")));
			queue(new Label("city", new PropertyModel(addr, "city")));
			queue(new Label("sate", new PropertyModel(addr, "state")));
			queue(new Label("zipcode", new PropertyModel(addr, "zipcode")));
		}
	}

	static class AbstractContactsPage extends TestPage
	{
		AbstractContactsPage()
		{
			// @formatter:off
			setPageMarkup(
				"  <div wicket:id='contacts'>"
				+ "  <span wicket:id='first'></span>"
				+ "  <span wicket:id='last'></span>"
				+ "  <div wicket:id='addr'></div>"
				+ "  <div wicket:id='work'></div>"
				+ "  <div wicket:id='cell'></div>"
				+ "</div>");
			// @formatter:on

		}
	}

	public static class AddContactsPage extends AbstractContactsPage
	{
		public AddContactsPage()
		{
			add(new RefreshingView<Contact>("contacts")
			{
				@Override
				protected Iterator<IModel<Contact>> getItemModels()
				{
					return new ModelIteratorAdapter<Contact>(store.contacts.values())
					{
						@Override
						protected IModel<Contact> model(Contact object)
						{
							return new ContactModel(object);
						}
					};
				}

				@Override
				protected void populateItem(Item<Contact> item)
				{
					IModel<Contact> model = item.getModel();
					item.add(new Label("first", new PropertyModel(model, "first")));
					item.add(new Label("last", new PropertyModel(model, "first")));
					item.add(new AddAddressPanel("addr", new PropertyModel<Address>(model, "address")));
					item.add(new AddPhonePanel("work", new PropertyModel<PhoneNumber>(model, "work")));
					item.add(new AddPhonePanel("cell", new PropertyModel<PhoneNumber>(model, "cell")));
				}
			});

		}
	}

	public static class QueueContactsPage extends AbstractContactsPage
	{
		public QueueContactsPage()
		{
			queue(new RefreshingView<Contact>("contacts")
			{
				@Override
				protected Iterator<IModel<Contact>> getItemModels()
				{
					return new ModelIteratorAdapter<Contact>(store.contacts.values())
					{
						@Override
						protected IModel<Contact> model(Contact object)
						{
							return new ContactModel(object);
						}
					};
				}

				@Override
				protected void populateItem(Item<Contact> item)
				{
					IModel<Contact> model = item.getModel();
					item.queue(new Label("first", new PropertyModel(model, "first")));
					item.queue(new Label("last", new PropertyModel(model, "first")));
					item.queue(new AddAddressPanel("addr", new PropertyModel<Address>(model, "address")));
					item.queue(new AddPhonePanel("work", new PropertyModel<PhoneNumber>(model, "work")));
					item.queue(new AddPhonePanel("cell", new PropertyModel<PhoneNumber>(model, "cell")));
				}
			});

		}
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private String markup;

		public TestPage()
		{
		}

		public TestPage(String markup)
		{
			this.markup = markup;
		}

		protected String getPageMarkup()
		{
			return markup;
		}

		public void setPageMarkup(String markup)
		{
			this.markup = markup;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(getPageMarkup());
		}

	}

	private static class TestPanel extends Panel implements IMarkupResourceStreamProvider
	{

		private String markup;

		public TestPanel(String id)
		{
			super(id);
		}

		protected void setPanelMarkup(String markup)
		{
			this.markup = markup;
		}

		protected String getPanelMarkup()
		{
			return markup;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(getPanelMarkup());
		}
	}
}
