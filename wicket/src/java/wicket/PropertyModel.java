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
package wicket;

import java.lang.reflect.Member;
import java.util.Locale;
import java.util.Map;

import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import wicket.util.convert.ConversionUtils;
import wicket.util.convert.ConverterRegistry;


/**
 * A PropertyModel is used to dynamically acces a model using an <a
 * href="www.ognl.org">Ognl expression </a>.
 * <p>
 * For example, take the following bean:
 * 
 * <pre>
 * 
 * public class Person
 * {
 * 
 *     private String name;
 * 
 *     public String getName()
 *     {
 *         return name;
 *     }
 * 
 *     public void setName(String name)
 *     {
 *         this.name = name;
 *     }
 * 
 * }
 * </pre>
 * 
 * We could construct a label that dynamically fetches the name property of the given
 * person object like this:
 * 
 * <pre>
 * 
 * 
 * 
 *    Person person = getSomePerson();
 *    ...
 *    add(new Label(&quot;myLabel&quot;, person, &quot;name&quot;);
 * 
 * 
 *  
 * </pre>
 * 
 * Where 'myLabel' is the name of the component, and 'name' is the Ognl expression to get
 * the name property.
 * </p>
 * <p>
 * In the same fashion, we can create form components that work dynamically on the given
 * model object. For instance, we could create a text field that updates the name property
 * of a person like this:
 * 
 * <pre>
 * 
 * 
 * 
 *    add(new TextField(&quot;myTextField&quot;, person, &quot;name&quot;);
 * 
 * 
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * For conversions and formatting, the converter sub framework is used. This allows for
 * plugging in custom converters and formatters that are used for converting and
 * formatting objects of a given type.
 * </p>
 * @see wicket.IModel
 * @see wicket.Model
 * @see wicket.DetachableModel
 * @see wicket.util.convert.ConverterRegistry
 * @see wicket.util.convert.ConversionUtils
 * @see wicket.util.convert.FormattingUtils
 */
public class PropertyModel extends DetachableModel
{
    /** Serial Version ID */
	private static final long serialVersionUID = -3136339624173288385L;

	/** the model. */
    private final IModel model;

    /** Ognl expression for property access. */
    private final String expression;

    /** the current locale. */
    private Locale locale;

    /** named formatter. */
    private String formatterName;

    /** pattern to use when formatting. */
    private String formatPattern;

    /** Ognl context wrapper object. It contains the type converter. */
    private transient OgnlContext context;

    /** the current instance of converterRegistry. */
    private transient ConverterRegistry converterRegistry;

    /**
     * When true, additionial formatting is done on the result object. It handles the part
     * of localization where you want model variables like numbers and dates formatted in
     * a localized way.
     */
    private boolean applyFormatting;

    /**
     * Construct with an IModel object and a Ognl expression that works on the given
     * model. Additional formatting will be not be used.
     * @param model the wrapper
     * @param expression Ognl expression for property access
     */
    public PropertyModel(final IModel model, final String expression)
    {
        this(model, expression, false);
    }

    /**
     * Construct with an IModel object and a Ognl expression that works on the given
     * model.
     * @param model the model that is used to get and set the property values
     * @param expression Ognl expression for property access
     * @param applyFormatting When true, additionial formatting is done on the result
     *            object. It handles the part of localization where you want model
     *            variables like numbers and dates formatted in a localized way. See also
     *            properties formatterName and formatPattern.
     */
    public PropertyModel(final IModel model, final String expression, boolean applyFormatting)
    {
        super(null);

        if (model == null)
        {
        	throw new NullPointerException("Parameter 'model' must not be null");
        }
        
        this.model = model;
        this.expression = expression;
        this.applyFormatting = applyFormatting;
    }

