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
package org.apache.wicket.markup.html.link;


/**
 * This link is stateless that means that the url to this link could generate a new page before the
 * link onClick is called. Because of this you can't depend on model data in the onClick method.
 * 
 * This Link component is the same as a normal link with the stateless hint to true.
 * 
 * @author jcompagner
 * 
 * @param <T>
 *            type of model object
 */
public abstract class StatelessLink<T> extends Link<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public StatelessLink(String id)
	{
		super(id);
	}


	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}

	@Override
	protected CharSequence getURL()
	{
		return urlForListener(getPage().getPageParameters());
	}
}
