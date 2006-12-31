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
package wicket.extensions.rating;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.behavior.HeaderContributor;
import wicket.extensions.behavior.SimpleAttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.Loop;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.StringResourceModel;

/**
 * Rating component that generates a number of stars where a user can click on
 * to rate something. Subclasses should implement
 * {@link #onRated(int, AjaxRequestTarget)} to provide the calculation of the
 * rating, and {@link #onIsStarActive(int)} to indicate whether to render an
 * active star or an inactive star.
 * <p>
 * Active stars are the stars that show the rating, inactive stars are the left
 * overs. E.G. a rating of 3.4 on a scale of 5 stars will render 3 active stars,
 * and 2 inactive stars (provided that the {@link #onIsStarActive(int)} returns
 * <code>true</code> for each of the first three stars).
 * <p>
 * Use this component in the following way:
 * 
 * <pre>
 * add(new RatingPanel(&quot;rating&quot;, new PropertyModel(rating, &quot;rating&quot;), 5)
 * {
 * 	protected boolean onIsStarActive(int star)
 * 	{
 * 		return rating.isActive(star);
 * 	}
 * 
 * 	protected void onRated(int rating, AjaxRequestTarget target)
 * 	{
 * 		rating1.addRating(rating);
 * 	}
 * });
 * </pre>
 * 
 * The user of this component is responsible for creating a model that supplies
 * a Double (or Float) value for the rating message, however the rating panel
 * doesn't necessarily have to contain a float or number rating value.
 * <p>
 * Though not obligatory, you could also supply a value for the number of votes
 * cast, which allows the component to render a more complete message in the
 * rating label.
 * 
 * <h2>Customizing the rating value and label</h2>
 * To customize the rating value, one should override the
 * {@link #newRatingLabel(MarkupContainer, String, IModel, IModel)} method and
 * create another label instead, based on the provided models. If you do so, and
 * use another system of rating than returning a Float or Double, then you
 * should also customize the rating resource bundle to reflect your message. The
 * default resource bundle assumes a numeric value for the rating.
 * 
 * <h2>Resource bundle</h2>
 * This component uses two types of messages: rating.simple and rating.complete.
 * The first message is used when no model is given for the number of cast
 * votes. The complete message shows the text 'Rating xx.yy from zz votes'.
 * 
 * <pre>
 *              rating.simple=Rated {0,number,#.#}
 *              rating.complete=Rated {0,number,#.#} from {1,number,#} votes
 * </pre>
 * 
 * <h2>Customizing the star images</h2>
 * To customize the images shown, override the {@link #getActiveStarUrl(int)}
 * and {@link #getInactiveStarUrl(int)} methods. Using the iteration parameter
 * it is possible to use a different image for each star, creating a fade effect
 * or something similar.
 * 
 * @author Martijn Dashorst
 */
public abstract class RatingPanel extends Panel<Integer>
{
	/**
	 * Renders the stars and the links necessary for rating.
	 */
	private final class RatingStarBar extends Loop
	{
		/** For serialization. */
		private static final long serialVersionUID = 1L;

		private RatingStarBar(MarkupContainer parent, String id, IModel<Integer> model)
		{
			super(parent, id, model);
		}

		@Override
		protected void populateItem(LoopItem item)
		{
			// Use an AjaxFallbackLink for rating to make voting work even
			// without Ajax.
			AjaxFallbackLink link = new AjaxFallbackLink(item, "link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled()
				{
					return !((Boolean)hasVoted.getObject()).booleanValue();
				}

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					LoopItem item = (LoopItem)getParent();

					// adjust the rating, and provide the target to the subclass
					// of our rating component, so other components can also get
					// updated in case of an AJAX event.

					onRated(item.getIteration() + 1, target);

					// if we process an AJAX event, update this panel
					if (target != null)
					{
						target.addComponent(RatingPanel.this.get("rater"));
					}
				}
			};

			int iteration = item.getIteration();

