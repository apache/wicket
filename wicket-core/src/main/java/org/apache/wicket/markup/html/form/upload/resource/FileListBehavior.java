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
package org.apache.wicket.markup.html.form.upload.resource;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import com.github.openjson.JSONObject;

/**
 * Lists the files chosen for the next upload of a {@link FileUploadToResourceField}, each with a
 * button removing it. It is attached to a {@code <ul>} or an {@code <ol>}, whose items it replaces
 * in the browser with one for each file:
 *
 * <pre>
 * &lt;li class="wicket-upload-file"&gt;
 *   &lt;span class="wicket-upload-file-name"&gt;report.pdf&lt;/span&gt;
 *   &lt;button type="button" class="wicket-upload-file-remove"&gt;Remove&lt;/button&gt;
 * &lt;/li&gt;
 * </pre>
 *
 * The name of a file is written as text, never as markup. The text of the buttons is the resource
 * {@value #REMOVE_RESOURCE_KEY} of the field.
 * <p>
 * With a list, the field collects the files of several choices: if its input is
 * {@code multiple}, files chosen in the browser's dialog or dropped on a
 * {@link FileDropZoneBehavior drop zone} are added to the listed ones instead of replacing them.
 * Once an upload succeeded, the files it sent leave the list. While an upload runs, no file can be
 * removed.
 *
 * <pre>
 * FileUploadToResourceField field = ...;
 * add(new WebMarkupContainer("files").add(new FileListBehavior(field)));
 * </pre>
 *
 * @since 11.0.0
 */
public class FileListBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * The resource key of the text of the buttons removing a file.
	 */
	public static final String REMOVE_RESOURCE_KEY = "uploadToResourceRemove";

	private final FileUploadToResourceField field;

	/**
	 * @param field
	 *            the field whose files are listed
	 */
	public FileListBehavior(FileUploadToResourceField field)
	{
		this.field = Args.notNull(field, "field");
	}

	@Override
	public void bind(Component component)
	{
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		if (!tag.getName().equalsIgnoreCase("ul") && !tag.getName().equalsIgnoreCase("ol"))
		{
			throw new MarkupException("A FileListBehavior can only be attached to <ul> or <ol>, " +
				"not to <" + tag.getName() + "> of " + component);
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(FileUploadToResourceField.JS));
		response.render(OnDomReadyHeaderItem.forScript("Wicket.FileUploadToResourceField.addList(" +
			JSONObject.quote(field.getMarkupId()) + ", " +
			JSONObject.quote(component.getMarkupId()) + ", " +
			JSONObject.quote(field.getString(REMOVE_RESOURCE_KEY)) + ");"));
	}
}
