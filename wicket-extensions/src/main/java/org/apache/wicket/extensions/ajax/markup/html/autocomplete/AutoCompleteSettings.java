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

import org.apache.wicket.IClusterable;

/**
 * This class encapsulates various settings for {@link AbstractAutoCompleteBehavior}. See the
 * documentation for the property accessors of this class for further information.
 * 
 * @author Gerolf Seitz
 */
public final class AutoCompleteSettings implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private boolean preselect = false;

	private int maxHeightInPx = -1;

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
	public AutoCompleteSettings setPreselect(boolean preselect)
	{
		this.preselect = preselect;
		return this;
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
	public AutoCompleteSettings setMaxHeightInPx(int maxHeightInPx)
	{
		this.maxHeightInPx = maxHeightInPx;
		return this;
	}
}