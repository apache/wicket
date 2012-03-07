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

import org.apache.wicket.util.io.IClusterable;

/**
 * domain object for demonstrations.
 * 
 * @author igor
 * 
 */
public class Contact implements IClusterable
{
	private long id;

	private String firstName;

	private String lastName;

	private String homePhone;

	private String cellPhone;

	/**
	 * Constructor
	 */
	public Contact()
	{

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Contact id=" + id + " firstName=" + firstName + " lastName=" + lastName +
				" homePhone=" + homePhone + " cellPhone=" + cellPhone + "]";
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof Contact)
		{
			Contact other = (Contact)obj;
			return other.getFirstName().equals(getFirstName()) &&
					other.getLastName().equals(getLastName()) &&
					other.getHomePhone().equals(getHomePhone()) &&
					other.getCellPhone().equals(getCellPhone());

		}
		else
		{
			return false;
		}
	}

	/**
	 * @param id
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * @return id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * Constructor
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public Contact(String firstName, String lastName)
	{
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * @return cellPhone
	 */
	public String getCellPhone()
	{
		return cellPhone;
	}

	/**
	 * @param cellPhone
	 */
	public void setCellPhone(String cellPhone)
	{
		this.cellPhone = cellPhone;
	}

	/**
	 * @return firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * @return homePhone
	 */
	public String getHomePhone()
	{
		return homePhone;
	}

	/**
	 * @param homePhone
	 */
	public void setHomePhone(String homePhone)
	{
		this.homePhone = homePhone;
	}

	/**
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * @param lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

}
