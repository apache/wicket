/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.markup.html.form;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.util.convert.Converter;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.AbstractConverter;

/**
 * A TextField that is mapped to a <code>java.util.Date</code> object.
 * 
 * If you provide a <code>SimpleDateFormat</code> pattern, it will both parse
 * and validate the text field according to it.
 * 
 * If you don't, it is the same as creating a <code>TextField</code> with
 * <code>java.util.Date</code> as it's type (it will get the pattern
 * from the user's locale)
 * 
 * @author Stefan Kanev
 *
 */
public class DateTextField extends TextField
{

	private static final long serialVersionUID = 1L;

	/**
	 * The date pattern of the text field
	 */
	private String datePattern = null;
	
	/**
	 * The converter for the TextField
	 */
	private IConverter converter = null;
	
	/**
	 * Creates a new DateTextField, without a specified pattern. This
	 * is the same as calling <code>new TextField(id, Date.class)</code>
	 *  
	 * @param id The id of the text field
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(String id)
	{
		super(id, Date.class);
	}

	/**
	 * Creates a new DateTextField, without a specified pattern. This
	 * is the same as calling <code>new TextField(id, object, Date.class)</code>
	 * 
	 * @param id The id of the text field
	 * @param object The model
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, IModel object)
	{
		super(id, object, Date.class);
	}

	/**
	 * Creates a new DateTextField bound with a specific 
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id The id of the text field
	 * @param datePattern A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, String datePattern)
	{
		super(id, Date.class);
		this.datePattern = datePattern;
		this.converter = new DateTextFieldConverter();
	}
	
	/**
	 * Creates a new DateTextField bound with a specific 
	 * <code>SimpleDateFormat</code> pattern.
	 * 
	 * @param id The id of the text field
	 * @param object The model
	 * @param datePattern A <code>SimpleDateFormat</code> pattern
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public DateTextField(String id, IModel object, String datePattern)
	{
		super(id, object, Date.class);
		this.datePattern = datePattern;
		this.converter = new DateTextFieldConverter();
	}

	/**
	 * Returns the default converter if created without pattern; otherwise it
	 * returns a pattern-specific converter.
	 * 
	 * @return A pattern-specific converter
	 * 
	 * @see wicket.markup.html.form.TextField
	 */
	public IConverter getConverter()
	{
		if (converter == null) 
		{ 
			return super.getConverter();
		} else 
		{
			return converter;
		}
	}
	
	/**
	 * Converts <code>String</code> to <code>java.util.Date</code> and back
	 * via the datePattern in the inner class
	 * 
	 * @author Stefan Kanev, s.kanev@spider.bg
	 *
	 */
	public class DateTextFieldConverter extends Converter 
	{

		private static final long serialVersionUID = 1L;
		
		/**
		 * Creates an instance, setting 
		 * <code>DateToStringPatternConverter</code> and
		 * <code>StringPatternToDateConverter</code> as it is appropriate.
		 */
		private DateTextFieldConverter() 
		{
			super(getSession().getLocale());

			set(String.class, new DateToStringPatternConverter());
			set(Date.class, new StringPatternToDateConverter());
		}
		
	}
	
	/**
	 * Converts a <code>java.util.Date</code> to <code>String</code> using
     * the the pattern in <code>DateTextField</code>
	 * 
	 * @author Stefan Kanev
	 *
	 */
	public final class DateToStringPatternConverter extends AbstractConverter
	{
		
		private static final long serialVersionUID = 1L;

		/**
		 * Converts a <code>java.util.Date</code> to <code>String</code> using
		 * the the pattern in <code>DateTextField</code>
		 * 
		 * @param value A <code>java.util.Date</code> object to parse
		 * @param locale The user locale (unused)
		 * @return The given value as string
		 */
		public Object convert(Object value, Locale locale)
		{
			if (!(value instanceof Date)) return null;
		
			SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
			String result = dateFormat.format((Date) value);
			
			return result;
		}

		protected Class getTargetType()
		{
			return String.class;
		}
		
	}

	/**
	 * Parses a <code>java.util.Date</code> from a <code>String</code>
	 * 
	 * @author Stefan Kanev, s.kanev@spider.bg
	 *
	 */
	public final class StringPatternToDateConverter extends AbstractConverter 
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Parses a <code>java.util.Date</code> from a <code>String</code>
		 * 
		 * @param value A date to parse
		 * @param locale User locale (rather unused)
		 * @return The date formatted as string according to the set pattern
		 */
		public Object convert(Object value, Locale locale)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
			
			return parse(dateFormat, value);
		}

		protected Class getTargetType()
		{
			return Date.class;
		}
	}
	
}
