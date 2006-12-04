package wicket.examples.stockquote;

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
 * Provides access to a SOAP service for getting stock quotes based on a symbol.
 * Found on http://www.devx.com/Java/Article/27559/0/page/2
 */
public class StockQuote
{
	/**
	 * Use the www.xmethods.com demo webservice for stockquotes.
	 */
	private static final String serviceUrl = "http://64.124.140.30:9090/soap";

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
		int start = response.indexOf("<Result xsi:type='xsd:float'>") + 29;
		int end = response.indexOf("</Result>");

		// if the string returned isn't valid, just return empty.
		if (start < 29)
		{
			return "(unknown)";
		}
		return response.substring(start, end);
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
		out.write(message.toString().getBytes());
		out.close();
	}

	/**
	 * Sets up the HTTP connection.
	 * 
	 * @param url
	 *            the url to connect to
	 * @param length
	 *            the length to the inpupt message
	 * @return the HttpurLConnection
	 * @throws IOException
	 * @throws ProtocolException
	 */
	private HttpURLConnection setUpHttpConnection(URL url, int length) throws IOException,
			ProtocolException
	{
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection)connection;
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "urn:xmethods-delayed-quotes#getQuote");
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

		StringBuffer sb = new StringBuffer();
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
		StringBuffer message = new StringBuffer("<?xml version = '1.0' encoding = 'UTF-8'?>");
		message.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=");
		message.append("'http://schemas.xmlsoap.org/soap/envelope/' ");
		message.append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' ");
		message.append("xmlns:xsd='http://www.w3.org/2001/XMLSchema'> ");
		message.append("<SOAP-ENV:Body>");
		message.append("<ns1:getQuote xmlns:ns1='urn:xmethods-delayed-quotes' ");
		message.append("SOAP-ENV:encodingStyle=" + "'http://schemas.xmlsoap.org/soap/encoding/'>");
		message.append("<symbol xsi:type='xsd:string'>" + symbol + "</symbol>" + "</ns1:getQuote>");
		message.append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
		return message.toString();
	}
}
