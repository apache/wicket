/*
 * $Id: JettyExternalVMStartupWorker.java 3905 2006-01-19 20:34:20 +0000 (Thu,
 * 19 Jan 2006) jdonnerstag $ $Revision$ $Date: 2006-01-19 20:34:20 +0000
 * (Thu, 19 Jan 2006) $
 * 
 * ====================================================================
 * Copyright (c) 2003, Open Edge B.V. All rights reserved. Redistribution and
 * use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of OpenEdge B.V. nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package nl.openedge.util.jetty;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Worker thread for starting Jetty in a seperate VM.
 */
class JettyExternalVMStartupWorker extends Thread
{
	/** Log. */
	private static final Log log = LogFactory.getLog(JettyExternalVMStartupWorker.class);

	/** start command. */
	private String[] startCommand;

	/**
	 * Has Jetty been started (and do we know about it).
	 */
	private boolean jettyStarted = false;

	/**
	 * Pointer to the external proces.
	 */
	private Process process = null;

	/**
	 * Maximum number of ping tries.
	 */
	private int maxTries;

	/**
	 * Miliseconds to wait between ping tries.
	 */
	private long sleepBetweenTries;

	/** command port. */
	private int monitorPort;

	/** auth key. */
	private String commKey;

	/**
	 * Construct worker with parameters.
	 * 
	 * @param startCommand
	 *            start command
	 * @param monitorPort
	 *            monitor port to ping
	 * @param commKey
	 *            auth key
	 * @param maxTries
	 *            maximum number of ping tries
	 * @param sleepBetweenTries
	 *            miliseconds to wait between ping tries
	 */
	public JettyExternalVMStartupWorker(String[] startCommand, int monitorPort, String commKey,
			int maxTries, long sleepBetweenTries)
	{
		this.startCommand = startCommand;
		this.monitorPort = monitorPort;
		this.commKey = commKey;
		this.maxTries = maxTries;
		this.sleepBetweenTries = sleepBetweenTries;
	}

	/**
	 * Ping Jetty tot succes of max tries.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{

		try
		{
			// start Jetty in another VM
			startExternalJettyInstance();
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}

		// ping the remote monitor for startup of Jetty
		jettyStarted = JettyHelper.pingMonitorForServerStarted(commKey, "127.0.0.1", monitorPort,
				maxTries, sleepBetweenTries);
		// if we get here, jetty started up successfully
		log.info("monitor acknowledged Jetty startup");
	}

	/**
	 * Start Jetty with OS call.
	 * 
	 * @throws IOException
	 *             see exception
	 */
	private void startExternalJettyInstance() throws IOException
	{
		log.info("execute command " + printCommand(startCommand));
		process = Runtime.getRuntime().exec(startCommand);

		// NOTE: output will only be there for the top-level proces; if the
		// command opens another window for instance, output will be in that
		// window and not to the process streams.
		connectOutput(process);
	}

	/**
	 * Print command.
	 * 
	 * @param command
	 *            the command
	 * @return String to command as a plain string
	 */
	private String printCommand(String[] command)
	{
		StringBuffer b = new StringBuffer();
		for (String element : command)
		{
			b.append(element).append(" ");
		}
		return b.toString();
	}

	/**
	 * Connect output of process.
	 * 
	 * @param processToConnect
	 *            the process
	 */
	private void connectOutput(Process processToConnect)
	{
		InputStream errInput = processToConnect.getErrorStream();
		LogConnector errConn = new LogConnector();
		errConn.setInputStream(errInput);
		errConn.start();
		InputStream outInput = processToConnect.getInputStream();
		LogConnector outConn = new LogConnector();
		outConn.setInputStream(outInput);
		outConn.start();
	}

	/**
	 * Whether Jetty has been started (and do we know about it).
	 * 
	 * @return boolean whether Jetty has been started (that we know).
	 */
	public boolean isJettyStarted()
	{
		return jettyStarted;
	}

	/**
	 * Get process.
	 * 
	 * @return Process Returns the process.
	 */
	public Process getProcess()
	{
		return process;
	}
}