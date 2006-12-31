/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.compref;

import java.io.Serializable;
import java.util.Date;

/**
 * A person.
 * 
 * @author Eelco Hillenius
 */
public class Person implements Serializable
{
	private String name;
	private String lastName;
	private Date dateOfBirth;
	private Address address;

	/**
	 * Construct.
	 */
	public Person()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name
	 * @param lastName
	 *            last name
	 */
	public Person(final String name, final String lastName)
	{
		this.name = name;
		this.lastName = lastName;
	}

	/**
	 * Gets the dateOfBirth.
	 * 
	 * @return dateOfBirth
	 */
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	/**
	 * Sets the dateOfBirth.
	 * 
	 * @param dateOfBirth
	 *            dateOfBirth
	 */
	public void setDateOfBirth(final Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * Gets the lastName.
	 * 
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the lastName.
	 * 
	 * @param lastName
	 *            lastName
	 */
	public void setLastName(final String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * Gets the name.
	 * 
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            name
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * Gets the address.
	 * 
	 * @return address
	 */
	public Address getAddress()
	{
		return address;
	}

	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            address
	 */
	public void setAddress(final Address address)
	{
		this.address = address;
	}

	/**
	 * @return full name of the person
	 */
	public String getFullName()
	{
		return name + " " + lastName;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Person name=" + name + ", lastName=" + lastName + ", dateOfBirth=" + dateOfBirth
				+ "]";
	}
}