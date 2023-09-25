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
package org.apache.wicket.resource.aggregator;

import static org.apache.wicket.markup.head.JavaScriptHeaderItem.forReference;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * js resource with dep on y
 */
public class ResourceReferenceBun2 extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ResourceReferenceBun2()
	{
		super(ResourceAggregatorTest.class, "bun2.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		return Collections.<HeaderItem> singletonList(forReference(new ResourceReferenceY()));
	}
}