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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.form.validation.ValidationErrorModelDecorator;


/**
 * Structure for recording  {@link wicket.FeebackMessage}s;
 * wraps a list and acts as a {@link wicket.IModel}.
 *
 * @author Eelco Hillenius
 */
public final class FeedbackMessages
{
    /** Log. */
    private static Log log = LogFactory.getLog(FeedbackMessages.class);

    /** holder for the current FeedbackMessages. */
    private static ThreadLocal current = new ThreadLocal();

    /**
     * Holds a list of
     * {@link wicket.markup.html.form.validation.ValidationErrorMessage}s.
     */
    private List messages = null;

    /**
     * Comparator for sorting messages on level.
     */
    public static final class LevelComparator implements Comparator
    {
        /** asc/ desc sign. */
        private final int sign;

        /**
         * Construct.
         * @param ascending whether to sort ascending (otherwise, it sorts descending)
         */
        public LevelComparator(boolean ascending)
        {
            sign = (ascending) ? 1 : -1;
        }

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            int l1 = ((FeebackMessage)o1).getLevel();
            int l2 = ((FeebackMessage)o2).getLevel();
            if(l1 < l2)
            {
                return sign * -1;
            }
            else if(l1 > l2)
            {
                return sign * 1;
            }
            else
            {
                return 0;
            }
        }
    }

    /**
     * The {@link IModel} representation of FeedbackMessages.
     */
    private static class UIMessagesModel implements IModel
    {
        /** level to narrow the model to. If undefined (the default), it is not used. */
        private int level = FeebackMessage.UNDEFINED;

        /**
         * Construct.
         */
        public UIMessagesModel()
        {
            
        }

        /**
         * Construct and narrow to the given level.
         * @param level the level to narrow to
         */
        public UIMessagesModel(int level)
        {
            this.level = level;
        }

        /**
         * Gets the messages.
         * @see wicket.IModel#getObject()
         */
        public Object getObject()
        {
            FeedbackMessages uim = get();
            if(level == FeebackMessage.UNDEFINED)
            {
                return uim.getMessages();
            }
            else
            {
                return uim.getMessages(level);
            }
        }

        /**
         * Sets the messages; the object should either be of type {@link java.util.List}
         * or an array of {@link FeebackMessage}s.
         * @see wicket.IModel#setObject(java.lang.Object)
         */
        public void setObject(Object object)
        {
            FeedbackMessages uim = get();
            if(object instanceof List)
            {
                uim.setMessages((List)object);
            }
            else if(object instanceof FeebackMessage[])
            {
                if(object != null)
                {
                    uim.setMessages(Arrays.asList((FeebackMessage[])object));
                }
                else
                {
                    uim.setMessages(null);
                }
            }
            else
            {
                throw new RuntimeException("invalid model type for FeedbackMessages");
            }
        }
    }

    /**
     * Hidden constructor; clients are not allowed to create instances as this
     * class is managed by the framework.
     */
    private FeedbackMessages()
    {

    }

    /** 
     * Gets the messages for the calling Thread. The current messages are lazily
     * constructed (thus created on the first call to this method within a request)
     * and are stored in a thread local variable.
     * @return the messages for the calling Thread
     */
    public static FeedbackMessages get()
    {
        FeedbackMessages currentMessages = (FeedbackMessages)current.get();
        if(currentMessages == null)
        {
            currentMessages = new FeedbackMessages();
            current.set(currentMessages);
            if(log.isDebugEnabled())
            {
                log.debug("FeedbackMessages created for thread " + Thread.currentThread());
            }
        }
        return currentMessages;
    }

    /**
     * Sets the messages for the current thread. Is used for handling redirects. 
     * @param messages the messages to use with the current thread
     */
    static void set(FeedbackMessages messages)
    {
        if(current.get() != null) // that would be wrong
        {
            log.error("messages were allready set for this thread!" +
            		" Either a former cleanup failed, or the current thread has illegal" +
            		" access. Trying to release the current messages first.");
            // try cleaning up
            release();
        }
        else
        {
            if(log.isDebugEnabled())
            {
                log.debug(messages + " set for thread " + Thread.currentThread());
            }
        }
        current.set(messages);
    }

    /**
     * Clears the current messages instance from the thread local and
     * reset the original models of the components that had theirs replaced
     * with decorator models. To be used by the framework only (package local).
     */
    static void release()
    {
        FeedbackMessages currentMessages = (FeedbackMessages)current.get();
        if(currentMessages != null)
        {
            if(log.isDebugEnabled())
            {
                log.debug("FeedbackMessages " + currentMessages + " released for thread "
                        + Thread.currentThread());
            }
            // only error level components have their models (possibly) replaced
            Set components = currentMessages.getErrorReporters();
            for(Iterator i = components.iterator(); i.hasNext();)
            {
                Component component = (Component)i.next();
                IModel currentModel = component.getModel();
                // if the model was wrapped (ie the error message was added by the
                // validation mechanism of this framework
                if(currentModel instanceof ValidationErrorModelDecorator)
                {
                    ValidationErrorModelDecorator deco =
                        ((ValidationErrorModelDecorator)currentModel);
                    IModel originalModel = deco.getOriginalModel();
                    // replace the model with the initial one
                    component.setModel(originalModel);
                }
            }
            // clear thread local
            current.set(null);
        }
        else
        {
            if(log.isDebugEnabled())
            {
                log.debug("no FeedbackMessages to release for thread " + Thread.currentThread());
            }
        }
    }

    /**
     * Removes from the thread local without releasing.
     */
    static void remove()
    {
        if(log.isDebugEnabled())
        {
            log.debug("FeedbackMessages cleared without releasing for thread "+ Thread.currentThread());
        }
        current.set(null);
    }

    /**
     * Gets the FeedbackMessages as an instance of {@link IModel}.
     * @return the FeedbackMessages as an instance of {@link IModel}
     */
    public static IModel model()
    {
        return new UIMessagesModel();
    }

    /**
     * Gets the FeedbackMessages as an instance of {@link IModel}, narrowed down to the given level.
     * @param level the level to narrow down to
     * @return tthe FeedbackMessages as an instance of {@link IModel}, narrowed down to the given level
     */
    public static IModel model(int level)
    {
        return new UIMessagesModel(level);
    }

    /**
     * Adds a new ui message with level DEBUG to the current messages.
     * @param reporter the reporting component
     * @param message the actual message
     */
    public static void debug(Component reporter, String message)
    {
        get().add(FeebackMessage.debug(reporter, message));
    }

    /**
     * Adds a new ui message with level INFO to the current messages.
     * @param reporter the reporting component
     * @param message the actual message
     */
    public static void info(Component reporter, String message)
    {
        get().add(FeebackMessage.info(reporter, message));
    }

    /**
     * Adds a new ui message with level WARN to the current messages.
     * @param reporter the reporting component
     * @param message the actual message
     */
    public static void warn(Component reporter, String message)
    {
        get().add(FeebackMessage.warn(reporter, message));
    }

    /**
     * Adds a new ui message with level ERROR to the current messages.
     * @param reporter the reporting component
     * @param message the actual message
     */
    public static void error(Component reporter, String message)
    {
        get().add(FeebackMessage.error(reporter, message));
    }

    /**
     * Adds a new ui message with level FATAL to the current messages.
     * @param reporter the reporting component
     * @param message the actual message
     */
    public static void fatal(Component reporter, String message)
    {
        get().add(FeebackMessage.fatal(reporter, message));
    }

    /**
     * Adds a message.
     * @param message the message
     * @return This
     */
    public FeedbackMessages add(FeebackMessage message)
    {
        if(log.isDebugEnabled())
        {
            log.debug("adding message " + message + " for thread " + Thread.currentThread());
        }
        if(messages == null) messages = new ArrayList();
        messages.add(message);
        return this;
    }

    /**
     * Gets whether this list contains any messages.
     * @return whether this list contains any messages
     */
    public boolean hasMessages()
    {
        return (messages != null && (!messages.isEmpty()));
    }

    /**
     * Convenience method that gets whether this list contains any messages with level
     * ERROR or up. This is the same as
     * calling 'hasMessages(FeebackMessage.ERROR)'.
     * @return whether this list contains any messages with level
     * ERROR or up
     */
    public boolean hasErrorMessages()
    {
        return hasMessages(FeebackMessage.ERROR);
    }

    /**
     * Gets whether this list contains any messages with the given level or up.
     * @param level the level
     * @return whether this list contains any messages with the given level or up
     */
    public boolean hasMessages(int level)
    {
        boolean errors = false;
        if(hasMessages())
        {
            for(Iterator i = messages.iterator(); i.hasNext();)
            {
                FeebackMessage message = (FeebackMessage)i.next();
                if(message.isLevel(level))
                {
                    errors = true;
                    break;
                }
            }
        }
        return errors;
    }

    /**
     * Gets the list with messages (not sorted; the same ordering as they were added).
     * @return the list with messages
     */
    public List getMessages()
    {
        List msgs = null;
        msgs = (this.messages != null) ? this.messages :  Collections.EMPTY_LIST;
        return Collections.unmodifiableList(msgs);
    }

    /**
     * Gets the list with messages sorted on level ascending
     * (from UNDEFINED/ DEBUG up to FATAL).
     * @return the list with messages
     */
    public List getMessagesAscending()
    {
        return getMessagesSorted(true);
    }

    /**
     * Gets the list with messages sorted on level descending
     * (from FATAL down to UNDEFINED/ DEBUG).
     * @return the list with messages
     */
    public List getMessagesDescending()
    {
        return getMessagesSorted(false);
    }

    /**
     * Gets the messages sorted.
     * @param asc whether to sort ascending (true) or descending (false)
     * @return sorted list
     */
    private List getMessagesSorted(boolean asc)
    {
        if(messages != null)
        {
	        List toSort = new ArrayList(messages);
	        Collections.sort(toSort, new LevelComparator(asc));
	        return toSort;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Convenience method to get the iterator for the currently registered messages.
     * @return the iterator for the currently registered messages
     */
    public Iterator iterator()
    {
        return getMessages().iterator();
    }

    /**
     * Looks up a message for the given component.
     * @param component the component to look up the message for
     * @return the message that is found for the given component (first match) or
     * null if none was found
     */
    public FeebackMessage getMessageFor(Component component)
    {
        if(messages != null)
        {
            FeebackMessage message = null;
            for(Iterator i = messages.iterator(); i.hasNext();)
            {
                FeebackMessage toTest = (FeebackMessage)i.next();
                if((toTest.getReporter() != null) && (toTest.getReporter().equals(component)))
                {
                    message = toTest;
                    break;
                }
            }
            return message;
        }
        else
        {
            return null;
        }
    }

    /**
     * Looks up whether the given component registered a message with this list.
     * @param component the component to look up whether it registered a message
     * @return whether the given component registered a message with this list
     */
    public boolean hasMessageFor(Component component)
    {
        return (getMessageFor(component) != null);
    }

    /**
     * Convenience method that looks up whether the given component registered a
     * message with this list with the level ERROR.
     * @param component the component to look up whether it registered a message
     * @return whether the given component registered a message with
     * this list with level ERROR
     */
    public boolean hasErrorMessageFor(Component component)
    {
        return hasMessageFor(component, FeebackMessage.ERROR);
    }

    /**
     * Looks up whether the given component registered a message with this list with the
     * given level.
     * @param component the component to look up whether it registered a message
     * @param level the level of the message
     * @return whether the given component registered a message with this list
     * with the given level
     */
    public boolean hasMessageFor(Component component, int level)
    {
        FeebackMessage message = getMessageFor(component);
        if(message != null)
        {
            return (message.isLevel(level));
        }
        else
        {
            return false;
        }
    }

    /**
     * Convenience method that gets a sub list of messages with messages that are
     * of level ERROR or above (FATAL). This is the same as calling
     * 'getMessages(FeebackMessage.ERROR)'.
     * @return the sub list of message with messages that are of level ERROR or above,
     * or an empty list
     */
    public FeedbackMessages getErrorMessages()
    {
        return getMessages(FeebackMessage.ERROR);
    }

    /**
     * Gets a sub list of messages with messages that are of the given level or above.
     * @param level the level to get the messages for
     * @return the sub list of message with messages that are of the given level or above,
     * or an empty list
     */
    public FeedbackMessages getMessages(int level)
    {
        if(messages != null)
        {
            List sublist = new ArrayList();
            for(Iterator i = messages.iterator(); i.hasNext();)
            {
                FeebackMessage message = (FeebackMessage)i.next();
                if(message.isLevel(level))
                {
                    sublist.add(message);
                }
            }
            return new FeedbackMessages().setMessages(sublist);
        }
        else
        {
            return new FeedbackMessages();
        }
    }

    /**
     * Sets the list with messages.
     * @param messages the messages
     * @return This
     */
    private FeedbackMessages setMessages(List messages)
    {
        this.messages = messages;
        return this;
    }

    /**
     * Convenience method that gets the set of reporters of messages that
     * are of the ERROR level or above.
     * @return the set of reporters of messages that are of the ERROR level or above
     */
    public Set getErrorReporters()
    {
        if(messages != null)
        {
            Set subset = new HashSet();
            for(Iterator i = messages.iterator(); i.hasNext();)
            {
                FeebackMessage message = (FeebackMessage)i.next();
                if(message.isLevel(FeebackMessage.ERROR))
                {
                    subset.add(message.getReporter());
                }
            }
            return subset;
        }
        else
        {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Gets the set of reporters of messages that are of the given level or above.
     * @param level the level to get the messages for
     * @return the set of reporters of messages that are of the given level or above
     */
    public Set getReporters(int level)
    {
        if(messages != null)
        {
            Set subset = new HashSet();
            for(Iterator i = messages.iterator(); i.hasNext();)
            {
                FeebackMessage message = (FeebackMessage)i.next();
                if(message.isLevel(level))
                {
                    subset.add(message.getReporter());
                }
            }
            return subset;
        }
        else
        {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer b = new StringBuffer("FeedbackMessages{");
        List msgs = getMessages();
        if(!msgs.isEmpty())
        {
	        for(Iterator i = msgs.iterator(); i.hasNext();)
	        {
	            b.append(i.next());
	            if(i.hasNext()) b.append(",");
	        }
        }
        else
        {
            b.append("<empty>");
        }
        b.append("}");
        return b.toString();
    }
}

