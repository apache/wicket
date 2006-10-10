/*
 * $Id$ $Revision:
 * 3905 $ $Date$
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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

/**
 * Monitor thread. This thread listens on the port specified by the STOP.PORT
 * system parameter (defaults to 8079) for request authenticated with the key
 * given by the STOP.KEY system parameter (defaults to "mortbay") for admin
 * requests. Commands "stop" and "status" are currently supported. Based on
 * Monitor from JettyServer (start and stop classes).
 * 
 * @author Eelco Hillenius
 */
public class JettyMonitor extends Thread
{

	/** Listen port. */
	private int monitorPort;

	/** Auth key. */
	private String commKey;

	/** Log. */
	private static final Log log = LogFactory.getLog(JettyMonitor.class);

	/** socket chanel for commands. */
	private ServerSocketChannel serverSocketChanel = null;

	/** JettyServer instantie, zodat we (nog) schoner kunnen afsluiten. */
	private Server server = null;

	/**
	 * Hidden constructor.
	 * 
	 * @param commKey
	 *            auth key
	 * @param monitorPort
	 *            monitor port
	 */
	private JettyMonitor(String commKey, int monitorPort)
	{
		this.commKey = commKey;
		this.monitorPort = monitorPort;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		log.info("Starting Jetty Monitor on port " + monitorPort);
		createServerSocket();
		log.info("Socket created");
		// listen to incomming connections until stop command is issued (method
		// blocks)
		listen();
		try
		{
			// exiting... close server socket
			serverSocketChanel.close();
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		log.info("exiting monitor and VM");
		System.exit(0);
	}

	/**
	 * Create the server socket.
	 */
	private void createServerSocket()
	{
		try
		{
			ServerSocket internalSocket = null;
			InetSocketAddress socketAddress = new InetSocketAddress(InetAddress
					.getByName("127.0.0.1"), monitorPort);
			serverSocketChanel = ServerSocketChannel.open();
			internalSocket = serverSocketChanel.socket();
			internalSocket.bind(socketAddress);
			if (monitorPort == 0)
			{
				monitorPort = internalSocket.getLocalPort();
				log.info("using internal port " + monitorPort);
			}
			if (!"mortbay".equals(commKey))
			{
				commKey = Long.toString((long)(Long.MAX_VALUE * Math.random()), 36);
				log.debug("Using key " + commKey);
			}
		}
		catch (Exception e)
		{
			log.fatal(e.getMessage(), e);
			log.fatal("************ SHUTTING DOWN VM!");
			System.exit(1);
		}
	}

	/**
	 * Listen to incomming commands until stop command is issued (method
	 * blocks).
	 */
	private void listen()
	{
		boolean goOn = true;
		while (goOn)
		{
			Socket socket = null;
			try
			{
				// wait in blocking mode for an incomming socket connection
				SocketChannel socketChanel = serverSocketChanel.accept();
				// we've got a connection here; get concrete socket
				socket = socketChanel.socket();
				// attach reader
				InputStream socketInputStream = socket.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(socketInputStream);
				LineNumberReader lnReader = new LineNumberReader(inputStreamReader);
				String key = lnReader.readLine(); // first line contains auth
				// key
				if (!commKey.equals(key))
				{
					log.warn("Keys '" + commKey + "' and '" + key + "' do not match!");
					continue;
				}

				String cmd = lnReader.readLine(); // second line contains
				// command
				OutputStream socketOutputStream = socket.getOutputStream();
				goOn = handleCommand(cmd, socketOutputStream, goOn);

			}
			catch (Exception e)
			{
				log.error(e.getMessage(), e);
			}
			finally
			{
				if (socket != null)
				{
					try
					{
						socket.close();
					}
					catch (Exception e)
					{
						log.error(e.getMessage());
					}
				}
				socket = null;
			}
		}
	}

	/**
	 * Handle given command.
	 * 
	 * @param cmd
	 *            the command to handle
	 * @param socketOutputStream
	 *            output stream of socket
	 * @param goOn
	 *            current value of goOn
	 * @return value of goOn, possibly changed
	 * @throws IOException
	 */
	private boolean handleCommand(String cmd, OutputStream socketOutputStream, boolean goOn)
			throws IOException
	{
		log.info("handle command '" + cmd + "'");
		if ("stop".equals(cmd))
		{
			handleStopCommand(socketOutputStream);
			goOn = false;
		}
		else if ("status".equals(cmd))
		{
			handleStatusCommand(socketOutputStream);
		}
		return goOn;
	}

	/**
	 * Handle stop command.
	 * 
	 * @param socketOutputStream
	 *            output stream of socket
	 */
	private void handleStopCommand(OutputStream socketOutputStream)
	{
		// write ack
		try
		{
			log.info("sending reply ACK_STOP");
			socketOutputStream.write("ACK_STOP\r\n".getBytes());
			socketOutputStream.flush();
		}
		catch (Exception e)
		{
			log.error("sending acknowledgement of stop command failed:", e);
		}
		try
		{
			server.stop();
			log.info("Jetty server stopped");
		}
		catch (Exception e)
		{
			log.fatal(e.getMessage(), e);
			log.fatal("************* Hard shutdown this server (System.exit)!");
			System.exit(1);
		}
		System.out.flush();
		System.err.flush();
	}

	/**
	 * Handle status command.
	 * 
	 * @param socketOutputStream
	 *            output stream of socket
	 * @throws IOException
	 */
	private void handleStatusCommand(OutputStream socketOutputStream) throws IOException
	{
		if ((server != null) && server.isStarted())
		{
			log.info("sending reply OK");
			socketOutputStream.write("OK\r\n".getBytes());
		}
		else
		{
			if (server == null)
			{
				log.info("Server (still) is null");
			}
			else
			{
				log.info("Server not started yet");
			}
			log.info("sending reply STARTING");
			socketOutputStream.write("STARTING\r\n".getBytes());
		}
		socketOutputStream.flush();
	}

	/**
	 * Starts a new monitor on the given port, holding the given instance of
	 * Jetty. This static method starts a monitor that listens for admin
	 * requests.
	 * 
	 * @param theServer
	 *            instance of Jetty Server
	 * @param commKey
	 *            auth key
	 * @param monitorPort
	 *            port of monitor
	 * @return instance of monitor
	 */
	public static JettyMonitor startMonitor(Server theServer, String commKey, int monitorPort)
	{
		JettyMonitor monitor = new JettyMonitor(commKey, monitorPort);
		monitor.setServer(theServer);
		monitor.setDaemon(true);
		monitor.start();
		return monitor;
	}

	/**
	 * Get server.
	 * 
	 * @return Server Returns the server.
	 */
	public Server getServer()
	{
		return server;
	}

	/**
	 * Set server.
	 * 
	 * @param server
	 *            server to set.
	 */
	public void setServer(Server server)
	{
		this.server = server;
	}
}