package wicket.examples.ajax.builtin;

import java.io.Serializable;

import wicket.ResourceReference;
import wicket.ajax.AjaxRequestTarget;
import wicket.extensions.rating.RatingPanel;
import wicket.markup.html.link.Link;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

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
	public static final ResourceReference WICKETSTAR0 = new ResourceReference(RatingsPage.class,
			"WicketStar0.png");

	/**
	 * Star image for selected star
	 */
	public static final ResourceReference WICKETSTAR1 = new ResourceReference(RatingsPage.class,
			"WicketStar1.png");

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Link to reset the ratings.
	 */
	private final class ResetRatingLink extends Link
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
		public ResetRatingLink(String id, IModel object)
		{
			super(id, object);
		}

		/**
		 * @see Link#onClick()
		 */
		public void onClick()
		{
			RatingModel rating = (RatingModel)getModelObject();
			rating.nrOfVotes = 0;
			rating.rating = 0;
			rating.sumOfRatings = 0;
		}
	}

	/**
	 * Rating model for storing the ratings, typically this comes from a
	 * database.
	 */
	public static class RatingModel implements Serializable
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
			return new Integer(nrOfVotes);
		}

		/**
		 * Adds the vote from the user to the total of votes, and calculates the
		 * rating.
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
			return new Double(rating);
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
	 * static models for the ratings, not thread safe, but in this case, we
	 * don't care.
	 */
	private static RatingModel rating1 = new RatingModel();

	/**
	 * static model for the ratings, not thread safe, but in this case, we don't
	 * care.
	 */
	private static RatingModel rating2 = new RatingModel();

	/**
	 * keeps track whether the user has already voted on this page, comes
	 * typically from the database, or is stored in a cookie on the client side.
	 */
	private Boolean hasVoted = Boolean.FALSE;

	/**
	 * Constructor.
	 */
	public RatingsPage()
	{
		add(new RatingPanel("rating1", new PropertyModel(rating1, "rating"), 5, new PropertyModel(
				rating1, "nrOfVotes"), true)
		{
			protected boolean onIsStarActive(int star)
			{
				return RatingsPage.rating1.isActive(star);
			}

			protected void onRated(int rating, AjaxRequestTarget target)
			{
				RatingsPage.rating1.addRating(rating);
			}
		});
		add(new RatingPanel("rating2", new PropertyModel(rating2, "rating"), new Model(new Integer(
				5)), new PropertyModel(rating2, "nrOfVotes"), new PropertyModel(this, "hasVoted"),
				true)
		{
			protected String getActiveStarUrl(int iteration)
			{
				return getRequestCycle().urlFor(WICKETSTAR1).toString();
			}

			protected String getInactiveStarUrl(int iteration)
			{
				return getRequestCycle().urlFor(WICKETSTAR0).toString();
			}

			protected boolean onIsStarActive(int star)
			{
				return RatingsPage.rating2.isActive(star);
			}

			protected void onRated(int rating, AjaxRequestTarget target)
			{
				// make sure the user can't vote again
				hasVoted = Boolean.TRUE;
				RatingsPage.rating2.addRating(rating);
			}
		});
		add(new ResetRatingLink("reset1", new Model(rating1)));
		add(new ResetRatingLink("reset2", new Model(rating2)));
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
}
