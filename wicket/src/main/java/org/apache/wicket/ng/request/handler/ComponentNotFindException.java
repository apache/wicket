package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.WicketRuntimeException;

public class ComponentNotFindException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	public ComponentNotFindException()
	{
	}

	public ComponentNotFindException(String message)
	{
		super(message);
	}

	public ComponentNotFindException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ComponentNotFindException(Throwable cause)
	{
		super(cause);
	}

}
