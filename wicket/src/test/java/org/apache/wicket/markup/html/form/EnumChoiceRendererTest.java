package org.apache.wicket.markup.html.form;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests {@link EnumChoiceRenderer}
 * 
 * @author igor.vaynberg
 */
public class EnumChoiceRendererTest extends TestCase
{

	/**
	 * 
	 */
	public void testResourceKeyGenerationForAnonymousEnums()
	{
		final EnumChoiceRenderer<TestEnum> renderer = new EnumChoiceRenderer<TestEnum>();
		Assert.assertEquals("TestEnum.ANONYMOUS", renderer.resourceKey(TestEnum.ANONYMOUS));
	}

	/**
	 * Enum for testing
	 * 
	 * @author igor.vaynberg
	 */
	public enum TestEnum {
		/** an anonymous enum value */
		ANONYMOUS {
		// anonymous enum value
		}
	}

}
