/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package wicket.examples.beanedit;

import java.io.Serializable;

/**
 * An address.
 */
public class Address implements Serializable
{
	private String address;
	private String postcode;
	private String city;
	private String country;

	/**
	 * Construct.
	 */
	public Address()
	{
	}

	/**
	 * Gets the address.
	 * @return address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Sets the address.
	 * @param address address
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}

	/**
	 * Gets the city.
	 * @return city
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * Sets the city.
	 * @param city city
	 */
	public void setCity(String city)
	{
		this.city = city;
	}

	/**
	 * Gets the country.
	 * @return country
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * Sets the country.
	 * @param country country
	 */
	public void setCountry(String country)
	{
		this.country = country;
	}

	/**
	 * Gets the postcode.
	 * @return postcode
	 */
	public String getPostcode()
	{
		return postcode;
	}

	/**
	 * Sets the postcode.
	 * @param postcode postcode
	 */
	public void setPostcode(String postcode)
	{
		this.postcode = postcode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Address{address=" + address + ",postcode=" + postcode +
			",city=" + city + ",country=" + country + "}";
	}
}
