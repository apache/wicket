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
package org.apache.wicket.util.markup.xhtml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Usable by tests to check that the html markup files are well formed.
 * 
 * @author akiraly
 */
public class WellFormedXmlTestCase
{
	private DocumentBuilderFactory factory;

	/**
	 * Checks xml well formedness of html markup files under the current working directory.
	 */
	@Test
	public void markupFiles()
	{
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		File root = new File("").getAbsoluteFile();
		processDirectory(root);
	}

	private void processDirectory(File dir)
	{
		for (File f : dir.listFiles(fileFilter))
		{
			if (f.isDirectory())
			{
				processDirectory(f);
			}
			else
			{
				processFile(f);
			}
		}
	}

	private void processFile(File file)
	{
		DocumentBuilder builder;

		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException("Configuration exception while parsing xml markup.", e);
		}

		builder.setEntityResolver(entityResolver);
		builder.setErrorHandler(errorHandler);

		try
		{
			builder.parse(file);
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Parsing xml sax failed, file: " + file, e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Parsing xml io failed, file: " + file, e);
		}
	}

	private static final FileFilter fileFilter = new FileFilter()
	{
		@Override
		public boolean accept(File pathname)
		{
			String path = pathname.getAbsolutePath().replace('\\', '/');
			return !path.contains("/src/test/") && !path.contains("/target/") &&
				!"package.html".equals(pathname.getName()) &&
				(pathname.isDirectory() || pathname.getName().endsWith(".html"));
		}
	};

	private static final ErrorHandler errorHandler = new ErrorHandler()
	{
		@Override
		public void warning(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException
		{
			throw exception;
		}

	};

	private static final EntityResolver entityResolver = new EntityResolver()
	{
		private final Map<String, String> systemIdToUri = new HashMap<String, String>();

		{
			systemIdToUri.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
				"xhtml1-transitional.dtd");
			systemIdToUri.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
				"xhtml1-strict.dtd");

			/*
			 * Cheating: using xhtml dtd-s for html4 files too because the html4 dtd-s are not valid
			 * xml dtd-s.
			 */
			systemIdToUri.put("http://www.w3.org/TR/html4/loose.dtd", "xhtml1-transitional.dtd");
			systemIdToUri.put("http://www.w3.org/TR/html4/strict.dtd", "xhtml1-strict.dtd");
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
			IOException
		{
			String uri = systemIdToUri.get(systemId);
			if (uri != null)
			{
				return new InputSource(WellFormedXmlTestCase.class.getResource(uri).toString());
			}

			return null;
		}
	};
}
