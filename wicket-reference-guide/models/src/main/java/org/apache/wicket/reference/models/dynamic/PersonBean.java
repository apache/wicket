package org.apache.wicket.reference.models.dynamic;

import java.io.Serializable;

public class PersonBean implements Serializable
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
