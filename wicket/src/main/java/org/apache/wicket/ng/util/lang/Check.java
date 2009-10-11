package org.apache.wicket.ng.util.lang;

import org.apache.wicket.util.string.Strings;

public class Check
{
    public static void argumentNotNull(Object argument, String name)
    {
        if (argument == null)
        {
            throw new IllegalArgumentException("Argument '" + name + "' may not be null.");
        }
    }

    public static void argumentNotEmpty(String argument, String name)
    {
        if (Strings.isEmpty(argument))
        {
            throw new IllegalArgumentException("Argument '" + name +
                    "' may not be null or empty string.");
        }
    }

    /**
     * TODO javadoc and unit test
     * @param <T>
     * @param min
     * @param max
     * @param value
     * @param name
     */
    public static <T extends Comparable<T>> void argumentWithinRange(T min, T max, T value,
            String name)
    {
        // TODO nullchecks
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0)
        {
            throw new IllegalArgumentException(String.format(
                    "Argument '%s' must have a value within [%s,%s], but was %s", name, min, max,
                    value));
        }

        return;
    }
}
