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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * A panel that works with four models: the customer it is given as its default model, and three
 * models registered beside it with {@link #addAdditionalModel(IModel)}. The registered models are
 * detached together with the default model at the end of each request, so this panel does not
 * override {@link #onDetach()} at all.
 *
 * @author reiern70
 */
public class CustomerCardPanel extends GenericPanel<Customer>
{
	private static final long serialVersionUID = 1L;

	/** the title, an IComponentAssignedModel and therefore registered wrapped */
	private final IModel<String> titleModel;

	/** the orders shown, replaceable through {@link #setOrdersModel(IModel)} */
	private IModel<List<Order>> ordersModel;

	/** derived from {@link #ordersModel}, loaded at most once per request */
	private final IModel<Integer> unpaidTotalModel;

	/** counts how often the orders were loaded, to show that they are detached every request */
	private int orderLoads;

	/**
	 * Construct.
	 *
	 * @param id
	 *            component id
	 * @param customer
	 *            the customer to show, the default model
	 */
	public CustomerCardPanel(String id, IModel<Customer> customer)
	{
		super(id, customer);

		// A ResourceModel is an IComponentAssignedModel: unlike the default model an additional
		// model is registered as it is given, so it is wrap()ed here to bind it to this panel.
		titleModel = addAdditionalModel(wrap(new ResourceModel("customer.card.title")));

		// Each model is registered in the statement that assigns it, addAdditionalModel returns
		// what it is given.
		ordersModel = addAdditionalModel(new LoadableDetachableModel<List<Order>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected List<Order> load()
			{
				orderLoads++;
				return CustomerRepository.getOrders(getModelObject().getId());
			}
		});

		unpaidTotalModel = addAdditionalModel(LoadableDetachableModel
			.of(() -> ordersModel.getObject()
				.stream()
				.filter(order -> !order.isPaid())
				.mapToInt(Order::getAmount)
				.sum()));
	}

	/**
	 * Replaces the orders shown by this panel. {@link #replaceAdditionalModel(IModel, IModel)}
	 * detaches and unregisters the previous model before it registers the given one.
	 *
	 * @param ordersModel
	 *            the model of the orders to show
	 */
	public void setOrdersModel(IModel<List<Order>> ordersModel)
	{
		this.ordersModel = replaceAdditionalModel(this.ordersModel, ordersModel);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new Label("title", titleModel));
		add(new Label("name", () -> getModelObject().getName()));

		// An Integer is written with the converter of the current locale, so the amounts need
		// no resource of their own.
		add(new Label("unpaidTotal", unpaidTotalModel));

		IModel<Integer> orderLoadsModel = () -> orderLoads;
		add(new Label("orderLoads",
			new StringResourceModel("order.loads", this).setParameters(orderLoadsModel)));

		// Reads through the field, so a model set with setOrdersModel() is picked up.
		add(new ListView<Order>("orders", () -> ordersModel.getObject())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Order> item)
			{
				item.add(new Label("number", () -> item.getModelObject().getNumber()));
				item.add(new Label("amount", () -> item.getModelObject().getAmount()));
				// The key is taken from the model: order.status.true or order.status.false.
				item.add(new Label("status",
					new StringResourceModel("order.status.${paid}", item.getModel())));
			}
		});

		// getModels() returns the default model followed by the registered ones, without
		// initializing a model that is not there yet.
		add(new ListView<String>("models",
			() -> CustomerCardPanel.this.getModels()
				.stream()
				.map(model -> model.getClass().getSimpleName())
				.collect(Collectors.toList()))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item)
			{
				item.add(new Label("model", item.getModel()));
			}
		});
	}
}
