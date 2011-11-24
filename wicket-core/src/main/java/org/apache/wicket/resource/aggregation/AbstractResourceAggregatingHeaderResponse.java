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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;

/**
 * A header response that can be used to aggregate resources (primarily resource references) into
 * groups that can be rendered after the entire hierarchy of IHeaderContributors have been
 * traversed. A subclass of this could use that group to render a single URL to some aggregating
 * servlet (for example) that could cut down on the number of HTTP requests the client must make.
 * 
 * Resource references are aggregated according to the key that your subclass creates in the
 * {@link #newGroupingKey(ResourceReferenceAndStringData)}. This key is used in a Map, so it needs
 * to properly implement hashCode and equals.
 * 
 * If your key does not implement Comparable&lt;KeyClass&gt;, you need to also return a Comparator
 * for it from the {@link #getGroupingKeyComparator()} method.
 * 
 * @author Jeremy Thomerson
 * @param <R>
 *            the type of ResourceReferenceCollection returned by
 *            {@link #newResourceReferenceCollection(Object)} and passed to all the methods that
 *            take a ResourceReferenceCollection. You will typically just use
 *            ResourceReferenceCollection for this param, unless you are returning a specific type
 *            of ResourceReferenceCollection from your subclass.
 * @param <K>
 *            the class of the key that you will create from
 *            {@link #newGroupingKey(ResourceReferenceAndStringData)}
 */
