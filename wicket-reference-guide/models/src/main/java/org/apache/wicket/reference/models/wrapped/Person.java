package org.apache.wicket.reference.models.wrapped;

import java.io.Serializable;

//#classOnly
public class Person implements Serializable
{
	String name;
	int age;
	Address address;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
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
