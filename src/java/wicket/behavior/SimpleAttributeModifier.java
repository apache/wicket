/*
 * Copyright Teachscape
 */
package wicket.behavior;

import wicket.Component;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;

/**
 * A light weight version of the attribute modifier. This is convenient for
 * simpler situations where you know the value upfront and you do not need a
 * push-based enable method.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class SimpleAttributeModifier extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;

	private String attr;
	private String value;

	/**
	 * Construct.
	 * 
	 * @param attr
	 * @param value
	 */
	public SimpleAttributeModifier(String attr, String value)
	{
		if (attr == null)
		{
			throw new IllegalArgumentException("argument [attr] cannot be null");
		}
		if (value == null)
		{
			throw new IllegalArgumentException("argument [value] cannot be null");
		}
		this.attr = attr;
		this.value = value;
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(Component component, ComponentTag tag)
	{
		if (isEnabled())
		{
			tag.getAttributes().put(attr, value);
		}
	}

	/**
	 * @return true to enable the modifier, false to disable
	 */
	protected boolean isEnabled()
	{
		return true;
	}
}
