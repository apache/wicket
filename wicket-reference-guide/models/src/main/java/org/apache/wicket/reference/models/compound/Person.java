package org.apache.wicket.reference.models.compound;

import java.io.Serializable;

//#classOnly
public class Person implements Serializable
{
	String name;
	Integer age;
	Address address;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getAge()
	{
		return age;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}

	public Address getAddress()
	{
		return address;
	}

	public void setAddress(Address address)
	{
		this.address = address;
	}
}
//#classOnly
