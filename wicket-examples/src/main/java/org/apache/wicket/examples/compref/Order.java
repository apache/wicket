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

import java.io.Serializable;

/**
 * An order of a {@link Customer}, used by the {@link MultipleModelsPage} example.
 *
 * @author reiern70
 */
public class Order implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String number;

	private final int amount;

	private final boolean paid;

	/**
	 * Construct.
	 *
	 * @param number
	 *            the order number
	 * @param amount
	 *            the order amount, in whole euros
	 * @param paid
	 *            whether the order has been paid
	 */
	public Order(String number, int amount, boolean paid)
	{
		this.number = number;
		this.amount = amount;
		this.paid = paid;
	}

	/**
	 * @return the order number
	 */
	public String getNumber()
	{
		return number;
	}

	/**
	 * @return the order amount, in whole euros
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * @return whether the order has been paid
	 */
	public boolean isPaid()
	{
		return paid;
	}
}
