/*
 * $Id: JettyHelperException.java 3905 2006-01-19 20:34:20 +0000 (Thu, 19 Jan
 * 2006) jdonnerstag $ $Revision$ $Date: 2006-01-19 20:34:20 +0000 (Thu,
 * 19 Jan 2006) $
 * 
 * ================================================================================
 * Copyright (c) All rechten voorbehouden.
 */
package nl.openedge.util.jetty;

/**
 * Exceptions that can be thrown by the JettyMonitor.
 */
public class JettyHelperException extends Exception
{

	/**
	 * Construct.
	 */
	public JettyHelperException()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 *            exception message
	 */
	public JettyHelperException(String message)
	{
		super(message);
	}

	/**
	 * Construct.
	 * 
	 * @param cause
	 *            exception cause
	 */
	public JettyHelperException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public JettyHelperException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
