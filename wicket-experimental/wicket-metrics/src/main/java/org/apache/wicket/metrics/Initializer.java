package org.apache.wicket.metrics;

import javax.servlet.ServletContext;

import com.codahale.metrics.MetricRegistry;
import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Creates an instance of {@link MetricRegistry} and saves it in the
 * {@link Application#getMetaData(MetaDataKey) meta data} and in the
 * {@link ServletContext}
 */
public class Initializer implements IInitializer
{
	public static final String METRICS_SERVLET_REGISTRY = "com.codahale.metrics.servlets.MetricsServlet.registry";
	public static final String WICKET_METRICS_SETTINGS = WicketMetricsSettings.class.getName();

	@Override
	public void init(Application application)
	{
		WebApplication webApplication = (WebApplication) application;
		ServletContext servletContext = webApplication.getServletContext();

		MetricRegistry metricRegistry = (MetricRegistry) servletContext.getAttribute(METRICS_SERVLET_REGISTRY);
		WicketMetricsSettings metricsSettings = (WicketMetricsSettings) servletContext.getAttribute(WICKET_METRICS_SETTINGS);
		if (metricRegistry == null)
		{
			metricRegistry = new MetricRegistry();
			metricsSettings = new WicketMetricsSettings();
			servletContext.setAttribute(METRICS_SERVLET_REGISTRY, metricRegistry);
			servletContext.setAttribute(WICKET_METRICS_SETTINGS, metricsSettings);
		}

		application.setMetaData(WicketMetrics.METRIC_REGISTRY, metricRegistry);
		application.setMetaData(WicketMetrics.METRIC_SETTINGS, metricsSettings);
	}

	@Override
	public void destroy(Application application)
	{
		application.setMetaData(WicketMetrics.METRIC_REGISTRY, null);
		application.setMetaData(WicketMetrics.METRIC_SETTINGS, null);

		WebApplication webApplication = (WebApplication) application;
		ServletContext servletContext = webApplication.getServletContext();
		servletContext.setAttribute(METRICS_SERVLET_REGISTRY, null);
		servletContext.setAttribute(WICKET_METRICS_SETTINGS, null);
	}
}
