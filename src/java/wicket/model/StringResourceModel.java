/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.model;

import java.text.MessageFormat;
import java.util.Locale;

import wicket.ApplicationSettings;
import wicket.Component;
import wicket.Localizer;
import wicket.RequestCycle;
import wicket.util.string.interpolator.OgnlVariableInterpolator;

/**
 * This model class encapsulates the full power of localization support within the Wicket
 * framework. It combines the flexible Wicket resource loading mechanism with OGNL expressions,
 * property models and standard Java <code>MessageFormat</code> substitutions. This combination
 * should be able to solve any dynamic localization requirement that a project has.
 * <p>
 * The model should be created with four parameters, which are described in detail below:
 * <ul>
 * <li><b>resourceKey</b> - This is the most important parameter as it contains the key that
 *     should be used to obtain resources from any string resource loaders. This paramater
 *     is mandatory: a null value will throw an exception. Typically it will contain an ordinary
 *     string such as &quot;label.username&quot;. To add extra power to the key functionality the key
 *     may also contain an OGNL expression which will be evaluated if the model parameter
 *     (see below) is not null. This allows keys to be changed dynamically as the application
 *     is running. For example, the key could be &quot;product.${product.id}&quot; which prior to
 *     rendering will call model.getObject().getProduct().getId() and substitute this value
 *     into the resource key before is is passed to the loader.
 *
 * <li><b>relativeComponent</b> - This parameter should be a component that the string
 *     resource is relative to. In a simple application this will usually be the Page on which
 *     the component resides. For reusable components/containers that are packaged with their
 *     own string resource bundles it should be the actual component/container rather than the
 *     page. For more information on this please see
 *     {@link wicket.resource.ComponentStringResourceLoader}. The relative
 *     component may actually be <code>null</code> when all resource loading is to be done
 *     from a global resource loader. However, we recommend that a relative component is
 *     still supplied even in these cases in order to 'future proof' your application with
 *     regards to changing resource loading strategies.
 *
 * <li><b>model</b> - This parameter is mandatory if either the resourceKey, the found string
 *     resource (see below) or any of the substitution parameters (see below) contain OGNL
 *     expressions. Where OGNL expressions are present they will all be evaluated relative
 *     to this model object. If there are no OGNL expressions present then this model
 *     parameter may be <code>null</code>
 *
 * <li><b>parameters</b> - The parameters parameter allows an array of objects to be passed
 *     for substitution on the found string resource (see below) using a standard
 *     <code>java.text.MessageFormat</code> object. Each parameter may be an ordinary Object,
 *     in which case it will be processed by the standard formatting rules associated with
 *     <code>java.text.MessageFormat</code>. Alternatively, the parameter may be an instance
 *     of <code>IModel</code> in which case the <code>getObject()</code> method will be
 *     applied prior to the parameter being passed to the <code>java.text.MessageFormat</code>.
 *     This allows such features dynamic parameters that are obtained using a
 *     <code>PropertyModel</code> object or even nested string resource models.
 * </ul>
 *
 * As well as the supplied parameters, the found string resource can contain formatting
 * information. It may contain OGNL expressions in which case these are evaluated using the
 * model object supplied when the string resource model is created. The string resource
 * may also contain <code>java.text.MessageFormat</code> style markup for replacement of
 * parameters. Where a string resource contains both types of formatting information then
 * the OGNL will be applied first.
 * <p>
 * <b>Example 1</b>
 * <p>
 * In its simplest form, the model can be used as follows:
 * <pre>
 * public MyPage extends HtmlPage {
 *
 *     public MyPage(final PageParameters parameters) {
 *         add(new Label(&quot;username&quot;, new StringResourceModel(&quot;label.username&quot;, this, null)));
 *     }
 *
 * }
 * </pre>
 * Where the resource bundle for the page contains the entry <code>label.username=Username</code>
 * <p>
 * <b>Example 2</b>
 * <p>
 * In this example, the resource key is selected based on the evaluation of an OGNL
 * expression:
 * <pre>
 * public MyPage extends HtmlPage {
 *
 *     public MyPage(final PageParameters parameters) {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *                       new StringResourceModel(&quot;weather.${currentStatus}&quot;, this, new Model(ws)));
 *     }
 *
 * }
 * </pre>
 * Which will call the WeatherStation.getCurrentStatus() method each time the
 * string resource model is used and where the resource bundle for the page contains the entries:
 * <pre>
 * weather.sunny=Don't forget sunscreen!
 * weather.raining=You might need an umberella
 * weather.snowing=Got your skis?
 * weather.overcast=Best take a coat to be safe
 * </pre>
 * <p>
 * <b>Example 3</b>
 * <p>
 * In this example the found resource string contains an OGNL expression that is
 * substituted via the model:
 * <pre>
 * public MyPage extends HtmlPage {
 *
 *     public MyPage(final PageParameters parameters) {
 *         WeatherStation ws = new WeatherStation();
 *         add(new Label(&quot;weatherMessage&quot;,
 *                       new StringResourceModel(&quot;weather.message&quot;, this, new Model(ws)));
 *     }
 *
 * }
 * </pre>
 * Where the resource bundle contains the entry <code>weather.message=Weather station reports that
 * the temperature is ${currentTemperature} ${units}</code>
 * <p>
 * <b>Example 4</b>
 * <p>
 * In this example, the use of substitution parameters is employed to format a quite
 * complex message string. This is an example of the most complex and powerful use
 * of the string resource model:
 * <pre>
 * public MyPage extends HtmlPage {
 *
 *     public MyPage(final PageParameters parameters) {
 *         WeatherStation ws = new WeatherStation();
 *         Model model = new Model(ws);
 *         add(new Label(&quot;weatherMessage&quot;,
 *                   new StringResourceModel(
 *                       &quot;weather.detail&quot;, this, model,
 *                       new Object[] {
 *                           new Date(),
 *                           new PropertyModel(model, &quot;currentStatus&quot;),
 *                           new PropertyModel(model, &quot;currentTemperature&quot;),
 *                           new PropertyModel(model, &quot;units&quot;)
 *                       }));
 *     }
 *
 * }
 * </pre>
 * And where the resource bundle entry is:
 * <pre>
 * weather.detail=The report for {0,date}, shows the temparature as {2,number,###.##} {3} \
 *                and the weather to be {1}
 * </pre>
 *
 * @author Chris Turner
 */
