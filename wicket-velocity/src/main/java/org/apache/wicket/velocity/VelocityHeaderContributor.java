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
package org.apache.wicket.velocity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;

/**
 * a simple header contributor that delegates to a List of {@link VelocityContributor}
 * 
 */
public class VelocityHeaderContributor extends AbstractHeaderContributor
{

	private final List<VelocityContributor> contributors = new ArrayList<VelocityContributor>(1);

	/**
	 * Adds a contributor.
	 * 
	 * @param velocityContributor
	 * @return This for chaining
	 */
	public VelocityHeaderContributor add(VelocityContributor velocityContributor)
	{
		contributors.add(velocityContributor);
		return this;
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractBehavior#detach(org.apache.wicket.Component)
	 */
	@Override
	public void detach(Component< ? > component)
	{
		for (Iterator<VelocityContributor> i = contributors.iterator(); i.hasNext();)
		{
			VelocityContributor vc = i.next();
			vc.detach(component);
		}
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractHeaderContributor#getHeaderContributors()
	 */
	@Override
	public IHeaderContributor[] getHeaderContributors()
	{
		return contributors.toArray(new IHeaderContributor[contributors.size()]);
	}
}
