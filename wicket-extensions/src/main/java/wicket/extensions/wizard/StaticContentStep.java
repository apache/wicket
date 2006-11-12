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
package wicket.extensions.wizard;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A wizard step that displays the provided static content without expecting any
 * input.
 * 
 * @author eelcohillenius
 */
public class StaticContentStep extends WizardStep
{
	/**
	 * The content.
	 */
	private final class Content extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param parent
		 * @param id
		 */
		public Content(MarkupContainer parent, String id)
		{
			super(parent, id);
			new Label(this, "content", content).setEscapeModelStrings(!allowHtml);
		}

	}

	private static final long serialVersionUID = 1L;

	/**
	 * Whether HTML codes should be rendered as is (true), or should be escaped
	 * (false).
	 */
	private final boolean allowHtml;

	/** The model that provided the actual content. */
	private IModel<String> content;

	/**
	 * Constructor for if you want to set all the properties yourself.
	 * 
	 * @param allowHtml
	 *            If true, any html of the content will be rendered as is.
	 *            Otherwise, it will be escaped.
	 */
	public StaticContentStep(boolean allowHtml)
	{
		this.allowHtml = allowHtml;
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
	 *            If true, any html of the content will be rendered as is.
	 *            Otherwise, it will be escaped.
	 */
	public StaticContentStep(IModel<String> title, IModel<String> summary, IModel<String> content,
			boolean allowHtml)
	{
		super(title, summary);
		this.content = content;
		this.allowHtml = allowHtml;
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
	 *            If true, any html of the content will be rendered as is.
	 *            Otherwise, it will be escaped.
	 */
	public StaticContentStep(IModel<String> title, IModel<String> summary, String content,
			boolean allowHtml)
	{
		this(title, summary, new Model<String>(content), allowHtml);
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
	 *            If true, any html of the content will be rendered as is.
	 *            Otherwise, it will be escaped.
	 */
	public StaticContentStep(String title, String summary, IModel<String> content, boolean allowHtml)
	{
		this(new Model<String>(title), new Model<String>(summary), content, allowHtml);
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
	 *            If true, any html of the content will be rendered as is.
	 *            Otherwise, it will be escaped.
	 */
	public StaticContentStep(String title, String summary, String content, boolean allowHtml)
	{
		this(title, summary, new Model<String>(content), allowHtml);
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
	public final IModel<String> getContentModel()
	{
		return content;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#getView(wicket.MarkupContainer,
	 *      java.lang.String, wicket.extensions.wizard.IWizard)
	 */
	public Component getView(MarkupContainer parent, String id, IWizard wizard)
	{
		return new Content(parent, id);
	}

	/**
	 * Sets the content model.
	 * 
	 * @param content
	 *            The content model
	 */
	public final void setContentModel(IModel<String> content)
	{
		this.content = content;
	}
}
