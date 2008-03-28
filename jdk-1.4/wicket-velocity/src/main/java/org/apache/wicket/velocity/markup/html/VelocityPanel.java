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
package org.apache.wicket.velocity.markup.html;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IStringResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

/**
 * Panel that displays the result of rendering a <a
 * href="http://jakarta.apache.org/velocity">Velocity</a> template. The template itself can be any
 * <code><a href="http://wicket.apache.org/wicket/apidocs/org/apache/wicket/util/resource/IStringResourceStream.html">IStringResourceStream</a></code>
 * implementation, of which there are a number of convenient implementations in the wicket.util
 * package. The model can be any normal
 * <code><a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/Map.html">Map</a></code>,
 * which will be used to create the
 * <code><a href="http://jakarta.apache.org/velocity/docs/api/org/apache/velocity/VelocityContext.html">VelocityContext</a></code>.
 * 
 * <p>
 * <b>Note:</b> Be sure to properly initialize the Velocity engine before using
 * <code>VelocityPanel</code>.
 * </p>
 */
public abstract class VelocityPanel extends Panel
		implements
			IMarkupResourceStreamProvider,
			IMarkupCacheKeyProvider
{
	/**
	 * Convenience factory method to create a {@link VelocityPanel} instance with a given
	 * {@link IStringResourceStream template resource}.
	 * 
	 * @param id
	 *            Component id
	 * @param model
	 *            optional model for variable substituation.
	 * @param templateResource
	 *            The template resource
	 * @return an instance of {@link VelocityPanel}
	 */
	public static VelocityPanel forTemplateResource(String id, IModel model,
			final IStringResourceStream templateResource)
	{
		if (templateResource == null)
		{
			throw new IllegalArgumentException("argument templateResource must be not null");
		}

		return new VelocityPanel(id, model)
		{
			protected IStringResourceStream getTemplateResource()
			{
				return templateResource;
			}
		};
	}

	private transient String stackTraceAsString;
	private transient String evaluatedTemplate;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            Component id
	 * @param templateResource
	 *            The velocity template as a string resource
	 * @param model
	 *            Model with variables that can be substituted by Velocity. Must return a
	 *            {@link Map}.
	 */
	public VelocityPanel(final String id, final IModel/* <Map> */model)
	{
		super(id, model);
	}

	/**
	 * Gets a reader for the velocity template.
	 * 
	 * @return reader for the velocity template
	 */
	private Reader getTemplateReader()
	{
		final IStringResourceStream resource = getTemplateResource();
		if (resource == null)
		{
			throw new IllegalArgumentException("getTemplateResource must return a resource");
		}

		final String template = resource.asString();
		if (template != null)
		{
			return new StringReader(template);
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		if (!Strings.isEmpty(stackTraceAsString))
		{
			// TODO: only display the velocity error/stacktrace in development mode?
			replaceComponentTagBody(markupStream, openTag, Strings
					.toMultilineMarkup(stackTraceAsString));
		}
		else if (!parseGeneratedMarkup())
		{
			// initialize evaluatedTemplate
			if (evaluatedTemplate == null)
			{
				getMarkupResourceStream(null, null);
			}
			replaceComponentTagBody(markupStream, openTag, evaluatedTemplate);
		}
		else
		{
			super.onComponentTagBody(markupStream, openTag);
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	protected void onBeforeRender()
	{
		// check that no components have been added in case the generated markup should not be
		// parsed
		if (!parseGeneratedMarkup() && size() > 0)
		{
			throw new WicketRuntimeException(
					"Components cannot be added if the generated markup should not be parsed.");
		}
		super.onBeforeRender();
	}

	/**
	 * Either print or rethrow the throwable.
	 * 
	 * @param exception
	 *            the cause
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the open tag
	 */
	private void onException(final Exception exception)
	{
		if (!throwVelocityExceptions())
		{
			// print the exception on the panel
			stackTraceAsString = Strings.toString(exception);
		}
		else
		{
			// rethrow the exception
			throw new WicketRuntimeException(exception);
		}
	}

	/**
	 * Gets whether to escape HTML characters.
	 * 
	 * @return whether to escape HTML characters. The default value is false.
	 */
	protected boolean escapeHtml()
	{
		return false;
	}

	/**
	 * Returns the template resource passed to the constructor.
	 * 
	 * @return The template resource
	 */
	protected abstract IStringResourceStream getTemplateResource();

	/**
	 * Evaluates the template and returns the result.
	 * 
	 * @param templateReader
	 *            used to read the template
	 * @return the result of evaluating the velocity template
	 */
	private String evaluateVelocityTemplate(Reader templateReader)
	{
		if (evaluatedTemplate == null)
		{
			// Get model as a map
			final Map map = (Map)getModelObject();

			// create a Velocity context object using the model if set
			final VelocityContext ctx = new VelocityContext(map);

			// create a writer for capturing the Velocity output
			StringWriter writer = new StringWriter();

			// string to be used as the template name for log messages in case
			// of error
			final String logTag = getId();
			try
			{
				// execute the velocity script and capture the output in writer
				Velocity.evaluate(ctx, writer, logTag, templateReader);

				// replace the tag's body the Velocity output
				evaluatedTemplate = writer.toString();

				if (escapeHtml())
				{
					// encode the result in order to get valid html output that
					// does not break the rest of the page
					evaluatedTemplate = Strings.escapeMarkup(evaluatedTemplate).toString();
				}
				return evaluatedTemplate;
			}
			catch (IOException e)
			{
				onException(e);
			}
			catch (ParseErrorException e)
			{
				onException(e);
			}
			catch (MethodInvocationException e)
			{
				onException(e);
			}
			catch (ResourceNotFoundException e)
			{
				onException(e);
			}
			return null;
		}
		return evaluatedTemplate;
	}

	/**
	 * Gets whether to parse the resulting Wicket markup.
	 * 
	 * @return whether to parse the resulting Wicket markup. The default is false.
	 */
	protected boolean parseGeneratedMarkup()
	{
		return false;
	}

	/**
	 * Whether any velocity exception should be trapped and displayed on the panel (false) or thrown
	 * up to be handled by the exception mechanism of Wicket (true). The default is false, which
	 * traps and displays any exception without having consequences for the other components on the
	 * page.
	 * <p>
	 * Trapping these exceptions without disturbing the other components is especially usefull in
	 * CMS like applications, where 'normal' users are allowed to do basic scripting. On errors, you
	 * want them to be able to have them correct them while the rest of the application keeps on
	 * working.
	 * </p>
	 * 
	 * @return Whether any velocity exceptions should be thrown or trapped. The default is false.
	 */
	protected boolean throwVelocityExceptions()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupResourceStreamProvider#getMarkupResourceStream(org.apache.wicket.MarkupContainer,
	 *      java.lang.Class)
	 */
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class containerClass)
	{
		Reader reader = getTemplateReader();
		if (reader == null)
		{
			throw new WicketRuntimeException("could not find velocity template for panel: " + this);
		}

		// evaluate the template and return a new StringResourceStream
		StringBuffer sb = new StringBuffer();
		sb.append("<wicket:panel>");
		sb.append(evaluateVelocityTemplate(reader));
		sb.append("</wicket:panel>");
		return new StringResourceStream(sb.toString());
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupCacheKeyProvider#getCacheKey(org.apache.wicket.MarkupContainer,
	 *      java.lang.Class)
	 */
	public String getCacheKey(MarkupContainer container, Class containerClass)
	{
		// don't cache the evaluated template
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();
		stackTraceAsString = null;
		evaluatedTemplate = null;
	}
}
