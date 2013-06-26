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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import org.apache.wicket.util.io.IClusterable;

/**
 * This class encapsulates various settings for {@link AbstractAutoCompleteBehavior}. See the
 * documentation for the property accessors of this class for further information.
 * <p>
 * Default settings:
 * <table>
 * <tr>
 * <th>setting</th>
 * <th>default value</th>
 * </tr>
 * <tr>
 * <td>preselect</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>maxHeightInPx</td>
 * <td>-1</td>
 * </tr>
 * <tr>
 * <td>showListOnEmptyInput</td>
 * <td>false</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Gerolf Seitz
 */
public final class AutoCompleteSettings implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private boolean preselect = false;

	private int maxHeightInPx = -1;

	private boolean showListOnEmptyInput = false;

	private boolean useSmartPositioning = false;

	private boolean ignoreBordersWhenPositioning = true;

	private String cssClassName = null;

	private boolean adjustInputWidth = true;

	private boolean showListOnFocusGain = false;

	private boolean showCompleteListOnFocusGain = false;

	private int throttleDelay = 300;

	private String parameterName = "q";

	/**
	 * Indicates whether the first item in the list is automatically selected when the autocomplete
	 * list is shown.
	 * 
	 * @return true if the first item of the autocomplete list should be preselected, false
	 *         (default) otherwise
	 */
	public boolean getPreselect()
	{
		return preselect;
	}

	/**
	 * Sets whether the first item in the autocomplete list should be selected when the autocomplete
	 * list is shown.
	 * 
	 * @param preselect
	 *            the flag
	 * @return this {@link AutoCompleteSettings}
	 */
	public AutoCompleteSettings setPreselect(final boolean preselect)
	{
		this.preselect = preselect;
		return this;
	}

	/**
	 * set the throttle delay how long the browser will wait before sending a request to the browser
	 * after the user released a key.
	 * 
	 * @param throttleDelay
	 *            The delay in milliseconds.
	 * @return this {@link AutoCompleteSettings}
	 */
	public AutoCompleteSettings setThrottleDelay(final int throttleDelay)
	{
		this.throttleDelay = throttleDelay;
		return this;
	}

	/**
	 * get the throttle delay how long the browser will wait before sending a request to the browser
	 * after the user released a key.
	 * 
	 * @return the throttle delay in milliseconds (default 300)
	 */
	public int getThrottleDelay()
	{
		return throttleDelay;
	}

	/**
	 * Gets the maximum height of the autocomplete list in pixels. <code>-1</code> indicates that
	 * the autocomplete list should have no maximum height.
	 * 
	 * @return the maximum height in pixels
	 */
	public int getMaxHeightInPx()
	{
		return maxHeightInPx;
	}

	/**
	 * Sets the maximum height in pixels of the autocomplete list.
	 * <p>
	 * The maximum height can also be specified via css (and by setting maxHeightInPx to -1):
	 * 
	 * <pre>
	 * div.wicket-aa-container { maxHeight: 100px; }
	 * </pre>
	 * 
	 * Note that this does not work in IE6.
	 * </p>
	 * 
	 * @param maxHeightInPx
	 *            the maximum height in pixels
	 * @return this {@link AutoCompleteSettings}
	 */
	public AutoCompleteSettings setMaxHeightInPx(final int maxHeightInPx)
	{
		this.maxHeightInPx = maxHeightInPx;
		return this;
	}

	/**
	 * Indicates whether the popup positioning will take into account the borders of the input
	 * element and its ancestors.
	 * 
	 * @return true if borders are ignored, false otherwise.
	 */
	public boolean getIgnoreBordersWhenPositioning()
	{
		return ignoreBordersWhenPositioning;
	}

	/**
	 * Sets whether the popup positioning will take into account the borders of the input element
	 * and its ancestors (by including the <code>clientLeft</code> and <code>clientTop</code> DOM
	 * properties in the computation).
	 * 
	 * @param ignoreBordersWhenPositioning
	 *            the flag
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setIgnoreBordersWhenPositioning(
		final boolean ignoreBordersWhenPositioning)
	{
		this.ignoreBordersWhenPositioning = ignoreBordersWhenPositioning;
		return this;
	}

	/**
	 * Indicates whether the popup positioning will take into account browser window visible area or
	 * not. (so always show popup bottom-right or not)
	 * 
	 * @return true if popup smart positioning is used, false otherwise.
	 */
	public boolean getUseSmartPositioning()
	{
		return useSmartPositioning;
	}

	/**
	 * Indicates whether the autocomplete list will be shown if the input is empty.
	 * 
	 * @return true if the autocomlete list will be shown if the input string is empty, false
	 *         otherwise
	 */
	public boolean getShowListOnEmptyInput()
	{
		return showListOnEmptyInput;
	}

	/**
	 * Sets whether the list should be shown when the input is empty.
	 * 
	 * @param showListOnEmptyInput
	 *            the flag
	 * @return this {@link AutoCompleteSettings}
	 */
	public AutoCompleteSettings setShowListOnEmptyInput(final boolean showListOnEmptyInput)
	{
		this.showListOnEmptyInput = showListOnEmptyInput;
		return this;
	}

	/**
	 * Get CSS class name to add to the autocompleter markup container
	 * 
	 * @return CSS class name, or <code>null</code> if not used
	 */
	public String getCssClassName()
	{
		return cssClassName;
	}

	/**
	 * Sets an CSS class name to add to the autocompleter markup container
	 * <p/>
	 * This makes it easier to have multiple autocompleters in your application with different style
	 * and layout.
	 * 
	 * @param cssClassName
	 *            valid CSS class name
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setCssClassName(final String cssClassName)
	{
		this.cssClassName = cssClassName;
		return this;
	}

	/**
	 * Tells if wicket should adjust the width of the autocompleter selection window to the width of
	 * the related input field.
	 * 
	 * @return <code>true</code> if the autocompleter should have the same size as the input field,
	 *         <code>false</code> for default browser behavior
	 */
	public boolean isAdjustInputWidth()
	{
		return adjustInputWidth;
	}

	/**
	 * Adjust the width of the autocompleter selection window to the width of the related input
	 * field.
	 * <p/>
	 * Otherwise the size will depend on the default browser behavior and CSS.
	 * 
	 * @param adjustInputWidth
	 *            <code>true</code> if the autocompleter should have the same size as the input
	 *            field, <code>false</code> for default browser behavior
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setAdjustInputWidth(final boolean adjustInputWidth)
	{
		this.adjustInputWidth = adjustInputWidth;
		return this;
	}

	/**
	 * Indicates whether the autocomplete list will be shown when the input field receives focus.
	 * 
	 * @return true if the autocomplete list will be shown when the input field receives focus,
	 *         false otherwise
	 */
	public boolean getShowListOnFocusGain()
	{
		return showListOnFocusGain;
	}

	/**
	 * Sets whether the list should be shown when the input field receives focus.
	 * 
	 * @param showCompleteListOnFocusGain
	 *            the flag
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setShowCompleteListOnFocusGain(
		final boolean showCompleteListOnFocusGain)
	{
		this.showCompleteListOnFocusGain = showCompleteListOnFocusGain;
		return this;
	}

	/**
	 * Indicates whether the autocomplete list will be shown when the input field receives focus.
	 * 
	 * @return true if the autocomplete list will be shown when the input field receives focus,
	 *         false otherwise
	 */
	public boolean getShowCompleteListOnFocusGain()
	{
		return showCompleteListOnFocusGain;
	}

	/**
	 * Sets whether the list should be shown when the input field receives focus.
	 * 
	 * @param showListOnFocusGain
	 *            the flag
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setShowListOnFocusGain(final boolean showListOnFocusGain)
	{
		this.showListOnFocusGain = showListOnFocusGain;
		return this;
	}

	/**
	 * Sets whether the popup positioning will take into account browser window visible area or not.
	 * (so always show popup bottom-right or not)<br>
	 * THIS WILL PRODUCE UNWANTED BEHAVIOR WITH IE versions < 8 (probably because of unreliable
	 * clientWidth/clientHeight browser element properties).
	 * 
	 * @param useSmartPositioning
	 *            the flag
	 * @return this {@link AutoCompleteSettings}.
	 */
	public AutoCompleteSettings setUseSmartPositioning(final boolean useSmartPositioning)
	{
		this.useSmartPositioning = useSmartPositioning;
		return this;
	}

	/**
	 * Sets the name of the request parameter that will bring the value of the user input
	 * 
	 * @param parameterName
	 *            the name of the request parameter that will bring the value of the user input
	 * @return this {@link AutoCompleteSettings}
	 */
	public AutoCompleteSettings setParameterName(final String parameterName)
	{
		this.parameterName = parameterName;
		return this;
	}

	/**
	 * @return the name of the request parameter that will bring the value of the user input
	 */
	public String getParameterName()
	{
		return parameterName;
	}
}
