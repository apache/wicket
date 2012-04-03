package org.apache.wicket.atmosphere;

import com.google.common.base.Predicate;

public class NoFilterPredicate implements Predicate<Object>
{
	@Override
	public boolean apply(Object input)
	{
		return true;
	}
}