public class StringResourceModel extends DetachableModel
{ // TODO finalize javadoc
	/** Serial Version ID */
	private static final long serialVersionUID = 6659487382203513733L;

	// The localizer to be used to access localized resources
	// And the associated locale for formatting
	private Localizer localizer = null;

	private Locale locale = null;

	// The information used to generate string resources
	private String resourceKey;

	private Component relativeComponent;

	private IModel model;

	private Object[] parameters;

	/**
	 * Construct.
	 * @param resourceKey The resource key for this string resource
	 * @param relativeComponent The component that the resource is relative to
	 * @param model The model to use for OGNL substitutions
	 * @see #StringResourceModel(String, Component, IModel, Object[])
	 */
	public StringResourceModel(final String resourceKey, final Component relativeComponent,
			final IModel model)
	{
		this(resourceKey, relativeComponent, model, null);
	}

	/**
	 * Create a new string resource model using the supplied
	 * parameters.
	 * <p>
	 * The relative component parameter should generally
	 * be supplied, as without it resources can not be obtained from
	 * resouce bundles that are held relative to a particular component
	 * or page. However, for application that use only global
	 * resources then this parameter may be null.
	 * <p>
	 * The model parameter is also optional and only needs to be supplied if
	 * value substitutions are to take place on either the resource
	 * key or the actual resource strings.
	 * <p>
	 * The parameters parameter is also optional and is used for
	 * substitutions.
	 *
	 * @param resourceKey The resource key for this string resource
	 * @param relativeComponent The component that the resource is relative to
	 * @param model The model to use for OGNL substitutions
	 * @param parameters The parameters to substitute using a Java MessageFormat object
	 */
	public StringResourceModel(final String resourceKey, final Component relativeComponent,
			final IModel model, final Object[] parameters)
	{
		super(null);
		if (resourceKey == null)
		{
			throw new NullPointerException("Resource key must not be null");
		}
		this.resourceKey = resourceKey;
		this.relativeComponent = relativeComponent;
		this.model = model;
		this.parameters = parameters;
	}

	/**
	 * Get the localizer that is being used by this string resource model.
	 *
	 * @return The localizer
	 */
	public Localizer getLocalizer()
	{
		return localizer;
	}

	/**
	 * Set the localizer that is being used by this string resource model.
	 * This method is provided to allow the default application localizer to
	 * be overridden if required.
	 *
	 * @param localizer The localizer to use
	 */
	public void setLocalizer(final Localizer localizer)
	{
		this.localizer = localizer;
	}

