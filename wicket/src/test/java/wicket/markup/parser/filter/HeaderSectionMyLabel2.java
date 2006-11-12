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
package wicket.markup.parser.filter;

import wicket.MarkupContainer;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.basic.Label;
import wicket.model.Model;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class HeaderSectionMyLabel2 extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param label
	 */
	public HeaderSectionMyLabel2(MarkupContainer parent, final String id, final String label)
	{
		super(parent, id, new Model<String>(label));
	}

	/**
	 * @param container
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.getResponse().write("text added by contributor");
	}
}
