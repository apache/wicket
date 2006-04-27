package wicket.extensions.markup.html.form;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.util.convert.Converter;
import wicket.util.convert.IConverter;
import wicket.util.convert.converters.AbstractConverter;
import wicket.util.convert.converters.StringConverter;

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
		this.converter = initializeConverter();
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
		this.converter = initializeConverter();
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
	 * Creates an appropriate converter for this <code>TextField</code>
	 *
	 * @return A converter that handles <code>String</code>-<code>Date</code>
	 *         conversions according to the <code>datePattern</code> field. 
	 */
	private Converter initializeConverter() {
		StringConverter stringConverter = new StringConverter();
		stringConverter.set(Date.class, new DateToStringPatternConverter());
		
		Converter converter = new Converter(getSession().getLocale());
		converter.set(String.class, stringConverter);
		converter.set(Date.class, new StringPatternToDateConverter());
		
		return converter;
	}

	/**
	 * Converts a <code>java.util.Date</code> to <code>String</code> using
     * the the pattern in <code>DateTextField</code>
	 * 
	 * @author Stefan Kanev
	 *
	 */
	public final class DateToStringPatternConverter extends AbstractConverter {
		
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
	public final class StringPatternToDateConverter extends AbstractConverter {

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
