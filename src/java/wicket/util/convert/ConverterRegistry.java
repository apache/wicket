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
package wicket.util.convert;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Date;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wicket.util.convert.converters.BaseLocaleConverter;
import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.ByteConverter;
import wicket.util.convert.converters.ByteLocaleConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.DateLocaleConverter;
import wicket.util.convert.converters.DoubleConverter;
import wicket.util.convert.converters.DoubleLocaleConverter;
import wicket.util.convert.converters.FloatConverter;
import wicket.util.convert.converters.FloatLocaleConverter;
import wicket.util.convert.converters.IntegerConverter;
import wicket.util.convert.converters.LocaleConverter;
import wicket.util.convert.converters.LocaleFormatter;
import wicket.util.convert.converters.LongConverter;
import wicket.util.convert.converters.LongLocaleConverter;
import wicket.util.convert.converters.NoopConverter;
import wicket.util.convert.converters.ShortConverter;
import wicket.util.convert.converters.ShortLocaleConverter;

/**
 * Global registry for converters. This serves as the alternative for ConvertUtils. We use
 * this instead of ConvertUtils for the following reasons:
 * <ul>
 * <li>to avoid conflicts with other uses of BeanUtils</li>
 * <li>to have a simple, flat registry with mixed usage of locales</li>
 * <li>to keep control of the registry within this framework</li>
 * <li>to have an extra option for the registration of 'just' formatters</li>
 * </ul>
 * The registry is using non localized converters by default. See methods
 * setLocalizedDefaults() and setNonLocalizedDefaults().
 */
public final class ConverterRegistry
{
    /**
     * converter for fallthrough.
     */
    private final static NoopConverter NOOP_CONVERTER = new NoopConverter();

    /**
     * The set of {@link Converter}s that can be used to convert Strings into objects of
     * a specified Class, keyed by the destination Class.
     */
    private Map converters = new HashMap();

    /**
     * The set of {@link Converter}s that can be used to convert Strings into objects of
     * a specified Class, keyed by the destination Class and locale.
     */
    private Map localizedConverters = new HashMap();

    /**
     * when true, a noopConverter is returned as a fallback. When false, null is returned.
     */
    private boolean useNoopConverter = true;

    /** utility for formatting. */
    private final FormattingUtils formattingUtils;

    /** utility for doing conversions. */
    private final ConversionUtils conversionUtils;

    /**
     * Construct.
     */
    public ConverterRegistry()
    {
        formattingUtils = new FormattingUtils(this);
        conversionUtils = new ConversionUtils(this);
        setNonLocalizedDefaults();
    }

    /**
     * Convenience method for registering converters that work in a localized environment.
     */
    public void setLocalizedDefaults()
    {
        clear();
        register(new BooleanConverter(), Boolean.TYPE);
        register(new BooleanConverter(), Boolean.class);
        register(new ByteLocaleConverter(), Byte.TYPE);
        register(new ByteLocaleConverter(), Byte.class);
        register(new CharacterConverter(), Character.TYPE);
        register(new CharacterConverter(), Character.class);
        register(new DoubleLocaleConverter(), Double.TYPE);
        register(new DoubleLocaleConverter(), Double.class);
        register(new FloatLocaleConverter(), Float.TYPE);
        register(new FloatLocaleConverter(), Float.class);
        register(new IntegerConverter(), Integer.TYPE);
        register(new IntegerConverter(), Integer.class);
        register(new LongLocaleConverter(), Long.TYPE);
        register(new LongLocaleConverter(), Long.class);
        register(new ShortLocaleConverter(), Short.TYPE);
        register(new ShortLocaleConverter(), Short.class);
        register(new DateLocaleConverter(), Date.class);
        register(new DateLocaleConverter(), java.sql.Date.class);
        register(new DateLocaleConverter(), Timestamp.class);
    }

