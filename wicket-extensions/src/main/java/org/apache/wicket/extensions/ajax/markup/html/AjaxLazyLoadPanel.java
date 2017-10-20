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
package org.apache.wicket.extensions.ajax.markup.html;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A panel which load lazily a single content component. This can be used if you have a
 * component that is pretty heavy in creation and you first want to show the user the page and
 * then replace the panel when it is ready.
 * <p>
 * This panel will wait with adding the content until {@link #isContentReady()} returns
 * {@code true}. It will poll using an AJAX timer behavior that is installed on the page. When the
 * component is replaced, the timer stops. When you have multiple {@code AjaxLazyLoadPanel}s on the
 * same page, only one timer is used and all panels piggyback on this single timer.
 * <p> 
 * This component will also replace the contents when a normal request comes through and the
 * content is ready.
 * 
 * @since 1.3
 */
public abstract class AjaxLazyLoadPanel<T extends Component> extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * The component id which will be used to load the lazily loaded component.
	 */
	private static final String CONTENT_ID = "content";

	private boolean loaded;

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public AjaxLazyLoadPanel(final String id)
	{
		this(id, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxLazyLoadPanel(final String id, final IModel<?> model)
	{
		super(id, model);

		setOutputMarkupId(true);
	}

	/**
	 * Determines that the content we're waiting for is ready, typically used in polling background
	 * threads for their result. Override this to implement your own check.
	 * <p>
	 * This default implementation returns {@code true}, i.e. assuming the content is ready immediately.
	 * 
	 * @return whether the actual content is ready
	 */
	protected boolean isContentReady()
	{
		return true;
	}

	/**
	 * Create a loading component shown instead of the actual content until it is {@link #isContentReady()}.
	 * 
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real content isn't ready yet
	 */
	protected Component createLoadingComponent(final String markupId)
	{
		IRequestHandler handler = new ResourceReferenceRequestHandler(
			AbstractDefaultAjaxBehavior.INDICATOR);
		return new Label(markupId,
			"<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler) + "\"/>")
				.setEscapeModelStrings(false);
	}

	/**
	 * Factory method for creating the lazily loaded content that replaces the loading component after
	 * {@link #isContentReady()} returns {@code true}. You may call setRenderBodyOnly(true)
	 * on this component if you need the body only.
	 * 
	 * @param markupId
	 *            The components markupid.
	 * @return the content to show after {@link #isContentReady()}
	 */
	protected abstract T createContentComponent(String markupId);

	/**
	 * Called after the loading component was replaced with the lazy loaded content.
	 * <p>
	 * This default implementation does nothing.
	 *
	 * @param component
	 *            The lazy loaded content
	 * @param target
	 *            optional Ajax request handler
	 */
	protected void onContentLoaded(T component, Optional<AjaxRequestTarget> target)
	{
	}

	/**
	 * Installs a page-global timer if not already present.
	 */
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// when the timer is not yet installed add it
		List<AjaxLazyLoadTimer> behaviors = getPage().getBehaviors(AjaxLazyLoadTimer.class);
		if (behaviors.isEmpty()) {
			AjaxLazyLoadTimer timer = new AjaxLazyLoadTimer();
			getPage().add(timer);
			
			getRequestCycle().find(AjaxRequestTarget.class).ifPresent(target -> {
				// the timer will not be rendered, so stop it first
				// and restart it immediately on the Ajax request
				timer.stop(null);
				timer.restart(target);
			});
		}
	}
	
	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		if (get(CONTENT_ID) == null) {
			add(createLoadingComponent(CONTENT_ID));
		} else {
			isLoaded();
		}
	}

	/**
	 * Get the preferred interval for updates.
	 * <p>
	 * Since all LazyLoadingPanels on a page share the same Ajax timer, its update interval
	 * is derived from the minimum of all panel's update intervals.
	 * 
	 * @return update interval
	 */
	protected Duration getUpdateInterval() {
		return Duration.seconds(1);
	}

	private boolean isLoaded() {
		if (loaded == false)
		{
			if (isContentReady())
			{
				loaded = true;

				// create the lazy load component
				T content = createContentComponent(CONTENT_ID);

				// replace the spinner with the new component
				AjaxLazyLoadPanel.this.replace(content);

				Optional<AjaxRequestTarget> target = getRequestCycle().find(AjaxRequestTarget.class);

				// notify our subclasses of the updated component
				onContentLoaded(content, target);

				// repaint our selves if there's an AJAX request in play, otherwise let the page
				// redraw itself
				target.ifPresent(t -> t.add(AjaxLazyLoadPanel.this));
			}
		}
		
		return loaded;
	}

	/**
	 * The AJAX timer for updating the AjaxLazyLoadPanel. Is designed to be a page-local singleton
	 * running as long as LazyLoadPanels are still loading.
	 * 
	 * @see AjaxLazyLoadPanel#isLoaded()
	 */
	private static class AjaxLazyLoadTimer extends AbstractAjaxTimerBehavior
	{
		private static final long serialVersionUID = 1L;

		public AjaxLazyLoadTimer()
		{
			super(Duration.ONE_SECOND);
		}

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setUpdateInterval(Duration.MAXIMUM);
			
			getComponent().getPage().visitChildren(AjaxLazyLoadPanel.class, new IVisitor<AjaxLazyLoadPanel<?>, Void>()
			{
				@Override
				public void component(AjaxLazyLoadPanel<?> panel, IVisit<Void> visit)
				{
					if (panel.isLoaded() == false) {
						setUpdateInterval(Duration.min(getUpdateInterval(), panel.getUpdateInterval()));
					}						
				}
			});

			// all panels have completed their replacements, we can stop the timer
			if (Duration.MAXIMUM.equals(getUpdateInterval()))
			{
				stop(target);
				
				getComponent().remove(this);
			}
		}
	}
}
