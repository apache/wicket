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
package org.apache.wicket.velocity;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * An IHeaderContributor implementation that renders a velocity template and writes it to the
 * response. The template is loaded via Velocity's resource loading mechanism, as defined in your
 * velocity.properties. If you do not have a velocity.properties for your app, it will default to a
 * directory "templates" in the root of your app.
 */
public class VelocityContributor extends Behavior
{
	private static final long serialVersionUID = 1L;

	private String encoding = "ISO-8859-1";

	private final IModel<? extends Map<String, Object>> model;

	private final String templateName;

	/**
	 * The templateName needs to have the full path relative to where the resource loader starts
	 * looking. For example, if there is a template next to this class in the package called foo.vm,
	 * and you have configured the ClassPathResourceLoader, template name will then be
	 * "wicket/contrib/util/resource/foo.vm". Wicket provides a nice utility
	 * {@link org.apache.wicket.util.lang.Packages} for this.
	 * 
	 * @param templateName
	 * @param model
	 */
	public VelocityContributor(final String templateName, final IModel<? extends Map<String, Object>> model)
	{
		Args.notNull(model, "model");

		this.templateName = templateName;
		this.model = model;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void detach(final Component c)
	{
		model.detach();
	}

	/**
	 * @return The encoding
	 */
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		CharSequence s = evaluate();
		if (null != s)
		{
			response.render(StringHeaderItem.forString(s));
		}
	}

	/**
	 * @param encoding
	 *            The encoding
	 */
	public void setEncoding(final String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * @return whether to escape HTML characters. The default value is false
	 */
	protected boolean escapeHtml()
	{
		return false;
	}

	/**
	 * Evaluate the template.
	 * 
	 * @return The evaluated template
	 */
	protected final CharSequence evaluate()
	{
		if (!Velocity.resourceExists(templateName))
		{
			return null;
		}

		// create a Velocity context object using the model if set
		final VelocityContext ctx = new VelocityContext(model.getObject());

		// create a writer for capturing the Velocity output
		StringWriter writer = new StringWriter();

		try
		{
			// execute the velocity script and capture the output in writer
			if (!Velocity.mergeTemplate(templateName, encoding, ctx, writer))
			{
				return null;
			}

			// replace the tag's body the Velocity output
			CharSequence result = writer.getBuffer();

			if (escapeHtml())
			{
				// encode the result in order to get valid html output that
				// does not break the rest of the page
				result = Strings.escapeMarkup(result.toString());
			}
			return result;
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Error while executing velocity script: " +
				templateName, e);
		}
	}
}
