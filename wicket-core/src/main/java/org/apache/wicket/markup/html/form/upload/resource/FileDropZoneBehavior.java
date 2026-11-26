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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import com.github.openjson.JSONObject;

/**
 * Makes a component a zone files are dropped on to choose them for the next upload of a
 * {@link FileUploadToResourceField}, as if they were chosen in the browser's dialog of its input.
 * The input receives the dropped files it accepts by its {@code accept} attribute: all of them if
 * it is {@code multiple}, else the first. Folders are left out. The input then fires its
 * {@code input} and {@code change} events, as it does for files chosen in the dialog.
 * <p>
 * With a {@link FileListBehavior}, dropped files are added to those chosen before. While files are
 * dragged over the component, it has the CSS class {@value #DRAG_OVER_CSS_CLASS}.
 *
 * <pre>
 * FileUploadToResourceField field = ...;
 * add(new WebMarkupContainer("dropZone").add(new FileDropZoneBehavior(field)));
 * </pre>
 *
 * @since 11.0.0
 */
public class FileDropZoneBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * The CSS class of the component while files are dragged over it.
	 */
	public static final String DRAG_OVER_CSS_CLASS = "wicket-upload-dragover";

	private final FileUploadToResourceField field;

	/**
	 * @param field
	 *            the field the dropped files are chosen for
	 */
	public FileDropZoneBehavior(FileUploadToResourceField field)
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
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(FileUploadToResourceField.JS));
		response.render(OnDomReadyHeaderItem.forScript(
			"Wicket.FileUploadToResourceField.addDropZone(" +
				JSONObject.quote(component.getMarkupId()) + ", " +
				JSONObject.quote(field.getMarkupId()) + ", " +
				JSONObject.quote(DRAG_OVER_CSS_CLASS) + ");"));
	}
}
