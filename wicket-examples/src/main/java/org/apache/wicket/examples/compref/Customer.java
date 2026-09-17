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
 * A customer of the {@link MultipleModelsPage} example.
 *
 * @author reiern70
 */
public class Customer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long id;

	private final String name;

	/**
	 * Construct.
	 *
	 * @param id
	 *            the customer id
	 * @param name
	 *            the customer name
	 */
	public Customer(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the customer id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return the customer name
	 */
	public String getName()
	{
		return name;
	}
}
