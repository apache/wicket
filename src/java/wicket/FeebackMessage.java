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

import java.io.Serializable;

/**
 * Represents a generic message meant for the end-user/ pages.
 *
 * @author Eelco Hillenius
 */
public class FeebackMessage implements Serializable
{ // TODO finalize javadoc
    /**
     * Constant for an undefined level; note that components might decide
     * not to render anything when this level is used.
     */
    public static final int UNDEFINED = 0;

    /** constant for debug level. */
    public static final int DEBUG = 1;

    /** constant for info level. */
    public static final int INFO = 2;

    /** constant for warn level. */
    public static final int WARN = 3;

    /** constant for error level. */
    public static final int ERROR = 4;

    /** constant for fatal level. */
    public static final int FATAL = 5;

    /** levels as strings for debugging/ toString method. */
    private static final String[] LEVELS_AS_STRING = new String[]
    {
        "UNDEFINED", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"
    };

    /** the reporting component. */
    private Component reporter;

    /** the actual message. */
    private String message;

    /**
     * The message level; can be used by rendering components.
     * Note that what actually happens with the level indication is
     * totally up to the components that render messages like these.
     * The default level is UNDEFINED.
     */
    private int level = UNDEFINED;

    /**
     * Construct using fields.
     * @param reporter the message reporter
     * @param message the actual message
     * @param level the level of the message
     */
    public FeebackMessage(Component reporter, String message, int level)
    {
        this.reporter = reporter;
        this.message = message;
        this.level = level;
    }

    /**
     * Gets a new constructed message with level DEBUG.
     * @param reporter the reporter of the message
     * @param message the actual message
     * @return a new message with level DEBUG
     */
    public final static FeebackMessage debug(Component reporter, String message)
    {
        return new FeebackMessage(reporter, message, DEBUG);
    }

    /**
     * Gets a new constructed message with level INFO.
     * @param reporter the reporter of the message
     * @param message the actual message
     * @return a new message with level INFO
     */
    public final static FeebackMessage info(Component reporter, String message)
    {
        return new FeebackMessage(reporter, message, INFO);
    }

    /**
     * Gets a new constructed message with level WARN.
     * @param reporter the reporter of the message
     * @param message the actual message
     * @return a new message with level WARN
     */
    public final static FeebackMessage warn(Component reporter, String message)
    {
        return new FeebackMessage(reporter, message, WARN);
    }

    /**
     * Gets a new constructed message with level ERROR.
     * @param reporter the reporter of the message
     * @param message the actual message
     * @return a new message with level ERROR
     */
    public final static FeebackMessage error(Component reporter, String message)
    {
        return new FeebackMessage(reporter, message, ERROR);
    }

    /**
     * Gets a new constructed message with level FATAL.
     * @param reporter the reporter of the message
     * @param message the actual message
     * @return a new message with level FATAL
     */
    public final static FeebackMessage fatal(Component reporter, String message)
    {
        return new FeebackMessage(reporter, message, FATAL);
    }

    /**
     * Gets the actual message.
     * @return the message.
     */
    public final String getMessage()
    {
        return message;
    }

    /**
     * Sets the actual message.
     * @param message the actual message.
     */
    protected final void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Gets the reporting component.
     * @return the reporting component.
     */
    public final Component getReporter()
    {
        return reporter;
    }

    /**
     * Sets the reporting component.
     * @param reporter the reporting component.
     */
    public final void setReporter(Component reporter)
    {
        this.reporter = reporter;
    }

    /**
     * Gets the message level; can be used by rendering components.
     * Note that what actually happens with the level indication is
     * totally up to the components that render messages like these.
     * @return the message level indicator.
     */
    public final int getLevel()
    {
        return level;
    }

    /**
     * Gets the current level as a String
     * @return the current level as a String
     */
    public final String getLevelAsString()
    {
        return LEVELS_AS_STRING[getLevel()];
    }

    /**
     * Sets the message level; can be used by rendering components.
     * Note that what actually happens with the level indication is
     * totally up to the components that render messages like these.
     * @param level the message level indicator.
     */
    protected final void setLevel(int level)
    {
        this.level = level;
    }

    /**
     * Gets whether the current level is UNDEFINED.
     * @return whether the current level is UNDEFINED.
     */
    public final boolean isLevelUndefined()
    {
        return (getLevel() == UNDEFINED); 
    }

    /**
     * Gets whether the current level is DEBUG or up.
     * @return whether the current level is DEBUG or up.
     */
    public final boolean isLevelDebug()
    {
        return isLevel(DEBUG); 
    }

    /**
     * Gets whether the current level is INFO or up.
     * @return whether the current level is INFO or up.
     */
    public final boolean isLevelInfo()
    {
        return isLevel(INFO); 
    }

    /**
     * Gets whether the current level is WARN or up.
     * @return whether the current level is WARN or up.
     */
    public final boolean isLevelWarn()
    {
        return isLevel(WARN); 
    }

    /**
     * Gets whether the current level is ERROR or up.
     * @return whether the current level is ERROR or up.
     */
    public final boolean isLevelError()
    {
        return isLevel(ERROR); 
    }

    /**
     * Gets whether the current level is FATAL or up.
     * @return whether the current level is FATAL or up.
     */
    public final boolean isLevelFatal()
    {
        return isLevel(FATAL); 
    }

    /**
     * Returns whether this level is greater than or equal to the given level.
     * @param level the level
     * @return whether this level is greater than or equal to the given level
     */
    public final boolean isLevel(int level)
    {
        return (getLevel() >= level);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return ("'" + getMessage() + "' (reporter: " + getReporter().getName()
                + ", level: " + LEVELS_AS_STRING[getLevel()] + ")");
    }
}
