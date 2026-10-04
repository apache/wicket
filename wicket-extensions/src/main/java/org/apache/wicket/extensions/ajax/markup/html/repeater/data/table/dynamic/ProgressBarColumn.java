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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import org.apache.wicket.extensions.markup.html.progress.ProgressBar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A column showing a {@link ProgressBar} for a percentage property of the row, rendered in the
 * browser from the bar's {@link ProgressBar#markup(String) markup}.
 * <p>
 * The property has to hold a number from 0 to 100; it is not clamped in the browser. Combined
 * with an {@link UpdateRowMessageType update row} message pushed whenever the value changes, the
 * bar follows the progress without reloading the table.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public class ProgressBarColumn<T> extends AbstractDynamicColumn<T> implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private final String template;

	/**
	 * @param header
	 *            the header text, rendered escaped
	 * @param path
	 *            the path of the percentage in the row's JSON, as the template engine reads it,
	 *            for example {@code progress}; authored by the developer
	 */
	public ProgressBarColumn(IModel<String> header, String path)
	{
		super(header);
		template = ProgressBar.markup("{{" + Args.notEmpty(path, "path") + "}}");
	}

	@Override
	public String getTemplate()
	{
		return template;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(CssHeaderItem.forReference(ProgressBar.CSS));
	}
}
