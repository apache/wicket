/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.test.jetty;

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

import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(JettyMonitor.class);

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
	private JettyMonitor(final String commKey, final int monitorPort)
	{
		this.commKey = commKey;
		this.monitorPort = monitorPort;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
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
			log.error(e.getMessage(), e);
			log.error("************ SHUTTING DOWN VM!");
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
	private boolean handleCommand(final String cmd, final OutputStream socketOutputStream, boolean goOn)
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
	private void handleStopCommand(final OutputStream socketOutputStream)
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
			log.error(e.getMessage(), e);
			log.error("************* Hard shutdown this server (System.exit)!");
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
	private void handleStatusCommand(final OutputStream socketOutputStream) throws IOException
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
	public static JettyMonitor startMonitor(final Server theServer, final String commKey, final int monitorPort)
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
	public void setServer(final Server server)
	{
		this.server = server;
	}
}