			// add the star image, which is either active (highlighted) or
			// inactive (no star)
			new WebMarkupContainer(link, "star").add(new SimpleAttributeModifier("src",
					(onIsStarActive(iteration)
							? getActiveStarUrl(iteration)
							: getInactiveStarUrl(iteration))));
		}
	}

	/**
	 * Star image for no selected star
	 */
	public static final ResourceReference STAR0 = new ResourceReference(RatingPanel.class,
			"star0.gif");

	/**
	 * Star image for selected star
	 */
	public static final ResourceReference STAR1 = new ResourceReference(RatingPanel.class,
			"star1.gif");

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * The flag on whether the current user has voted already.
	 */
	private IModel hasVoted;

	/**
	 * The number of stars that need to be shown, should result in an Integer
	 * object.
	 */
	private IModel<Integer> nrOfStars = new Model<Integer>(Integer.valueOf(5));

	/**
	 * Handle to the rating label to set the visibility.
	 */
	private Component ratingLabel;

	/**
	 * Constructs a rating component with 5 stars, using a compound property
	 * model as its model to retrieve the rating.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id.
	 */
	public RatingPanel(MarkupContainer parent, String id)
	{
		this(parent, id, null, 5, true);
	}

	/**
	 * Constructs a rating component with 5 stars, using the rating for
	 * retrieving the rating.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id
	 * @param rating
	 *            the model to get the rating
	 */
	public RatingPanel(MarkupContainer parent, String id, IModel<Integer> rating)
	{
		this(parent, id, rating, new Model<Integer>(Integer.valueOf(5)), null, new Model<Boolean>(
				Boolean.FALSE), true);
	}

	/**
	 * Constructs a rating panel with nrOfStars stars, where the rating model is
	 * used to retrieve the rating, the nrOfVotes model used to retrieve the
	 * number of votes cast and the hasVoted model to retrieve whether the user
	 * already had cast a vote.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id.
	 * @param rating
	 *            the (calculated) rating, i.e. 3.4
	 * @param nrOfStars
	 *            the number of stars to display
	 * @param nrOfVotes
	 *            the number of cast votes
	 * @param hasVoted
	 *            has the user already voted?
	 * @param addDefaultCssStyle
	 *            should this component render its own default CSS style?
	 */
	public RatingPanel(MarkupContainer parent, String id, IModel<Integer> rating,
			IModel<Integer> nrOfStars, IModel<Integer> nrOfVotes, IModel<Boolean> hasVoted,
			boolean addDefaultCssStyle)
	{
		super(parent, id, rating);

		this.nrOfStars = nrOfStars;
		this.hasVoted = hasVoted;

		WebMarkupContainer rater = new WebMarkupContainer(this, "rater");
		newRatingStarBar(rater, "element", nrOfStars);

		// add the text label for the message 'Rating 4.5 out of 25 votes'
		ratingLabel = newRatingLabel(rater, "rating", rating, nrOfVotes);

		// set auto generation of the markup id on, such that ajax calls work.
		rater.setOutputMarkupId(true);

		// don't render the outer tags in the target document, just the div that
		// is inside the panel.
		setRenderBodyOnly(true);
		if (addDefaultCssStyle)
		{
			addDefaultCssStyle();
		}
	}

	/**
	 * Constructs a rating component with nrOfStars stars, using the rating for
	 * retrieving the rating.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id
	 * @param rating
	 *            the model to get the rating
	 * @param nrOfStars
	 *            the number of stars to display
	 * @param addDefaultCssStyle
	 *            should this component render its own default CSS style?
	 */
	public RatingPanel(MarkupContainer parent, String id, IModel<Integer> rating, int nrOfStars,
			boolean addDefaultCssStyle)
	{
		this(parent, id, rating, new Model<Integer>(Integer.valueOf(nrOfStars)), null,
				new Model<Boolean>(Boolean.FALSE), addDefaultCssStyle);
	}

	/**
	 * Constructs a rating panel with nrOfStars stars, where the rating model is
	 * used to retrieve the rating, the nrOfVotes model to retrieve the number
	 * of casted votes. This panel doens't keep track of whether the user has
	 * already voted.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id
	 * @param rating
	 *            the model to get the rating
	 * @param nrOfStars
	 *            the number of stars to display
	 * @param nrOfVotes
	 *            the number of cast votes
	 * @param addDefaultCssStyle
	 *            should this component render its own default CSS style?
	 */
	public RatingPanel(MarkupContainer parent, String id, IModel<Integer> rating, int nrOfStars,
			IModel<Integer> nrOfVotes, boolean addDefaultCssStyle)
	{
		this(parent, id, rating, new Model<Integer>(Integer.valueOf(nrOfStars)), nrOfVotes,
				new Model<Boolean>(Boolean.FALSE), addDefaultCssStyle);
	}

	/**
	 * Constructs a rating component with nrOfStars stars, using a compound
	 * property model as its model to retrieve the rating.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the component id
	 * @param nrOfStars
	 *            the number of stars to display
	 */
	public RatingPanel(MarkupContainer parent, String id, int nrOfStars)
	{
		this(parent, id, null, 5, true);
	}

	/**
	 * Will let the rating panel contribute a CSS include to the page's header.
	 * It will add RatingPanel.css from this package. This method is typically
	 * called by the class that creates the rating panel.
	 */
	public final void addDefaultCssStyle()
	{
		add(HeaderContributor.forCss(RatingPanel.class, "RatingPanel.css"));
	}

	/**
	 * Sets the visibility of the rating label.
	 * 
	 * @param visible
	 *            true when the label should be visible
	 * @return this for chaining.
	 */
	public RatingPanel setRatingLabelVisible(boolean visible)
	{
		ratingLabel.setVisible(visible);
		return this;
	}

	/**
	 * Returns the url pointing to the image of active stars, is used to set the
	 * URL for the image of an active star. Override this method to provide your
	 * own images.
	 * 
	 * @param iteration
	 *            the sequence number of the star
	 * @return the url pointing to the image for active stars.
	 */
	protected String getActiveStarUrl(int iteration)
	{
		return getRequestCycle().urlFor(STAR1).toString();
	}

	/**
	 * Returns the url pointing to the image of inactive stars, is used to set
	 * the URL for the image of an inactive star. Override this method to
	 * provide your own images.
	 * 
	 * @param iteration
	 *            the sequence number of the star
	 * @return the url pointing to the image for inactive stars.
	 */
	protected String getInactiveStarUrl(int iteration)
	{
		return getRequestCycle().urlFor(STAR0).toString();
	}

	/**
	 * Creates a new rating label, showing a message like 'Rated 5.4 from 53
	 * votes'.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the id of the label
	 * @param rating
	 *            the model containing the rating
	 * @param nrOfVotes
	 *            the model containing the number of votes (may be null)
	 * @return the label component showing the message.
	 */
	protected Component newRatingLabel(MarkupContainer parent, String id, IModel<Integer> rating,
			IModel<Integer> nrOfVotes)
	{
		IModel<String> model = null;
		if (nrOfVotes == null)
		{
			Object[] parameters = new Object[] { rating };
			model = new StringResourceModel("rating.simple", this, null, parameters);
		}
		else
		{
			Object[] parameters = new Object[] { rating, nrOfVotes };
			model = new StringResourceModel("rating.complete", this, null, parameters);
		}
		return new Label(parent, id, model);
	}

	/**
	 * Creates a new bar filled with stars to click on.
	 * 
	 * @param parent
	 *            The parent
	 * @param id
	 *            the bar id
	 * @param nrOfStars
	 *            the number of stars to generate
	 * @return the bar with rating stars
	 */
	protected Component newRatingStarBar(MarkupContainer parent, String id,
			IModel<Integer> nrOfStars)
	{
		return new RatingStarBar(parent, id, nrOfStars);
	}

	/**
	 * Returns <code>true</code> when the star identified by its sequence
	 * number should be shown as active.
	 * 
	 * @param star
	 *            the sequence number of the star (ranging from 0 to nrOfStars)
	 * @return <code>true</code> when the star should be rendered as active
	 */
	protected abstract boolean onIsStarActive(int star);

	/**
	 * Notification of a click on a rating star. Add your own components to the
	 * request target when you want to have them updated in the Ajax request.
	 * <strong>NB</strong> the target may be null when the click isn't handled
	 * using AJAX, but using a fallback scenario.
	 * 
	 * @param rating
	 *            the number of the star that is clicked, ranging from 1 to
	 *            nrOfStars
	 * @param target
	 *            the request target, null if the request is a regular, non-AJAX
	 *            request.
	 */
	protected abstract void onRated(int rating, AjaxRequestTarget target);
}
