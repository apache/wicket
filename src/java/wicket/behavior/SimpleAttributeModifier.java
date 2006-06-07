/*
 * Copyright Teachscape
 */
package wicket.behavior;

import wicket.Component;
import wicket.markup.ComponentTag;

/**
 * A lightweight version of the attribute modifier. This is convenient for
 * simpler situations where you know the value upfront and you do not need a
 * pull-based model.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class SimpleAttributeModifier extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;

	/** The attribute */
	private String attribute;

	/** The value to set */
	private CharSequence value;

	/**
	 * Construct.
	 * 
	 * @param attribute
	 *            The attribute
	 * @param value
	 *            The value
	 */
	public SimpleAttributeModifier(final String attribute, final CharSequence value)
	{
		if (attribute == null)
		{
			throw new IllegalArgumentException("Argument [attr] cannot be null");
		}
		if (value == null)
		{
			throw new IllegalArgumentException("Argument [value] cannot be null");
		}
		this.attribute = attribute;
		this.value = value;
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		if (isEnabled())
		{
			tag.getAttributes().put(attribute, value);
		}
	}

	/**
	 * @return True to enable the modifier, false to disable
	 */
	protected boolean isEnabled()
	{
		return true;
	}
}
