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
package org.apache.wicket.model;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.PropertyVariableInterpolator;


/**
 * This model class encapsulates the full power of localization support within the Wicket framework.
 * It combines the flexible Wicket resource loading mechanism with property expressions, property
 * models and standard Java <code>MessageFormat</code> substitutions. This combination should be
 * able to solve any dynamic localization requirement that a project has.
 * <p>
 * The model should be created with four parameters, which are described in detail below:
 * <ul>
 * <li><b>resourceKey </b>- This is the most important parameter as it contains the key that should
 * be used to obtain resources from any string resource loaders. This parameter is mandatory: a null
 * value will throw an exception. Typically it will contain an ordinary string such as
 * &quot;label.username&quot;. To add extra power to the key functionality the key may also contain
 * a property expression which will be evaluated if the model parameter (see below) is not null.
 * This allows keys to be changed dynamically as the application is running. For example, the key
 * could be &quot;product.${product.id}&quot; which prior to rendering will call
 * model.getObject().getProduct().getId() and substitute this value into the resource key before is
 * is passed to the loader.
 * <li><b>component </b>- This parameter should be a component that the string resource is relative
 * to. In a simple application this will usually be the Page on which the component resides. For
 * reusable components/containers that are packaged with their own string resource bundles it should
 * be the actual component/container rather than the page. For more information on this please see
 * {@link org.apache.wicket.resource.loader.ComponentStringResourceLoader}. The relative component
 * may actually be {@code null} if this model is wrapped on assignment (
 * {@link IComponentAssignedModel}) or when all resource loading is to be done from a global
 * resource loader. However, we recommend that a relative component is still supplied even in the
 * latter case in order to 'future proof' your application with regards to changing resource loading
 * strategies.
 * <li><b>model </b>- This parameter is mandatory if either the resourceKey, the found string
 * resource (see below) or any of the substitution parameters (see below) contain property
 * expressions. Where property expressions are present they will all be evaluated relative to this
 * model object. If there are no property expressions present then this model parameter may be
 * <code>null</code>
 * <li><b>parameters </b>- The parameters parameter allows an array of objects to be passed for
 * substitution on the found string resource (see below) using a standard
 * <code>java.text.MessageFormat</code> object. Each parameter may be an ordinary Object, in which
 * case it will be processed by the standard formatting rules associated with
 * <code>java.text.MessageFormat</code>. Alternatively, the parameter may be an instance of
 * <code>IModel</code> in which case the <code>getObject()</code> method will be applied prior to
 * the parameter being passed to the <code>java.text.MessageFormat</code>. This allows such features
 * dynamic parameters that are obtained using a <code>PropertyModel</code> object or even nested
 * string resource models.
 * </ul>
 * As well as the supplied parameters, the found string resource can contain formatting information.
 * It may contain property expressions in which case these are evaluated using the model object
 * supplied when the string resource model is created. The string resource may also contain
 * <code>java.text.MessageFormat</code> style markup for replacement of parameters. Where a string
 * resource contains both types of formatting information then the property expression will be
 * applied first.
 * <p>
 * <b>Example 1 </b>
 * <p>
 * In its simplest form, the model can be used as follows:
 * 
 * <pre>
 * public MyPage extends WebPage&lt;Void&gt;
 * {
 *    public MyPage(final PageParameters parameters)
 *    {
 *        add(new Label(&quot;username&quot;, new StringResourceModel(&quot;label.username&quot;, this, null)));
 *    }
 * }
 * </pre>
 * 
 * Where the resource bundle for the page contains the entry <code>label.username=Username</code>
 * <p>
 * <b>Example 2 </b>
 * <p>
 * In this example, the resource key is selected based on the evaluation of a property expression:
 * 
 * <pre>
 * public MyPage extends WebPage&lt;Void&gt;
 * {
 *     public MyPage(final PageParameters parameters)
 *     {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *             new StringResourceModel(&quot;weather.${currentStatus}&quot;, this, new Model&lt;String&gt;(ws)));
 *     }
 * }
 * </pre>
 * 
 * Which will call the WeatherStation.getCurrentStatus() method each time the string resource model
 * is used and where the resource bundle for the page contains the entries:
 * 
 * <pre>
 * weather.sunny=Don't forget sunscreen!
 * weather.raining=You might need an umbrella
 * weather.snowing=Got your skis?
 * weather.overcast=Best take a coat to be safe
 * </pre>
 * 
 * <p>
 * <b>Example 3 </b>
 * <p>
 * In this example the found resource string contains a property expression that is substituted via
 * the model:
 * 
 * <pre>
 * public MyPage extends WebPage&lt;Void&gt;
 * {
 *     public MyPage(final PageParameters parameters)
 *     {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *             new StringResourceModel(&quot;weather.message&quot;, this, new Model&lt;String&gt;(ws)));
 *     }
 * }
 * </pre>
 * 
 * Where the resource bundle contains the entry <code>weather.message=Weather station reports that
 * the temperature is ${currentTemperature} ${units}</code>
 * <p>
 * <b>Example 4 </b>
 * <p>
 * In this example, the use of substitution parameters is employed to format a quite complex message
 * string. This is an example of the most complex and powerful use of the string resource model:
 * 
 * <pre>
 * public MyPage extends WebPage&lt;Void&gt;
 * {
 *     public MyPage(final PageParameters parameters)
 *     {
 *         WeatherStation ws = new WeatherStation();
 *         IModel&lt;WeatherStation&gt; model = new Model&lt;WeatherStation&gt;(ws);
 *         add(new Label(&quot;weatherMessage&quot;,
 *             new StringResourceModel(
 *                 &quot;weather.detail&quot;, this, model,
 *                     new Object[]
 *                     {
 *                         new Date(),
 *                         new PropertyModel&lt;?&gt;(model, &quot;currentStatus&quot;),
 *                         new PropertyModel&lt;?&gt;(model, &quot;currentTemperature&quot;),
 *                         new PropertyModel&lt;?&gt;(model, &quot;units&quot;)
 *         }));
 *     }
 * }
 * </pre>
 * 
 * And where the resource bundle entry is:
 * 
 * <pre>
 * weather.detail=The report for {0,date}, shows the temperature as {2,number,###.##} {3} \
 *     and the weather to be {1}
 * </pre>
 * 
 * @see ComponentStringResourceLoader for additional information especially on the component search
 *      order
 * 
 * @author Chris Turner
 */
