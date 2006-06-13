/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.wizard;

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
public class StaticContentStep extends WizardStep<String>
{
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
	 * Sets the content model.
	 * 
	 * @param content
	 *            The content model
	 */
	public final void setContentModel(IModel<String> content)
	{
		this.content = content;
	}

	/**
	 * @see wicket.extensions.wizard.WizardStep#populate(wicket.markup.html.panel.Panel)
	 */
	@Override
	protected void populate(Panel contentPanel)
	{
		new Label(contentPanel, "content", content).setEscapeModelStrings(!allowHtml);
	}
}
