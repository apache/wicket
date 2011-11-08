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
package org.apache.wicket.resource.aggregation;

import java.util.Set;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;


/**
 * An implementation of AbstractResourceAggregatingHeaderResponse that renders references in the
 * correct order if they are {@link AbstractResourceDependentResourceReference} references, ensuring
 * that dependencies are rendered in the proper order before their parent (even if they do not
 * appear in the same group as the parent of the dependencies).
 * 
 * @author Jeremy Thomerson
 * @param <R>
 *            the type of ResourceReferenceCollection returned by
 *            {@link #newResourceReferenceCollection()} and passed to all the methods that take a
 *            ResourceReferenceCollection. You will typically just use ResourceReferenceCollection
 *            for this param, unless you are returning a specific type of
 *            ResourceReferenceCollection from your subclass.
 * @param <K>
 *            the class of the key that you will create from
 *            {@link #newGroupingKey(ResourceReferenceAndStringData)}
 */
public abstract class AbstractDependencyRespectingResourceAggregatingHeaderResponse<R extends ResourceReferenceCollection, K>
	extends AbstractResourceAggregatingHeaderResponse<R, K>
{

	/**
	 * Construct.
	 * 
	 * @param real
	 *            the header response we decorate
	 */
	public AbstractDependencyRespectingResourceAggregatingHeaderResponse(IHeaderResponse real)
	{
		super(real);
	}

	@Override
	protected void renderCollection(Set<ResourceReferenceAndStringData> alreadyRendered, K key,
		R coll)
	{
		for (ResourceReferenceAndStringData data : coll)
		{
			ResourceReference ref = data.getReference();
			if (ref instanceof AbstractResourceDependentResourceReference)
			{
				AbstractResourceDependentResourceReference parent = (AbstractResourceDependentResourceReference)ref;
				R childColl = newResourceReferenceCollection(key);
				for (AbstractResourceDependentResourceReference child : parent.getDependentResourceReferences())
				{
					childColl.add(toData(child));
				}
				// render the group of dependencies before the parent
				renderCollection(alreadyRendered, key, childColl);
			}
			// now render the parent since the dependencies are rendered
			renderIfNotAlreadyRendered(alreadyRendered, data);
		}
	}

	private static ResourceReferenceAndStringData toData(
		AbstractResourceDependentResourceReference reference)
	{
		boolean css = ResourceType.CSS.equals(reference.getResourceType());
		String string = css ? reference.getMedia() : reference.getUniqueId();

		return new ResourceReferenceAndStringData(reference, null, null, string,
			reference.getResourceType(), false, null, null);
	}
}
