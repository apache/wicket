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
import org.apache.wicket.IClusterable;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.ISpringContextLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {@link IComponentInstantiationListener} that injects component properties annotated with
 * {@link SpringBean} annotations.
 * 
 * To install in yourapplication.init() call
 * <code>addComponentInstantiationListener(new SpringComponentInjector(this));</code>
 * 
 * Non-wicket components such as {@link Session}, {@link Model}, and any other pojo can be
 * injected by calling <code>InjectorHolder.getInjector().inject(this)</code> in their
 * constructor.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 * 
 */
public class SpringComponentInjector extends ComponentInjector
{

	/**
	 * Metadata key used to store application context holder in application's metadata
	 */
	private static MetaDataKey CONTEXT_KEY = new MetaDataKey(ApplicationContextHolder.class)
	{

		private static final long serialVersionUID = 1L;

	};

	/**
	 * Constructor used when spring application context is declared in the spring standard way and
	 * can be located through
	 * {@link WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)}
	 * 
	 * @param webapp
	 *            wicket web application
	 */
	public SpringComponentInjector(WebApplication webapp)
	{
		// locate application context through spring's default location
		// mechanism and pass it on to the proper constructor
		this(webapp, WebApplicationContextUtils.getRequiredWebApplicationContext(webapp
				.getServletContext()));
	}

	/**
	 * Constructor
	 * 
	 * @param webapp
	 *            wicket web application
	 * @param ctx
	 *            spring's application context
	 */
	public SpringComponentInjector(WebApplication webapp, ApplicationContext ctx)
	{
		if (webapp == null)
		{
			throw new IllegalArgumentException("Argument [[webapp]] cannot be null");
		}

		if (ctx == null)
		{
			throw new IllegalArgumentException("Argument [[ctx]] cannot be null");
		}

		// store context in application's metadata ...
		webapp.setMetaData(CONTEXT_KEY, new ApplicationContextHolder(ctx));

		// ... and create and register the annotation aware injector
		InjectorHolder.setInjector(new AnnotSpringInjector(new ContextLocator()));
	}

	/**
	 * This is a holder for the application context. The reason we need a holder is that metadata
	 * only supports storing serializable objects but application context is not. The holder acts as
	 * a serializable wrapper for the context. Notice that although holder implements IClusterable
	 * it really is not because it has a reference to non serializable context - but this is ok
	 * because metadata objects in application are never serialized.
	 * 
	 * @author ivaynberg
	 * 
	 */
	private static class ApplicationContextHolder implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private final ApplicationContext context;

		/**
		 * Constructor
		 * 
		 * @param context
		 */
		public ApplicationContextHolder(ApplicationContext context)
		{
			this.context = context;
		}

		/**
		 * @return the context
		 */
		public ApplicationContext getContext()
		{
			return context;
		}
	}

	/**
	 * A context locator that locates the context in application's metadata. This locator also keeps
	 * a transient cache of the lookup.
	 * 
	 * @author ivaynberg
	 * 
	 */
	private static class ContextLocator implements ISpringContextLocator
	{
		private transient ApplicationContext context;

		private static final long serialVersionUID = 1L;

		public ApplicationContext getSpringContext()
		{
			if (context == null)
			{
				context = ((ApplicationContextHolder)Application.get().getMetaData(CONTEXT_KEY))
						.getContext();
			}
			return context;
		}

	}

}
