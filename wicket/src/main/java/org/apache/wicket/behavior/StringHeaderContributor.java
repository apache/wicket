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
package org.apache.wicket.behavior;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Objects;

/**
 * A simple header contributor that just spits out the string it is constructed with as a header
 * contribution.
 * 
 * @author Eelco Hillenius
 */
public class StringHeaderContributor extends AbstractHeaderContributor
{
	private static final long serialVersionUID = 1L;

	/** The contribution as a model that returns a plain string. */
	private final IModel<?> contribution;

	/**
	 * Construct.
	 * 
	 * @param contribution
	 *            The contribution as a plain string
	 */
	public StringHeaderContributor(final String contribution)
	{
		if (contribution == null)
		{
			throw new IllegalArgumentException("argument contribition must be not null");
		}

		this.contribution = new Model<String>(contribution);
	}

	/**
	 * Construct.
	 * 
	 * @param contribution
	 *            The contribution as a model that returns a plain string
	 */
	public StringHeaderContributor(final IModel<?> contribution)
	{
		if (contribution == null)
		{
			throw new IllegalArgumentException("argument contribition must be not null");
		}

		this.contribution = contribution;
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		Object object = contribution.getObject();
		if (object != null)
		{
			response.getResponse().println(object.toString());
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		Object object = contribution.getObject();
		return (object != null) ? object.hashCode() : 0;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof StringHeaderContributor)
		{
			Object thisContrib = contribution.getObject();
			Object thatContrib = ((StringHeaderContributor)obj).contribution.getObject();
			return Objects.equal(thisContrib, thatContrib);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "StringContributor[contribution=" + contribution + "]";
	}
}