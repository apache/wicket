/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ================================================================================
 * Copyright (c) All rechten voorbehouden.
 */
package nl.openedge.util.jetty;

/**
 * Utility class for package local common methods.
 * 
 * @author Eelco Hillenius
 */
final class Util
{
	/** internal const. */
	private static final int NEW_CMD_LENGTH_ARGS = 11;
	/** internal const. */
	private static final int NEW_CMD_LENGTH_NO_ARGS = 7;

	/**
	 * Hidden utility constructor.
	 */
	private Util()
	{
		super();
	}

	/**
	 * Add arguments for command call.
	 * 
	 * @param cmd
	 *            commands and args to add arguments to
	 * @param jettyConfig
	 *            jetty configuration location
	 * @param port
	 *            the port for listening
	 * @param webappContextRoot
	 *            context root
	 * @param contextPath
	 *            context path
	 * @param useJettyPlus
	 *            whether to use JettyPlus
	 * @return String[] new command with arguments
	 */
	public static String[] addCommandArguments(String[] cmd, String jettyConfig, int port,
			String webappContextRoot, String contextPath, boolean useJettyPlus)
	{
		String[] newCmd = null;
		int currentArg = 0;
		int length = cmd.length;
		String classPath = System.getProperty("java.class.path");
		if (jettyConfig != null) // append xml arg
		{
			newCmd = new String[length + NEW_CMD_LENGTH_NO_ARGS];
			currentArg = length;
			System.arraycopy(cmd, 0, newCmd, 0, cmd.length);
			newCmd[currentArg++] = "-classpath";
			newCmd[currentArg++] = classPath;
			newCmd[currentArg++] = JettyStarterPrg.class.getName();
			newCmd[currentArg++] = JettyStarterPrg.CMDARG_XML_CONFIG;
			newCmd[currentArg++] = jettyConfig;
		}
		else
		// append basic args
		{
			newCmd = new String[length + NEW_CMD_LENGTH_ARGS];
			currentArg = length;
			System.arraycopy(cmd, 0, newCmd, 0, cmd.length);
			newCmd[currentArg++] = "-classpath";
			newCmd[currentArg++] = classPath;
			newCmd[currentArg++] = JettyStarterPrg.class.getName();
			newCmd[currentArg++] = JettyStarterPrg.CMDARG_PORT;
			newCmd[currentArg++] = String.valueOf(port);
			newCmd[currentArg++] = JettyStarterPrg.CMDARG_WEBAPP_CONTEXT_ROOT;
			newCmd[currentArg++] = String.valueOf(webappContextRoot);
			newCmd[currentArg++] = JettyStarterPrg.CMDARG_CONTEXT_PATH;
			newCmd[currentArg++] = String.valueOf(contextPath);
		}
		newCmd[currentArg++] = JettyStarterPrg.CMDARG_USE_JETTY_PLUS;
		newCmd[currentArg++] = String.valueOf(useJettyPlus);

		return newCmd;
	}
}