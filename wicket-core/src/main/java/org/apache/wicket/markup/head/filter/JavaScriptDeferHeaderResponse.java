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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.markup.head.AbstractJavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;

/**
 * A header response that defers all {@link AbstractJavaScriptReferenceHeaderItem}s.
 * 
 * @author svenmeier
 */
public class JavaScriptDeferHeaderResponse extends DecoratingHeaderResponse
{
	public JavaScriptDeferHeaderResponse(IHeaderResponse response)
	{
		super(response);
	}
	
	@Override
	public void render(HeaderItem item)
	{
		defer(item);
		
		super.render(item);
	}

	private void defer(HeaderItem item)
	{
		if (item instanceof IWrappedHeaderItem) {
			item = ((IWrappedHeaderItem)item).getWrapped();
		}
		
		if (item instanceof AbstractJavaScriptReferenceHeaderItem) {
			((AbstractJavaScriptReferenceHeaderItem)item).setDefer(true);
		}

		for (HeaderItem dependency : item.getDependencies()) {
			defer(dependency);
		}
	}
}