    /**
     * Convenience method for registering converters that work in a non-localized
     * environment.
     */
    public void setNonLocalizedDefaults()
    {
        clear();
        register(new BooleanConverter(), Boolean.TYPE);
        register(new BooleanConverter(), Boolean.class);
        register(new ByteConverter(), Byte.TYPE);
        register(new ByteConverter(), Byte.class);
        register(new CharacterConverter(), Character.TYPE);
        register(new CharacterConverter(), Character.class);
        register(new DoubleConverter(), Double.TYPE);
        register(new DoubleConverter(), Double.class);
        register(new FloatConverter(), Float.TYPE);
        register(new FloatConverter(), Float.class);
        register(new IntegerConverter(), Integer.TYPE);
        register(new IntegerConverter(), Integer.class);
        register(new LongConverter(), Long.TYPE);
        register(new LongConverter(), Long.class);
        register(new ShortConverter(), Short.TYPE);
        register(new ShortConverter(), Short.class);
    }

    /**
     * Register a custom {@link Converter}for the specified destination
     * <code>Class</code>, replacing any previously registered Converter.
     * @param converter Converter to be registered
     * @param clazz Destination class for conversions performed by this Converter
     */
    public void register(Converter converter, Class clazz)
    {
        converters.put(clazz, converter);
    }

    /**
     * Register a custom {@link LocaleConverter}for the specified destination
     * <code>Class</code>, replacing any previously registered Converter.
     * @param converter LocaleConverter to be registered
     * @param clazz Destination class for conversions performed by this Converter
     */
    public void register(LocaleConverter converter, Class clazz)
    {
        localizedConverters.put(clazz, converter);
    }

    /**
     * Register a custom {@link LocaleConverter}for the specified destination
     * <code>Class</code>, replacing any previously registered Converter.
     * @param converter LocaleConverter to be registered
     * @param clazz Destination class for conversions performed by this Converter
     * @param locale Locale class
     */
    public void register(LocaleConverter converter, Class clazz, Locale locale)
    {
        String lockey = getLocKey(clazz, locale);

        localizedConverters.put(lockey, converter);
    }

    /**
     * register a global formatter with the given key
     * @param formatter the formatter
     * @param key the key to register the instance of Formatter with
     */
    public void register(Formatter formatter, String key)
    {
        key = getLocKey(key);
        converters.put(key, formatter);
    }

    /**
     * register a global locale aware formatter with the given key and locale
     * @param formatter the formatter
     * @param key the key to register the instance of Formatter with
     * @param locale the locale
     */
    public void register(LocaleFormatter formatter, String key, Locale locale)
    {
        key = getLocKey(key, locale);
        localizedConverters.put(key, formatter);
    }

    /**
     * register a global locale aware formatter with the given key
     * @param formatter the formatter
     * @param key the key to register the instance of Formatter with
     */
    public void register(LocaleFormatter formatter, String key)
    {
        key = getLocKey(key);
        localizedConverters.put(key, formatter);
    }

    /**
     * Remove any registered {@link Converter}for the specified destination
     * <code>Class</code> and <code>Locale</code>.
     * @param clazz Class for which to remove a registered Converter
     * @param locale the locale
     */
    public void deregister(Class clazz, Locale locale)
    {
        String lockey = getLocKey(clazz, locale);

        localizedConverters.remove(lockey);
    }

    /**
     * Remove any registered {@link Converter}for the specified destination
     * <code>Class</code>.
     * @param clazz Class for which to remove a registered Converter
     */
    public void deregister(Class clazz)
    {
        if (LocaleConverter.class.isAssignableFrom(clazz))
        {
            converters.remove(clazz);
        }
        else
        {
            localizedConverters.remove(clazz);
        }
    }

    /**
     * Remove all instances registered {@link Converter}by class of converter
     * @param clazz Class of converter to remove. Removes all subclasses as well.
     */
    public void deregisterByConverterClass(Class clazz)
    {
        List keys = new ArrayList();

        for (Iterator i = converters.keySet().iterator(); i.hasNext();)
        {
            Object key = i.next();
            Converter converter = (Converter) converters.get(key);

            if (converter.getClass().isAssignableFrom(clazz))
            {
                keys.add(key);
            }
        }

        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            Object key = i.next();

            converters.remove(key);
        }

        keys.clear();

        for (Iterator i = localizedConverters.keySet().iterator(); i.hasNext();)
        {
            Object key = i.next();
            LocaleConverter converter = (LocaleConverter) localizedConverters.get(key);

            if (converter.getClass().isAssignableFrom(clazz))
            {
                keys.add(key);
            }
        }

        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            Object key = i.next();

