/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit;

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
		super();
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

}
