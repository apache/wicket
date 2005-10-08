/*
 * $Id$
 * $Revision$ $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.resource.DynamicByteArrayResource;
import wicket.util.file.Files;
import wicket.util.time.Duration;

/**
 * Class which holds shared resources. Resources can be shared by name. An
 * optional scope can be given to prevent naming conflicts and a locale and/or
 * style can be given as well.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Gili Tzabari
 */
public class SharedResources
{
	/** Logger */
	private static Log log = LogFactory.getLog(SharedResources.class);
	
	/**
	 * The state associated with each shared resource.
	 */
	private class ResourceState
	{
		/**
		 * The resource of this ResourceState;
		 */
		private Resource resource;

		/**
		 * The idle Task for this resource (if any) 
		 */
		private TimerTask idleTask;
		
		/**
		 * The cacheTimout Task for this resource (if any) 
		 */
		private TimerTask cacheTask;
		
		ResourceState(String key, Resource resource)
		{
			this.resource = resource;
			makeIdleTask(key);
			makeCacheTask(key);
		}

		private void makeCacheTask(String key)
		{
			if (resource instanceof DynamicByteArrayResource)
			{
				DynamicByteArrayResource dynamicResource = (DynamicByteArrayResource)resource;
				Duration cacheTimeout = dynamicResource.getCacheTimeout();
				long  milliseconds = cacheTimeout.getMilliseconds();
				if (milliseconds != 0)
				{
					this.cacheTask = getDynamicImageFlushTask(key, (DynamicByteArrayResource)resource);
					idleTimer.schedule(cacheTask, milliseconds);
				}
			}
		}

		private void makeIdleTask(String key)
		{
			Duration idleTimeout = resource.getIdleTimeout();
			long milliseconds = idleTimeout.getMilliseconds();
			if (milliseconds != 0)
			{
				this.idleTask = getResourceTimeoutTask(key);
				idleTimer.schedule(idleTask, milliseconds);
			}
		}

		/**
		 * Cancel all timer task (if any) 
		 */
		void cancel()
		{
			if (idleTask != null)
			{
				idleTask.cancel();
				idleTask = null;
			}
			if (cacheTask != null)
			{
				cacheTask.cancel();
				cacheTask = null;
			}
		}
		
		/**
		 * Touch this resource, update timer task (if any)
		 * @param key They key of the resource
		 */
		void touch(String key)
		{
			if (idleTask != null)
			{
				idleTask.cancel();
				makeIdleTask(key);
			}
			if (cacheTask != null)
			{
				cacheTask.cancel();
				makeCacheTask(key);
			}
		}
	}

	/** Map of shared resources states */
	private final Map resourceMap = new HashMap();
	
	/** Executes tasks when resources become idle */
	private final Timer idleTimer = new Timer(true);

	private final Application application;

