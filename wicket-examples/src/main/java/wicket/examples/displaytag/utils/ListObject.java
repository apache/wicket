/*
 * $Id: ListObject.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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
package wicket.examples.displaytag.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import wicket.util.string.Strings;


/**
 * Just a test class that returns columns of data that are useful for testing
 * out the ListTag class and ListColumn class.
 * 
 * @author epesh (wicket.examples.wicket.examples.displaytag)
 */
public class ListObject implements Serializable
{
	/**
	 * random number generator.
	 */
	private static final Random random = new Random();

	/**
	 * id.
	 */
	private int id = -1;

	/**
	 * name.
	 */
	private String name;

	/**
	 * email.
	 */
	private String email;

	/**
	 * date.
	 */
	private Date date;

	/**
	 * money.
	 */
	private double money;

	/**
	 * description.
	 */
	private String description;

	/**
	 * long description.
	 */
	private String longDescription;

	/**
	 * status.
	 */
	private String status;

	/**
	 * url.
	 */
	private String url;

	/**
	 * sub list used to test nested tables.
	 */
	private List<SubListItem> subList;

	/**
	 * Checkbox example
	 */
	private boolean active;

	/**
	 * Constructor for ListObject.
	 */
	public ListObject()
	{
		this.id = random.nextInt(99998) + 1;
		this.money = (random.nextInt(999998) + 1) / 100;

		String firstName = RandomSampleUtil.getRandomWord();
		String lastName = RandomSampleUtil.getRandomWord();

		this.name = Strings.capitalize(firstName) + " " + Strings.capitalize(lastName);

		this.email = firstName + "-" + lastName + "@" + RandomSampleUtil.getRandomWord() + ".com";

		this.date = RandomSampleUtil.getRandomDate();

		this.description = RandomSampleUtil.getRandomWord() + " "
				+ RandomSampleUtil.getRandomWord() + "...";

		this.longDescription = RandomSampleUtil.getRandomSentence(10);

		this.status = RandomSampleUtil.getRandomWord().toUpperCase();

		// added sublist for testing of nested tables
		this.subList = new ArrayList<SubListItem>();
		this.subList.add(new SubListItem());
		this.subList.add(new SubListItem());
		this.subList.add(new SubListItem());

		this.url = "http://www." + lastName + ".org/";

		this.active = RandomSampleUtil.getRandomBoolean();
	}

	/**
	 * getter for id.
	 * 
	 * @return int id
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * setter for id.
	 * 
	 * @param value
	 *            int id
	 */
	public void setId(final int value)
	{
		this.id = value;
	}

	/**
	 * getter for name.
	 * 
	 * @return String name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * getter for email.
	 * 
	 * @return String email
	 */
	public String getEmail()
	{
		return this.email;
	}

	/**
	 * setter for email.
	 * 
	 * @param value
	 *            String email
	 */
	public void setEmail(final String value)
	{
		this.email = value;
	}

	/**
	 * getter for date.
	 * 
	 * @return Date
	 */
	public Date getDate()
	{
		return this.date;
	}

	/**
	 * getter for money.
	 * 
	 * @return double money
	 */
	public double getMoney()
	{
		return this.money;
	}

	/**
	 * getter for description.
	 * 
	 * @return String description
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * getter for long description.
	 * 
	 * @return String long description
	 */
	public String getLongDescription()
	{
		return this.longDescription;
	}

	/**
	 * getter for status.
	 * 
	 * @return String status
	 */
	public String getStatus()
	{
		return this.status;
	}

	/**
	 * getter for url.
	 * 
	 * @return String url
	 */
	public String getUrl()
	{
		return this.url;
	}

	/**
	 * test for null values.
	 * 
	 * @return null
	 */
	public String getNullValue()
	{
		return null;
	}

	/**
	 * Returns a simple string representation of the object.
	 * 
	 * @return String simple representation of the object
	 */
	@Override
	public String toString()
	{
		return "ListObject(" + this.id + ")";
	}

	/**
	 * Returns a detailed string representation of the object.
	 * 
	 * @return String detailed representation of the object
	 */
	public String toDetailedString()
	{
		return "ID:          " + this.id + "\n" + "Name:        " + this.name + "\n"
				+ "Email:       " + this.email + "\n" + "Date:        " + this.date + "\n"
				+ "Money:       " + this.money + "\n" + "Description: " + this.description + "\n"
				+ "Status:      " + this.status + "\n" + "URL:         " + this.url + "\n"
				+ "Activ:         " + String.valueOf(this.active) + "\n";
	}

	/**
	 * Returns the subList.
	 * 
	 * @return List
	 */
	public List getSubList()
	{
		return this.subList;
	}

	/**
	 * Inner class used in testing nested tables.
	 * 
	 * @author fgiust
	 */
	public class SubListItem implements Serializable
	{

		/**
		 * name.
		 */
		private String itemName;

		/**
		 * email.
		 */
		private String itemEmail;

		/**
		 * Constructor for SubListItem.
		 */
		public SubListItem()
		{
			this.itemName = RandomSampleUtil.getRandomWord();
			this.itemEmail = RandomSampleUtil.getRandomEmail();
		}

		/**
		 * getter for name.
		 * 
		 * @return String name
		 */
		public String getName()
		{
			return this.itemName;
		}

		/**
		 * getter for email.
		 * 
		 * @return String
		 */
		public String getEmail()
		{
			return this.itemEmail;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "name=" + this.itemName + "; email=" + this.itemEmail;
		}
	}

	/**
	 * 
	 * @return True if active
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * 
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
}
