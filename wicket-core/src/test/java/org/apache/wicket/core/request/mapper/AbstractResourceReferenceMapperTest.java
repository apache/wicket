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
package org.apache.wicket.core.request.mapper;

import java.util.Locale;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.junit.Before;

/**
 * @author Matej Knopp
 */
public abstract class AbstractResourceReferenceMapperTest extends AbstractMapperTest
{
	/**
	 * Construct.
	 */
	public AbstractResourceReferenceMapperTest()
	{
	}

	protected final IResource resource1 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected final IResource resource2 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected final IResource resource3 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected final IResource resource4 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected final IResource resource5 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected final IResource resource6 = new IResource()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void respond(Attributes attributes)
		{
		}
	};

	protected String CLASS_NAME = AbstractResourceReferenceMapperTest.class.getName();

	protected ResourceReference reference1 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference1", null, null, null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource1;
		}
	};

	protected ResourceReference reference1_a = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference1", Locale.ENGLISH, null, null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource1;
		}
	};

	protected ResourceReference reference1_b = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference1", null, "style", null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource1;
		}
	};

	protected ResourceReference reference2 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference2/name2", new Locale("en", "en"),
		null, null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource2;
		}
	};

	protected ResourceReference reference2_a = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference2/name2", new Locale("en", "en"),
		"style", null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource2;
		}
	};

	protected ResourceReference reference3 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference3", null, "style", null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource3;
		}
	};

	protected ResourceReference reference4 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference4", Locale.ENGLISH, "style", null)
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource4;
		}
	};

	protected ResourceReference reference5 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference5", Locale.ENGLISH, null, "variation")
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource5;
		}
	};

	protected ResourceReference reference6 = new ResourceReference(
		AbstractResourceReferenceMapperTest.class, "reference6", Locale.ENGLISH, "style",
		"variation")
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResource getResource()
		{
			return resource6;
		}
	};

	@Override
	@Before
	public void before() throws Exception
	{
		context.getResourceReferenceRegistry().registerResourceReference(reference1);
		context.getResourceReferenceRegistry().registerResourceReference(reference1_a);
		context.getResourceReferenceRegistry().registerResourceReference(reference1_b);
		context.getResourceReferenceRegistry().registerResourceReference(reference2);
		context.getResourceReferenceRegistry().registerResourceReference(reference2_a);
		context.getResourceReferenceRegistry().registerResourceReference(reference3);
		context.getResourceReferenceRegistry().registerResourceReference(reference4);
		context.getResourceReferenceRegistry().registerResourceReference(reference5);
		context.getResourceReferenceRegistry().registerResourceReference(reference6);
	}
}