            localizedConverters.remove(key);
        }
    }

    /**
     * Remove the instances of registered {@link LocaleConverter}
     * @param converter instance of converter to remove
     */
    public void deregister(LocaleConverter converter)
    {
        List keys = new ArrayList();

        for (Iterator i = localizedConverters.keySet().iterator(); i.hasNext();)
        {
            Object key = i.next();
            LocaleConverter _converter = (LocaleConverter) localizedConverters.get(key);

            if (converter == _converter)
            {
                keys.add(key);
            }
        }

        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            Object key = i.next();

            localizedConverters.remove(key);
        }
    }

    /**
     * Remove any registered {@link Formatter}for the specified key
     * @param key key for which to remove a registered Formatter
     */
    public void deregister(String key)
    {
        key = getLocKey(key);
        converters.remove(key);
    }

    /**
     * deregister a global formatter with the given key and locale
     * @param key the key of the formatter
     * @param locale the locale
     */
    public void deregister(String key, Locale locale)
    {
        key = getLocKey(key, locale);
        localizedConverters.remove(key);
    }

    /**
     * clear all registrations
     */
    public void clear()
    {
        converters.clear();
        localizedConverters.clear();
    }

    /**
     * lookup a globally registered formatter
     * @param key key of formatter
     * @return Formatter instance of Formatter that was registered with the specified key
     *         or null if not found
     */
    public Formatter lookup(String key)
    {
        key = getLocKey(key);

        return (Formatter) converters.get(key);
    }

    /**
     * Look up and return any registered {@link Converter}for the specified destination
     * class. If there is no registered Converter, return an instance of NoopConverter if
     * returnNoopConverterWhenNotFound == true or else <code>null</code>.
     * @param clazz Class for which to return a registered Converter
     * @return Converter converter
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Converter lookup(Class clazz) throws NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException
    {
        return lookup(clazz, null);
    }

    /**
     * Look up and return any registered {@link Converter}for the specified destination
     * class and locale. If there is no registered Converter, return an instance of
     * NoopConverter if returnNoopConverterWhenNotFound == true or else <code>null</code>.
     * Precedence: if a locale is given the first search is for a converter that was
     * registered for the given type and locale. If it is not found, the second search is
     * for any converter of the type LocaleConverter that was registered for the given
     * type. If it is found, a new instance will be created for the given locale, the
     * pattern will be copied if possible and the newly instantiated converter will be
     * registered for the given type and locale (and thus will be found at the first
     * search next time). If it is not found, the search is the same as when no locale was
     * given (locale == null): the 'normal', not localized registry will be searched for
     * an entry with the given type. If still no Converter is found after this, and
     * returnNoopConverterWhenNotFound is true an instance of NoopConverter is returned,
     * so that clients allways get a valid converter. If returnNoopConverterWhenNotFound
     * is false, null will be returned.
     * @param clazz Class for which to return a registered Converter
     * @param locale The Locale
     * @return Converter converter
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Converter lookup(Class clazz, Locale locale) throws NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException
    {
        Converter converter = null;

        if (locale != null)
        {
            String lockey = getLocKey(clazz, locale);

            // first try registration for specific locale
            converter = (LocaleConverter) localizedConverters.get(lockey);

            if (converter == null) // not found, try generic localized// registration
            {
                LocaleConverter _converter = (LocaleConverter) localizedConverters.get(clazz);

                // if found, instantiate a localized one and store for next use
                if (_converter != null)
                {
                    Class cls = _converter.getClass();
                    Class[] paramTypes = new Class[] {Locale.class};
                    Constructor constructor = cls.getConstructor(paramTypes);
                    Object[] initArgs = new Object[] {locale};

                    // create new instance for this locale
                    LocaleConverter _newConverter = (LocaleConverter) constructor
                            .newInstance(initArgs);

                    // try to copy the pattern
                    if ((_converter instanceof BaseLocaleConverter)
                            && (_newConverter instanceof BaseLocaleConverter))
                    {
                        String pattern = ((BaseLocaleConverter) _converter).getPattern();

                        ((BaseLocaleConverter) _newConverter).setPattern(pattern);
                    }

                    // else: too bad, but it's probably not a problem
                    // register the new instance for this locale
                    localizedConverters.put(lockey, _newConverter);
                    converter = _newConverter;
                }
            }
        }

        // else // get without locale right away
        if (converter == null) // (still) not found, try generic non-localized//
                               // registration
        {
            converter = (Converter) converters.get(clazz);
        }

        if ((converter == null) && useNoopConverter) // STILL not found; return// no-op
        {
            converter = NOOP_CONVERTER;
        }

        return converter;
    }

    /**
     * Look up and return any registered {@link Formatter}for the specified destination
     * key and locale; if there is no registered Formatter, return <code>null</code>.
     * Precedence: if a locale is given the first search is for a formatter that was
     * registered for the given type and locale. If it is not found, the second search is
     * for any formatter of the type LocaleFormatter that was registered for the given
     * key. If it is found, a new instance will be created for the given locale and the
     * newly instantiated formatter will be registered for the given key and locale (and
     * thus will be found at the first search next time). If it is not found, the search
     * is the same as when no locale was given (locale == null): the 'normal', not
     * localized registry will be searched for an entry with the given key. If this is not
     * found either, null will be returned.
     * @param key key that the formatter was registered with
     * @param locale the Locale
     * @return the formatter
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Formatter lookup(String key, Locale locale) throws NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException
    {
        Formatter formatter = null;

        if (locale != null)
        {
            String lockey = getLocKey(key, locale);

            // first try registration for specific locale
            formatter = (LocaleFormatter) localizedConverters.get(lockey);

            if (formatter == null) // not found, try generic localized// registration
            {
                String globLocKey = getLocKey(key);
                LocaleFormatter _formatter = (LocaleFormatter) localizedConverters.get(globLocKey);

                // if found, instantiate a localized one and store for next use
                if (_formatter != null)
                {
                    Class cls = _formatter.getClass();
                    LocaleFormatter _newFormatter = (LocaleFormatter) cls.newInstance();

                    _newFormatter.setLocale(locale);

                    // register the new instance for this locale
                    localizedConverters.put(key, _newFormatter);
                    formatter = _newFormatter;
                }
            }
        }

        // else // get without locale right away
        if (formatter == null) // (still) not found, try generic non-localized//
                               // registration
        {
            formatter = (Formatter) converters.get(getLocKey(key));
        }

        return formatter;
    }

    /*
     * get key for localized converters @param clazz class @param locale locale @return
     * String key
     */
    private String getLocKey(Class clazz, Locale locale)
    {
        return clazz.getName()
                + "|" + ((locale.getCountry() != null) ? locale.getCountry() : "_") + "|"
                + ((locale.getLanguage() != null) ? locale.getLanguage() : "_") + "|"
                + ((locale.getVariant() != null) ? locale.getVariant() : "_");
    }

    /*
     * get key for localized formatters @param key key @param locale locale @return String
     * key
     */
    private String getLocKey(String key, Locale locale)
    {
        return "_fmt"
                + key + "|" + ((locale.getCountry() != null) ? locale.getCountry() : "_") + "|"
                + ((locale.getLanguage() != null) ? locale.getLanguage() : "_") + "|"
                + ((locale.getVariant() != null) ? locale.getVariant() : "_");
    }

    /*
     * get key for localized formatters @param key key @return String key
     */
    private String getLocKey(String key)
    {
        return "_fmt" + key;
    }

    /**
     * Whether to return a noopConverter as a fallback.
     * @return boolean when true, a noopConverter is returned as a fallback. When false,
     *         null is returned
     */
    public boolean isUseNoopConverter()
    {
        return useNoopConverter;
    }

    /**
     * Whether to return a noopConverter as a fallback. false, null is returned
     * @param b when true, a noopConverter is returned as a fallback. When false, null is
     *            returned
     */
    public void setUseNoopConverter(boolean b)
    {
        useNoopConverter = b;
    }

    /**
     * Get the formatting utility that works with this converter registry instance.
     * @return the formatting utility that works with this converter registry instance
     */
    public final FormattingUtils getFormattingUtils()
    {
        return formattingUtils;
    }

    /**
     * Get the conversion utility that works with this converter registry instance.
     * @return the conversion utility that works with this converter registry instance
     */
    public final ConversionUtils getConversionUtils()
    {
        return conversionUtils;
    }
}
