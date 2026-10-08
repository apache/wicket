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
package org.apache.wicket.extensions.ajax.veil;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

/**
 * Veils the whole page while an Ajax request is in flight, see {@link AbstractVeilBehavior}.
 * <p>
 * Add it to a page, typically a base page, to cover every Ajax request fired from it. A request
 * fired from inside a component with a {@link LocalVeilBehavior} veils only that component
 * instead. Requests the user did not start, such as those of an Ajax timer, are veiled as well.
 * To leave a request unveiled, call {@link #noVeil(AjaxRequestAttributes)} from the
 * {@code updateAjaxAttributes} of its behavior:
 *
 * <pre>
 * &#064;Override
 * protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
 * {
 * 	super.updateAjaxAttributes(attributes);
 * 	PageVeilBehavior.noVeil(attributes);
 * }
 * </pre>
 *
 * @since 11.0.0
 */
public class PageVeilBehavior extends AbstractVeilBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * The extra request parameter exempting an Ajax request from any veil, page or local.
	 */
	public static final String NO_VEIL_PARAMETER = "wicket_nb";

	/**
	 * Exempts an Ajax request from being veiled, by the page veil as well as by any
	 * {@link LocalVeilBehavior}. It adds the extra parameter {@value #NO_VEIL_PARAMETER}, so the
	 * request carries it to the server too.
	 *
	 * @param attributes
	 *            the attributes of the request to exempt
	 */
	public static void noVeil(AjaxRequestAttributes attributes)
	{
		attributes.getExtraParameters().put(NO_VEIL_PARAMETER, "true");
	}

	/**
	 * @throws IllegalArgumentException
	 *             if the component is not a {@link Page}
	 */
	@Override
	public void bind(Component component)
	{
		super.bind(component);

		if (component instanceof Page == false)
		{
			throw new IllegalArgumentException(PageVeilBehavior.class.getSimpleName() +
				" can only be added to a page, not to " + component.getClass().getName());
		}
	}

	@Override
	protected CharSequence getInitScript(Component component)
	{
		return "Wicket.Veil.page(" + getOptions() + ");";
	}
}
