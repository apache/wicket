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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.rating.RatingPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;


/**
 * Demo page for the rating component.
 * 
 * @author Martijn Dashorst
 */
public class RatingsPage extends BasePage
{
	/**
	 * Star image for no selected star
	 */
	public static final ResourceReference WICKETSTAR0 = new PackageResourceReference(
		RatingsPage.class, "WicketStar0.png");

	/**
	 * Star image for selected star
	 */
	public static final ResourceReference WICKETSTAR1 = new PackageResourceReference(
		RatingsPage.class, "WicketStar1.png");

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Link to reset the ratings.
	 */
	private final class ResetRatingLink extends Link<RatingModel>
	{
		/** For serialization. */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            component id
		 * @param object
		 *            the model to reset.
		 */
		public ResetRatingLink(String id, IModel<RatingModel> object)
		{
			super(id, object);
		}

		/**
		 * @see Link#onClick()
		 */
		@Override
		public void onClick()
		{
			RatingModel rating = getModelObject();
			rating.nrOfVotes = 0;
			rating.rating = 0;
			rating.sumOfRatings = 0;
		}
	}

	/**
	 * Rating model for storing the ratings, typically this comes from a database.
	 */
	public static class RatingModel implements IClusterable
	{
		private int nrOfVotes = 0;
		private int sumOfRatings = 0;
		private double rating = 0;

		/**
		 * Returns whether the star should be rendered active.
		 * 
		 * @param star
		 *            the number of the star
		 * @return true when the star is active
		 */
		public boolean isActive(int star)
		{
			return star < ((int)(rating + 0.5));
		}

		/**
		 * Gets the number of cast votes.
		 * 
		 * @return the number of cast votes.
		 */
		public Integer getNrOfVotes()
		{
			return nrOfVotes;
		}

		/**
		 * Adds the vote from the user to the total of votes, and calculates the rating.
		 * 
		 * @param nrOfStars
		 *            the number of stars the user has cast
		 */
		public void addRating(int nrOfStars)
		{
			nrOfVotes++;
			sumOfRatings += nrOfStars;
			rating = sumOfRatings / (1.0 * nrOfVotes);
		}

		/**
		 * Gets the rating.
		 * 
		 * @return the rating
		 */
		public Double getRating()
		{
			return rating;
		}

		/**
		 * Returns the sum of the ratings.
		 * 
		 * @return the sum of the ratings.
		 */
		public int getSumOfRatings()
		{
			return sumOfRatings;
		}
	}

	/**
	 * static models for the ratings, not thread safe, but in this case, we don't care.
	 */
	private static RatingModel rating1 = new RatingModel();

	/**
	 * static model for the ratings, not thread safe, but in this case, we don't care.
	 */
	private static RatingModel rating2 = new RatingModel();

	/**
	 * keeps track whether the user has already voted on this page, comes typically from the
	 * database, or is stored in a cookie on the client side.
	 */
	private Boolean hasVoted = Boolean.FALSE;

	/**
	 * Constructor.
	 */
	public RatingsPage()
	{
		add(new RatingPanel("rating1", new PropertyModel<Integer>(rating1, "rating"), 5,
			new PropertyModel<Integer>(rating1, "nrOfVotes"), true)
		{
			@Override
			public boolean onIsStarActive(int star)
			{
				return RatingsPage.rating1.isActive(star);
			}

			@Override
			public void onRated(int rating, AjaxRequestTarget target)
			{
				RatingsPage.rating1.addRating(rating);
			}
		});

		add(new RatingPanel("rating2", new PropertyModel<Integer>(rating2, "rating"),
			new Model<Integer>(5), new PropertyModel<Integer>(rating2, "nrOfVotes"),
			new PropertyModel<Boolean>(this, "hasVoted"), true)
		{
			@Override
			protected String getActiveStarUrl(int iteration)
			{
				IRequestHandler handler = new ResourceReferenceRequestHandler(WICKETSTAR1);
				return getRequestCycle().urlFor(handler).toString();
			}

			@Override
			protected String getInactiveStarUrl(int iteration)
			{
				IRequestHandler handler = new ResourceReferenceRequestHandler(WICKETSTAR0);
				return getRequestCycle().urlFor(handler).toString();
			}

			@Override
			public boolean onIsStarActive(int star)
			{
				return RatingsPage.rating2.isActive(star);
			}

			@Override
			public void onRated(int rating, AjaxRequestTarget target)
			{
				// make sure the user can't vote again
				hasVoted = Boolean.TRUE;
				RatingsPage.rating2.addRating(rating);
			}
		});
		add(new ResetRatingLink("reset1", new Model<>(rating1)));
		add(new ResetRatingLink("reset2", new Model<>(rating2)));
	}

	/**
	 * Getter for the hasVoted flag.
	 * 
	 * @return <code>true</code> when the user has already voted.
	 */
	public Boolean getHasVoted()
	{
		return hasVoted;
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}


}
