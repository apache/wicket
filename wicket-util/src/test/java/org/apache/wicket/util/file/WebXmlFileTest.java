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
package org.apache.wicket.util.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;


/**
 * 
 */
public class WebXmlFileTest extends Assert
{
	/**
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void test_filter() throws ParserConfigurationException, SAXException, IOException
	{
		filterOrServlet(false);
	}

	/**
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void test_servlet() throws ParserConfigurationException, SAXException, IOException
	{
		filterOrServlet(true);
	}

	/**
	 * 
	 * @param servlet
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void filterOrServlet(final boolean servlet) throws ParserConfigurationException,
		SAXException, IOException
	{
		String tag = servlet ? "servlet" : "filter";

		String webxml = getWebXml(tag, "/*");
		String path = new WebXmlFile().getUniqueFilterPath(servlet, "HelloWorldApplication",
			new ByteArrayInputStream(webxml.getBytes()));
		assertEquals("", path);

		webxml = getWebXml(tag, "/test/*");
		path = new WebXmlFile().getUniqueFilterPath(servlet, "HelloWorldApplication",
			new ByteArrayInputStream(webxml.getBytes()));
		assertEquals("test/", path);

		path = new WebXmlFile().getUniqueFilterPath(servlet, "xxx",
			new ByteArrayInputStream(webxml.getBytes()));
		assertNull(path);

		path = new WebXmlFile().getUniqueFilterPath(!servlet, "HelloWorldApplication",
			new ByteArrayInputStream(webxml.getBytes()));
		assertNull(path);
	}

	/**
	 * 
	 * @param filter
	 * @param url
	 * @return webxml
	 */
	private String getWebXml(final String filter, final String url)
	{
		StringBuilder webxml = new StringBuilder();
		webxml.append("<web-app>");
		webxml.append("<" + filter + ">");
		webxml.append(" <" + filter + "-name>HelloWorldApplication</" + filter + "-name>");
		webxml.append(" <" + filter + "-class>org.apache.wicket.protocol.http.WicketFilter</" +
			filter + "-class>");
		webxml.append(" <init-param>");
		webxml.append("  <param-name>applicationClassName</param-name>");
		webxml.append("  <param-value>org.apache.wicket.examples.helloworld.HelloWorldApplication</param-value>");
		webxml.append(" </init-param>");
		webxml.append("</" + filter + ">");
		webxml.append("");
		webxml.append("<" + filter + "-mapping>");
		webxml.append(" <" + filter + "-name>HelloWorldApplication</" + filter + "-name>");
		webxml.append(" <url-pattern>" + url + "</url-pattern>");
		webxml.append(" <dispatcher>REQUEST</dispatcher>");
		webxml.append(" <dispatcher>INCLUDE</dispatcher>");
		webxml.append("</" + filter + "-mapping>");
		webxml.append("</web-app>");

		return webxml.toString();
	}
}
