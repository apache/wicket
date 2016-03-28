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
package org.apache.wicket.metrics.aspects.session;

import static org.apache.wicket.metrics.Initializer.METRICS_SERVLET_REGISTRY;
import static org.apache.wicket.metrics.Initializer.WICKET_METRICS_SETTINGS;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.codahale.metrics.MetricRegistry;
import org.apache.wicket.metrics.WicketMetricsSettings;


/**
 * Listener that counts the current active sessions
 * 
 * @author Tobias Soloschenko
 *
 */
@WebListener
public class SessionCountListener implements HttpSessionListener
{
	@Override
	public void sessionDestroyed(HttpSessionEvent event)
	{
		ServletContext servletContext = event.getSession().getServletContext();
		MetricRegistry metricRegistry = (MetricRegistry) servletContext.getAttribute(METRICS_SERVLET_REGISTRY);
		WicketMetricsSettings metricsSettings = (WicketMetricsSettings) servletContext.getAttribute(WICKET_METRICS_SETTINGS);
		if (metricRegistry != null)
		{
			dec(event, metricRegistry, metricsSettings);
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent event)
	{
		ServletContext servletContext = event.getSession().getServletContext();
		MetricRegistry metricRegistry = (MetricRegistry) servletContext.getAttribute(METRICS_SERVLET_REGISTRY);
		WicketMetricsSettings metricsSettings = (WicketMetricsSettings) servletContext.getAttribute(WICKET_METRICS_SETTINGS);
		if (metricRegistry != null)
		{
			inc(event, metricRegistry, metricsSettings);
		}
	}

	/**
	 * Used to wire an aspect around
	 *  @param event the http session event
	 * @param metricRegistry
	 * @param metricsSettings
	 */
	public void dec(HttpSessionEvent event, MetricRegistry metricRegistry, WicketMetricsSettings metricsSettings)
	{
		// NOOP for aspect usage
	}
	
	/**
	 * Used to wire an aspect around
	 *  @param event the http session event
	 * @param metricRegistry
	 * @param metricsSettings
	 */
	public void inc(HttpSessionEvent event, MetricRegistry metricRegistry, WicketMetricsSettings metricsSettings)
	{
		// NOOP for aspect usage
	}
}
