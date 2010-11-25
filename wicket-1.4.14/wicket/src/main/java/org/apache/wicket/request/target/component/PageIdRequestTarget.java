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
package org.apache.wicket.request.target.component;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;

/**
 * Target that navigates to a page pointed to by its id. The great benefit of this target over the
 * PageRequestTarget is that no reference to the actual page is needed, which greatly facilitates
 * navigational usecases where a list or a stack of page references is needed (ie breadcrumbs).
 * 
 * @see PageReference
 * 
 * @author igor.vaynberg
 * @deprecated will be removed before 1.5
 */
@Deprecated
public class PageIdRequestTarget extends PageReferenceRequestTarget
{
	@Deprecated
	public PageIdRequestTarget(Page page)
	{
		super(page);
	}

	@Deprecated
	public PageIdRequestTarget(PageReference reference)
	{
		super(reference);
	}
}
