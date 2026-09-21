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
package org.apache.wicket.examples.compref;

import java.util.stream.Collectors;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Page with an example of a component using more models than its default model, see
 * {@link CustomerCardPanel}. Its models are registered with
 * {@link org.apache.wicket.Component#addAdditionalModel(org.apache.wicket.model.IModel)} and
 * detached by the component itself.
 *
 * @author reiern70
 */
public class MultipleModelsPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private long customerId = 1;

	/**
	 * Constructor
	 */
	public MultipleModelsPage()
	{
		CustomerCardPanel card = new CustomerCardPanel("card",
			LoadableDetachableModel.of(() -> CustomerRepository.getCustomer(customerId)));
		add(card);

		add(new ListView<Customer>("customers", CustomerRepository.getCustomers())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Customer> item)
			{
				item.add(new Link<Void>("select")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
						customerId = item.getModelObject().getId();
					}
				}.setBody(() -> item.getModelObject().getName()));
			}
		});

		add(new Link<Void>("unpaidOnly")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				// replaceAdditionalModel detaches and unregisters the model the panel used
				// before, so this link can be clicked as often as one likes.
				card.setOrdersModel(LoadableDetachableModel.of(
					() -> CustomerRepository.getOrders(customerId)
						.stream()
						.filter(order -> !order.isPaid())
						.collect(Collectors.toList())));
			}
		});

		add(new Link<Void>("allOrders")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				card.setOrdersModel(
					LoadableDetachableModel.of(() -> CustomerRepository.getOrders(customerId)));
			}
		});
	}

	@Override
	protected void explain()
	{
		String html = "<div wicket:id=\"card\">customer card</div>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public CustomerCardPanel(String id, IModel&lt;Customer&gt; customer)\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;{\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;super(id, customer);\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;titleModel = addAdditionalModel(wrap(new ResourceModel(\"customer.card.title\")));\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ordersModel = addAdditionalModel(LoadableDetachableModel.of(...));\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;unpaidTotalModel = addAdditionalModel(LoadableDetachableModel.of(...));\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;}\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;public void setOrdersModel(IModel&lt;List&lt;Order&gt;&gt; ordersModel)\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;{\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this.ordersModel = replaceAdditionalModel(this.ordersModel, ordersModel);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;}\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// no onDetach(): the registered models are detached with the default model";
		add(new ExplainPanel(html, code));
	}
}
