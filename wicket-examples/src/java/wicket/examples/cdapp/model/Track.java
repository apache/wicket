/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp.model;

/**
 * @author Eelco Hillenius
 */
public class Track extends Entity
{
	private CD cd;
	private Integer number;
	private String title;
	private Double length;
	private String performer;

	/**
	 * Construct.
	 */
	public Track()
	{

	}

	/**
	 * Construct.
	 * @param number
	 * @param title
	 * @param length
	 * @param performer
	 */
	public Track(int number, String title, double length, String performer)
	{
		super();
		this.number = new Integer(number);
		this.title = title;
		this.length = new Double(length);
		this.performer = performer;
	}

	/**
	 * @return CD
	 */
	public CD getCd()
	{
		return cd;
	}

	/**
	 * @return Double
	 */
	public Double getLength()
	{
		return length;
	}

	/**
	 * @return String
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return Integer
	 */
	public Integer getNumber()
	{
		return number;
	}

	/**
	 * @param cd
	 */
	public void setCd(CD cd)
	{
		this.cd = cd;
	}

	/**
	 * @param length
	 */
	public void setLength(Double length)
	{
		this.length = length;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @param number
	 */
	public void setNumber(Integer number)
	{
		this.number = number;
	}

	/**
	 * @return performer
	 */
	public String getPerformer()
	{
		return performer;
	}

	/**
	 * @param string
	 */
	public void setPerformer(String string)
	{
		performer = string;
	}
}
