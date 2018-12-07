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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
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

/**
 * A panel which load lazily a single content component. This can be used if you have a
 * component that is pretty heavy in creation and you first want to show the user the page and
 * then replace the panel when it is ready.
 * <p>
 * This panel will wait with adding the content until {@link #contentState()} returns
 * {@link State#READY} or {@link State#TERMINATED}.
 * Note: Returning {@link State#TERMINATED} will act as though the panel as finished,
 * but will not call {@link #getLazyLoadComponent(String)}.
 * It will poll using an {@link AbstractAjaxTimerBehavior} that is installed on the page. When the
 * component is replaced, the timer stops. When you have multiple {@code AjaxLazyLoadPanel}s on the
 * same page, only one timer is used and all panels piggyback on this single timer.
 * <p>
 * This component will also replace the contents when a normal request comes through and the
 * content is ready.
 *
 * @since 1.3
 */
public abstract class AjaxLazyLoadPanel<T extends Component> extends Panel {
	private static final long serialVersionUID = 1L;
	private static final String SUPER_SCAN = "__super";

	/**
	 * The component id which will be used to load the lazily loaded component.
	 */
	private static final String CONTENT_ID = "content";

	private boolean loaded;

	public AjaxLazyLoadPanel(String id) {
		this(id, null);
	}

	public AjaxLazyLoadPanel(String id, IModel<?> model) {
		super(id, model);

		setOutputMarkupId(true);
	}

	public String findIndicatorId() {
		return SUPER_SCAN;
	}

	/**
	 * Get the preferred interval for updates.
	 * <p>
	 * Since all LazyLoadingPanels on a page share the same Ajax timer, its update interval
	 * is derived from the minimum of all panel's update intervals.
	 *
	 * @return update interval, must not be null.
	 */
	protected Duration getUpdateInterval() {
		return Duration.seconds(1);
	}

	public enum State {
		LOADING,
		READY,
		TERMINATED
	}

	/**
	 * Determines that the content we're waiting for is ready, typically used in polling background
	 * threads for their result. Override this to implement your own check.
	 * <p>
	 * This default implementation returns {@link State#READY}, i.e. assuming the content is ready immediately.
	 *
	 * @return whether the actual content is ready
	 */
	protected State contentState() {
		return State.READY;
	}

	/**
	 * Create a loading component shown instead of the actual content until it is {@link #contentState()}.
	 *
	 * @param id The components id
	 * @return The component to show while the real content isn't ready yet
	 */
	public Component getLoadingComponent(String id) {
		IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
		return new Label(id, "<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler) + "\"/>")
				.setEscapeModelStrings(false);
	}

	/**
	 * Factory method for creating the lazily loaded content that replaces the loading component after
	 * {@link #contentState()} returns {@code DONE}.
	 * You may call setRenderBodyOnly(true) on this component if you need the body only.
	 *
	 * @param markupId The components markup id.
	 * @return the content to show after {@link #contentState()} returns {@link State#READY}
	 */
	public abstract T getLazyLoadComponent(String markupId);

	/**
	 * Called after the loading component was replaced with the lazy loaded content.
	 * <p>
	 * This default implementation does nothing.
	 *
	 * @param content The lazy loaded content
	 * @param target  optional Ajax request handler
	 */
	protected void onContentLoaded(T content, AjaxRequestTarget target) {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(getLoadingComponent(CONTENT_ID));

		// See if we can get out early.
		if (!isLoaded()) {
			initTimer();
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		if (!loaded) {
			initTimer();
		}
	}

	/**
	 * Initialize a timer - default implementation installs an {@link AbstractAjaxTimerBehavior} on the page,
	 * if it is not already present.
	 */
	protected void initTimer() {
		Page page = getPage();
		// when the timer is not yet installed add it
		List<AjaxLazyLoadTimer> behaviours = page.getBehaviors(AjaxLazyLoadTimer.class);
		if (behaviours.isEmpty()) {
			AbstractAjaxTimerBehavior timer = new AjaxLazyLoadTimer(getUpdateInterval(), findIndicatorId());
			page.add(timer);

			// the timer will not be rendered, so restart it immediately on the Ajax target
			getRequestCycle().find(AjaxRequestTarget.class).ifPresent(timer::restart);
		}
	}

	/**
	 * Check whether the content is loaded.
	 * <p>
	 * If not loaded already and the content is ready, replaces the lazy loading component with
	 * the lazily loaded content.
	 *
	 * @return {@code true} if content is loaded
	 * @see #contentState()
	 */
	protected final boolean isLoaded() {
		if (!loaded) {
			State state = contentState();
			if (state == State.READY) {
				loaded = true;

				// create the lazy load component
				T content = getLazyLoadComponent(CONTENT_ID);

				// replace the loading component with the new component
				AjaxLazyLoadPanel.this.replace(content);

				AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class)
						.orElse(null);

				// notify our subclasses of the updated component
				onContentLoaded(content, target);

				// repaint our selves if there's an AJAX request in play,
				// otherwise let the page redraw itself
				if (target != null) {
					target.add(this);
				}
			} else if (state == State.TERMINATED) {
				loaded = true;
			}
		}
		return loaded;
	}

	/**
	 * The AJAX timer for updating the BatchedLazyLoadPanel.
	 * <p>
	 * It's designed to be a page-local singleton running as long
	 * as {@link AjaxLazyLoadPanel}s are still loading.
	 *
	 * @see AjaxLazyLoadPanel#isLoaded()
	 */
	static final class AjaxLazyLoadTimer extends AbstractAjaxTimerBehavior {
		private static final long serialVersionUID = 1L;

		private final String indicatorId;

		AjaxLazyLoadTimer(Duration duration, String indicatorId) {
			super(duration);
			this.indicatorId = indicatorId;
		}

		@Override
		protected String findIndicatorId() {
			String id = indicatorId;
			return SUPER_SCAN.equals(id)
					? super.findIndicatorId()
					: id;
		}

		@Override
		protected void onBind() {
			super.onBind();

			if (updateDuration()) {
				stop(null);
			}
		}

		@Override
		protected void onTimer(AjaxRequestTarget target) {
			// all panels have completed their replacements, we can stop the timer
			if (updateDuration()) {
				stop(target);
			}
		}

		private boolean updateDuration() {
			setUpdateInterval(Duration.MAXIMUM);

			getComponent().getPage()
					.visitChildren(AjaxLazyLoadPanel.class, (AjaxLazyLoadPanel<?> panel, IVisit<Void> visit) -> {
						if (panel.isVisibleInHierarchy() && !panel.isLoaded()) {
							Duration updateInterval = panel.getUpdateInterval();
							Duration currentInterval = getUpdateInterval();
							if (currentInterval == null) {
								throw new IllegalArgumentException("update interval must not be null");
							}
							long min = Math.min(currentInterval.getMilliseconds(), updateInterval.getMilliseconds());
							setUpdateInterval(Duration.milliseconds(min));
						}
					});

			return Duration.MAXIMUM.equals(getUpdateInterval());
		}
	}
}