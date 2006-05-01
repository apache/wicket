/*
 * $Id: FormInputModel.java 4776 2006-03-05 17:10:05 -0800 (Sun, 05 Mar 2006)
 * joco01 $ $Revision$ $Date: 2006-03-05 17:10:05 -0800 (Sun, 05 Mar
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
package wicket.examples.forminput;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simple model object for FormInput example. Has a number of simple properties
 * that can be retrieved and set.
 */
public final class FormInputModel implements Serializable
{
	/**
	 * Represents a line of text. Hack to get around the fact that strings are
	 * immutable.
	 */
	public final class Line implements Serializable
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
		public String toString()
		{
			return text;
		}
	}

	private String stringProperty = "test";
	private Integer integerProperty = new Integer(100);
	private Double doubleProperty = new Double(20.5);
	private Date dateProperty = new Date();
	private Integer integerInRangeProperty = new Integer(50);
	private Boolean booleanProperty;
	private URL urlProperty;
	private String numberRadioChoice = (String)FormInput.NUMBERS.get(0);
	private Set siteSelection = new HashSet();
	private List lines = new ArrayList();
	private String numbersGroup;

	/** US phone number with mask '(###) ###-####'. */
	private UsPhoneNumber phoneNumberUS = new UsPhoneNumber("(123) 456-1234");
	private List numbersCheckGroup = new ArrayList();

	/**
	 * Construct.
	 */
	public FormInputModel()
	{
		try
		{
			urlProperty = new URL("http://wicket.sourceforge.net");
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
	 * Gets dateProperty.
	 * 
	 * @return dateProperty
	 */
	public Date getDateProperty()
	{
		return dateProperty;
	}

	/**
	 * Sets dateProperty.
	 * 
	 * @param dateProperty
	 *            dateProperty
	 */
	public void setDateProperty(Date dateProperty)
	{
		this.dateProperty = dateProperty;
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
	 * Gets integerProperty.
	 * 
	 * @return integerProperty
	 */
	public Integer getIntegerProperty()
	{
		return integerProperty;
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
	 * Gets stringProperty.
	 * 
	 * @return stringProperty
	 */
	public String getStringProperty()
	{
		return stringProperty;
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
	 * Gets integerInRangeProperty.
	 * 
	 * @return integerInRangeProperty
	 */
	public Integer getIntegerInRangeProperty()
	{
		return integerInRangeProperty;
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
	 * Gets the booleanProperty.
	 * 
	 * @return booleanProperty
	 */
	public Boolean getBooleanProperty()
	{
		return booleanProperty;
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
	 * Gets the urlProperty.
	 * 
	 * @return urlProperty
	 */
	public URL getUrlProperty()
	{
		return urlProperty;
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
	 * Gets the favoriteColor.
	 * 
	 * @return favoriteColor
	 */
	public String getNumberRadioChoice()
	{
		return numberRadioChoice;
	}

	/**
	 * Sets the favoriteColor.
	 * 
	 * @param favoriteColor
	 *            favoriteColor
	 */
	public void setNumberRadioChoice(String favoriteColor)
	{
		this.numberRadioChoice = favoriteColor;
	}

	/**
	 * Sets the number.
	 * 
	 * @param group
	 *            number
	 */
	public void setNumbersGroup(String group)
	{
		this.numbersGroup = group;
	}

	/**
	 * @return the group number
	 */
	public String getNumbersGroup()
	{
		return this.numbersGroup;
	}

	/**
	 * @return the numbers list
	 */
	public List getNumbersCheckGroup()
	{
		return numbersCheckGroup;
	}

	/**
	 * Gets the selectedSites.
	 * 
	 * @return selectedSites
	 */
	public Set getSiteSelection()
	{
		return siteSelection;
	}

	/**
	 * Sets the selectedSites.
	 * 
	 * @param selectedSites
	 *            selectedSites
	 */
	public void setSiteSelection(Set selectedSites)
	{
		this.siteSelection = selectedSites;
	}

	/**
	 * Gets lines.
	 * 
	 * @return lines
	 */
	public List getLines()
	{
		return lines;
	}

	/**
	 * Sets lines.
	 * 
	 * @param lines
	 *            lines
	 */
	public void setLines(List lines)
	{
		this.lines = lines;
	}

	/**
	 * @return the phoneNumberUS
	 */
	public UsPhoneNumber getPhoneNumberUS()
	{
		return phoneNumberUS;
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
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer();
		b.append("[TestInputObject stringProperty = '").append(stringProperty).append(
				"', integerProperty = ").append(integerProperty).append(", doubleProperty = ")
				.append(doubleProperty).append(", dateProperty = ").append(dateProperty).append(
						", booleanProperty = ").append(booleanProperty).append(
						", integerInRangeProperty = ").append(integerInRangeProperty).append(
						", urlProperty = ").append(urlProperty).append(", phoneNumberUS = ")
				.append(phoneNumberUS).append(", numberRadioChoice = ").append(numberRadioChoice)
				.append(", numbersCheckgroup ").append(numbersCheckGroup).append(
						", numberRadioGroup= ").append(numbersGroup);
		b.append(", selected sites {");
		for (Iterator i = siteSelection.iterator(); i.hasNext();)
		{
			b.append(i.next());
			if (i.hasNext())
			{
				b.append(",");
			}
		}
		b.append("]");
		b.append(", lines [");
		for (Iterator i = lines.iterator(); i.hasNext();)
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