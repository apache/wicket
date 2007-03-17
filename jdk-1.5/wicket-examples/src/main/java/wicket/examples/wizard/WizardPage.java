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
package wicket.examples.wizard;

import java.lang.reflect.Constructor;

import wicket.examples.WicketExamplePage;
import wicket.extensions.wizard.Wizard;

/**
 * Page for displaying a wizard.
 * 
 * @author Eelco Hillenius
 */
public class WizardPage extends WicketExamplePage
{
	/**
	 * Construct.
	 * 
	 * @param wizardClass
	 *            class of the wizard component
	 */
	public WizardPage(Class wizardClass)
	{
		if (wizardClass == null)
		{
			throw new IllegalArgumentException("argument wizardClass must be not null");
		}
		try
		{
			Constructor ctor = wizardClass.getConstructor(new Class[] { String.class });
			Wizard wizard = (Wizard)ctor.newInstance(new String[] { "wizard" });
			add(wizard);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
