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
package org.apache.wicket.resource.loader;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * 
 */
public class ClassStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ClassStringResourceLoaderTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 */
	public void testValidator1()
	{
		ClassStringResourceLoader loader = new ClassStringResourceLoader(MyValidator.class);
		tester.getApplication().getResourceSettings().addStringResourceLoader(loader);

		assertEquals("${label} is invalid", loader.loadStringResource(null, "error"));
	}

	/**
	 * 
	 */
	public class MyValidator extends AbstractValidator
	{
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * @see org.apache.wicket.validation.validator.AbstractValidator#onValidate(org.apache.wicket.validation.IValidatable)
		 */
		protected void onValidate(IValidatable v)
		{
			error(v);
		}
	}
}
