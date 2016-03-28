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
package org.apache.wicket.metrics;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.aspectj.lang.ProceedingJoinPoint;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;

/**
 * Base aspect provides access to the metric registry
 * 
 * @author Tobias Soloschenko
 *
 */
public class WicketMetrics
{

	private static final String APPLICATION_NAME_PROPERTY = "wicket.metrics.applicationName";

	private static final String METRICS_STATIC_REGISTRATION = "wicket.metrics.staticRegistration";
	/**
	 * if the application has been resolved
	 */
	private static boolean applicationResolved;

	/**
	 * The application
	 */
	private static Application application;

	/**
	 * Fall back if the application couldn't be resolved the registry is stored static
	 */
	private static MetricRegistry metricRegistry;

	/**
	 * Fall back if the application couldn't be resolved the settings are stored static
	 */
	private static WicketMetricsSettings wicketMetricsSettings;

	/** The key for metrics registry **/
	public static final MetaDataKey<MetricRegistry> METRIC_REGISTRY = new MetaDataKey<MetricRegistry>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** The key for metrics registry **/
	public static final MetaDataKey<WicketMetricsSettings> METRIC_SETTINGS = new MetaDataKey<WicketMetricsSettings>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Simply measure the time for a {@literal @}around
	 * 
	 * @param name
	 *            the name of the timer context
	 * @param joinPoint
	 *            the joinPoint to be proceed
	 * 
	 * @return the value of the join point
	 * @throws Throwable
	 *             if there is an exception while execution
	 * @see org.apache.wicket.metrics.WicketMetrics#measureTime(String, ProceedingJoinPoint,
	 *      boolean)
	 */
	public Object measureTime(String name, ProceedingJoinPoint joinPoint) throws Throwable
	{
		return this.measureTime(name, joinPoint, true);
	}

	/**
	 * Simply measure the time for a {@literal @}around
	 * 
	 * @param name
	 *            the name of the timer context
	 * @param joinPoint
	 *            the joinPoint to be proceed
	 * @param renderClass
	 *            if the class name should be rendered behind the metric path
	 * 
	 * @return the value of the join point
	 * @throws Throwable
	 *             if there is an exception while execution
	 */
	public Object measureTime(String name, ProceedingJoinPoint joinPoint, boolean renderClass)
		throws Throwable
	{
		WicketMetricsSettings settings = getSettings();
		MetricRegistry registry = getMetricRegistry();

		if (settings.isEnabled())
		{
			Context context = registry
				.timer(
					settings.getPrefix() + name + (renderClass ? renderClassName(joinPoint) : ""))
				.time();
			try
			{
				return joinPoint.proceed();
			}
			finally
			{
				stopQuietly(context);
			}
		}
		else
		{
			return joinPoint.proceed();
		}
	}

	/**
	 * 
	 * @author Tobias Soloschenko
	 *
	 */
	public enum CounterOperation {
		/**
		 * Increments
		 */
		INC,
		/**
		 * Decrements
		 */
		DEC
	}

	/**
	 * Creates a histogram of the given arguments
	 * 
	 * @param name
	 *            the name of the meter to be marked
	 * @param joinPoint
	 *            the join point
	 * @param counterOperation
	 *            the operation
	 * @param value
	 *            the value to update the counter
	 * @return the result of the proceeded join point
	 * @throws Throwable
	 */
	public Object counter(String name, ProceedingJoinPoint joinPoint,
		CounterOperation counterOperation, Long value) throws Throwable
	{
		WicketMetricsSettings settings = getSettings();
		MetricRegistry registry = getMetricRegistry();
		return counter(name, joinPoint, counterOperation, value, registry, settings);
	}

	/**
	 * Creates a histogram of the given arguments
	 *
	 * @param name
	 *            the name of the meter to be marked
	 * @param joinPoint
	 *            the join point
	 * @param counterOperation
	 *            the operation
	 * @param value
	 *            the value to update the counter
	 * @param registry
	 *            the registry with the metrics
	 * @param settings
	 *            the Wicket metrics settings
	 * @return the result of the proceeded join point
	 * @throws Throwable
	 */
	public Object counter(String name, ProceedingJoinPoint joinPoint,
	                      CounterOperation counterOperation, Long value,
	                      MetricRegistry registry, WicketMetricsSettings settings) throws Throwable
	{
		if (settings.isEnabled())
		{
			Counter counter = registry
				.counter(settings.getPrefix() + name + renderClassName(joinPoint));
			if (counterOperation == CounterOperation.INC)
			{
				counter.inc(value);
			}
			else
			{
				counter.dec(value);
			}
		}
		if (joinPoint != null)
		{
			return joinPoint.proceed();
		}
		return null;
	}


	/**
	 * Marks the meter with the given name
	 * 
	 * @param name
	 *            the name of the meter to be marked
	 * @param joinPoint
	 *            the join point
	 * @return the result of the proceeded join point
	 * @throws Throwable
	 */
	public Object mark(String name, ProceedingJoinPoint joinPoint) throws Throwable
	{
		WicketMetricsSettings settings = getSettings();
		MetricRegistry registry = getMetricRegistry();

		if (settings.isEnabled())
		{
			registry.meter(settings.getPrefix() + name + renderClassName(joinPoint)).mark();
		}
		if (joinPoint != null)
		{
			return joinPoint.proceed();
		}
		return null;
	}

	/**
	 * Stops the context quietly
	 * 
	 * @param context
	 *            the context to stop
	 */
	public void stopQuietly(Context context)
	{
		if (context != null)
		{
			context.stop();
		}
	}

	/**
	 * Renders the class name of the given join point
	 * 
	 * @param joinPoint
	 *            the join point to get the class of
	 * @return the class name representation
	 */
	public String renderClassName(ProceedingJoinPoint joinPoint)
	{
		return joinPoint != null && joinPoint.getTarget() != null
			? "/" + joinPoint.getTarget().getClass().getName().replace('.', '_') : "";
	}

	/**
	 * Gets the metric registry
	 * 
	 * @return the metric registry
	 */
	public static synchronized MetricRegistry getMetricRegistry()
	{
		if (!applicationResolved)
		{
			application = getApplication();
			applicationResolved = true;
		}
		if (application != null && System.getProperty(METRICS_STATIC_REGISTRATION) == null)
		{
			MetricRegistry metricRegistry = application.getMetaData(METRIC_REGISTRY);
			if (metricRegistry == null)
			{
				metricRegistry = new MetricRegistry();
				application.setMetaData(METRIC_REGISTRY, metricRegistry);
			}
			return metricRegistry;
		}
		else
		{
			if (WicketMetrics.metricRegistry == null)
			{
				WicketMetrics.metricRegistry = new MetricRegistry();
			}
			return WicketMetrics.metricRegistry;
		}
	}

	/**
	 * Gets the wicket metrics settings
	 * 
	 * @return the wicket metrics settings
	 */
	public static synchronized WicketMetricsSettings getSettings()
	{
		if (!applicationResolved)
		{
			application = getApplication();
			applicationResolved = true;
		}
		if (application != null && System.getProperty(METRICS_STATIC_REGISTRATION) == null)
		{
			WicketMetricsSettings wicketMetricsSettings = application.getMetaData(METRIC_SETTINGS);
			if (wicketMetricsSettings == null)
			{
				wicketMetricsSettings = new WicketMetricsSettings();
				application.setMetaData(METRIC_SETTINGS, wicketMetricsSettings);
			}
			return wicketMetricsSettings;
		}
		else
		{
			if (wicketMetricsSettings == null)
			{
				wicketMetricsSettings = new WicketMetricsSettings();
			}
			return wicketMetricsSettings;
		}
	}

	/**
	 * Gets the application. First it tries to resolve the application with Application.get(String)
	 * - the String is resolved by the system property "wicket.applicationName". If the application
	 * can't be found by the corresponding name a Application.get() will be invoked to resolve it.
	 * 
	 * 
	 * @return the application or null if the application can't be resolved via get() or get(String)
	 */
	private static Application getApplication()
	{
		Application application = getApplicationBySystemProperty();
		if (application == null)
		{
			application = getApplicationFromThreadLocal();
		}
		return application;
	}

	/**
	 * Gets the application from thread local
	 * 
	 * @return the application or null if not available
	 */
	private static Application getApplicationFromThreadLocal()
	{
		Application application = null;
		if (Application.exists())
		{
			application = Application.get();
		}
		return application;
	}

	/**
	 * Gets the application by the system property wicket.applicationName
	 * 
	 * @return the application or null if not available
	 */
	private static Application getApplicationBySystemProperty()
	{
		Application application = null;
		String applicatioName = System.getProperty(APPLICATION_NAME_PROPERTY);
		if (applicatioName != null)
		{
			application = Application.get(applicatioName);
		}
		return application;
	}
}
