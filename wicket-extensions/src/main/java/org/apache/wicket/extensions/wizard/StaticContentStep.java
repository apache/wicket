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
package org.apache.wicket.extensions.wizard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A wizard step that displays the provided static content without expecting any input.
 * 
 * @author eelcohillenius
 */
public class StaticContentStep extends WizardStep
{
	private static final long serialVersionUID = 1L;

	/**
	 * Whether HTML codes should be rendered as is (true), or should be escaped (false).
	 */
	private final boolean allowHtml;

	/** The model that provided the actual content. */
	private IModel<?> content;

	/**
	 * Constructor for if you want to set all the properties yourself.
	 * 
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is. Otherwise, it will be
	 *            escaped.
	 */
	public StaticContentStep(final boolean allowHtml)
	{
		this.allowHtml = allowHtml;
		add(new Label("content", ""));
	}

	/**
	 * Construct.
	 * 
	 * @param title
	 *            The title of this step
	 * @param summary
	 *            The summary of this step
	 * @param content
	 *            The content of the step panel
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is. Otherwise, it will be
	 *            escaped.
	 */
	public StaticContentStep(final IModel<String> title, final IModel<String> summary,
		final IModel<?> content, final boolean allowHtml)
	{
		super(title, summary);
		this.content = content;
		this.allowHtml = allowHtml;
		add(new Label("content", content).setEscapeModelStrings(!allowHtml));
	}

	/**
	 * Construct.
	 * 
	 * @param title
	 *            The title of this step
	 * @param summary
	 *            The summary of this step
	 * @param content
	 *            The content of the step panel
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is. Otherwise, it will be
	 *            escaped.
	 */
	public StaticContentStep(final IModel<String> title, final IModel<String> summary,
		final String content, final boolean allowHtml)
	{
		this(title, summary, new Model<>(content), allowHtml);
	}

	/**
	 * Construct.
	 * 
	 * @param title
	 *            The title of this step
	 * @param summary
	 *            The summary of this step
	 * @param content
	 *            The content of the step panel
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is. Otherwise, it will be
	 *            escaped.
	 */
	public StaticContentStep(final String title, final String summary, final IModel<?> content,
		final boolean allowHtml)
	{
		this(new Model<String>(title), new Model<>(summary), content, allowHtml);
	}

	/**
	 * Construct.
	 * 
	 * @param title
	 *            The title of this step
	 * @param summary
	 *            The summary of this step
	 * @param content
	 *            The content of the step panel
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is. Otherwise, it will be
	 *            escaped.
	 */
	public StaticContentStep(final String title, final String summary, final String content,
		final boolean allowHtml)
	{
		this(title, summary, new Model<>(content), allowHtml);
	}

	/**
	 * Gets whether html is allowed as output.
	 * 
	 * @return Whether html is allowed as output
	 */
	public final boolean getAllowHtml()
	{
		return allowHtml;
	}

	/**
	 * Gets the content from the content model.
	 * 
	 * @return The content
	 */
	public final String getContent()
	{
		return (content != null) ? (String)content.getObject() : null;
	}

	/**
	 * Gets the content model.
	 * 
	 * @return The content model
	 */
	public final IModel<?> getContentModel()
	{
		return content;
	}

	/**
	 * Sets the content model.
	 * 
	 * @param <T>
	 *            The model object type
	 * 
	 * @param content
	 *            The content model
	 */
	public final <T> void setContentModel(final IModel<T> content)
	{
		this.content = content;
		replace(new Label("content", content).setEscapeModelStrings(!allowHtml));
	}
}
