// $Id: $
package org.apache.wicket.guice;

public class TestService implements ITestService
{
	public String getString()
	{
		return EXPECTED_RESULT;
	}
}
