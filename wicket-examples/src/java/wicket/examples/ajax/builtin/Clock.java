package wicket.examples.ajax.builtin;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * A simple component that displays current time
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class Clock extends Label
{

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public Clock(String id, TimeZone tz)
	{
		super(id, new ClockModel(tz));

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
		public ClockModel(TimeZone tz)
		{
			df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			df.setTimeZone(tz);
		}

		/**
		 * @see wicket.model.AbstractReadOnlyModel#getObject(wicket.Component)
		 */
		public Object getObject(Component component)
		{
			return df.format(new Date());
		}

	}

}
