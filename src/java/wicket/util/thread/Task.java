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
package wicket.util.thread;

import org.apache.commons.logging.Log;

import wicket.util.time.Duration;
import wicket.util.time.Time;


/**
 * Runs a block of code periodically. The Task can be started at a given time and can be a
 * daemon.
 * @author Jonathan Locke
 */
public final class Task
{
    /** The name of this task. */
    private final String name;

    /** The time that the task should start. */
    private Time startTime = Time.now();

    /** True if the task's thread should be a daemon. */
    private boolean isDaemon = true;

    /** True if the tasks's thread has already started executing. */ 
    private boolean isStarted = false;

    /** The log to give to the user's code. */
    private Log log = null;

    /**
     * Constructor.
     * @param name The name of this task
     */
    public Task(final String name)
    {
        this.name = name;
    }

    /**
     * Runs this task at the given frequency.
     * @param frequency The frequency at which to run the code
     * @param code The code to run
     * @throws IllegalStateException Thrown if task is already running
     */
    public synchronized final void run(final Duration frequency, final ICode code)
    {
        if (!isStarted)
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    // Sleep until start time
                    startTime.fromNow().sleep();

                    while (true)
                    {
                        // Get the start of the current period
                        final Time startOfPeriod = Time.now();

                        try
                        {
                            // Run the user's code
                            code.run(log);
                        }
                        catch (Exception e)
                        {
                            log.error("Unhandled exception thrown by user code in task " + name, e);
                        }

                        // Sleep until the period is over (or not at all if it's
                        // already passed)
                        startOfPeriod.add(frequency).fromNow().sleep();
                    }
                }
            };

            final Thread thread = new Thread(runnable, name + " Task");

            thread.setDaemon(isDaemon);
            thread.start();

            isStarted = true;
        }
        else
        {
            throw new IllegalStateException("Attempt to start task that is already started");
        }
    }

    /**
     * Sets start time for this task.
     * @param startTime The time this task should start running
     * @throws IllegalStateException Thrown if task is already running
     */
    public synchronized void setStartTime(final Time startTime)
    {
        if (isStarted)
        {
            throw new IllegalStateException(
                    "Attempt to set start time of task that is already started");
        }

        this.startTime = startTime;
    }

    /**
     * Set daemon or not.
     * @param daemon True if this task's thread should be a daemon
     * @throws IllegalStateException Thrown if task is already running
     */
    public synchronized void setDaemon(final boolean daemon)
    {
        if (isStarted)
        {
            throw new IllegalStateException(
                    "Attempt to set daemon boolean of task that is already started");
        }

        isDaemon = daemon;
    }

    /**
     * Set log for user code.
     * @param log The log
     */
    public synchronized void setLog(final Log log)
    {
        this.log = log;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[name="
                + name + ", startTime=" + startTime + ", isDaemon=" + isDaemon + ", isStarted="
                + isStarted + ", codeListener=" + log + "]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
