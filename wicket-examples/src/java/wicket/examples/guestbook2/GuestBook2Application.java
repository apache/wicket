/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.examples.guestbook2;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import wicket.examples.WicketExampleApplication;
import wicket.util.convert.Converter;
import wicket.util.convert.IConverter;
import wicket.util.convert.IConverterFactory;
import wicket.util.convert.converters.DateToStringConverter;
import wicket.util.convert.converters.StringConverter;

/**
 * Guest book application.
 * 
 * @author Jonathan Locke
 */
public class GuestBook2Application extends WicketExampleApplication
{
	/**
	 * Constructor
	 */
	public GuestBook2Application()
	{
		getPages().setHomePage(GuestBook2.class);
	}

	/**
	 * @see wicket.Application#getConverterFactory()
	 */
	public IConverterFactory getConverterFactory()
	{
        return new IConverterFactory()
        {
			public IConverter newConverter(final Locale locale)
			{
                // Create converter
                final Converter converter = new Converter(locale);
                
                // Create date to string converter
                final DateToStringConverter dateToStringConverter = new DateToStringConverter();
                dateToStringConverter.setLocale(locale);
                dateToStringConverter.setDateFormat(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT));
                
                // Set string conversion for Date and Timestamp classes
                final StringConverter stringConverter = new StringConverter();
                stringConverter.setLocale(locale);
                stringConverter.set(Date.class, dateToStringConverter);
                stringConverter.set(Timestamp.class, dateToStringConverter);
                           
                // Set new string converter
                converter.set(String.class, stringConverter);
                
                return converter;
			}            
        };
	}
}
