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
package org.apache.wicket.util.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Bytes;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * {@link IResourceStream} that applies XSLT on an input {@link IResourceStream}. The XSL stylesheet
 * itself is also an {@link IResourceStream}. Override {@link #getParameters()} to pass parameters
 * to the XSL stylesheet.
 * 
 * <p>
 * NOTE: this is an experimental feature which does not implement any kind of caching, use with
 * care, running an XSL transformation for every request is very expensive! Please have a look at
 * {@link ZipResourceStream} for an in-depth explanation of what needs to be done with respect to
 * caching.
 * </p>
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class XSLTResourceStream extends AbstractResourceStream
{
	private static final long serialVersionUID = 1L;
	private final transient ByteArrayOutputStream out;

	/**
	 * @return a {@link Map} of XSLT parameters, appropriate for passing information to the XSL
	 *         stylesheet
	 */
	protected Map<Object, Object> getParameters()
	{
		return null;
	}

	/**
	 * Construct.
	 *
	 * @param xsltResource
	 *            the XSL stylesheet as an {@link IResourceStream}
	 * @param xmlResource
	 *            the input XML document as an {@link IResourceStream}
	 */
	public XSLTResourceStream(final IResourceStream xsltResource, final IResourceStream xmlResource)
	{
		this(xsltResource, xmlResource, defaultTransformerFactory());
	}

	/**
	 * Creates a default transformer factory with XMLConstants.FEATURE_SECURE_PROCESSING set to true
	 *
	 * @return a default transformer factory
	 */
	private static TransformerFactory defaultTransformerFactory()
	{
		TransformerFactory factory = TransformerFactory.newInstance();
		try
		{
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
		return factory;
	}

	/**
	 * Construct.
	 * 
	 * @param xsltResource
	 *            the XSL stylesheet as an {@link IResourceStream}
	 * @param xmlResource
	 *            the input XML document as an {@link IResourceStream}
	 * @param transformerFactory
	 * 			  the transformer factory used to transform the xmlResource
	 */
	public XSLTResourceStream(final IResourceStream xsltResource, final IResourceStream xmlResource, TransformerFactory transformerFactory)
	{
		try
		{
			Source xmlSource = new StreamSource(xmlResource.getInputStream());
			Source xsltSource = new StreamSource(xsltResource.getInputStream());
			out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);

			Transformer trans = transformerFactory.newTransformer(xsltSource);

			Map<Object, Object> parameters = getParameters();
			if (parameters != null)
			{
				for (Entry<Object, Object> e : parameters.entrySet())
				{
					trans.setParameter(e.getKey().toString(), e.getValue().toString());
				}
			}

			trans.transform(xmlSource, result);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtils.closeQuietly(xmlResource);
			IOUtils.closeQuietly(xsltResource);
		}
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#close()
	 */
	@Override
	public void close() throws IOException
	{
	}

	/**
	 * Returns always null
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	@Override
	public String getContentType()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	@Override
	public Bytes length()
	{
		return Bytes.bytes(out.size());
	}

	/**
	 * Returns always null
	 * 
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	@Override
	public Instant lastModifiedTime()
	{
		return null;
	}

}
