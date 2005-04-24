/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit.beans;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple person object.
 */
public class Person implements Serializable
{
	private String name;
	private String lastName;
	private Date dateOfBirth;
	
	/**
	 * Construct.
	 */
	public Person()
	{
	}

	/**
	 * Construct.
	 * @param name name
	 * @param lastName last name
	 */
	public Person(String name, String lastName)
	{
		this.name = name;
		this.lastName = lastName;
	}

	/**
	 * Gets the dateOfBirth.
	 * @return dateOfBirth
	 */
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	/**
	 * Sets the dateOfBirth.
	 * @param dateOfBirth dateOfBirth
	 */
	public void setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * Gets the lastName.
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the lastName.
	 * @param lastName lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * Gets the name.
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @param name name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Person{name=" + name + ",lastName=" + lastName +
			",dateOfBirth=" + dateOfBirth + "}";
	}
}