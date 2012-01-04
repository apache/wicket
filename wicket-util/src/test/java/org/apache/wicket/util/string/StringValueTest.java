package org.apache.wicket.util.string;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class StringValueTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4309
	 */
	@Test
	public void toOptionalXyzWithEmptyString()
	{
		StringValue sv = new StringValue("");
		assertNull(sv.toOptionalBoolean());
		assertNull(sv.toOptionalCharacter());
		assertNull(sv.toOptionalDouble());
		assertNull(sv.toOptionalDuration());
		assertNull(sv.toOptionalInteger());
		assertNull(sv.toOptionalLong());
		assertEquals("", sv.toOptionalString());
		assertNull(sv.toOptionalTime());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4309
	 */
	@Test
	public void toOptionalXyzWithNull()
	{
		StringValue sv = new StringValue(null);
		assertNull(sv.toOptionalBoolean());
		assertNull(sv.toOptionalCharacter());
		assertNull(sv.toOptionalDouble());
		assertNull(sv.toOptionalDuration());
		assertNull(sv.toOptionalInteger());
		assertNull(sv.toOptionalLong());
		assertNull(sv.toOptionalString());
		assertNull(sv.toOptionalTime());
	}
}
