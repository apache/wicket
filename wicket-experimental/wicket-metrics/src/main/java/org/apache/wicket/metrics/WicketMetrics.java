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
import org.apache.wicket.WicketRuntimeException;
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

	/**
	 * The name of the filter the metrics are going to collect of
	 */
	private static String filterName;

	private static final String APPLICATION_ERROR = "The application couldn't be resolved, please ensure to apply \"<aspect name=\"org.apache.wicket.metrics.aspects.WicketFilterInitAspect\" />\" to your aspects";

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
				return proceedSilent(joinPoint);
			}
			finally
			{
				stopQuietly(context);
			}
		}
		else
		{
			return proceedSilent(joinPoint);
		}
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
	 * Creates a count of the given arguments
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
	public Object count(String name, ProceedingJoinPoint joinPoint,
		CounterOperation counterOperation, Long value) throws Throwable
	{
		WicketMetricsSettings settings = getSettings();
		MetricRegistry registry = getMetricRegistry();

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
		return proceedSilent(joinPoint);
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
		return proceedSilent(joinPoint);
	}

	/**
	 * Proceed the join point silent
	 * 
	 * @param joinPoint
	 *            the join point to proceed
	 * @return the result of the proceeded join point
	 * @throws Throwable
	 *             if the invocation throws an error
	 */
	private Object proceedSilent(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return joinPoint != null ? joinPoint.proceed() : null;
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
		Application application = Application.get(getFilterName());
		if (application == null)
		{
			throw new WicketRuntimeException(APPLICATION_ERROR);
		}
		MetricRegistry metricRegistry = application.getMetaData(METRIC_REGISTRY);
		if (metricRegistry == null)
		{
			metricRegistry = new MetricRegistry();
			application.setMetaData(METRIC_REGISTRY, metricRegistry);
		}
		return metricRegistry;
	}

	/**
	 * Gets the wicket metrics settings
	 * 
	 * @return the wicket metrics settings
	 */
	public static synchronized WicketMetricsSettings getSettings()
	{
		Application application = Application.get(getFilterName());
		if (application == null)
		{
			throw new WicketRuntimeException(APPLICATION_ERROR);
		}
		WicketMetricsSettings wicketMetricsSettings = application.getMetaData(METRIC_SETTINGS);
		if (wicketMetricsSettings == null)
		{
			wicketMetricsSettings = new WicketMetricsSettings();
			wicketMetricsSettings.setPrefix(getFilterName());
			application.setMetaData(METRIC_SETTINGS, wicketMetricsSettings);
		}
		return wicketMetricsSettings;
	}

	/**
	 * Gets the filter name the application should be resolved with
	 * 
	 * @return the filter name
	 */
	public static String getFilterName()
	{
		return filterName;
	}

	/**
	 * Sets the filter name the application should be resolved with
	 * 
	 * @param filterName
	 *            the filter name
	 */
	public static void setFilterName(String filterName)
	{
		WicketMetrics.filterName = filterName;
	}
}
