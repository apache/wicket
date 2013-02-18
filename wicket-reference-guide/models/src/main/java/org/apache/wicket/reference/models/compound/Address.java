package org.apache.wicket.reference.models.compound;

import java.io.Serializable;

//#classOnly
public class Address implements Serializable
{
	String city;

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}
}
//#classOnly

