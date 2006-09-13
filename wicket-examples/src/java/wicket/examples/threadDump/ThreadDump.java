/*
 * $Id: HelloWorld.java 5884 2006-05-26 10:17:55Z joco01 $ $Revision$
 * $Date: 2006-05-26 12:17:55 +0200 (Fr, 26 Mai 2006) $
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
package wicket.examples.threadDump;

import java.util.Arrays;
import java.util.Map;

import wicket.AttributeModifier;
import wicket.MarkupContainer;
import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;

/**
 * 
 * 
 * @author Juergen Donnerstag
 */
public class ThreadDump extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public ThreadDump()
	{
		ThreadDumpBean bean = new ThreadDumpBean();

		RepeatingView threads = new RepeatingView(this, "threads");
		for (Thread thread : bean.getThreads())
		{
			WebMarkupContainer item = new WebMarkupContainer(threads, threads.newChildId());

			MarkupContainer anchor = new WebMarkupContainer(item, "anchor");
			anchor.add(new AttributeModifier("href", true, new Model<String>("#" + thread.getId())));

			new Label(anchor, "name", thread.getName());
			new Label(item, "state", thread.getState().toString());
			new Label(item, "priority", new Model<Integer>(Integer.valueOf(thread.getPriority())));
			new Label(item, "daemon", new Model<Boolean>(Boolean.valueOf(thread.isDaemon())));
		}

		RepeatingView traces = new RepeatingView(this, "traces");
		for (Map.Entry<Thread, StackTraceElement[]> entry : bean.getTraces().entrySet())
		{
			Thread thread = entry.getKey();
			StackTraceElement[] trace = entry.getValue();

			WebMarkupContainer item = new WebMarkupContainer(traces, traces.newChildId());
			MarkupContainer anchor = new WebMarkupContainer(item, "anchor");
			anchor.add(new AttributeModifier("name", true, new Model<Long>(thread.getId())));

			new Label(anchor, "key", new Model<String>(thread.getName()));

			new ListView<StackTraceElement>(item, "lines", Arrays.asList(trace))
			{
				@Override
				protected void populateItem(final ListItem item)
				{
					new Label(item, "line", item.getModel());
				}
			};
		}
	}
}