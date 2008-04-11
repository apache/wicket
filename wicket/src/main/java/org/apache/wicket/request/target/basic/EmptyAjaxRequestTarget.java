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
package org.apache.wicket.request.target.basic;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;

/**
 * The empty AJAX request target does output an empty AJAX response.
 * 
 * @author Matej Knopp
 */
public final class EmptyAjaxRequestTarget implements IRequestTarget
{
	/** immutable hashcode. */
	private static final int HASH = 17 * 1542323;

	/** singleton instance. */
	private static final EmptyAjaxRequestTarget instance = new EmptyAjaxRequestTarget();

	/**
	 * Construct.
	 */
	private EmptyAjaxRequestTarget()
	{
	}

	/**
	 * Gets the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static final EmptyAjaxRequestTarget getInstance()
	{
		return instance;
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		requestCycle.getResponse().write("<ajax-response></ajax-response>");
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof EmptyAjaxRequestTarget)
		{
			return true;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return HASH;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "EmptyAjaxRequestTarget";
	}
}
