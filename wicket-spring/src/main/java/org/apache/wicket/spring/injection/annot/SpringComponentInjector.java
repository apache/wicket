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
package org.apache.wicket.spring.injection.annot;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IBehaviorInstantiationListener;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.util.lang.Args;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {@link IComponentInstantiationListener} that injects component and behavior properties
 * annotated with {@link SpringBean} annotations.
 * 
 * To install in yourapplication.init() call
 * <code>getComponentInstantiationListeners().add(new SpringComponentInjector(this));</code>
 * <p>
 * Only Wicket {@link Component}s and {@link Behavior}s are automatically injected, other classes
 * such as {@link Session}, {@link Model}, and any other POJO can be injected by calling
 * <code>Injector.get().inject(this)</code> in their constructor.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 * 
 */
public class SpringComponentInjector extends Injector
	implements
		IComponentInstantiationListener,
		IBehaviorInstantiationListener
{
	private final IFieldValueFactory fieldValueFactory;

	/**
	 * Metadata key used to store application context in application's metadata
	 */
	private static MetaDataKey<ApplicationContext> CONTEXT_KEY = new MetaDataKey<ApplicationContext>()
	{
		private static final long serialVersionUID = 1L;

	};

	/**
	 * Constructor used when spring application context is declared in the spring standard way and
	 * can be located through
	 * {@link WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)}.
	 * 
	 * @param webapp
	 *            wicket web application
	 */
	public SpringComponentInjector(final WebApplication webapp)
	{
		this(webapp, getDefaultContext(webapp));
	}

	/**
	 * Constructor
	 * 
	 * @param webapp
	 *            wicket web application
	 * @param ctx
	 *            spring's application context
	 */
	public SpringComponentInjector(final WebApplication webapp, final ApplicationContext ctx)
	{
		this(webapp, ctx, true);
	}

	/**
	 * Constructor
	 * 
	 * @param webapp
	 *            wicket web application
	 * @param ctx
	 *            spring's application context
	 * 
	 * @param wrapInProxies
	 *            whether or not wicket should wrap dependencies with specialized proxies that can
	 *            be safely serialized. in most cases this should be set to true.
	 */
	public SpringComponentInjector(final WebApplication webapp, final ApplicationContext ctx,
		final boolean wrapInProxies)
	{
		Args.notNull(webapp, "webapp");

		Args.notNull(ctx, "ctx");

		// store context in application's metadata ...
		webapp.setMetaData(CONTEXT_KEY, ctx);
		fieldValueFactory = new AnnotProxyFieldValueFactory(new ContextLocator(), wrapInProxies);
		webapp.getBehaviorInstantiationListeners().add(this);
		bind(webapp);
	}

	@Override
	public void inject(final Object object)
	{
		inject(object, fieldValueFactory);
	}

	@Override
	public void onInstantiation(final Component component)
	{
		inject(component);
	}

	@Override
	public void onInstantiation(Behavior behavior)
	{
		inject(behavior);
	}

	/**
	 * A context locator that locates the context in application's metadata. This locator also keeps
	 * a transient cache of the lookup.
	 * 
	 * @author ivaynberg
	 */
	private static class ContextLocator implements ISpringContextLocator
	{
		private transient ApplicationContext context;

		private static final long serialVersionUID = 1L;

		@Override
		public ApplicationContext getSpringContext()
		{
			if (context == null)
			{
				context = Application.get().getMetaData(CONTEXT_KEY);
			}
			return context;
		}

	}

	/**
	 * Try to use an already pre-configured application context or locate it through Spring's default
	 * location mechanism.
	 * 
	 * @param webapp
	 * @return the application context to use for injection
	 */
	private static ApplicationContext getDefaultContext(final WebApplication webapp)
	{
		ApplicationContext context = webapp.getMetaData(CONTEXT_KEY);
		if (context == null)
		{
			context = WebApplicationContextUtils.getRequiredWebApplicationContext(webapp.getServletContext());
		}
		return context;
	}

	/**
	 * Set the default context for the given webapp.
	 * 
	 * @param webapp
	 *            web application
	 * @param context
	 *            context to use as default if non is explicitely specified for the injector
	 */
	public static void setDefaultContext(final WebApplication webapp, ApplicationContext context)
	{
		Args.notNull(context, "context");

		if (webapp.getMetaData(CONTEXT_KEY) == null)
		{
			webapp.setMetaData(CONTEXT_KEY, context);
		}
	}
}
