package wicket.examples.stockquote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Gets a stock quote from a webservice. Provided by devx.com
 * 
 * http://www.devx.com/Java/Article/27559/0/page/2
 */
public class StockQuoteLabel extends WebComponent
{
	/**
	 * Use the www.xmethods.com demo webservice for stockquotes.
	 */
	private String serviceUrl = "http://64.124.140.30:9090/soap";

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 */
	public StockQuoteLabel(String id)
	{
		super(id);
	}

	/**
	 * Convenience constructor. Same as StockQuoteLabel(String, new Model(String))
	 * 
	 * @param id
	 *            See Component
	 * @param symbol
	 *            The symbol to look up
	 * 
	 * @see wicket.Component#Component(String, IModel)
	 */
	public StockQuoteLabel(String id, String symbol)
	{
		super(id, new Model(symbol));
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public StockQuoteLabel(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		String symbol = getModelObjectAsString();
		if ("".equals(symbol))
		{
			symbol = getId();
		}
		replaceComponentTagBody(markupStream, openTag, getQuote(symbol));
	}

	/**
	 * Gets a stock quote for the ticker name.
	 * 
	 * @return the stock quote
	 */
	private String getQuote(String symbol)
	{
		String strReturn = "";
		String strSOAPPacket = getSOAPQuote(symbol);

		int nStart = strSOAPPacket.indexOf("<Result xsi:type='xsd:float'>");
		if (nStart == -1)
		{
		    return "(unknown)";
		}
		int nEnd = strSOAPPacket.indexOf("</Result>");
		strReturn = strSOAPPacket.substring(nStart + 29, nEnd);
		return strReturn;
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
		StringBuffer strReturn = new StringBuffer();
		StringBuffer strSOAPENV = new StringBuffer("<?xml version = '1.0' encoding = 'UTF-8'?>");
		strSOAPENV.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=");
		strSOAPENV.append("'http://schemas.xmlsoap.org/soap/envelope/' ");
		strSOAPENV.append("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' ");
		strSOAPENV.append("xmlns:xsd='http://www.w3.org/2001/XMLSchema'> ");
		strSOAPENV.append("<SOAP-ENV:Body>");
		strSOAPENV.append("<ns1:getQuote xmlns:ns1='urn:xmethods-delayed-quotes' ");
		strSOAPENV.append("SOAP-ENV:encodingStyle="
				+ "'http://schemas.xmlsoap.org/soap/encoding/'>");
		strSOAPENV.append("<symbol xsi:type='xsd:string'>" + symbol + "</symbol>"
				+ "</ns1:getQuote>");
		strSOAPENV.append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");

		// Create the connection where we're going to send the file.
		try
		{
			URL url = new URL(serviceUrl);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection)connection;
			// Set the appropriate HTTP parameters.
			httpConn.setRequestProperty("Content-Length", String.valueOf(strSOAPENV.length()));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			httpConn.setRequestProperty("SOAPAction", "urn:xmethods-delayed-quotes#getQuote");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			// Everything's set up; send the XML that was read in to
			// the service.
			OutputStream out = httpConn.getOutputStream();
			out.write(strSOAPENV.toString().getBytes());
			out.close();
			// Read the response and write it to standard out.
			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				strReturn.append(inputLine);
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return strReturn.toString();
	}
}
