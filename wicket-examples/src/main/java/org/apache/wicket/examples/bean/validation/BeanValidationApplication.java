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
package org.apache.wicket.examples.bean.validation;

import java.util.Collections;
import java.util.List;

import javax.validation.metadata.ConstraintDescriptor;

import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.bean.validation.Property;
import org.apache.wicket.examples.WicketExampleApplication;

public class BeanValidationApplication extends WicketExampleApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return BeanValidationPage.class;
	}

	@Override
	protected void init()
	{
		new BeanValidationConfiguration()
		{
			/**
			 * Let bean-validation handle required constraints.
			 */
			@Override
			public List<ConstraintDescriptor<?>> getRequiredConstraints(Property property) {
				return Collections.emptyList();
			}
		}.configure(this);
	}
}
