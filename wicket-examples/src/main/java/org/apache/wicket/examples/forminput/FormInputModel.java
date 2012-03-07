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
package org.apache.wicket.examples.forminput;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.util.io.IClusterable;


/**
 * Simple model object for FormInput example. Has a number of simple properties that can be
 * retrieved and set.
 */
public final class FormInputModel implements IClusterable
{
	/**
	 * Represents a line of text. Hack to get around the fact that strings are immutable.
	 */
	public final class Line implements IClusterable
	{
		private String text;

		/**
		 * Construct.
		 * 
		 * @param text
		 */
		public Line(String text)
		{
			this.text = text;
		}

		/**
		 * Gets text.
		 * 
		 * @return text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * Sets text.
		 * 
		 * @param text
		 *            text
		 */
		public void setText(String text)
		{
			this.text = text;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return text;
		}
	}

	private Boolean booleanProperty;
	private Double doubleProperty = 20.5;
	private Integer integerInRangeProperty = 50;
	private Integer integerProperty = 100;
	private List<Line> lines = new ArrayList<Line>();
	private Integer multiply = 0;
	private String numberRadioChoice = FormInput.NUMBERS.get(0);
	private final List<String> numbersCheckGroup = new ArrayList<String>();
	private String numbersGroup;
	/** US phone number with mask '(###) ###-####'. */
	private UsPhoneNumber phoneNumberUS = new UsPhoneNumber("(123) 456-1234");
	private Set<String> siteSelection = new HashSet<String>();
	private String stringProperty = "test";
	private URL urlProperty;

