/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

/**
 * A factory class to load Pages. This is the default implementation which
 * requires Pages to be Java classes. Subclasses may augment it to support
 * Groovy bases Pages.
 * <p>
 * Page implementation must either have a constructor with a single argument
 * (PageParameters or Page) and/or a default constructor. The factory tries to
 * instantiate a object of class Page with constructor argument first, and
 * default constructor second.
 * 
 * @see ApplicationSettings on how to change the default DefaultPageFactory used.
 * 
 * @author Juergen Donnerstag
 */
public final class DefaultPageFactory implements IPageFactory
{ // TODO finalize javadoc
    /** Logging */
    private final Log log = LogFactory.getLog(DefaultPageFactory.class);

    /** Map class name to Constructor. */
    private final Map constructors = new ConcurrentHashMap();

    /** The application object */
    private final IApplication application;

    /**
     * Constructor
     * 
     * @param application
     *            The application object
     */
    public DefaultPageFactory(final IApplication application)
    {
        this.application = application;
    }

    /**
     * Getter to access the application object
     * 
     * @return The application object
     */
    public final IApplication getApplication()
    {
        return this.application;
    }

    /**
     * Creates a new page. If Page with PageParameter argument constructor
     * exists, PageParameter will be null.
     * 
     * @param pageClass
     *            The page class to instantiate
     * @return The page
     * @throws WicketRuntimeException
     */
    public final Page newPage(final Class pageClass)
    {
        return newPage(pageClass, (PageParameters) null);
    }

    /**
     * Creates a new Page and apply the PageParameters to the Page constructor
     * if a proper constructor exists. Else use the default constructor.
     * 
     * @param pageClass
     *            The name of the page class to create
     * @param parameters
     *            The page parameters
     * @return The new page
     * @throws WicketRuntimeException
     */
    public Page newPage(final Class pageClass, final PageParameters parameters)
    {
        // Find constructor for page class. Try constructor with PageParameters
        // first. If not available try default constructor.
        Constructor constructor = null;
        try
        {
            // Constructor with PageParameter argument
            constructor = getConstructor(pageClass, PageParameters.class);
        }
        catch (RuntimeException e)
        {
            // Try default constructor
            constructor = getConstructor(pageClass, null);
        }

        if (constructor == null)
        {
            throw new WicketRuntimeException("Could not find constructor for page '"
                    + pageClass
                    + "' with PageParameter argument or default constructor");
        }

        // Create an instance of Page. PageParameters will be applied
        // Depending on the constructor found,
        return newPage(constructor, parameters);
    }

    /**
     * Creates a new instance of a page using the given class name. If pageClass
     * implements a constructor with Page argument, page will be forwarded to
     * that constructor. Else, the default constructor will be used and page be
     * neglected.
     * 
     * @param pageClass
     *            The class of page to create
     * @param page
     *            Parameter to page constructor
     * @return The new page
     * @throws WicketRuntimeException
     */
    public final Page newPage(final Class pageClass, final Page page)
    {
        // Find constructor for page class with Page argument. Else, if not
        // found the default constructor for pageClass.
        final Constructor constructor = getConstructor(pageClass, Page.class);
        if (constructor != null)
        {
            // Create an instance of type pageClass and provide page as
            // constructor argument, if proper constructor was found. Else
            // page will be neglected.
            return newPage(constructor, page);
        }

        throw new WicketRuntimeException("Could not find constructor for page '"
                + pageClass
                + "' with Page argument constructor or default constructor");
    }

    /**
     * Looks up a page constructor by class name and constructor argument. If
     * argument == null, the default constructor will be taken.
     * 
     * @param pageClass
     *            The class of page
     * @param parameter
     *            The parameter class for the constructor
     * @return The page constructor
     * @throws WicketRuntimeException
     */
    protected final Constructor getConstructor(final Class pageClass,
            final Class parameter)
    {
        // Constructor already in cache?

        // Note: the class name has been deliberately taken as map key, as
        // Groovy e.g. will create a new Class if the groovy file has been
        // changed and afterwards reloaded: new Class != old Class.
        Constructor constructor = (Constructor) constructors.get(pageClass
                .getName());
        if (constructor == null)
        {
            try
            {
                // Not found. Create a new Constructor
                if (parameter == null)
                {
                    constructor = pageClass.getConstructor((Class[]) null);
                }
                else
                {
                    constructor = pageClass
                            .getConstructor(new Class[] { parameter });
                }

                // Store it in the cache
                constructors.put(pageClass.getName(), constructor);
            }
            catch (NoSuchMethodException e)
            {
                throw new WicketRuntimeException(
                        "Could not find proper page constructor in "
                                + pageClass
                                + "; Constructor parameter="
                                + (parameter == null ? "null" : parameter
                                        .getName()), e);
            }
        }

        return constructor;
    }

    /**
     * Creates a new instance of a page using the given constructor and
     * constructor argument.
     * 
     * @param constructor
     *            The constructor to invoke
     * @param parameter
     *            The parameter to pass to the constructor
     * @return The new page
     * @throws WicketRuntimeException
     */
    protected Page newPage(final Constructor constructor, final Object parameter)
    {
        try
        {
            if (constructor.getParameterTypes().length == 0)
            {
                return (Page) constructor.newInstance(new Object[0]);
            }

            return (Page) constructor.newInstance(new Object[] { parameter });
        }
        catch (InstantiationException e)
        {
            throw new WicketRuntimeException("Cannot instantiate page object with "
                    + constructor, e);
        }
        catch (IllegalAccessException e)
        {
            throw new WicketRuntimeException("Cannot access " + constructor, e);
        }
        catch (InvocationTargetException e)
        {
            // In order to avoid classloader issues within Wicket, wicket.jar
            // has to be in /WEB-INF/lib
            final Class homePageClass = application.getPages().getHomePage();
            if (homePageClass.getClassLoader() != constructor.getClass()
                    .getClassLoader())
            {
                throw new WicketRuntimeException(
                        "Classloader problems: Most probably Wicket-xxx.jar is not in /WEB-INF/lib: Exception thrown by "
                                + constructor, e);
            }

            throw new WicketRuntimeException("Exception thrown by " + constructor, e);
        }
    }
}