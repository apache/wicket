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
package org.apache.wicket.examples.stockquote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Provides access to a SOAP service for getting stock quotes based on a symbol. Found on
 * http://www.devx.com/Java/Article/27559/0/page/2
 */
public class StockQuote
{
	/**
	 * We used to use the www.xmethods.com demo webservice for stockquotes. We now use webservicex,
	 * as xmethods was really overloaded and unreliable.
	 */
	private static final String serviceUrl = "http://www.webservicex.net/stockquote.asmx";

	/** the symbol to get the quote for. */
	private String symbol;

	/**
	 * Default constructor.
	 */
	public StockQuote()
	{
	}

	/**
	 * Constructor setting the symbol to get the quote for.
	 * 
	 * @param symbol
	 *            the symbol to look up
	 */
	public StockQuote(String symbol)
	{
		this.symbol = symbol;
	}

	/**
	 * Gets the symbol.
	 * 
	 * @return the symbol
	 */
	public String getSymbol()
	{
		return symbol;
	}

	/**
	 * Sets the symbol for getting the quote.
	 * 
	 * @param symbol
	 */
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	/**
	 * Gets a stock quote for the given symbol
	 * 
	 * @return the stock quote
	 */
	public String getQuote()
	{
		final String response = getSOAPQuote(symbol);

		// make sure we get
		int start = response.indexOf("&lt;Last&gt;") + "&lt;Last&gt;".length();
		int end = response.indexOf("&lt;/Last&gt;");

		// if the string returned isn't valid, just return empty.
		if (start < "&lt;Last&gt;".length())
		{
			return "(unknown)";
		}
		String result = response.substring(start, end);
		return result.equals("0.00") ? "(unknown)" : result;
	}

	/**
	 * Calls the SOAP service to get the stock quote for the symbol.
	 * 
	 * @param symbol
	 *            the name to search for
	 * @return the SOAP response containing the stockquote
	 */
	private String getSOAPQuote(String symbol)
	{
		String response = "";

		try
		{
			final URL url = new URL(serviceUrl);
			final String message = createMessage(symbol);

			// Create the connection where we're going to send the file.
			HttpURLConnection httpConn = setUpHttpConnection(url, message.length());

			// Everything's set up; send the XML that was read in to
			// the service.
			writeRequest(message, httpConn);

			// Read the response and write it to standard out.
			response = readResult(httpConn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * Writes the message to the connection.
	 * 
	 * @param message
	 *            the message to write
	 * @param httpConn
	 *            the connection
	 * @throws IOException
	 */
	private void writeRequest(String message, HttpURLConnection httpConn) throws IOException
	{
		OutputStream out = httpConn.getOutputStream();
		out.write(message.getBytes());
		out.close();
	}

	/**
	 * Sets up the HTTP connection.
	 * 
	 * @param url
	 *            the url to connect to
	 * @param length
	 *            the length to the input message
	 * @return the HttpurLConnection
	 * @throws IOException
	 * @throws ProtocolException
	 */
	private HttpURLConnection setUpHttpConnection(URL url, int length) throws IOException
	{
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "\"http://www.webserviceX.NET/GetQuote\"");
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		return httpConn;
	}

	/**
	 * Reads the response from the http connection.
	 * 
	 * @param connection
	 *            the connection to read the response from
	 * @return the response
	 * @throws IOException
	 */
	private String readResult(HttpURLConnection connection) throws IOException
	{
		InputStream inputStream = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader in = new BufferedReader(isr);

		StringBuilder sb = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null)
		{
			sb.append(inputLine);
		}

		in.close();
		return sb.toString();
	}

	/**
	 * Creates the request message for retrieving a stock quote.
	 * 
	 * @param symbol
	 *            the symbol to query for
	 * @return the request message
	 */
	private String createMessage(String symbol)
	{
		StringBuilder message = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		message.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		message.append("  <soap:Body>");
		message.append("    <GetQuote xmlns=\"http://www.webserviceX.NET/\">");
		message.append("      <symbol>").append(symbol).append("</symbol>");
		message.append("    </GetQuote>");
		message.append("  </soap:Body>");
		message.append("</soap:Envelope>");
		return message.toString();
	}
}