	/**
	 * Construct.
	 */
	public FormInputModel()
	{
		try
		{
			urlProperty = new URL("http://wicket.apache.org");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		lines.add(new Line("line one"));
		lines.add(new Line("line two"));
		lines.add(new Line("line three"));
	}

	/**
	 * Gets the booleanProperty.
	 * 
	 * @return booleanProperty
	 */
	public Boolean getBooleanProperty()
	{
		return booleanProperty;
	}

	/**
	 * Gets doubleProperty.
	 * 
	 * @return doubleProperty
	 */
	public Double getDoubleProperty()
	{
		return doubleProperty;
	}

	/**
	 * Gets integerInRangeProperty.
	 * 
	 * @return integerInRangeProperty
	 */
	public Integer getIntegerInRangeProperty()
	{
		return integerInRangeProperty;
	}

	/**
	 * Gets integerProperty.
	 * 
	 * @return integerProperty
	 */
	public Integer getIntegerProperty()
	{
		return integerProperty;
	}

	/**
	 * Gets lines.
	 * 
	 * @return lines
	 */
	public List<Line> getLines()
	{
		return lines;
	}

	/**
	 * @return gets multiply
	 */
	public Integer getMultiply()
	{
		return multiply;
	}

	/**
	 * Gets the favoriteColor.
	 * 
	 * @return favoriteColor
	 */
	public String getNumberRadioChoice()
	{
		return numberRadioChoice;
	}

	/**
	 * @return the numbers list
	 */
	public List<String> getNumbersCheckGroup()
	{
		return numbersCheckGroup;
	}

	/**
	 * @return the group number
	 */
	public String getNumbersGroup()
	{
		return numbersGroup;
	}

	/**
	 * @return the phoneNumberUS
	 */
	public UsPhoneNumber getPhoneNumberUS()
	{
		return phoneNumberUS;
	}

	/**
	 * Gets the selectedSites.
	 * 
	 * @return selectedSites
	 */
	public Set<String> getSiteSelection()
	{
		return siteSelection;
	}

	/**
	 * Gets stringProperty.
	 * 
	 * @return stringProperty
	 */
	public String getStringProperty()
	{
		return stringProperty;
	}

	/**
	 * Gets the urlProperty.
	 * 
	 * @return urlProperty
	 */
	public URL getUrlProperty()
	{
		return urlProperty;
	}

	/**
	 * Sets the booleanProperty.
	 * 
	 * @param booleanProperty
	 *            booleanProperty
	 */
	public void setBooleanProperty(Boolean booleanProperty)
	{
		this.booleanProperty = booleanProperty;
	}

	/**
	 * Sets doubleProperty.
	 * 
	 * @param doubleProperty
	 *            doubleProperty
	 */
	public void setDoubleProperty(Double doubleProperty)
	{
		this.doubleProperty = doubleProperty;
	}

	/**
	 * Sets integerInRangeProperty.
	 * 
	 * @param integerInRangeProperty
	 *            integerInRangeProperty
	 */
	public void setIntegerInRangeProperty(Integer integerInRangeProperty)
	{
		this.integerInRangeProperty = integerInRangeProperty;
	}

	/**
	 * Sets integerProperty.
	 * 
	 * @param integerProperty
	 *            integerProperty
	 */
	public void setIntegerProperty(Integer integerProperty)
	{
		this.integerProperty = integerProperty;
	}

	/**
	 * Sets lines.
	 * 
	 * @param lines
	 *            lines
	 */
	public void setLines(List<Line> lines)
	{
		this.lines = lines;
	}

	/**
	 * @param multiply
	 *            the multiply to set
	 */
	public void setMultiply(Integer multiply)
	{
		this.multiply = multiply;
	}

	/**
	 * Sets the favoriteColor.
	 * 
	 * @param favoriteColor
	 *            favoriteColor
	 */
	public void setNumberRadioChoice(String favoriteColor)
	{
		numberRadioChoice = favoriteColor;
	}

	/**
	 * Sets the number.
	 * 
	 * @param group
	 *            number
	 */
	public void setNumbersGroup(String group)
	{
		numbersGroup = group;
	}

	/**
	 * @param phoneNumberUS
	 *            the phoneNumberUS to set
	 */
	public void setPhoneNumberUS(UsPhoneNumber phoneNumberUS)
	{
		this.phoneNumberUS = phoneNumberUS;
	}

	/**
	 * Sets the selectedSites.
	 * 
	 * @param selectedSites
	 *            selectedSites
	 */
	public void setSiteSelection(Set<String> selectedSites)
	{
		siteSelection = selectedSites;
	}

	/**
	 * Sets stringProperty.
	 * 
	 * @param stringProperty
	 *            stringProperty
	 */
	public void setStringProperty(String stringProperty)
	{
		this.stringProperty = stringProperty;
	}

	/**
	 * Sets the urlProperty.
	 * 
	 * @param urlProperty
	 *            urlProperty
	 */
	public void setUrlProperty(URL urlProperty)
	{
		this.urlProperty = urlProperty;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
	 StringBuilder b = new StringBuilder();
		b.append("[TestInputObject stringProperty = '")
			.append(stringProperty)
			.append("', integerProperty = ")
			.append(integerProperty)
			.append(", doubleProperty = ")
			.append(doubleProperty)
			.append(", booleanProperty = ")
			.append(booleanProperty)
			.append(", integerInRangeProperty = ")
			.append(integerInRangeProperty)
			.append(", urlProperty = ")
			.append(urlProperty)
			.append(", phoneNumberUS = ")
			.append(phoneNumberUS)
			.append(", numberRadioChoice = ")
			.append(numberRadioChoice)
			.append(", numbersCheckgroup ")
			.append(numbersCheckGroup)
			.append(", numberRadioGroup= ")
			.append(numbersGroup);
		b.append(", selected sites {");
		for (Iterator<String> i = siteSelection.iterator(); i.hasNext();)
		{
			b.append(i.next());
			if (i.hasNext())
			{
				b.append(",");
			}
		}
		b.append("]");
		b.append(", lines [");
		for (Iterator<Line> i = lines.iterator(); i.hasNext();)
		{
			b.append(i.next());
			if (i.hasNext())
			{
				b.append(", ");
			}
		}
		b.append("]");
		b.append("]");
		return b.toString();
	}
}