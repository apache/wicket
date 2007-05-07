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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IStringResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

/**
 * Panel that displays the result of rendering a <a
 * href="http://jakarta.apache.org/velocity">Velocity</a> template. The
 * template itself can be any
 * <code><a href="http://wicket.sourceforge.net/apidocs/wicket/util/resource/IStringResourceStream.html">IStringResourceStream</a></code>
 * implementation, of which there are a number of convenient implementations in
 * the wicket.util package. The model can be any normal
 * <code><a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/Map.html">Map</a></code>,
 * which will be used to create the
 * <code><a href="http://jakarta.apache.org/velocity/docs/api/org/apache/velocity/VelocityContext.html">VelocityContext</a></code>.
 * 
 * <p>
 * <b>Note:</b> Be sure to properly initialize the Velocity engine before using
 * <code>VelocityPanel</code>.
 * </p>
 */
public class VelocityPanel extends Panel
{
	/** Velocity template resource */
	private final IStringResourceStream templateResource;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            See Component
	 * @param templateResource
	 *            The velocity template as a string resource
	 * @param model
	 *            MapModel with variables that can be substituted by Velocity
	 */
	public VelocityPanel(final String name, final IStringResourceStream templateResource,
			final Model model)
	{
		super(name, model);
		this.templateResource = templateResource;
	}

	/**
	 * Gets a reader for the velocity template.
	 * 
	 * @return reader for the velocity template
	 */
	private Reader getTemplateReader()
	{
		final String template = templateResource.asString();
		if (template != null)
		{
			return new StringReader(template);
		}
		return null;
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
	private void onException(final Exception exception, final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		if (!throwVelocityExceptions())
		{
			// print the exception on the panel
			String stackTraceAsString = Strings.toString(exception);
			replaceComponentTagBody(markupStream, openTag, stackTraceAsString);
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
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		final Reader templateReader = getTemplateReader();
		if (templateReader != null)
		{
			// Get model as a map
			final Map map = (Map) getModelObject();

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
				String result = writer.toString();

				if (escapeHtml())
				{
					// encode the result in order to get valid html output that
					// does not break the rest of the page
					result = Strings.escapeMarkup(result).toString();
				}

				if (!parseGeneratedMarkup())
				{
					// now replace the body of the tag with the velocity merge
					// result
					replaceComponentTagBody(markupStream, openTag, result);
				}
				else
				{
					// now parse the velocity merge result
					Markup markup;
					try
					{
						MarkupParser parser = getApplication().getMarkupSettings()
								.getMarkupParserFactory().newMarkupParser(
										new MarkupResourceStream(
												new StringResourceStream(result)));
						markup = parser.parse();
					}
					catch (ResourceStreamNotFoundException e)
					{
						throw new RuntimeException(
								"Could not parse resulting markup from '"
										+ templateResource + "'", e);
					}
					renderAll(new MarkupStream(markup));
				}
			}
			catch (ParseErrorException e)
			{
				onException(e, markupStream, openTag);
			}
			catch (MethodInvocationException e)
			{
				onException(e, markupStream, openTag);
			}
			catch (ResourceNotFoundException e)
			{
				onException(e, markupStream, openTag);
			}
			catch (IOException e)
			{
				onException(e, markupStream, openTag);
			}
		}
		else
		{
			replaceComponentTagBody(markupStream, openTag, ""); // just empty it
		}
	}

	/**
	 * Gets whether to parse the resulting Wicket markup.
	 * 
	 * @return whether to parse the resulting Wicket markup. The default is
	 *         false.
	 */
	protected boolean parseGeneratedMarkup()
	{
		return false;
	}

	/**
	 * Whether any velocity exception should be trapped and displayed on the
	 * panel (false) or thrown up to be handled by the exception mechanism of
	 * Wicket (true). The default is false, which traps and displays any
	 * exception without having consequences for the other components on the
	 * page.
	 * <p>
	 * Trapping these exceptions without disturbing the other components is
	 * especially usefull in CMS like applications, where 'normal' users are
	 * allowed to do basic scripting. On errors, you want them to be able to
	 * have them correct them while the rest of the application keeps on
	 * working.
	 * </p>
	 * 
	 * @return Whether any velocity exceptions should be thrown or trapped. The
	 *         default is false.
	 */
	protected boolean throwVelocityExceptions()
	{
		return false;
	}
}
