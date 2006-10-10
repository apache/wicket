/*
 * $Id: FormInputModel.java 4776 2006-03-05 17:10:05 -0800 (Sun, 05 Mar 2006)
 * joco01 $ $Revision: 4776 $ $Date: 2006-03-05 17:10:05 -0800 (Sun, 05 Mar
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
package wicket.threadtest.apps.app1;

import java.io.Serializable;

/**
 * Represents a US phone number. We use this instead of the direct string to
 * trigger conversion to and from string. Conversion in general may be
 * re-evaluated in Wicket 1.3, hopefully making this a hack from the past by
 * then.
 * 
 * @author Eelco Hillenius
 */
public class UsPhoneNumber implements Serializable
{
	private String number;

	/**
	 * Construct.
	 * 
	 * @param number
	 */
	public UsPhoneNumber(String number)
	{
		this.number = number;
	}

	/**
	 * Gets text.
	 * 
	 * @return text
	 */
	public String getNumber()
	{
		return number;
	}

	/**
	 * Sets text.
	 * 
	 * @param number
	 *            text
	 */
	public void setNumber(String number)
	{
		this.number = number;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return number;
	}
}