public abstract class AbstractResourceAggregatingHeaderResponse<R extends ResourceReferenceCollection, K>
	extends DecoratingHeaderResponse
{

	private final List<ResourceReferenceAndStringData> topLevelReferences = new ArrayList<ResourceReferenceAndStringData>();

	/**
	 * Construct.
	 * 
	 * @param real
	 *            the wrapped header response
	 */
	public AbstractResourceAggregatingHeaderResponse(IHeaderResponse real)
	{
		super(real);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, null, null, null,
			ResourceType.JS, false, null, null));
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, String id)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, null, null, id,
			ResourceType.JS, false, null, null));
	}

	@Override
	public void renderCSSReference(ResourceReference reference)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, null, null, null,
			ResourceType.CSS, false, null, null));
	}

	@Override
	public void renderCSSReference(ResourceReference reference, String media)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, null, null, media,
			ResourceType.CSS, false, null, null));
	}

	@Override
	public void close()
	{
		// up until now, no ResourceReference objects have been passed to the real response. we need
		// to group them into top-level groups and then render those groups
		SortedMap<K, R> map = new TreeMap<K, R>(getGroupingKeyComparator());
		for (ResourceReferenceAndStringData ref : topLevelReferences)
		{
			K key = newGroupingKey(ref);
			R coll = map.get(key);
			if (coll == null)
			{
				map.put(key, coll = newResourceReferenceCollection(key));
			}
			coll.add(ref);
		}

		// now, render our groups to the real response
		Set<ResourceReferenceAndStringData> alreadyRendered = new LinkedHashSet<ResourceReferenceAndStringData>();
		for (Entry<K, R> entry : map.entrySet())
		{
			renderCollection(alreadyRendered, entry.getKey(), entry.getValue());
		}

		onAllCollectionsRendered(topLevelReferences);

		// finally, we close the real response
		super.close();
	}

	/* methods designed to be overridden if needed */
	/**
	 * creates a ResourceReferenceCollection. If you want a specific type of
	 * ResourceReferenceCollection for your subclass of
	 * {@link AbstractResourceAggregatingHeaderResponse}, override this method.
	 * 
	 * Note that because of the generics definition, you will probably have to cast to R. R is the
	 * parameter used when creating your subclass defining the type of ResourceReferenceCollection
	 * this returns and is passed into all methods that take a ResourceReferenceCollection
	 * 
	 * @param key
	 *            the grouping key that will be used for this collection. all references added to it
	 *            will have the same key
	 * @return a newly created collection to contain resource references
	 */
	@SuppressWarnings("unchecked")
	protected R newResourceReferenceCollection(K key)
	{
		return (R)new ResourceReferenceCollection();
	}

	/**
	 * This key is what is used to determine how to group (or aggregate) your resources. It must
	 * implement equals and hashCode correctly so that it can be used as a key in a HashMap. These
	 * methods must be implemented so that if two references are given over the course of the
	 * hierarchy traversal, and those two references should be grouped (or aggregated), the keys
	 * returned for each should equal each other and their hash codes should be equal as well.
	 * 
	 * Typical implementations should use whether or not the resource reference is CSS as their
	 * first grouping parameter, since you don't want to render JS and CSS in the same tag (one
	 * needs to be in a link tag and one in a script tag).
	 * 
	 * Note that if your grouping key class (K) does not implement Comparable&lt;K&gt;, you must
	 * also override {@link #getGroupingKeyComparator()} and return a valid comparator that sorts
	 * keys in the order you want references rendered.
	 * 
	 * @param ref
	 *            the resource reference with associated data that came from the render*Reference
	 *            methods
	 * @return a new key used to group the references.
	 */
	protected abstract K newGroupingKey(ResourceReferenceAndStringData ref);

	/**
	 * This comparator is used to sort the grouping keys that you return from
	 * {@link #newGroupingKey(ResourceReferenceAndStringData)}.
	 * 
	 * Note that if your grouping key class (K) implements Comparable&lt;K&gt;, you do not need to
	 * override this method.
	 * 
	 * @return a Comparator for K
	 */
	protected Comparator<K> getGroupingKeyComparator()
	{
		return null;
	}

	/**
	 * When the entire hierarchy has been traversed and {@link #close()} is called, we loop through
	 * the grouped collections and render them in this method. This method is typically overridden
	 * to render your collection how you want to render them.
	 * 
	 * For instance, if you want to aggregate your groups into a single HTTP request, you can
	 * override this method, create the URL to your aggregation servlet (or {@link IResource}), and
	 * then call <tt>getRealResponse().renderJavaScriptReference(yourUrl)</tt>, or the appropriate
	 * method to render the URL for a group of CSS references.
	 * 
	 * @param alreadyRendered
	 *            a set of resource references that have already been rendered in other groups
	 * @param key
	 *            they grouping key for this group
	 * @param coll
	 *            the collection of resource references to render
	 */
	protected void renderCollection(Set<ResourceReferenceAndStringData> alreadyRendered, K key,
		R coll)
	{
		for (ResourceReferenceAndStringData data : coll)
		{
			renderIfNotAlreadyRendered(alreadyRendered, data);
		}
	}

	/**
	 * Renders a single resource reference, only if it has not already been rendered. Note that you
	 * will typically not need to override this method. You should typically override
	 * {@link #render(ResourceReferenceAndStringData)} directly, which is called from this method if
	 * the resource reference has not been rendered elsewhere.
	 * 
	 * @param alreadyRendered
	 *            the set of references that have already been rendered in other groups
	 * @param data
	 *            the reference (and associated data) to conditionally render.
	 */
	protected void renderIfNotAlreadyRendered(Set<ResourceReferenceAndStringData> alreadyRendered,
		ResourceReferenceAndStringData data)
	{
		if (!alreadyRendered.contains(data))
		{
			render(data);
			alreadyRendered.add(data);
		}
	}

	/**
	 * Renders a single resource reference. This is called from
	 * {@link #renderIfNotAlreadyRendered(Set, ResourceReferenceAndStringData)} for references that
	 * had not been rendered elsewhere.
	 * 
	 * @param data
	 *            the reference (and associated data) to conditionally render.
	 */
	protected void render(ResourceReferenceAndStringData data)
	{
		ResourceUtil.renderTo(getRealResponse(), data);
	}

	/**
	 * After all the collections have been rendered, we call this callback so your subclass can add
	 * any other logic as needed. For instance, if you are aggregating YUI resources, your
	 * {@link #renderCollection(Set, Object, ResourceReferenceCollection)} method might have
	 * rendered only a YUI constructor that loaded all the JS files for each group. Then, you need
	 * to loop through the references again, and render any JS inside a sandboxed YUI.use()
	 * statement. You would render those here by creating the YUI.use statement, and call
	 * <tt>getHeaderResponse().renderJavaScript(yourJS, null)</tt>
	 * 
	 * @param allTopLevelReferences
	 *            all the references that were rendered by the developers
	 */
	protected void onAllCollectionsRendered(
		List<ResourceReferenceAndStringData> allTopLevelReferences)
	{

	}

	/* other interface methods: */
	@Override
	public void renderJavaScriptReference(String url)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, null,
			ResourceType.JS, false, null, null));
	}

	@Override
	public void renderJavaScriptReference(String url, String id)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, id,
			ResourceType.JS, false, null, null));
	}

	@Override
	public void renderCSSReference(String url)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, null,
			ResourceType.CSS, false, null, null));
	}

	@Override
	public void renderCSSReference(String url, String media)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, media,
			ResourceType.CSS, false, null, null));
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, PageParameters parameters,
		String id)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, parameters, null, id,
			ResourceType.JS, false, null, null));
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, PageParameters parameters,
		String id, boolean defer)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, parameters, null, id,
			ResourceType.JS, defer, null, null));
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, PageParameters parameters,
		String id, boolean defer, String charset)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, parameters, null, id,
			ResourceType.JS, defer, charset, null));
	}

	@Override
	public void renderJavaScriptReference(String url, String id, boolean defer)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, id,
			ResourceType.JS, defer, null, null));
	}

	@Override
	public void renderJavaScriptReference(String url, String id, boolean defer, String charset)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, id,
			ResourceType.JS, defer, charset, null));
	}

	@Override
	public void renderJavaScript(CharSequence javascript, String id)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(javascript, ResourceType.JS, id));
	}

	@Override
	public void renderCSS(CharSequence css, String media)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(css, ResourceType.CSS, media));
	}

	@Override
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, pageParameters, null,
			media, ResourceType.CSS, false, null, null));
	}

	@Override
	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media, String condition)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(reference, pageParameters, null,
			media, ResourceType.CSS, false, null, condition));
	}

	@Override
	public void renderCSSReference(String url, String media, String condition)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(null, null, url, media,
			ResourceType.CSS, false, null, condition));
	}

	@Override
	public void renderString(CharSequence string)
	{
		topLevelReferences.add(new ResourceReferenceAndStringData(string, ResourceType.PLAIN, null));
	}

	@Override
	public void renderOnDomReadyJavaScript(String javascript)
	{
		super.renderOnDomReadyJavaScript(javascript);
	}

	@Override
	public void renderOnLoadJavaScript(String javascript)
	{
		super.renderOnLoadJavaScript(javascript);
	}

	@Override
	public void renderOnEventJavaScript(String target, String event, String javascript)
	{
		super.renderOnEventJavaScript(target, event, javascript);
	}
}
