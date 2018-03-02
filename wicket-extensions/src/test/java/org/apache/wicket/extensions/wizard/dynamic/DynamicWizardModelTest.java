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
package org.apache.wicket.extensions.wizard.dynamic;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link DynamicWizardModel}.
 */
public class DynamicWizardModelTest extends WicketTestCase
{

	private IDynamicWizardStep step4;
	
	@Test
	public void testDynamicWizard()
	{
		DynamicWizardStep step1 = new DynamicWizardStep(null)
		{
			@Override
			public boolean isLastStep()
			{
				return false;
			}

			@Override
			public IDynamicWizardStep next()
			{
				return step4;
			}
		};
			
		step4 = new DynamicWizardStep(step1)
		{
			@Override
			public boolean isLastStep()
			{
				return true;
			}
			
			@Override
			public IDynamicWizardStep next()
			{
				return null;
			}
		};
		
		DynamicWizardModel model = new DynamicWizardModel(step1);
		
		model.reset();
		
		assertSame(step1, model.getActiveStep());
		assertTrue(model.isNextAvailable());
		assertFalse(model.isLastStep(model.getActiveStep()));
		assertFalse(model.isFinishAvailable());
		
		model.next();
		assertSame(step4, model.getActiveStep());
		assertFalse(model.isNextAvailable());
		assertTrue(model.isLastStep(model.getActiveStep()));
		assertTrue(model.isFinishAvailable());
		
		try {
			model.next();
			fail();
		} catch (Exception expected) {
		}
	}
}
