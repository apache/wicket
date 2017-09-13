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
package org.apache.wicket.examples.wizard;

import org.apache.wicket.extensions.wizard.StaticContentStep;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;

/**
 * This is kind of the hello world example for wizards. It doesn't do anything useful, except
 * displaying some static text and following static flow.
 * <p>
 * {@link StaticContentStep static content steps} are useful when you have some text to display that
 * you don't want to define seperate panels for. E.g. when the contents come from a database, this
 * is a convenient class to use.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class StaticWizardWithPanels extends Wizard
{

	/**
	 * The first step of this wizard.
	 */
	private static final class Step1 extends WizardStep
	{
		/**
		 * Construct.
		 */
		public Step1()
		{
			super("One", "The first step");
		}
	}

	/**
	 * The second step of this wizard.
	 */
	private static final class Step2 extends WizardStep
	{
		/**
		 * Construct.
		 */
		public Step2()
		{
			super("Two", "The second step");
		}
	}

	/**
	 * The third step of this wizard.
	 */
	private static final class Step3 extends WizardStep
	{
		/**
		 * Construct.
		 */
		public Step3()
		{
			super("Three", "The third step");
		}
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public StaticWizardWithPanels(String id)
	{
		super(id);

		// create a model with a couple of custom panels
		// still not that spectacular, but at least it
		// will give you a hint of how nice it is to
		// be able to work with custom panels
		WizardModel model = new WizardModel();
		model.add(new Step1());
		model.add(new Step2());
		model.add(new Step3());

		// initialize the wizard
		init(model);
	}

	@Override
	public void onCancel()
	{
		setResponsePage(Index.class);
	}

	@Override
	public void onFinish()
	{
		setResponsePage(Index.class);
	}
}
