/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.repeater;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.io.IClusterable;

/**
 * domain object for demonstrations.
 * 
 * @author igor
 * 
 */
public class Contact implements IClusterable
{
	private long id;

	private String firstName;

	private String lastName;

	private String homePhone;

	private String cellPhone;
	
	private Date bornDate;

	private String address;

	private String city;

	private String country;

	private volatile int progress;

	/**
	 * Constructor
	 */
	public Contact()
	{

	}

	@Override
	public String toString()
	{
		return "[Contact id=" + id + " firstName=" + firstName + " lastName=" + lastName +
				" homePhone=" + homePhone + " cellPhone=" + cellPhone + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof Contact)
		{
			Contact other = (Contact)obj;
			return other.getFirstName().equals(getFirstName()) &&
					other.getLastName().equals(getLastName()) &&
					other.getHomePhone().equals(getHomePhone()) &&
					other.getCellPhone().equals(getCellPhone());

		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		int result = firstName.hashCode();
		result = 31 * result + lastName.hashCode();
		result = 31 * result + homePhone.hashCode();
		result = 31 * result + cellPhone.hashCode();
		return result;
	}

	/**
	 * @param id
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * @return id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * Constructor
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public Contact(String firstName, String lastName)
	{
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * @return cellPhone
	 */
	public String getCellPhone()
	{
		return cellPhone;
	}

	/**
	 * @param cellPhone
	 */
	public void setCellPhone(String cellPhone)
	{
		this.cellPhone = cellPhone;
	}

	/**
	 * @return firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * @return homePhone
	 */
	public String getHomePhone()
	{
		return homePhone;
	}

	/**
	 * @param homePhone
	 */
	public void setHomePhone(String homePhone)
	{
		this.homePhone = homePhone;
	}

	/**
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * @param lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * 
	 * @return bornDate
	 */
	public Date getBornDate()
	{
	    return bornDate;
	}

	/**
	 * 
	 * @param bornDate
	 */
	public void setBornDate(Date bornDate)
	{
	    this.bornDate = bornDate;
	}

	/**
	 * @return the born date as {@code yyyy-MM-dd}, or {@code null}
	 */
	public String getBorn()
	{
		return bornDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(bornDate) : null;
	}

	/**
	 * @return the progress of some work on the contact, from 0 to 100
	 */
	public int getProgress()
	{
		return progress;
	}

	/**
	 * @param progress
	 *            the progress of some work on the contact, from 0 to 100
	 */
	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	/**
	 * @return the street address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * @param address
	 *            the street address
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}

	/**
	 * @return the city
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * @param city
	 *            the city
	 */
	public void setCity(String city)
	{
		this.city = city;
	}

	/**
	 * @return the ISO 3166 code of the country
	 */
	public String getCountry()
	{
		return country;
	}

	/**
	 * @param country
	 *            the ISO 3166 code of the country
	 */
	public void setCountry(String country)
	{
		this.country = country;
	}

	/**
	 * @return the English name of the country
	 */
	public String getCountryName()
	{
		return country != null ? Locale.of("", country).getDisplayCountry(Locale.ENGLISH) : null;
	}
}
