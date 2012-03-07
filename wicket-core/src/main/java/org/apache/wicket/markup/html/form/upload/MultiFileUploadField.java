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
package org.apache.wicket.markup.html.form.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.upload.FileItem;


/**
 * Form component that allows the user to select multiple files to upload via a single &lt;input
 * type=&quot;file&quot;/&gt; field.
 * 
 * Notice that this component clears its model at the end of the request, so the uploaded files MUST
 * be processed within the request they were uploaded.
 * 
 * Uses javascript implementation from
 * http://the-stickman.com/web-development/javascript/upload-multiple
 * -files-with-a-single-file-element/
 * 
 * For customizing caption text see {@link #RESOURCE_LIMITED} and {@link #RESOURCE_UNLIMITED}
 * 
 * For an example of styling using CSS see the upload example in wicket-examples
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MultiFileUploadField extends FormComponentPanel<Collection<FileUpload>>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Represents an unlimited max count of uploads
	 */
	public static final int UNLIMITED = -1;

	/**
	 * Resource key used to retrieve caption message for when a limit on the number of uploads is
	 * specified. The limit is represented via ${max} variable.
	 * 
	 * Example: org.apache.wicket.mfu.caption.limited=Files (maximum ${max}):
	 */
	public static final String RESOURCE_LIMITED = "org.apache.wicket.mfu.caption.limited";

	/**
	 * Resource key used to retrieve caption message for when no limit on the number of uploads is
	 * specified.
	 * 
	 * Example: org.apache.wicket.mfu.caption.unlimited=Files:
	 */
	public static final String RESOURCE_UNLIMITED = "org.apache.wicket.mfu.caption.unlimited";


	private static final String NAME_ATTR = "name";

	private static final String MAGIC_SEPARATOR = "_mf_";


	private static final ResourceReference JS = new JavaScriptResourceReference(
		MultiFileUploadField.class, "MultiFileUploadField.js");


	private final WebComponent upload;
	private final WebMarkupContainer container;

	private final int max;

	private transient String[] inputArrayCache = null;

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public MultiFileUploadField(String id)
	{
		this(id, null, UNLIMITED);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param max
	 *            max number of files a user can upload
	 */
	public MultiFileUploadField(String id, int max)
	{
		this(id, null, max);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public MultiFileUploadField(String id, IModel<? extends Collection<FileUpload>> model)
	{
		this(id, model, UNLIMITED);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @param max
	 *            max number of files a user can upload
	 * 
	 */
	@SuppressWarnings("unchecked")
	public MultiFileUploadField(String id, IModel<? extends Collection<FileUpload>> model, int max)
	{
		super(id, (IModel<Collection<FileUpload>>)model);

		this.max = max;

		upload = new WebComponent("upload");
		upload.setOutputMarkupId(true);
		add(upload);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);

		container.add(new Label("caption", new CaptionModel()));
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponentPanel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// remove the name attribute added by the FormComponent
		if (tag.getAttributes().containsKey(NAME_ATTR))
		{
			tag.getAttributes().remove(NAME_ATTR);
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		Form<?> form = findParent(Form.class);
		if (form == null)
		{
			// woops
			throw new IllegalStateException("Component " + getClass().getName() + " must have a " +
				Form.class.getName() + " component above in the hierarchy");
		}
	}

	@Override
	public boolean isMultiPart()
	{
		return true;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		// initialize the javascript library
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(OnDomReadyHeaderItem.forScript("new MultiSelector('" + getInputName() +
			"', document.getElementById('" + container.getMarkupId() + "'), " + max + ",'" +
			getString("org.apache.wicket.mfu.delete") + "').addElement(document.getElementById('" +
			upload.getMarkupId() + "'));"));
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInputAsArray()
	 */
	@Override
	public String[] getInputAsArray()
	{
		// fake the input array as if it contained an array of all uploaded file
		// names

		if (inputArrayCache == null)
		{
			// this array will aggregate all input names
			ArrayList<String> names = null;

			final Request request = getRequest();
			if (request instanceof IMultipartWebRequest)
			{
				// retrieve the filename->FileItem map from request
				final Map<String, List<FileItem>> itemNameToItem = ((IMultipartWebRequest)request).getFiles();
				for (Entry<String, List<FileItem>> entry : itemNameToItem.entrySet())
				{
					// iterate over the map and build the list of filenames

					final String name = entry.getKey();
					final List<FileItem> fileItems = entry.getValue();

					if (!Strings.isEmpty(name) &&
						name.startsWith(getInputName() + MAGIC_SEPARATOR) && !fileItems.isEmpty() &&
						!Strings.isEmpty(fileItems.get(0).getName()))
					{

						// make sure the fileitem belongs to this component and
						// is not empty

						names = (names != null) ? names : new ArrayList<String>();
						names.add(name);
					}
				}
			}

			if (names != null)
			{
				inputArrayCache = names.toArray(new String[names.size()]);
			}
		}
		return inputArrayCache;

	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(java.lang.String[])
	 */
	@Override
	protected Collection<FileUpload> convertValue(String[] value) throws ConversionException
	{
		// convert the array of filenames into a collection of FileItems

		Collection<FileUpload> uploads = null;

		final String[] filenames = getInputAsArray();

		if (filenames != null)
		{
			final IMultipartWebRequest request = (IMultipartWebRequest)getRequest();

			uploads = new ArrayList<FileUpload>(filenames.length);

			for (String filename : filenames)
			{
				List<FileItem> fileItems = request.getFile(filename);
				for (FileItem fileItem : fileItems)
				{
					uploads.add(new FileUpload(fileItem));
				}
			}
		}

		return uploads;

	}

	/**
	 * See {@link FormComponent#updateCollectionModel(FormComponent)} for details on how the model
	 * is updated.
	 */
	@Override
	public void updateModel()
	{
		FormComponent.updateCollectionModel(this);
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		// cleanup any opened filestreams
		Collection<FileUpload> uploads = getConvertedInput();
		if (uploads != null)
		{
			for (FileUpload upload : uploads)
			{
				upload.closeStreams();
			}
		}

		// cleanup any caches
		inputArrayCache = null;

		// clean up the model because we don't want FileUpload objects in session
		Collection<FileUpload> modelObject = getModelObject();
		if (modelObject != null)
		{
			modelObject.clear();
		}

		super.onDetach();
	}

	/**
	 * Model that will construct the caption string
	 * 
	 * @author ivaynberg
	 */
	private class CaptionModel extends AbstractReadOnlyModel<String>
	{

		private static final long serialVersionUID = 1L;

		/**
		 * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
		 */
		@Override
		public String getObject()
		{
			if (max == UNLIMITED)
			{
				return getString(RESOURCE_UNLIMITED);
			}
			else
			{
				HashMap<String, Object> vars = new HashMap<String, Object>(1);
				vars.put("max", max);
				return getString(RESOURCE_LIMITED, new Model<HashMap<String, Object>>(vars));
			}
		}

	}
}