	/**
	 * Get the string currently represented by this string resource model.
	 * The string that is returned may vary for each call to this method
	 * depending on the values contained in the model and an the
	 * parameters that were passed when this string resource model
	 * was created.
	 *
	 * @return The string
	 */
	public final String getString()
	{
		Component c = getRelativeComponent();

		// Make sure we have a localizer before commencing
		if (getLocalizer() == null)
		{
			if (c != null)
			{
				setLocalizer(c.getLocalizer());
			}
			else
			{
				throw new IllegalStateException("No localizer has been set");
			}
		}

		// Get the string resource, doing any OGNL substitutions as part
		// of the get operation
		String s = localizer.getString(getResourceKey(), c, getModel());

		// Substitute any parameters if necessary
		Object[] parameters = getParameters();
		if (parameters != null)
		{
			// Build the real parameters
			Object[] realParams = new Object[parameters.length];
			for (int i = 0; i < parameters.length; i++)
			{
				if (parameters[i] instanceof IModel)
				{
					realParams[i] = ((IModel) parameters[i]).getObject();
				}
				else if (model != null && parameters[i] instanceof String)
				{
					realParams[i] = OgnlVariableInterpolator.interpolate((String) parameters[i],
							model.getObject());
				}
				else
				{
					realParams[i] = parameters[i];
				}
			}

			// Apply the parameters
			Locale workingLocale = locale;
			if (c != null)
			{
				// Use component locale out of preference
				workingLocale = c.getLocale();
			}
			MessageFormat format = new MessageFormat(s, workingLocale);
			s = format.format(realParams);
		}

		// Return the string resource
		return s;
	}

	/**
	 * Get the string that this string resource model currently
	 * represents. The string is returned as an object to allow it
	 * to be used generically within components.
	 *
	 * @return The string for this model object
	 * @see #getString()
	 */
	public final Object getObject()
	{
		return getString();
	}

	/**
	 * This method does not apply for string resource models. The
	 * contents of the model are set on construction and any
	 * variations should be achieved through OGNL expressions in
	 * the key, the resource string and the parameters or by the
	 * contents of the model object.
	 *
	 * @param object Not used
	 */
	public final void setObject(final Object object)
	{
		// No implementation
	}

	/**
	 * Override of the default method to return the resource
	 * string represented by this string resource model.
	 * Useful in debugging and so on, to avoid the explicit need
	 * to call the getString() method.
	 *
	 * @return The string for this model object
	 * @see #getString()
	 */
	public String toString()
	{
		return getString();
	}

	/**
	 * Attach to the current request.
	 * @param cycle the current request cycle
	 */
	protected final void doAttach(RequestCycle cycle)
	{
		// Attach the model if necessary
		if (model != null && model instanceof IDetachableModel)
		{
			((IDetachableModel) model).attach(cycle);
		}

		// Initialise information that we need to work successfully
		ApplicationSettings settings = cycle.getApplication().getSettings();
		localizer = settings.getLocalizer();
		locale = cycle.getSession().getLocale();
	}

	/**
	 * Detach from the current request.
	 * @param cycle the current request cycle
	 */
	protected final void doDetach(RequestCycle cycle)
	{
		// Detach the model if necessary
		if (model != null && model instanceof IDetachableModel)
		{
			((IDetachableModel) model).detach(cycle);
		}

		// Clear down any information we don't want held in the session
		localizer = null;
		locale = null;
	}

	/**
	 * Get the resource key for this string resource. If the resource
	 * key contains OGNL and the model is null then the returned value
	 * is the actual resource key with all substitutions undertaken.
	 *
	 * @return The (possibly substituted) resource key
	 */
	protected final String getResourceKey()
	{
		if (model != null)
		{
			return OgnlVariableInterpolator.interpolate(resourceKey, model.getObject());
		}
		else
		{
			return resourceKey;
		}
	}

	/**
	 * Get the component that this string resource is relative to.
	 *
	 * @return The relative component
	 */
	protected final Component getRelativeComponent()
	{
		return relativeComponent;
	}

	/**
	 * Get the model used for OGNL substitutions.
	 *
	 * @return The model
	 */
	protected final IModel getModel()
	{
		return model;
	}

	/**
	 * Get the Java MessageFormat substitution parameters.
	 *
	 * @return The substitution parameters
	 */
	protected final Object[] getParameters()
	{
		return parameters;
	}

}
