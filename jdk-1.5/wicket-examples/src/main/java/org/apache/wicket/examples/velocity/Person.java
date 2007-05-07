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
package org.apache.wicket.examples.velocity;

import java.io.Serializable;

/**
 * Simple person object.
 */
public final class Person implements Serializable
{
	private String firstName;

	private String lastName;

	/**
	 * Construct.
	 */
	public Person()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public Person(String firstName, String lastName)
	{
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Gets the firstName.
	 * 
	 * @return firstName
	 */
	public final String getFirstName()
	{
		return firstName;
	}

	/**
	 * Gets the lastName.
	 * 
	 * @return lastName
	 */
	public final String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the firstName.
	 * 
	 * @param firstName
	 *            firstName
	 */
	public final void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * Sets the lastName.
	 * 
	 * @param lastName
	 *            lastName
	 */
	public final void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
}