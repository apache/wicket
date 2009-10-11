package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.WicketRuntimeException;

public class ComponentNotFoundException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	public ComponentNotFoundException()
	{
	}

	public ComponentNotFoundException(String message)
	{
		super(message);
	}

	public ComponentNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ComponentNotFoundException(Throwable cause)
	{
		super(cause);
	}

}
