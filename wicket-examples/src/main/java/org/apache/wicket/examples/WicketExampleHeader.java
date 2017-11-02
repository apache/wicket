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
package org.apache.wicket.examples;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Navigation panel for the examples project.
 * 
 * @author Eelco Hillenius
 */
public final class WicketExampleHeader extends Panel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 *            id of the component
	 * @param exampleTitle
	 *            title of the example
	 * @param page
	 *            The example page
	 */
	public WicketExampleHeader(String id)
	{
		super(id);

		setRenderBodyOnly(true);
		
		add(new Image("exampleheaderimage", new PackageResourceReference(
			WicketExamplePage.class, "logo-apachewicket-examples-white.svg")));
		add(new DebugBar("debug"));
	}
}
