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
package org.apache.wicket.examples.sri;

import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.markup.head.ISubresourceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.SubresourceHeaderResponse;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class SriApplication extends WicketExampleApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		return IntegrityDemoPage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		setHeaderResponseDecorator(
			response -> new ResourceAggregator(new SubresourceHeaderResponse(response)
			{
				@Override
				protected void configure(ISubresourceHeaderItem item)
				{
					if (item instanceof JavaScriptReferenceHeaderItem) {
						ResourceReference reference = ((JavaScriptReferenceHeaderItem)item).getReference();
						
						if (reference.equals(IntegrityDemoPage.JS)) {
							String algorithm = "sha384";
							String value = "yDSj1gWA4teUdCx2/5M0RsK1jovKR0RdUeeLXKU1gRpNWevoQDGhjHEd1R6Jb+FQ";
							item.setIntegrity(algorithm + "-" + value);
						}
					}
				}
			}));

		mountPage("integritydemo", IntegrityDemoPage.class);
	}
}
