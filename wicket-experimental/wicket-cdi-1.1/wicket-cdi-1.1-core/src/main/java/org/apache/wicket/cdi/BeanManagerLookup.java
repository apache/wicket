package org.apache.wicket.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class BeanManagerLookup
{
	private BeanManagerLookup()
	{
	}

	public static BeanManager lookup() {
		try
		{
			return InitialContext.doLookup("java:comp/BeanManager");
		}
		catch (NamingException e)
		{
			return CDI.current().getBeanManager();
		}
	}
}
