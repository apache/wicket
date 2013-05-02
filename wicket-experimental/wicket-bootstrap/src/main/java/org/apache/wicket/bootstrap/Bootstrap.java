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
package org.apache.wicket.bootstrap;

import java.util.List;

import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * Resources for <a href="http://twitter.github.com/bootstrap">Twitter Bootstrap</a>. This class
 * exposes a {@linkplain #plain() plain} and a {@linkplain #responsive() responsive} bootstrap
 * resource reference.
 * 
 * The resource references have the correct dependencies: jquery, bootstrap (responsive) stylesheet
 * and javascript. They will use the minified variants when running in deployment mode.
 * 
 * For your convenience you can also use {@link #renderHeadPlain(IHeaderResponse)} and
 * {@link #renderHeadResponsive(IHeaderResponse)} to write these references to the header.
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * public class BootstrapPage extends WebPage
 * {
 * 	public void renderHead(IHeaderResponse response)
 * 	{
 * 		Bootstrap.renderHeadResponsive(response);
 * 	}
 * }
 * </pre>
 */
public class Bootstrap
{
	/**
	 * Defines a resource reference for plain, non-responsive bootstrap. This resource reference
	 * depends on jquery, bootstrap.css and bootstrap.js
	 * 
	 * @return a bootstrap resource reference that includes everything necessary for bootstrap
	 */
	public static JavaScriptResourceReference plain()
	{
		return bootstrapPlain;
	}

	/**
	 * Defines a resource reference for responsive bootstrap. This resource reference depends on
	 * jquery, bootstrap.css, bootstrap-responsive.css and bootstrap.js
	 * 
	 * @return a bootstrap resource reference that includes everything necessary for responsive
	 *         bootstrap
	 */
	public static JavaScriptResourceReference responsive()
	{
		return bootstrapResponsive;
	}

	/**
	 * Convenience method for rendering a dependency on bootstrap (without responsive layout) in
	 * your header.
	 */
	public static void renderHeadPlain(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(Bootstrap.plain()));
	}

	/**
	 * Convenience method for rendering a dependency on bootstrap (with responsive layout) in your
	 * header.
	 */
	public static void renderHeadResponsive(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(Bootstrap.responsive()));
	}

	private static final BootstrapResourceReference bootstrapPlain = new BootstrapResourceReference();

	private static final BootstrapResponsiveResourceReference bootstrapResponsive = new BootstrapResponsiveResourceReference();

	public static final CssResourceReference BOOTSTRAP_CSS = new CssResourceReference(
		Bootstrap.class, "css/bootstrap.css");

	public static final CssResourceReference BOOTSTRAP_RESPONSIVE_CSS = new CssResourceReference(
		Bootstrap.class, "css/bootstrap-responsive.css");

	private Bootstrap()
	{
	}

	private static class BootstrapResourceReference extends JavaScriptResourceReference
	{
		private static final long serialVersionUID = 1L;

		public BootstrapResourceReference()
		{
			super(Bootstrap.class, "js/bootstrap.js");
		}

		@Override
		public List<HeaderItem> getDependencies()
		{
			HeaderItem jquery = JavaScriptHeaderItem.forReference(WicketEventJQueryResourceReference.get());
			HeaderItem stylesheet = CssHeaderItem.forReference(BOOTSTRAP_CSS);

			List<HeaderItem> dependencies = super.getDependencies();
			dependencies.add(jquery);
			dependencies.add(stylesheet);
			return dependencies;
		}
	}

	private static class BootstrapResponsiveResourceReference extends JavaScriptResourceReference
	{
		private static final long serialVersionUID = 1L;

		public BootstrapResponsiveResourceReference()
		{
			super(Bootstrap.class, "js/bootstrap.js");
		}

		@Override
		public List<HeaderItem> getDependencies()
		{
			HeaderItem jquery = JavaScriptHeaderItem.forReference(WicketEventJQueryResourceReference.get());
			HeaderItem stylesheet = CssHeaderItem.forReference(BOOTSTRAP_CSS);
			HeaderItem responsive = CssHeaderItem.forReference(BOOTSTRAP_RESPONSIVE_CSS);
			List<HeaderItem> dependencies = super.getDependencies();
			dependencies.add(jquery);
			dependencies.add(stylesheet);
			dependencies.add(responsive);
			return dependencies;
		}
	}
}
