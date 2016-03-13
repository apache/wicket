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

	/** The key for metrics **/
	public static final MetaDataKey<WicketMetrics> METRICS = new MetaDataKey<WicketMetrics>()
	{
		private static final long serialVersionUID = 1L;
	};
	
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
	 * Creates the wicket metrics
	 */
	public WicketMetrics()
	{
		Application application = Application.get();
		application.setMetaData(METRICS, this);
		application.setMetaData(METRIC_SETTINGS, new WicketMetricsSettings());
	}

	/**
	 * Simply measure the time for a {@literal @}around
	 * 
	 * @param name
	 *            the name of the timer context
	 * @param joinPoint
	 *            the joinPoint to be proceed
	 * @return the value of the join point
	 * @throws Throwable
	 *             if there is an exception while execution
	 */
	public Object measureTime(String name, ProceedingJoinPoint joinPoint) throws Throwable
	{
		WicketMetricsSettings settings = getSettings();
		MetricRegistry registry = getMetricRegistry();
		
		if (settings.isEnabled())
		{
			Context context = registry
				.timer(settings.getPrefix() + name + renderClassName(joinPoint)).time();
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
		return joinPoint != null
			? "/" + joinPoint.getTarget().getClass().getName().replace('.', '_') : "";
	}

	/**
	 * Gets the metric registry
	 * 
	 * @return the metric registry
	 */
	private MetricRegistry getMetricRegistry()
	{
		Application application = Application.get();
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
	private WicketMetricsSettings getSettings()
	{
		Application application = Application.get();
		
		WicketMetricsSettings metricRegistry = application.getMetaData(METRIC_SETTINGS);
		if (metricRegistry == null)
		{
			metricRegistry = new WicketMetricsSettings();
			Application.get().setMetaData(METRIC_SETTINGS, metricRegistry);
		}
		return metricRegistry;
	}
}
