package org.apache.wicket.model.lambda;

import java.io.Serializable;

/**
 * A test object for lambda related tests
 */
public class Address implements Serializable
{
	private String street;

	private int number;

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}
}
