package org.apache.wicket.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

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

	private static final String JMX_PREFIX = "ApacheWicket/";

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
	 * Marks the meter with the given name
	 * 
	 * @param name
	 *            the name of the meter to be marked
	 */
	protected void mark(String name)
	{
		if (WicketMetrics.enabled)
		{
			getMetricRegistry().meter(JMX_PREFIX + name).mark();
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
}
