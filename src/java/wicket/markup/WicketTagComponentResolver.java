/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Container;
import wicket.WicketRuntimeException;
import wicket.Session;
import wicket.util.string.StringValueConversionException;

/**
 * &lt;wicket:component class="myApp.MyTable" key=value&gt; tags may be used to add 
 * Wicket components (e.g. a specialized table) and pass parameters (e.g. the number
 * of rows per table page). The object is automatically instantiated, initialized
 * and added to the page's component hierarchy.
 * <p>
 * Note: The component must have a constructor with a single String parameter: 
 * the component name.
 * <p>
 * Note: The component must provide a setter for each key/value attribute provided.
 * <p>
 * This class is currently experimental only.
 * 
 * @author Juergen Donnerstag
 */
public class WicketTagComponentResolver implements IComponentResolver
{
    /** Logging */
    private static Log log = LogFactory.getLog(WicketTagComponentResolver.class);

    /**
     * @see wicket.markup.IComponentResolver#resolve(Container, MarkupStream,
     *      ComponentTag)
     * @param container
     *            The container parsing its markup
     * @param markupStream
     *            The current markupStream
     * @param tag
     *            The current component tag while parsing the markup
     * @return true, if componentName was handle by the resolver. False,
     *         otherwise
     */
    public boolean resolve(final Container container, final MarkupStream markupStream,
            final ComponentTag tag)
    {
        // It must be <wicket:...>
        if (tag instanceof ComponentWicketTag)
        {
            // It must be <wicket:component...>
            final ComponentWicketTag wicketTag = (ComponentWicketTag)tag;
            if (wicketTag.isComponentTag())
            {
                // Create and initialize the component
                final Component component = createComponent(container, wicketTag);
                if (component != null)
                {
                    // Add it to the hierarchy and render it
                    container.add(component);
                    component.render();
                    return true;
                }
            }
        }

        // We were not able to handle the componentName
        return false;
    }

    /**
     * Based on the tag, create and initalize the component.
     *  
     * @param container The current container. The new compent will be added to that container.
     * @param tag The tag containing the information about component 
     * @return The new component
     * @throws WicketRuntimeException in case the component could not be created
     */
    // Wicket is current not using any bean util jar, which is why ...
    private Component createComponent(final Container container, final ComponentWicketTag tag)
    {
        // If no component name is given, create a page-unique one yourself.
        String componentName = tag.getNameAttribute();
        if (componentName == null)
        {
            componentName = "anonymous-" + container.getPage().getAutoIndex();
        }

        // Get the component class name
        final String classname = tag.getAttributes().getString("class");
        if ((classname == null) || (classname.trim().length() == 0))
        {
            throw new MarkupException("Tag <wicket:component> must have attribute 'class'");
        }

        // Load the class. In case a Groovy Class Resolver has been provided,
        // the name might be a Groovy file.
        // Note: Spring based components are not supported this way. May be we
        //  should provide a ComponentFactory like we provide a PageFactory.
        final Class componentClass = Session.get().getClassResolver().resolveClass(classname);

        // construct the component. It must have a constructor with a single
        // String (componentName) parameter.
        final Component component;
        try
        {
            final Constructor constructor = componentClass
                    .getConstructor(new Class[] { String.class });
            component = (Component)constructor.newInstance(new Object[] { componentName });
        }
        catch (NoSuchMethodException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }
        catch (InvocationTargetException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }
        catch (InstantiationException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }
        catch (ClassCastException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }
        catch (SecurityException e)
        {
            throw new MarkupException(
                    "Unable to create Component from wicket tag: Cause: " 
                    + e.getMessage());
        }

        // Get all remaining attributes and invoke the component's setters
        final Iterator iter = tag.getAttributes().entrySet().iterator();
        while (iter.hasNext())
        {
            final Map.Entry entry = (Map.Entry)iter.next();
            final String key = (String)entry.getKey();
            final String value = (String)entry.getKey();

            // Ignore attributes 'name' and 'class'
            if ("name".equalsIgnoreCase(key) || ("class".equalsIgnoreCase(key)))
            {
                continue;
            }

            // Get the setter for the attribute
            final String methodName = "set" + key;
            final Method[] methods = component.getClass().getMethods();
            Method method = null;
            for (int i = 0; i < methods.length; i++)
            {
                if (methods[i].getName().equalsIgnoreCase(methodName))
                {
                    method = methods[i];
                }
            }

            if (method == null)
            {
                throw new MarkupException(
                        "Unable to initialize Component. Method with name " + methodName
                                + " not found");
            }

            // The method must have a single parameter
            final Class[] parameterClasses = method.getParameterTypes();
            if (parameterClasses.length != 1)
            {
                throw new MarkupException(
                        "Unable to initialize Component. Method with name " + methodName
                                + " must have one and only one parameter");
            }

            // Convert the parameter if necessary, depending on the setter's attribute
            final Class paramClass = parameterClasses[0];
            try
            {
                if (paramClass.equals(String.class))
                {
                    method.invoke(component, new Object[] { value });
                }
                else if (paramClass.equals(int.class))
                {
                    method.invoke(component, new Object[] { new Integer(tag.getAttributes().getInt(
                            key)) });
                }
                else if (paramClass.equals(long.class))
                {
                    method.invoke(component, new Object[] { new Long(tag.getAttributes().getLong(
                            (key))) });
                }
            }
            catch (IllegalAccessException ex)
            {
                throw new MarkupException(
                        "Unable to initialize Component. Failure while invoking method "
                                + methodName + ". Cause: " + ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new MarkupException(
                        "Unable to initialize Component. Failure while invoking method "
                                + methodName + ". Cause: " + ex);
            }
            catch (StringValueConversionException ex)
            {
                throw new MarkupException(
                        "Unable to initialize Component. Failure while invoking method "
                                + methodName + ". Cause: " + ex);
            }
        }

        return component;
    }
}