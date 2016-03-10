package org.apache.wicket.metrics.aspects;

import org.apache.wicket.metrics.WicketMetrics;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Aspect to handle basic web application information
 * 
 * @author Tobias Soloschenko
 */
@Aspect
public class ApplicationAspect extends WicketMetrics
{

	/**
	 * Collects data how often a request has been made against the web app
	 */
	@Before("call(* org.apache.wicket.protocol.http.WicketFilter.processRequest(..))")
	public void beforeRequestProcessed()
	{
		mark("core/application/request");
	}
}
