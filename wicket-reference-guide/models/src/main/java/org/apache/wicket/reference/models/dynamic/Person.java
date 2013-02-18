package org.apache.wicket.reference.models.dynamic;

import java.io.Serializable;

//#classOnly
public class Person implements Serializable
{
	String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
//#classOnly
