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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

/**
 * {@link IResourceStream} that applies XSLT on an input {@link IResourceStream}.
 * The XSL stylesheet itself is also an {@link IResourceStream}. Override
 * {@link #getParameters()} to pass parameters to the XSL stylesheet.
 * 
 * <p>
 * FIXME experimental feature, does not implement any kind of caching, use with
 * care, running an XSL transformation for every request is very expensive!
 * Please have a look at {@link ZipResourceStream} for an in-depth explanation
 * of what needs to be done with respect to caching.
 * </p>
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class XSLTResourceStream extends AbstractResourceStream
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ByteArrayOutputStream out;

	/**
	 * @return a {@link Map} of XSLT parameters, appropriate for passing
	 *         information to the XSL stylesheet
	 */
	protected Map getParameters()
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
	public XSLTResourceStream(IResourceStream xsltResource, IResourceStream xmlResource)
	{
		try
		{
			javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(
					xmlResource.getInputStream());
			javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(
					xsltResource.getInputStream());
			out = new ByteArrayOutputStream();
			javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(out);

			// create an instance of TransformerFactory
			javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory
					.newInstance();

			javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
			Map parameters = getParameters();
			if (parameters != null)
			{
				Iterator it = parameters.entrySet().iterator();
				while (it.hasNext())
				{
					Entry e = (Entry)it.next();
					trans.setParameter(e.getKey().toString(), e.getValue().toString());
				}
			}

			trans.transform(xmlSource, result);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
		finally
		{
			try
			{
				xmlResource.close();
				xsltResource.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#close()
	 */
	public void close() throws IOException
	{
	}

	/**
	 * Returns always null
	 * 
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	public String getContentType()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#length()
	 */
	public long length()
	{
		return out.size();
	}

	/**
	 * Returns always null
	 * 
	 * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
	 */
	public Time lastModifiedTime()
	{
		return null;
	}

}
