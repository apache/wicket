/*
 * $Id: MarkupContainer.java 4965 2006-03-16 11:21:07 -0800 (Thu, 16 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-16 11:21:07 -0800 (Thu, 16 Mar
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.threadDump;


import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ThreadDumpBean implements Serializable
{
	private final Map<Thread, StackTraceElement[]> traces;

	/**
	 * Construct
	 *
	 */
	public ThreadDumpBean()
	{
		this.traces = new TreeMap<Thread, StackTraceElement[]>(THREAD_COMP);
		this.traces.putAll(Thread.getAllStackTraces());
	}

	/**
	 * 
	 * @return collection of threads
	 */
	public Collection<Thread> getThreads()
	{
		return traces.keySet();
	}

	/**
	 * Stacktrace per thread
	 * 
	 * @return Map
	 */
	public Map<Thread, StackTraceElement[]> getTraces()
	{
		return traces;
	}

	/**
	 * Compare the threads by name and id.
	 */
	private static final Comparator<Thread> THREAD_COMP = new Comparator<Thread>()
	{
		public int compare(Thread o1, Thread o2)
		{
			int result = o1.getName().compareTo(o2.getName());
			if (result == 0)
			{
				Long id1 = o1.getId();
				Long id2 = o2.getId();
				return id1.compareTo(id2);
			}
			return result;
		}
	};
}
