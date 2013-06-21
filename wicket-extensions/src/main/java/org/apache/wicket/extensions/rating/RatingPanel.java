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
package org.apache.wicket.extensions.rating;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Rating component that generates a number of stars where a user can click on to rate something.
 * Subclasses should implement {@link #onRated(int, org.apache.wicket.ajax.AjaxRequestTarget)} to provide the calculation
 * of the rating, and {@link #onIsStarActive(int)} to indicate whether to render an active star or
 * an inactive star.
 * <p>
 * Active stars are the stars that show the rating, inactive stars are the left overs. E.G. a rating
 * of 3.4 on a scale of 5 stars will render 3 active stars, and 2 inactive stars (provided that the
 * {@link #onIsStarActive(int)} returns <code>true</code> for each of the first three stars).
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
 * The user of this component is responsible for creating a model that supplies a Double (or Float)
 * value for the rating message, however the rating panel doesn't necessarily have to contain a
 * float or number rating value.
 * <p>
 * Though not obligatory, you could also supply a value for the number of votes cast, which allows
 * the component to render a more complete message in the rating label.
 * 
 * <h2>Customizing the rating value and label</h2>
 * To customize the rating value, one should override the
 * {@link #newRatingLabel(String, IModel, IModel)} method and create another label instead, based on
 * the provided models. If you do so, and use another system of rating than returning a Float or
 * Double, then you should also customize the rating resource bundle to reflect your message. The
 * default resource bundle assumes a numeric value for the rating.
 * 
 * <h2>Resource bundle</h2>
 * This component uses two types of messages: rating.simple and rating.complete. The first message
 * is used when no model is given for the number of cast votes. The complete message shows the text
 * 'Rating xx.yy from zz votes'.
 * 
 * <pre>
 *       rating.simple=Rated {0,number,#.#}
 *       rating.complete=Rated {0,number,#.#} from {1,number,#} votes
 * </pre>
 * 
 * <h2>Customizing the star images</h2>
 * To customize the images shown, override the {@link #getActiveStarUrl(int)} and
 * {@link #getInactiveStarUrl(int)} methods. Using the iteration parameter it is possible to use a
 * different image for each star, creating a fade effect or something similar.
 * 
 * @author Martijn Dashorst
 */
public abstract class RatingPanel extends Panel
{
	/**
	 * Renders the stars and the links necessary for rating.
	 */
	private final class RatingStarBar extends Loop
	{
		/** For serialization. */
		private static final long serialVersionUID = 1L;

		private RatingStarBar(final String id, final IModel<Integer> model)
		{
			super(id, model);
		}

		@Override
		protected void populateItem(final LoopItem item)
		{
			// Use an AjaxFallbackLink for rating to make voting work even
			// without Ajax.
			AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(final AjaxRequestTarget target)
				{
					LoopItem item = (LoopItem)getParent();

					// adjust the rating, and provide the target to the subclass
					// of our rating component, so other components can also get
					// updated in case of an AJAX event.

					onRated(item.getIndex() + 1, target);

					// if we process an AJAX event, update this panel
					if (target != null)
					{
						target.add(RatingPanel.this.get("rater"));
					}
				}

				@Override
				public boolean isEnabled()
				{
					return !hasVoted.getObject();
				}
			};

			int iteration = item.getIndex();

			// add the star image, which is either active (highlighted) or
			// inactive (no star)
			link.add(new WebMarkupContainer("star").add(AttributeModifier.replace("src",
				(onIsStarActive(iteration) ? getActiveStarUrl(iteration)
					: getInactiveStarUrl(iteration)))));
			item.add(link);
		}
	}

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Star image for no selected star
	 */
	public static final ResourceReference STAR0 = new PackageResourceReference(RatingPanel.class,
		"star0.gif");

	/**
	 * Star image for selected star
	 */
	public static final ResourceReference STAR1 = new PackageResourceReference(RatingPanel.class,
		"star1.gif");

	/**
	 * The number of stars that need to be shown, should result in an Integer object.
	 */
	private IModel<Integer> nrOfStars = new Model<>(5);

	/**
	 * The number of votes that have been cast, should result in an Integer object.
	 */
	private final IModel<Integer> nrOfVotes;

	/**
	 * The flag on whether the current user has voted already.
	 */
	private final IModel<Boolean> hasVoted;

	/**
	 * Handle to the rating label to set the visibility.
	 */
	private Component ratingLabel;

	private final boolean addDefaultCssStyle;

	/**
	 * Constructs a rating component with 5 stars, using a compound property model as its model to
	 * retrieve the rating.
	 * 
	 * @param id
	 *            the component id.
	 */
	public RatingPanel(final String id)
	{
		this(id, null, 5, true);
	}

	/**
	 * Constructs a rating component with 5 stars, using the rating for retrieving the rating.
	 * 
	 * @param id
	 *            the component id
	 * @param rating
	 *            the model to get the rating
	 */
	public RatingPanel(final String id, final IModel<? extends Number> rating)
	{
		this(id, rating, new Model<Integer>(5), null, new Model<>(Boolean.FALSE), true);
	}

	/**
	 * Constructs a rating component with nrOfStars stars, using a compound property model as its
	 * model to retrieve the rating.
	 * 
	 * @param id
	 *            the component id
	 * @param nrOfStars
	 *            the number of stars to display
	 */
	public RatingPanel(final String id, final int nrOfStars)
	{
		this(id, null, nrOfStars, true);
	}

	/**
	 * Constructs a rating component with nrOfStars stars, using the rating for retrieving the
	 * rating.
	 * 
	 * @param id
	 *            the component id
	 * @param rating
	 *            the model to get the rating
	 * @param nrOfStars
	 *            the number of stars to display
	 * @param addDefaultCssStyle
	 *            should this component render its own default CSS style?
	 */
	public RatingPanel(final String id, final IModel<? extends Number> rating, final int nrOfStars,
		final boolean addDefaultCssStyle)
	{
		this(id, rating, new Model<Integer>(nrOfStars), null, new Model<Boolean>(Boolean.FALSE),
			addDefaultCssStyle);
	}

	/**
	 * Constructs a rating panel with nrOfStars stars, where the rating model is used to retrieve
	 * the rating, the nrOfVotes model to retrieve the number of casted votes. This panel doens't
	 * keep track of whether the user has already voted.
	 * 
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
	public RatingPanel(final String id, final IModel<? extends Number> rating, final int nrOfStars,
		final IModel<Integer> nrOfVotes, final boolean addDefaultCssStyle)
	{
		this(id, rating, new Model<Integer>(nrOfStars), nrOfVotes,
			new Model<>(Boolean.FALSE), addDefaultCssStyle);
	}

	/**
	 * Constructs a rating panel with nrOfStars stars, where the rating model is used to retrieve
	 * the rating, the nrOfVotes model used to retrieve the number of votes cast and the hasVoted
	 * model to retrieve whether the user already had cast a vote.
	 * 
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
	public RatingPanel(final String id, final IModel<? extends Number> rating,
		final IModel<Integer> nrOfStars, final IModel<Integer> nrOfVotes,
		final IModel<Boolean> hasVoted, final boolean addDefaultCssStyle)
	{
		super(id, rating);
		this.addDefaultCssStyle = addDefaultCssStyle;

		this.nrOfStars = wrap(nrOfStars);
		this.nrOfVotes = wrap(nrOfVotes);
		this.hasVoted = wrap(hasVoted);

		WebMarkupContainer rater = new WebMarkupContainer("rater");
		rater.add(newRatingStarBar("element", this.nrOfStars));

		// add the text label for the message 'Rating 4.5 out of 25 votes'
		rater.add(ratingLabel = newRatingLabel("rating", wrap(rating), this.nrOfVotes));

		// set auto generation of the markup id on, such that ajax calls work.
		rater.setOutputMarkupId(true);

		add(rater);

		// don't render the outer tags in the target document, just the div that
		// is inside the panel.
		setRenderBodyOnly(true);
	}

	@Override
	public void renderHead(final IHeaderResponse response)
	{
		super.renderHead(response);
		if (addDefaultCssStyle)
		{
			response.render(CssHeaderItem.forReference(new CssResourceReference(
				RatingPanel.class, "RatingPanel.css")));
		}

	}

	/**
	 * Creates a new bar filled with stars to click on.
	 * 
	 * @param id
	 *            the bar id
	 * @param nrOfStars
	 *            the number of stars to generate
	 * @return the bar with rating stars
	 */
	protected Component newRatingStarBar(final String id, final IModel<Integer> nrOfStars)
	{
		return new RatingStarBar(id, nrOfStars);
	}

	/**
	 * Creates a new rating label, showing a message like 'Rated 5.4 from 53 votes'.
	 * 
	 * @param id
	 *            the id of the label
	 * @param rating
	 *            the model containing the rating
	 * @param nrOfVotes
	 *            the model containing the number of votes (may be null)
	 * @return the label component showing the message.
	 */
	protected Component newRatingLabel(final String id, final IModel<? extends Number> rating,
		final IModel<Integer> nrOfVotes)
	{
		IModel<String> model;
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
		return new Label(id, model);
	}

	/**
	 * Returns the url pointing to the image of active stars, is used to set the URL for the image
	 * of an active star. Override this method to provide your own images.
	 * 
	 * @param iteration
	 *            the sequence number of the star
	 * @return the url pointing to the image for active stars.
	 */
	protected String getActiveStarUrl(final int iteration)
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(STAR1);
		return getRequestCycle().urlFor(handler).toString();
	}

	/**
	 * Returns the url pointing to the image of inactive stars, is used to set the URL for the image
	 * of an inactive star. Override this method to provide your own images.
	 * 
	 * @param iteration
	 *            the sequence number of the star
	 * @return the url pointing to the image for inactive stars.
	 */
	protected String getInactiveStarUrl(final int iteration)
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(STAR0);
		return getRequestCycle().urlFor(handler).toString();
	}

	/**
	 * Sets the visibility of the rating label.
	 * 
	 * @param visible
	 *            true when the label should be visible
	 * @return this for chaining.
	 */
	public RatingPanel setRatingLabelVisible(final boolean visible)
	{
		ratingLabel.setVisible(visible);
		return this;
	}

	/**
	 * Returns <code>true</code> when the star identified by its sequence number should be shown as
	 * active.
	 * 
	 * @param star
	 *            the sequence number of the star (ranging from 0 to nrOfStars)
	 * @return <code>true</code> when the star should be rendered as active
	 */
	protected abstract boolean onIsStarActive(int star);

	/**
	 * Notification of a click on a rating star. Add your own components to the request target when
	 * you want to have them updated in the Ajax request. <strong>NB</strong> the target may be null
	 * when the click isn't handled using AJAX, but using a fallback scenario.
	 * 
	 * @param rating
	 *            the number of the star that is clicked, ranging from 1 to nrOfStars
	 * @param target
	 *            the request target, null if the request is a regular, non-AJAX request.
	 */
	protected abstract void onRated(int rating, AjaxRequestTarget target);
}
