/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.ArrayList;
import java.util.List;

import wicket.markup.AutolinkComponentResolver;
import wicket.markup.IMarkupParser;
import wicket.markup.WicketTagComponentResolver;
import wicket.markup.html.form.ICrypt;
import wicket.util.convert.ConverterRegistry;
import wicket.util.lang.Classes;
import wicket.util.time.Duration;
import wicket.util.watch.ModificationWatcher;

/**
 * Base class for all Wicket applications. An application has a name, settings,
 * a particular set of required pages and a variety of resources such as a
 * localizer, a markup parser factory method, a resource watcher and more.
 * <p>
 * To create a Wicket application, you generally do not want to directly
 * subclass this class. Instead, you want to subclass a subclass of Application,
 * like WebApplication, which is appropriate for the protocol and markup type
 * you are working with.
 * <p>
 * The application's settings specify how the application is to function.
 * <p>
 * The required pages returned by getPages() include a home page and pages for
 * handling common error conditions.
 * <p>
 * The getLocalizer() method returns an object encapsulating all of the
 * functionality required to access localized resources.
 * <p>
 * The getConverterRegistry() method returns a registry with converters that
 * should be used for type conversion e.g. by {@link wicket.model.PropertyModel}.
 * Use the converter registry to register/deregister type converters if needed.
 * Also, there are convenience method in converterRegistry to switch to a
 * localized/ non-localized set of type converters.
 * 
 * @see WebApplication
 * @author Jonathan Locke
 */
public abstract class Application
{
    /** List of (static) ComponentResolvers */
    private List componentResolvers;

    /** Registry with type converters */
    private ConverterRegistry converterRegistry = new ConverterRegistry();

    /** The single application-wide localization class */
    private final Localizer localizer;
    
    /** Name of application subclass. */
    private final String name;

    /** ModificationWatcher to watch for changes in markup files */
    private ModificationWatcher resourceWatcher;
    
    /**
     * Constructor
     */
    public Application()
    {
        this.name = Classes.name(getClass());

        // Construct localizer for this application
        this.localizer = new Localizer(this);

        // Install default component resolvers
        componentResolvers = new ArrayList();
        componentResolvers.add(new AutolinkComponentResolver());
        componentResolvers.add(new WicketTagComponentResolver());
    }

    /**
     * Get the (modifiable) List of ComponentResolvers.
     * 
     * @see WicketTagComponentResolver for an example
     * @return List of ComponentResolvers
     */
    public final List getComponentResolvers()
    {
        return componentResolvers;
    }

    /**
     * Get converterRegistry.
     * 
     * @return converterRegistry.
     */
    public final ConverterRegistry getConverterRegistry()
    {
        return converterRegistry;
    }

    /**
     * Get instance of de-/encryption class.
     * 
     * @return instance of de-/encryption class
     */
    public ICrypt getCrypt()
    {
        try
        {
            final ICrypt crypt = (ICrypt)getSettings().getCryptClass().newInstance();
            crypt.setKey(getSettings().getEncryptionKey());
            return crypt;
        }
        catch (InstantiationException e)
        {
            throw new WicketRuntimeException(
                    "Encryption/decryption object can not be instantiated", e);
        }
        catch (IllegalAccessException e)
        {
            throw new WicketRuntimeException(
                    "Encryption/decryption object can not be instantiated", e);
        }
    }

    /**
     * @return The application wide localizer instance
     */
    public Localizer getLocalizer()
    {
        return localizer;
    }

    /**
     * Get and initialize a markup parser.
     *  
     * @param page Autolinks are resolved relative to a Page.
     * @return A new MarkupParser
     */
    public IMarkupParser getMarkupParser(final Page page)
    {
        final ApplicationSettings settings = getSettings();
        try
        {
            final IMarkupParser parser = (IMarkupParser)settings.getMarkupParserClass()
                    .newInstance();
            parser.setComponentNameAttribute(settings.getComponentNameAttribute());
            parser.setWicketNamespace(settings.getWicketNamespace());
            parser.setStripComments(settings.getStripComments());
            parser.setCompressWhitespace(settings.getCompressWhitespace());
            parser.setStripWicketParamTag(settings.getStripWicketParamTag());
            parser.setAutolinking(settings.getAutomaticLinking());
            parser.setAutolinkBasePage(page);
            return parser;
        }
        catch (IllegalAccessException e)
        {
            throw new WicketRuntimeException("Failed to get markup parser", e);
        }
        catch (InstantiationException e)
        {
            throw new WicketRuntimeException("Failed to get markup parser", e);
        }
    }

    /**
     * Gets the name of this application.
     * 
     * @return The application name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets special pages for this application
     * 
     * @return The pages
     */
    public abstract ApplicationPages getPages();

    /**
     * @return Resource watcher with polling frequency determined by setting, or
     *         null if no polling frequency has been set.
     */
    public ModificationWatcher getResourceWatcher()
    {
        if (resourceWatcher == null)
        {
            final Duration pollFrequency = getSettings().getResourcePollFrequency();
            if (pollFrequency != null)
            {
                resourceWatcher = new ModificationWatcher(pollFrequency);
            }
        }
        return resourceWatcher;
    }

    /**
     * Gets settings for this application.
     * 
     * @return The applications settings
     */
    public abstract ApplicationSettings getSettings();
}