public class StringResourceModel extends LoadableDetachableModel<String>
	implements
		IComponentAssignedModel<String>
{
	private static final long serialVersionUID = 1L;

	/** The wrapped model. */
	private final IModel<?> model;

	/** Optional parameters. */
	private final Object[] parameters;

	/** The relative component used for lookups. */
	private final Component component;

	/** The key of message to get. */
	private final String resourceKey;

	/** The default value of the message. */
	private final String defaultValue;

	public IWrapModel<String> wrapOnAssignment(Component component)
	{
		return new AssignmentWrapper(component);
	}

	private class AssignmentWrapper extends LoadableDetachableModel<String>
		implements
			IWrapModel<String>
	{
		private static final long serialVersionUID = 1L;

		private final Component component;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		public AssignmentWrapper(Component component)
		{
			this.component = component;
		}

		@Override
		public void detach()
		{
			super.detach();

			StringResourceModel.this.detach();
		}

		@Override
		protected void onDetach()
		{
			if (StringResourceModel.this.component == null)
			{
				StringResourceModel.this.onDetach();
			}
		}

		@Override
		protected String load()
		{
			if (StringResourceModel.this.component != null)
			{
				// ignore assignment if component was specified explicitely
				return StringResourceModel.this.getObject();
			}
			else
			{
				return getString(component);
			}
		}

		@Override
		public void setObject(String object)
		{
			StringResourceModel.this.setObject(object);
		}

		public IModel<String> getWrappedModel()
		{
			return StringResourceModel.this;
		}
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param model
	 *            The model to use for property substitutions
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 */
	public StringResourceModel(final String resourceKey, final Component component,
		final IModel<?> model, final Object... parameters)
	{
		this(resourceKey, component, model, null, parameters);
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The relative component parameter should generally be supplied, as without it resources can
	 * not be obtained from resource bundles that are held relative to a particular component or
	 * page. However, for application that use only global resources then this parameter may be
	 * null.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param component
	 *            The component that the resource is relative to
	 * @param model
	 *            The model to use for property substitutions
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 */
	public StringResourceModel(final String resourceKey, final Component component,
		final IModel<?> model, final String defaultValue, final Object... parameters)
	{
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Resource key must not be null");
		}
		this.resourceKey = resourceKey;
		this.component = component;
		this.model = model;
		this.defaultValue = defaultValue;
		this.parameters = parameters;
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            The model to use for property substitutions
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 */
	public StringResourceModel(final String resourceKey, final IModel<?> model,
		final Object... parameters)
	{
		this(resourceKey, null, model, null, parameters);
	}

	/**
	 * Creates a new string resource model using the supplied parameters.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if value substitutions are
	 * to take place on either the resource key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for substitutions.
	 * 
	 * @param resourceKey
	 *            The resource key for this string resource
	 * @param model
	 *            The model to use for property substitutions
	 * @param parameters
	 *            The parameters to substitute using a Java MessageFormat object
	 * @param defaultValue
	 *            The default value if the resource key is not found.
	 */
	public StringResourceModel(final String resourceKey, final IModel<?> model,
		final String defaultValue, final Object... parameters)
	{
		this(resourceKey, null, model, defaultValue, parameters);
	}


	/**
	 * Gets the localizer that is being used by this string resource model.
	 * 
	 * @return The localizer
	 */
	public Localizer getLocalizer()
	{
		return Application.get().getResourceSettings().getLocalizer();
	}


	/**
	 * Gets the string currently represented by this model. The string that is returned may vary for
	 * each call to this method depending on the values contained in the model and an the parameters
	 * that were passed when this string resource model was created.
	 * 
	 * @return The string
	 */
	public final String getString()
	{
		return getString(component);
	}

	private String getString(final Component component)
	{

		final Localizer localizer = getLocalizer();
		final Locale locale;
		if (component != null)
		{
			locale = component.getLocale();
		}
		else
		{
			locale = Session.exists() ? Session.get().getLocale() : Locale.getDefault();
		}

		String value;

		// Substitute any parameters if necessary
		Object[] parameters = getParameters();
		if (parameters == null || parameters.length == 0)
		{
			// Get the string resource, doing any property substitutions as part
			// of the get operation
			value = localizer.getString(getResourceKey(), component, model, defaultValue);
			if (value == null)
			{
				value = defaultValue;
			}
		}
		else
		{
			// Get the string resource, doing not any property substitutions
			// that has to be done later after MessageFormat
			value = localizer.getString(getResourceKey(), component, null, defaultValue);
			if (value == null)
			{
				value = defaultValue;
			}
			if (value != null)
			{
				// Build the real parameters
				Object[] realParams = new Object[parameters.length];
				for (int i = 0; i < parameters.length; i++)
				{
					if (parameters[i] instanceof IModel<?>)
					{
						realParams[i] = ((IModel<?>)parameters[i]).getObject();
					}
					else if (model != null && parameters[i] instanceof String)
					{
						realParams[i] = localizer.substitutePropertyExpressions(component,
							(String)parameters[i], model);
					}
					else
					{
						realParams[i] = parameters[i];
					}
				}

				// Escape all single quotes outside {..}
				if (value.indexOf('\'') != -1)
				{
					value = escapeQuotes(value);
				}

				if (model != null)
				{
					// First escape all substitute properties so that message format doesn't try to
					// parse that.
					value = Strings.replaceAll(value, "${", "$'{'").toString();
				}

				// Apply the parameters
				final MessageFormat format = new MessageFormat(value, locale);
				value = format.format(realParams);

				if (model != null)
				{
					// un escape the substitute properties
					value = Strings.replaceAll(value, "$'{'", "${").toString();
					// now substitute the properties
					value = localizer.substitutePropertyExpressions(component, value, model);
				}
			}
		}

		// Return the string resource
		return value;
	}

	/**
	 * Replace "'" with "''" outside of "{..}"
	 * 
	 * @param value
	 * @return escaped message format
	 */
	private String escapeQuotes(final String value)
	{
		StringBuilder newValue = new StringBuilder(value.length() + 10);
		int count = 0;
		for (int i = 0; i < value.length(); i++)
		{
			char ch = value.charAt(i);
			if (ch == '{')
			{
				count += 1;
			}
			else if (ch == '}')
			{
				count -= 1;
			}

			newValue.append(ch);
			if ((ch == '\'') && (count == 0))
			{
				// Escape "'"
				newValue.append(ch);
			}
		}

		return newValue.toString();
	}

	/**
	 * This method just returns debug information, so it won't return the localized string. Please
	 * use getString() for that.
	 * 
	 * @return The string for this model object
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("StringResourceModel[");
		sb.append("key:");
		sb.append(resourceKey);
		sb.append(",default:");
		sb.append(defaultValue);
		sb.append(",params:");
		if (parameters != null)
		{
			sb.append(Arrays.asList(parameters));
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Gets the Java MessageFormat substitution parameters.
	 * 
	 * @return The substitution parameters
	 */
	protected Object[] getParameters()
	{
		return parameters;
	}

	/**
	 * Gets the resource key for this string resource. If the resource key contains property
	 * expressions and the model is not null then the returned value is the actual resource key with
	 * all substitutions undertaken.
	 * 
	 * @return The (possibly substituted) resource key
	 */
	protected final String getResourceKey()
	{
		if (model != null)
		{
			return new PropertyVariableInterpolator(resourceKey, model.getObject()).toString();
		}
		else
		{
			return resourceKey;
		}
	}

	/**
	 * Gets the string that this string resource model currently represents.
	 */
	@Override
	protected String load()
	{
		return getString();
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	protected final void onDetach()
	{
		super.onDetach();

		// detach any model
		if (model != null)
		{
			model.detach();
		}

		// some parameters can be detachable
		if (parameters != null)
		{
			for (Object parameter : parameters)
			{
				if (parameter instanceof IDetachable)
				{
					((IDetachable)parameter).detach();
				}
			}
		}
	}

	@Override
	public void setObject(String object)
	{
		throw new UnsupportedOperationException();
	}
}