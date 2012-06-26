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
package org.apache.wicket.request.resource;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.wicket.request.resource.ResourceReference.Key;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to register and lookup {@link ResourceReference}s per Application.
 *
 * @see org.apache.wicket.Application#getResourceReferenceRegistry()
 * @see org.apache.wicket.Application#newResourceReferenceRegistry()
 *
 * @author Matej Knopp
 * @author Juergen Donnerstag
 */
public class ResourceReferenceRegistry
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(ResourceReferenceRegistry.class);

	// Scan classes and its superclasses for static ResourceReference fields. For each
	// RR field found, the callback method is called and the RR gets registered. It's kind of
	// auto-register all RRs in your Component hierarchy.
	private ClassScanner scanner = new ClassScanner()
	{
		@Override
		boolean foundResourceReference(final ResourceReference reference)
		{
			// register the RR found (static field of Scope class)
			return registerResourceReference(reference);
		}
	};

	// The Map (registry) maintaining the resource references
	private final ConcurrentHashMap<Key, ResourceReference> map = Generics.newConcurrentHashMap();

	// If combinations of paramaters (Key) have no registered resource reference yet, a default
	// resource reference can be created and added to the registry. The following list keeps track
	// of all auto added references.
	private Queue<Key> autoAddedQueue;

	// max entries. If the queue is full and new references are auto generated, references are
	// removed starting with the first entry and unregistered from the registry.
	private int autoAddedCapacity = 1000;

	/**
	 * Construct.
	 */
	public ResourceReferenceRegistry()
	{
		// Initial the auto-add list for a maximum of 1000 entries
		setAutoAddedCapacity(autoAddedCapacity);
	}

	/**
	 * Registers the given {@link ResourceReference}.
	 * <p>
	 * {@link ResourceReference#canBeRegistered()} must return <code>true</code>. Else, the resource
	 * reference will not be registered.
	 *
	 * @param reference
	 * @return True, if the resource was registered successfully or has been registered previously
	 *         already.
	 */
	public final boolean registerResourceReference(final ResourceReference reference)
	{
		return null != _registerResourceReference(reference);
	}

	/**
	 * Registers the given {@link ResourceReference}.
	 * <p>
	 * {@link ResourceReference#canBeRegistered()} must return <code>true</code>. Else, the resource
	 * reference will not be registered.
	 *
	 * @param reference
	 * @return True, if the resource was registered successfully or has been registered previously
	 *         already.
	 */
	private final Key _registerResourceReference(final ResourceReference reference)
	{
		Args.notNull(reference, "reference");

		if (reference.canBeRegistered())
		{
			Key key = reference.getKey();
			map.putIfAbsent(key, reference);
			return key;
		}

		log.warn("{} cannot be added to the registry.", reference.getClass().getName());
		return null;
	}

	/**
	 * Unregisters the given {@link ResourceReference}.
	 *
	 * @param key
	 *            the {@link ResourceReference}'s identifier
	 * @return Null, if the registry did not contain an entry for the resource reference.
	 */
	public final ResourceReference unregisterResourceReference(final Key key)
	{
		Args.notNull(key, "key");

		// remove from registry
		ResourceReference removed = map.remove(key);

		// remove from auto-added list, in case the RR was auto-added
		if (autoAddedQueue != null)
		{
			autoAddedQueue.remove(key);
		}

		return removed;
	}

	/**
	 * Get a resource reference matching the parameters from the registry or if not found and
	 * requested, create an default resource reference and add it to the registry.
	 * <p>
	 * Part of the search is scanning the class (scope) and it's superclass for static
	 * ResourceReference fields. Found fields get registered automatically (but are different from
	 * auto-generated ResourceReferences).
	 *
	 * @see #createDefaultResourceReference(Class, String, Locale, String, String)
	 * @see ClassScanner
	 *
	 * @param scope
	 *            The scope of resource reference (e.g. the Component's class)
	 * @param name
	 *            The name of resource reference (e.g. filename)
	 * @param locale
	 *            see Component
	 * @param style
	 *            see Component
	 * @param variation
	 *            see Component
	 * @param strict
	 *            If true, "weaker" combination of scope, name, locale etc. are not tested
	 * @param createIfNotFound
	 *            If true a default resource reference is created if no entry can be found in the
	 *            registry. The newly created resource reference will be added to the registry.
	 * @return Either the resource reference found in the registry or, if requested, a resource
	 *         reference automatically created based on the parameters provided. The automatically
	 *         created resource reference will automatically be added to the registry.
	 */
	public final ResourceReference getResourceReference(final Class<?> scope, final String name,
		final Locale locale, final String style, final String variation, final boolean strict,
		final boolean createIfNotFound)
	{
		return getResourceReference(new Key(scope.getName(), name, locale, style, variation),
			strict, createIfNotFound);
	}

	/**
	 * Get a resource reference matching the parameters from the registry or if not found and
	 * requested, create an default resource reference and add it to the registry.
	 * <p>
	 * Part of the search is scanning the class (scope) and it's superclass for static
	 * ResourceReference fields. Found fields get registered automatically (but are different from
	 * auto-generated ResourceReferences).
	 *
	 * @see #createDefaultResourceReference(Class, String, Locale, String, String)
	 * @see ClassScanner
	 *
	 * @param key
	 *            The data making up the resource reference
	 * @param strict
	 *            If true, "weaker" combination of scope, name, locale etc. are not tested
	 * @param createIfNotFound
	 *            If true a default resource reference is created if no entry can be found in the
	 *            registry. The newly created resource reference will be added to the registry.
	 * @return Either the resource reference found in the registry or, if requested, a resource
	 *         reference automatically created based on the parameters provided. The automatically
	 *         created resource reference will automatically be added to the registry.
	 */
	public final ResourceReference getResourceReference(final Key key, final boolean strict,
		final boolean createIfNotFound)
	{
		ResourceReference resource = _getResourceReference(key.getScope(), key.getName(),
			key.getLocale(), key.getStyle(), key.getVariation(), strict);

		// Nothing found so far?
		if (resource == null)
		{
			// Scan the class (scope) and it's super classes for static fields containing resource
			// references. Such resources are registered as normal resource reference (not
			// auto-added).
			if (scanner.scanClass(key.getScopeClass()) > 0)
			{
				// At least one new resource reference got registered => Search the registry again
				resource = _getResourceReference(key.getScope(), key.getName(), key.getLocale(),
					key.getStyle(), key.getVariation(), strict);
			}

			// Still nothing found => Shall a new reference be auto-created?
			if ((resource == null) && createIfNotFound)
			{
				resource = addDefaultResourceReference(key);
			}
		}

		return resource;
	}

	/**
	 * Get a resource reference matching the parameters from the registry.
	 *
	 * @param scope
	 *            The scope of resource reference (e.g. the Component's class)
	 * @param name
	 *            The name of resource reference (e.g. filename)
	 * @param locale
	 *            see Component
	 * @param style
	 *            see Component
	 * @param variation
	 *            see Component
	 * @param strict
	 *            If true, "weaker" combination of scope, name, locale etc. are not tested
	 * @return Either the resource reference found in the registry or null if not found
	 */
	private final ResourceReference _getResourceReference(final String scope, final String name,
		final Locale locale, final String style, final String variation, final boolean strict)
	{
		// Create a registry key containing all of the relevant attributes
		Key key = new Key(scope, name, locale, style, variation);

		// Get resource reference matching exactly the attrs provided
		ResourceReference res = map.get(key);
		if ((res != null) || strict)
		{
			return res;
		}

		res = _getResourceReference(scope, name, locale, style, null, true);
		if (res == null)
		{
			res = _getResourceReference(scope, name, locale, null, variation, true);
		}
		if (res == null)
		{
			res = _getResourceReference(scope, name, locale, null, null, true);
		}
		if (res == null)
		{
			res = _getResourceReference(scope, name, null, style, variation, true);
		}
		if (res == null)
		{
			res = _getResourceReference(scope, name, null, style, null, true);
		}
		if (res == null)
		{
			res = _getResourceReference(scope, name, null, null, variation, true);
		}
		if (res == null)
		{
			res = _getResourceReference(scope, name, null, null, null, true);
		}
		return res;
	}

	/**
	 * Creates a default resource reference and registers it.
	 *
	 * @param key
	 * @return The default resource created
	 */
	private ResourceReference addDefaultResourceReference(final Key key)
	{
		// Can be subclassed to create other than PackagedResourceReference
		ResourceReference reference = createDefaultResourceReference(key);

		if (reference != null)
		{
			// number of RRs which can be auto-added is restricted (cache size). Remove entries, and
			// unregister excessive ones, if needed.
			enforceAutoAddedCacheSize(getAutoAddedCapacity());

			// Register the new RR
			_registerResourceReference(reference);

			// Add it to the auto-added list
			if (autoAddedQueue != null)
			{
				autoAddedQueue.add(key);
			}
		}
		else
		{
			log.warn("A ResourceReference wont be created for a resource with key [{}] because it cannot be located.", key);
		}
		return reference;
	}

	/**
	 * The Number of RRs which can be auto-added is restricted (cache size). Remove entries, and
	 * unregister excessive ones, if needed.
	 *
	 * @param maxSize
	 *            Remove all entries, head first, until auto-added cache is smaller than maxSize.
	 */
	private void enforceAutoAddedCacheSize(int maxSize)
	{
		if (autoAddedQueue != null)
		{
			while (autoAddedQueue.size() > maxSize)
			{
				// remove entry from auto-added list
				Key first = autoAddedQueue.remove();

				// remove entry from registry
				map.remove(first);
			}
		}
	}

	/**
	 * Creates a default resource reference in case no registry entry and it was requested to create
	 * one.
	 * <p>
	 * A {@link PackageResourceReference} will be created by default
	 *
	 * @param key
	 * @return The RR created or null if not successful
	 */
	protected ResourceReference createDefaultResourceReference(final Key key)
	{
		if (PackageResource.exists(key.getScopeClass(), key.getName(), key.getLocale(),
			key.getStyle(), key.getVariation()))
		{
			return new PackageResourceReference(key);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Set the cache size in number of entries
	 *
	 * @param autoAddedCapacity
	 *            A value < 0 will disable aging of auto-create resource references. They will be
	 *            created, added to the registry and live their until manually removed or the
	 *            application shuts down.
	 */
	public final void setAutoAddedCapacity(final int autoAddedCapacity)
	{
		// Disable aging of auto-added references?
		if (autoAddedCapacity < 0)
		{
			// unregister all auto-added references
			clearAutoAddedEntries();

			// disable aging from now on
			autoAddedQueue = null;
		}
		else
		{
			this.autoAddedCapacity = autoAddedCapacity;

			if (autoAddedQueue == null)
			{
				autoAddedQueue = new ConcurrentLinkedQueue<Key>();
			}
			else
			{
				// remove all extra entries if necessary
				enforceAutoAddedCacheSize(autoAddedCapacity);
			}
		}
	}

	/**
	 * Gets cache size in number of entries
	 *
	 * @return capacity
	 */
	public final int getAutoAddedCapacity()
	{
		return autoAddedCapacity;
	}

	/**
	 * Unregisters all auto-added Resource References
	 */
	public final void clearAutoAddedEntries()
	{
		enforceAutoAddedCacheSize(0);
	}

	/**
	 * @return Number of auto-generated (and registered) resource references.
	 */
	public final int getAutoAddedCacheSize()
	{
		return autoAddedQueue == null ? -1 : autoAddedQueue.size();
	}

	/**
	 * @return Number of registered resource references (normal and auto-generated)
	 */
	public final int getSize()
	{
		return map.size();
	}
}
