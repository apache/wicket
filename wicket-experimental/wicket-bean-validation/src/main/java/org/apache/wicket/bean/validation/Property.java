package org.apache.wicket.bean.validation;

import java.io.Serializable;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.reference.ClassReference;

/**
 * A reference to a property that can be validated.
 * 
 * @author igor
 */
public final class Property implements Serializable
{
	private final ClassReference<?> owner;
	private final String name;

	public Property(ClassReference<?> owner, String name)
	{
		Args.notNull(owner, "owner");
		Args.notEmpty(name, "name");

		this.owner = owner;
		this.name = name;
	}

	public Property(Class<?> owner, String name)
	{
		this(ClassReference.of(owner), name);
	}

	public Class<?> getOwner()
	{
		return owner.get();
	}

	public String getName()
	{
		return name;
	}
}
