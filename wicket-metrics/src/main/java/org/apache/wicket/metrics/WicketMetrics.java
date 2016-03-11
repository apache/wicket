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

import org.aspectj.lang.ProceedingJoinPoint;

import com.codahale.metrics.JmxReporter;
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

	private static MetricRegistry metricRegistry;

	private static boolean enabled = true;

	private static final String PREFIX = "ApacheWicket/";

	/**
	 * Gets the metric registry
	 * 
	 * @return the metric registry
	 */
	public static MetricRegistry getMetricRegistry()
	{
		if (metricRegistry == null)
		{
			metricRegistry = new MetricRegistry();
		}
		return metricRegistry;
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
		if (WicketMetrics.enabled)
		{
			Context context = getMetricRegistry().timer(PREFIX + name + renderClassName(joinPoint))
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
		if (WicketMetrics.enabled)
		{
			getMetricRegistry().meter(PREFIX + name + renderClassName(joinPoint)).mark();
		}
		if (joinPoint != null)
		{
			return joinPoint.proceed();
		}
		return null;
	}

	/**
	 * Stops the contex quietly
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
	 * Starts the jmx reporter
	 */
	public static void startJmxReporter()
	{
		JmxReporter.forRegistry(getMetricRegistry()).build().start();
	}

	/**
	 * Stops the jmx reporter
	 */
	public static void stopJmxReporter()
	{
		JmxReporter.forRegistry(getMetricRegistry()).build().stop();
	}

	/**
	 * If the metrics should be enabled
	 * 
	 * @param enabled
	 *            if the metrics should be enabled
	 */
	public static void setEnabled(boolean enabled)
	{
		WicketMetrics.enabled = enabled;
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
}
