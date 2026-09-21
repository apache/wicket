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

import java.util.Arrays;
import java.util.List;

/**
 * Stands in for the data store of the {@link MultipleModelsPage} example.
 *
 * @author reiern70
 */
public class CustomerRepository
{
	private static final List<Customer> CUSTOMERS = Arrays.asList(
		new Customer(1, "Wile E. Coyote"), new Customer(2, "Road Runner"));

	private CustomerRepository()
	{
	}

	/**
	 * @return all customers
	 */
	public static List<Customer> getCustomers()
	{
		return CUSTOMERS;
	}

	/**
	 * @param id
	 *            the customer id
	 * @return the customer with the given id
	 */
	public static Customer getCustomer(long id)
	{
		return CUSTOMERS.stream()
			.filter(customer -> customer.getId() == id)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("no customer " + id));
	}

	/**
	 * @param customerId
	 *            the customer id
	 * @return the orders of the given customer
	 */
	public static List<Order> getOrders(long customerId)
	{
		if (customerId == 1)
		{
			return Arrays.asList(new Order("ACME-1", 120, true), new Order("ACME-2", 45, false),
				new Order("ACME-3", 980, false));
		}
		return Arrays.asList(new Order("RR-7", 15, true));
	}
}
