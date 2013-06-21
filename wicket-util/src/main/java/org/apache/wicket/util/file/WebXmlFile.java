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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.wicket.util.xml.CustomEntityResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A utility class providing helper methods in dealing with web.xml
 * 
 * @author jcompagner
 * @author Juergen Donnerstag
 */
public class WebXmlFile
{
	private static final Logger log = LoggerFactory.getLogger(WebXmlFile.class);

	/**
	 * Construct.
	 */
	public WebXmlFile()
	{
	}

	/**
	 * Gets unique Wicket filter path via FilterConfig
	 * 
	 * @param isServlet
	 *            true if Servlet, false if Filter
	 * @param filterConfig
	 * @return Filter path retrieved from "url-pattern". Null if not found or error occured
	 */
	public final String getUniqueFilterPath(final boolean isServlet, final FilterConfig filterConfig)
	{
		String filterName = filterConfig.getFilterName();
		Set<String> paths = getFilterPath(isServlet, filterConfig.getServletContext(), filterName);
		return uniquePath(paths, isServlet, filterName);
	}

	/**
	 * Gets Wicket filter path via ServletContext and the filter name
	 * 
	 * @param isServlet
	 *            true if Servlet, false if Filter
	 * @param servletContext
	 * @param filterName
	 * @return Filter paths retrieved from "url-pattern"
	 */
	public final Set<String> getFilterPath(final boolean isServlet,
		final ServletContext servletContext, final String filterName)
	{
		InputStream is = servletContext.getResourceAsStream("/WEB-INF/web.xml");
		if (is != null)
		{
			try
			{
				return getFilterPath(isServlet, filterName, is);
			}
			catch (ParserConfigurationException ex)
			{
				log.error("Error reading servlet/filter path from web.xml", ex);
			}
			catch (SAXException ex)
			{
				log.error("Error reading servlet/filter path from web.xml", ex);
			}
			catch (IOException ex)
			{
				log.error("Error reading servlet/filter path from web.xml", ex);
			}
			catch (SecurityException e)
			{
				// Swallow this at INFO.
				log.info("Couldn't read web.xml to automatically pick up servlet/filter path: " +
					e.getMessage());
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Gets unique filter path via filter name and InputStream.
	 * 
	 * @param isServlet
	 *            true if Servlet, false if Filter
	 * @param filterName
	 * @param is
	 *            The web.xml file
	 * @return Filter path retrieved from "url-pattern". Null if not found.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 * @see #getFilterPath(boolean, String, java.io.InputStream)
	 */
	public final String getUniqueFilterPath(final boolean isServlet, final String filterName,
		final InputStream is) throws ParserConfigurationException, SAXException, IOException
	{
		return uniquePath(getFilterPath(isServlet, filterName, is), isServlet, filterName);
	}

	/**
	 * return unique path from set of paths
	 * 
	 * @param paths
	 *            <code>null</code> if set was empty, unique path if set was of length == 1
	 * @param isServlet
	 * @param filterName
	 * @return unique path
	 * @throws RuntimeException
	 *             in case length > 1
	 */
	private String uniquePath(final Set<String> paths, final boolean isServlet,
		final String filterName)
	{
		if (paths.size() > 1)
		{
			StringBuilder err = new StringBuilder();
			err.append("web.xml: expected one ");
			err.append(isServlet ? "servlet" : "filter");
			err.append(" path for [");
			err.append(filterName);
			err.append("] but found multiple:");

			for (String path : paths)
			{
				err.append(" [" + path + "]");
			}
			throw new RuntimeException(err.toString());
		}

		if (paths.size() == 1)
		{
			return paths.iterator().next();
		}
		return null;
	}

	/**
	 * Gets Wicket filter path via filter name and InputStream. The InputStream is assumed to be an
	 * web.xml file.
	 * <p>
	 * A typical Wicket web.xml entry looks like:
	 * 
	 * <pre>
	 * <code>
	 * &lt;filter&gt;
	 *   &lt;filter-name&gt;HelloWorldApplication&lt;/filter-name&gt;
	 *   &lt;filter-class&gt;org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
	 *   &lt;init-param&gt;
	 *     &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
	 *     &lt;param-value&gt;org.apache.wicket.examples.helloworld.HelloWorldApplication&lt;/param-value&gt;
	 *   &lt;/init-param&gt;
	 * &lt;/filter&gt;
	 * 
	 * &lt;filter-mapping&gt;
	 *   &lt;filter-name&gt;HelloWorldApplication&lt;/filter-name&gt;
	 *   &lt;url-pattern&gt;/helloworld/*&lt;/url-pattern&gt;
	 *   &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
	 *   &lt;dispatcher&gt;INCLUDE&lt;/dispatcher&gt;
	 * &lt;/filter-mapping&gt;
	 * </code>
	 * </pre>
	 * 
	 * @param isServlet
	 *            true if Servlet, false if Filter
	 * @param filterName
	 * @param is
	 *            The web.xml file
	 * @return Filter paths retrieved from "url-pattern".
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public final Set<String> getFilterPath(final boolean isServlet, final String filterName,
		final InputStream is) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// try to pull DTD from local set of entities
		builder.setEntityResolver(CustomEntityResolver.getPreloaded());
		Document document = builder.parse(is);

		String tag = (isServlet ? "servlet" : "filter");
		String mapping = tag + "-mapping";
		String name = tag + "-name";

		Set<String> urlPatterns = getFilterPaths(filterName, mapping, name,
			document.getChildNodes());

		if (urlPatterns.size() == 0)
		{
			log.warn("web.xml: No url-pattern found for '{}' with name '{}'", tag, filterName);
		}

		if (log.isInfoEnabled())
		{
			StringBuilder msg = new StringBuilder();
			msg.append("web.xml: url mapping found for ")
				.append(tag)
				.append(" with name ")
				.append(filterName)
				.append(':');

			for (String urlPattern : urlPatterns)
			{
				msg.append(" [");
				msg.append(urlPattern);
				msg.append(']');
			}
			log.info(msg.toString());
		}
		Set<String> stripped = new HashSet<>(urlPatterns.size());

		for (String urlPattern : urlPatterns)
		{
			stripped.add(urlPattern.substring(1, urlPattern.length() - 1));
		}
		return stripped;
	}

	/**
	 * Iterate through all children of 'node' and search for a node with name "filterName". Return
	 * the value of node "url-pattern" if "filterName" was found.
	 * 
	 * @param filterName
	 * @param name
	 * @param node
	 * @return value of node "url-pattern"
	 */
	private Set<String> getFilterPaths(final String filterName, final String name, final Node node)
	{
		Set<String> paths = new HashSet<>();
		String foundUrlPattern = null;
		String foundFilterName = null;

		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
		{
			Node n = node.getChildNodes().item(i);

			if (name.equals(n.getNodeName()))
			{
				foundFilterName = n.getTextContent();
			}
			else if ("url-pattern".equals(n.getNodeName()))
			{
				foundUrlPattern = n.getTextContent();
			}

			if (foundFilterName != null)
			{
				foundFilterName = foundFilterName.trim();
			}

			if (filterName.equals(foundFilterName))
			{
				if (foundUrlPattern != null)
				{
					paths.add(foundUrlPattern.trim());
				}
			}
		}
		return paths;
	}

	/**
	 * Find a node with name 'mapping' within 'nodeList' and if found continue to search amongst its
	 * children for a node with 'filterName' and "url-pattern'
	 * 
	 * 
	 * @param filterName
	 * @param mapping
	 * @param name
	 * @param nodeList
	 * @return The value assigned to node "url-pattern"
	 */
	private Set<String> getFilterPaths(final String filterName, final String mapping,
		final String name, final NodeList nodeList)
	{
		Set<String> paths = new HashSet<>(1);

		for (int i = 0; i < nodeList.getLength(); ++i)
		{
			Node node = nodeList.item(i);

			if (mapping.equals(node.getNodeName()))
			{
				paths.addAll(getFilterPaths(filterName, name, node));
			}
			else
			{
				paths.addAll(getFilterPaths(filterName, mapping, name, node.getChildNodes()));
			}
		}
		return paths;
	}
}