    /**
     * Gets the value that results when the given Ognl expression is applied to the model
     * object (Ognl.getValue).
     * @return the value that results when the given Ognl expression is applied to the
     *         model object
     * @see wicket.IModel#getObject()
     */
    public Object getObject()
    {
        if ((getExpression() == null) || (getExpression().trim().length() == 0))
        {
            // No expression will cause OGNL to throw an exception. The OGNL
            // expression to return the current object is "#this". Instead
            // of throwing that exception, we'll provide a meaningfull
            // return value
            return model.getObject();
        }
        IModel theModel = getModel();
        if(theModel == null)
        {
            return null;
        }
        Object modelObject = theModel.getObject();
        if(modelObject == null)
        {
            return null;
        }
        try
        {
            Object raw = Ognl.getValue(getExpression(), getContext(), modelObject);
            if (applyFormatting)
            {
                return converterRegistry.getFormattingUtils().getObjectFormatted(
                        raw, getLocale(), getFormatPattern(), getFormatPattern());
            }
            else
            {
                return raw;
            }
        }
        catch (OgnlException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies the Ognl expression on the model object using the given object argument
     * (Ognl.setValue).
     * @param object the object that will be used when applying Ognl.setValue on the model
     *            object
     * @see wicket.IModel#setObject(java.lang.Object)
     */
    public void setObject(Object object)
    {
        try
        {
            Ognl.setValue(getExpression(), getContext(), getModel().getObject(), object);
        }
        catch (OgnlException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the current {@link Locale}and the {@link ConverterRegistry}, and in case the
     * wrapped model is a {@link IDetachableModel}, calls attach on the wrapped model.
     * @see wicket.DetachableModel#doAttach(wicket.RequestCycle)
     */
    protected final void doAttach(final RequestCycle cycle)
    {
        if (model instanceof IDetachableModel)
        {
            ((IDetachableModel) model).attach(cycle);
        }

        // save the reference to the current locale
        this.locale = cycle.getSession().getLocale();

        ApplicationSettings settings = cycle.getApplication().getSettings();

        // save the reference to the current converter registry instance
        this.converterRegistry = settings.getConverterRegistry();
    }

    /**
     * Unsets the current {@link Locale}and {@link ConverterRegistry}and, in case the
     * wrapped model is a {@link IDetachableModel}, calls dettach on the wrapped model.
     * @see wicket.DetachableModel#doDetach(wicket.RequestCycle)
     */
    protected final void doDetach(final RequestCycle cycle)
    {
        if (model instanceof IDetachableModel)
        {
            ((IDetachableModel) model).detach(cycle);
        }

        // reset ognl context
        this.context = null;

        // reset the reference to the current locale
        this.locale = null;

        // reset the reference to the current converter registry
        this.converterRegistry = null;
    }

    /**
     * Get the Ognl context that is used for evaluating expressions. It contains the type
     * converter that is used to access the converter framework.
     * @return the Ognl context that is used for evaluating expressions.
     */
    protected final OgnlContext getContext()
    {
        if(context == null)
        {
            // setup ognl context for this request
            this.context = new OgnlContext();
            context.setTypeConverter(new OgnlConverterWrapper());
        }
        return context;
    }

    /**
     * Set the Ognl context that is used for evaluating expressions. It contains the type
     * converter that is used to access the converter framework.
     * @param context the Ognl context that is used for evaluating expressions
     */
    protected final void setContext(OgnlContext context)
    {
        this.context = context;
    }

    /**
     * Get the Ognl expression that works on the model. This expression is used with both
     * Ognl.getValue (used in getObject) and Ognl.setValue (used in setObject). Usually,
     * this expression accords with simple property acces (like if we have a Person object
     * with a name property, the expression would be 'name'), but it can in principle
     * contain any valid Ognl expression that has meaning with both the Ognl.getValue and
     * Ognl.setValue operations.
     * @return expression the Ognl expression that works on the model.
     */
    protected final String getExpression()
    {
        return expression;
    }

    /**
     * Get the current locale. The locale will be used for conversions and formatting if
     * localized Converters and Formatters are registered for the target types of the
     * applied expression on the model.
     * @return the current locale.
     */
    protected final Locale getLocale()
    {
        return locale;
    }

    /**
     * Get the model on which the Ognl expressions are applied. The expression will
     * actually not be applied on the instance of IModel, but (naturally) on the wrapped
     * model object or more accurate, the object that results from calling getObject on
     * the instance of IModel.
     * @return the model on which the Ognl expressions are applied.
     */
    protected final IModel getModel()
    {
        return model;
    }

    /**
     * Set whether to apply formatting when getObject is invoked.
     * @param applyFormatting whether to apply formatting when getObject is invoked
     * @return This
     */
    public final PropertyModel setApplyFormatting(boolean applyFormatting)
    {
        this.applyFormatting = applyFormatting;

        return this;
    }

    /**
     * Set an optional format pattern to use when formatting is used.
     * @param formatPattern the format pattern to use
     * @return This
     */
    public final PropertyModel setFormatPattern(String formatPattern)
    {
        this.formatPattern = formatPattern;

        return this;
    }

    /**
     * Set the keyed formatter that should be used when formatting is used.
     * @param formatterName the keyed formatter that should be used when formatting is
     *            used.
     * @return This
     */
    public final PropertyModel setFormatterName(String formatterName)
    {
        this.formatterName = formatterName;

        return this;
    }

    /**
     * Get whether to apply formatting when getObject is invoked.
     * @return whether to apply formatting when getObject is invoked.
     */
    protected final boolean isApplyFormatting()
    {
        return applyFormatting;
    }

    /**
     * Get format pattern to use when formatting is used.
     * @return format pattern to use when formatting is used.
     */
    protected final String getFormatPattern()
    {
        return formatPattern;
    }

    /**
     * Get the keyed formatter that should be used when formatting is used.
     * @return the keyed formatter that should be used when formatting is used.
     */
    protected final String getFormatterName()
    {
        return formatterName;
    }

    /**
     * Get the instance of {@link ConverterRegistry}that is used for conversions and
     * formatting.
     * @return the instance of {@link ConverterRegistry}that is used for conversions and
     *         formatting.
     */
    public final ConverterRegistry getConverterRegistry()
    {
        return converterRegistry;
    }

    /**
     * This class is registered with the Ognl context before parsing in order to be abel
     * to use our converters. It implements Ognl TypeConverter and uses the
     * ConverterRegistry to lookup converters. If no converter is found for a given type,
     * the default conversion of Ognl is used.
     */
    protected class OgnlConverterWrapper extends DefaultTypeConverter
    {
        /**
         * Construct.
         */
        public OgnlConverterWrapper()
        {
        }

        /**
         * Convert the provided value to provided type using provided context.
         * @param context Ognl context
         * @param value the current, unconverted value
         * @param toType the type that should be converted to
         * @return Object the converted value
         * @see ognl.DefaultTypeConverter#convertValue(java.util.Map, java.lang.Object,
         *      java.lang.Class)
         */
        public Object convertValue(Map context, Object value, Class toType)
        {
            if (value == null)
            {
                return null;
            }

            if ((!toType.isArray())
                    && value instanceof String[] && (((String[]) value).length == 1))
            {
                value = ((String[]) value)[0];
            }

            //TODO this might be just too crude for strings
            if ((value instanceof String) && ((String) value).trim().equals(""))
            {
                return null;
            }

            ConversionUtils conversionUtils = converterRegistry.getConversionUtils();

            return conversionUtils.convert(value, toType, getLocale());
        }

        /**
         * This method is only here to satisfy the interface.
         * Method convertValue(Map,Object, Class) is called, so parameters member and propertyName are ignored.
         * @param context
         * @param target
         * @param member
         * @param propertyName
         * @param value
         * @param toType
         * @return the converted value
         * @see ognl.DefaultTypeConverter#convertValue(java.util.Map, java.lang.Object,
         *      java.lang.Class)
         */
        public Object convertValue(Map context, Object target, Member member, String propertyName,
                Object value, Class toType)
        {
            return convertValue(context, value, toType);
        }
    }
}
