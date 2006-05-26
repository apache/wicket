/*
 * $Id: HelloWorldApplication.java 3646 2006-01-04 13:32:14 -0800 (Wed, 04 Jan
 * 2006) ivaynberg $ $Revision: 3646 $ $Date: 2006-01-04 13:32:14 -0800 (Wed, 04
 * Jan 2006) $
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
package wicket.examples.unicodeconverter;

import wicket.Page;
import wicket.examples.WicketExampleApplication;

/**
 * Application class for the unicode converter example.
 * 
 * @author Eelco Hillenius
 */
public class UnicodeConverterApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public UnicodeConverterApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class< ? extends Page> getHomePage()
	{
		return UnicodeConverter.class;
	}
}
