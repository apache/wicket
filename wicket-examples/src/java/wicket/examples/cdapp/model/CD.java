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

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Eelco Hillenius
 */
public class CD extends Entity
{
	private String title;
	private String performers;
	private String label;
	private String description;
	private Integer rating;

	private Integer year;
	private List tracks;
	private Set categories = new HashSet();

	private int[] availableRatings;


	/**
	 * Construct.
	 */
	public CD()
	{

	}

	/**
	 * Construct.
	 * @param title
	 * @param performers
	 * @param label
	 * @param description
	 * @param year
	 * @param tracks
	 * @param categories
	 * @param rating
	 */
	public CD(String title, String performers, String label, String description, int year,
			List tracks, Set categories, int rating)
	{
		super();
		this.title = title;
		this.performers = performers;
		this.label = label;
		this.description = description;
		this.rating = new Integer(rating);
		this.year = new Integer(year);
		this.tracks = tracks;
		this.categories = categories;
	}

	/**
	 * get label
	 * @return String
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * get performers
	 * @return String
	 */
	public String getPerformers()
	{
		return performers;
	}

	/**
	 * get title
	 * @return String
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * get tracks
	 * @return List
	 */
	public List getTracks()
	{
		return tracks;
	}

	/**
	 * get year
	 * @return Integer
	 */
	public Integer getYear()
	{
		return year;
	}

	/**
	 * set label
	 * @param label
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * set performers
	 * @param performers
	 */
	public void setPerformers(String performers)
	{
		this.performers = performers;
	}

	/**
	 * set title
	 * @param title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * set tracks
	 * @param tracks
	 */
	public void setTracks(List tracks)
	{
		this.tracks = tracks;
	}

	/**
	 * set year
	 * @param year
	 */
	public void setYear(Integer year)
	{
		this.year = year;
	}

	/**
	 * add a track
	 * @param track
	 */
	public void addTrack(Track track)
	{

		track.setCd(this);

		tracks.add(track);
	}

	/**
	 * This methods returns the total trackTime rounded to 2 decimals
	 * @return Double totalTrackTime
	 */
	public Double getTotalTrackTime()
	{
		double totalTrackTime = 0;

		if (tracks == null)
		{
			return new Double(0);
		}

		Iterator iter = tracks.iterator();
		while (iter.hasNext())
		{
			Track aTrack = (Track)iter.next();
			if (aTrack != null && aTrack.getLength() != null)
			{
				totalTrackTime += aTrack.getLength().doubleValue();
			}
		}

		DecimalFormat dc = new DecimalFormat(".00");

		String formattedDouble = dc.format(totalTrackTime);

		return new Double(formattedDouble);
	}

	/**
	 * @return String description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param string description
	 */
	public void setDescription(String string)
	{
		description = string;
	}

	/**
	 * get categories for this cd
	 * @return Set set of categories for this cd
	 */
	public Set getCategories()
	{
		return categories;
	}

	/**
	 * set categories for this cd
	 * @param categories categories for this cd
	 */
	public void setCategories(Set categories)
	{
		this.categories = categories;
	}

	/**
	 * does this cd has (is member of) the given category?
	 * @param category category
	 * @return boolean true if this cd has (is member of) the given category,
	 *         false otherwise
	 */
	public boolean hasCategory(Category category)
	{
		return (categories != null && categories.contains(category));
	}

	/**
	 * get rating
	 * @return Integer
	 */
	public Integer getRating()
	{
		return rating;
	}

	/**
	 * set rating
	 * @param rating
	 */
	public void setRating(Integer rating)
	{
		this.rating = rating;
	}

	/**
	 * help method to get the available ratings for cd's
	 * @return int[] the available ratings for cd's
	 */
	public int[] getAvailableRatings()
	{
		// ugly code, but works for this example
		return new int[] { 1, 2, 3, 4, 5 };
	}

	/**
	 * set the available ratings for cd's
	 * @param availableRatings the available ratings for cd's
	 */
	public void setAvailableRatings(int[] availableRatings)
	{
		this.availableRatings = availableRatings;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "CD{" + title + "}";
	}
}
