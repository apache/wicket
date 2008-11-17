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
package org.apache._wicket.request.encoder;

import java.util.Locale;

import org.apache._wicket.resource.Resource;
import org.apache._wicket.resource.ResourceReference;

/**
 * @author Matej Knopp
 */
public class AbstractResourceReferenceEncoderTest extends AbstractEncoderTest
{

	/**
	 * Construct.
	 */
	public AbstractResourceReferenceEncoderTest()
	{
	}

	protected final Resource resource1 = new Resource()
	{
		private static final long serialVersionUID = 1L;

		public void respond(Attributes attributes)
		{
		}
	};
	
	protected final Resource resource2 = new Resource()
	{
		private static final long serialVersionUID = 1L;

		public void respond(Attributes attributes)
		{
		}
	};
	
	protected final Resource resource3 = new Resource()
	{
		private static final long serialVersionUID = 1L;

		public void respond(Attributes attributes)
		{
		}
	};
	
	protected final Resource resource4 = new Resource()
	{
		private static final long serialVersionUID = 1L;

		public void respond(Attributes attributes)
		{
		}
	};
	
	protected String CLASS_NAME = AbstractResourceReferenceEncoderTest.class.getName();

	protected ResourceReference reference1 = new ResourceReference(
		AbstractResourceReferenceEncoderTest.class, "reference1", null, null)
	{
		public Resource getResource()
		{
			return resource1;
		};
	};
	
	protected ResourceReference reference2 = new ResourceReference(
		AbstractResourceReferenceEncoderTest.class, "reference2/name2", new Locale("en", "en"), null)
	{
		public Resource getResource()
		{
			return resource2;
		};
	};
	
	protected ResourceReference reference3 = new ResourceReference(
		AbstractResourceReferenceEncoderTest.class, "reference3", null, "style")
	{
		public Resource getResource()
		{
			return resource3;
		};
	};
	
	protected ResourceReference reference4 = new ResourceReference(
		AbstractResourceReferenceEncoderTest.class, "reference4", Locale.ENGLISH, "style")
	{
		public Resource getResource()
		{
			return resource4;
		};
	};

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		context.getResourceReferenceRegistry().registerResourceReference(reference1);
		context.getResourceReferenceRegistry().registerResourceReference(reference2);
		context.getResourceReferenceRegistry().registerResourceReference(reference3);
		context.getResourceReferenceRegistry().registerResourceReference(reference4);
	}
}
