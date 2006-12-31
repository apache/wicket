/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:57:30 +0200 (vr, 26 mei 2006) $
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
package wicket.examples.ajax.builtin;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * A simple component that displays current time
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class Clock extends Label
{
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            Component id
	 * @param tz
	 *            Timezone
	 */
	public Clock(final MarkupContainer parent, final String id, final TimeZone tz)
	{
		super(parent, id, new ClockModel(tz));

	}

	/**
	 * A model that returns current time in the specified timezone via a
	 * formatted string
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static class ClockModel extends AbstractReadOnlyModel
	{
		private DateFormat df;

		/**
		 * @param tz
		 */
		public ClockModel(final TimeZone tz)
		{
			df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			df.setTimeZone(tz);
		}

		/**
		 * @see wicket.model.AbstractReadOnlyModel#getObject()
		 */
		@Override
		public Object getObject()
		{
			return df.format(new Date());
		}
	}
}