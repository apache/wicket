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

	@Override
	public void init(Application application)
	{
		WebApplication webApplication = (WebApplication) application;
		MetricRegistry metricRegistry = new MetricRegistry();
		application.setMetaData(WicketMetrics.METRIC_REGISTRY, metricRegistry);

		ServletContext servletContext = webApplication.getServletContext();
		servletContext.setAttribute(METRICS_SERVLET_REGISTRY, metricRegistry);
	}

	@Override
	public void destroy(Application application)
	{
		WebApplication webApplication = (WebApplication) application;
		webApplication.setMetaData(WicketMetrics.METRIC_REGISTRY, null);

		ServletContext servletContext = webApplication.getServletContext();
		servletContext.setAttribute(METRICS_SERVLET_REGISTRY, null);
	}
}