	SharedResources(Application application)
	{
		this.application = application;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT. Inserts
	 * _[locale] and _[style] into path just before any extension that might
	 * exist.
	 * 
	 * @param path
	 *            The resource path
	 * @param locale
	 *            The locale
	 * @param style
	 *            The style (see {@link wicket.Session})
	 * @return The localized path
	 */
	public static String path(final String path, final Locale locale, final String style)
	{
		final StringBuffer buffer = new StringBuffer();
		final String extension = Files.extension(path);
		final String basePath = Files.basePath(path, extension);
		buffer.append(basePath);
		// first style because locale can append later on.
		if (style != null)
		{
			buffer.append('_');
			buffer.append(style);
		}
		if (locale != null)
		{
			buffer.append('_');
			buffer.append(locale.toString());
		}
		if (extension != null)
		{
			buffer.append('.');
			buffer.append(extension);
		}
		return buffer.toString();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param application
	 *            The application object
	 * @param scope
	 *            The scope of the resource
	 * @param path
	 *            The resource path
	 * @param locale
	 *            The locale
	 * @param style
	 *            The style (see {@link wicket.Session})
	 * @return The localized path
	 */
	public static String path(final Application application, final Class scope, final String path,
			final Locale locale, final String style)
	{
		return application.getPages().aliasForClass(scope) + '/' + path(path, locale, style);
	}

	/**
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link wicket.Session})
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final Class scope, final String name, final Locale locale,
			final String style, final Resource resource)
	{
		// Store resource
		final String key = path(application, scope, name, locale, style);
		ResourceState resourceState;
		synchronized (resourceMap)
		{
			resourceState = (ResourceState)resourceMap.get(key);
		}
		if (resourceState == null)
		{
			resourceState = new ResourceState(key, resource);
		}
		synchronized (resourceMap)
		{
			resourceMap.put(key, resourceState);
		}

		// shared resources CAN'T be cacheable by default.
		// The resource itself should take care of this.. (so package resource
		// can be cacheable by default but dynamic once not)
		// this is up to the user
		// Application shared resources are cacheable.
		// resource.setCacheable(true);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final Locale locale, final Resource resource)
	{
		add(Application.class, name, locale, null, resource);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final Resource resource)
	{
		add(Application.class, name, null, null, resource);
	}

	/**
	 * @param scope
	 *            The resource's scope
	 * @param name
	 *            Name of resource to get
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link wicket.Session})
	 * @param exact
	 *			  If true then only return the resource that is registerd for the given locale and style.
	 *  
	 * @return The logical resource
	 */
	public final Resource get(final Class scope, final String name, final Locale locale,
			final String style, boolean exact)
	{
		// 1. Look for fully qualified entry with locale and style
		if (locale != null && style != null)
		{
			final String key = path(application, scope, name, locale, style);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
			if (exact)
			{
				return null;
			}
		}

		// 2. Look for entry without style
		if (locale != null)
		{
			final String key = path(application, scope, name, locale, null);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
			if (exact) 
			{
				return null;
			}
		}

		// 3. Look for entry without locale
		if (style != null)
		{
			final String key = path(application, scope, name, null, style);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
			if (exact) 
			{
				return null;
			}
		}

		// 4. Look for base name with no locale or style
		final String key = path(application, scope, name, null, null);
		return get(key);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param key
	 *            Shared resource key
	 * @return The resource
	 */
	public final Resource get(final String key)
	{
		synchronized (resourceMap)
		{
			ResourceState resourceState = ((ResourceState)resourceMap.get(key));
			if (resourceState != null)
			{
				return resourceState.resource;
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Removes a shared resource.
	 * 
	 * @param key
	 *            Shared resource key
	 */
	public final void remove(final String key)
	{
		ResourceState state;
		synchronized (resourceMap)
		{
			state = (ResourceState)resourceMap.get(key);
			if (state == null)
			{
				return;
			}
			resourceMap.remove(key);
		}
		state.cancel();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT. Called
	 * when a resource is requested.
	 * 
	 * @param key
	 *            Shared resource key
	 */
	public final void onResourceRequested(final String key)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Resource " + key + " requested");
		}

		final ResourceState resourceState;
		synchronized (resourceMap)
		{
			resourceState = (ResourceState)resourceMap.get(key);
		}
		if (resourceState == null)
		{
			throw new IllegalArgumentException("No resource associated with key " + key);
		}
		resourceState.touch(key);
	}

	/**
	 * Returns a task which removes idle resources.
	 * @param key The key for which a TimerTaks must be made
	 * @return The TimerTask for the given key
	 */
	private TimerTask getResourceTimeoutTask(final String key)
	{
		return new TimerTask()
		{
			public void run()
			{
				if (log.isDebugEnabled())
				{
					log.debug("Resource " + key + " removed");
				}
				remove(key);
			}
		};
	}

	/**
	 * Returns a task which flushes a DynamicImageResource.
	 * 
	 * @param key The key of the resource
	 * @param resource
	 *            The DynamicImageResource where a TimerTaks must be made for.
	 * @return The TimerTaks for that Dymamic Image
	 */
	private TimerTask getDynamicImageFlushTask(final String key, final DynamicByteArrayResource resource)
	{
		return new TimerTask()
		{
			public void run()
			{
				// Someone might hit the image while we flush its cache
				synchronized (resource)
				{
					if (log.isDebugEnabled())
					{
						log.debug("Resource " + key + " flushed");
					}
					resource.invalidate();
				}
			}
		};
	}
}
