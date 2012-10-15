package org.apache.wicket.core.util.objects.checker;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for AbstractObjectChecker
 */
public class AbstractObjectCheckerTest extends Assert
{
	@Test
	public void doCheckIsNotCalledForExcludedTypes()
	{
		List exclusions = Arrays.asList(CharSequence.class);

		IObjectChecker checker = new AbstractObjectChecker(exclusions)
		{
			@Override
			protected Result doCheck(Object object)
			{
				throw new AssertionError("Must not be called");
			}
		};

		IObjectChecker.Result result = checker.check("A String. It's type is excluded by CharSequence");
		assertEquals(IObjectChecker.Result.SUCCESS, result);
	}
}
