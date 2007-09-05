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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Request;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.upload.FileItem;


/**
 * Form component that allows the user to select multiple files to upload via a
 * single &lt;input type=&quot;file&quot;/&gt; field.
 * 
 * Notice that this component clears its model at the end of the request, so the
 * uploaded files MUST be processed within the request they were uploaded.
 * 
 * Uses javascript implementation from
 * http://the-stickman.com/web-development/javascript/upload-multiple-files-with-a-single-file-element/
 * 
 * For customizing caption text see {@link #RESOURCE_LIMITED} and
 * {@link #RESOURCE_UNLIMITED}
 * 
 * For an example of styling using CSS see the upload example in wicket-examples
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class MultiFileUploadField extends FormComponentPanel implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	/**
	 * Represents an unlimited max count of uploads
	 */
	public static final int UNLIMITED = 1;

	/**
	 * Resource key used to retrieve caption message for when a limit on the
	 * number of uploads is specfied. The limit is represented via ${max}
	 * variable.
	 * 
	 * Example: org.apache.wicket.mfu.caption.limited=Files (maximum ${max}):
	 */
	public static final String RESOURCE_LIMITED = "org.apache.wicket.mfu.caption.limited";

	/**
	 * Resource key used to retrieve caption message for when no limit on the
	 * number of uploads is specfied.
	 * 
	 * Example: org.apache.wicket.mfu.caption.unlimited=Files:
	 */
	public static final String RESOURCE_UNLIMITED = "org.apache.wicket.mfu.caption.unlimited";


	private static final String NAME_ATTR = "name";

	private static final String MAGIC_SEPARATOR = "_mf_";


	private static final ResourceReference JS = new JavascriptResourceReference(
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
		this(id, null, -1);
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
	public MultiFileUploadField(String id, IModel model)
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
	public MultiFileUploadField(String id, IModel model, int max)
	{
		super(id, model);

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
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		// auto toggle form's multipart property
		Form form = (Form)findParent(Form.class);
		if (form == null)
		{
			// woops
			throw new IllegalStateException("Component " + getClass().getName() + " must have a " +
					Form.class.getName() + " component above in the hierarchy");
		}
		form.setMultiPart(true);
	}


	/**
	 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		// initialize the javascript library
		response.renderJavascriptReference(JS);
		response.renderOnDomReadyJavascript("new MultiSelector('" + getInputName() +
				"', document.getElementById('" + container.getMarkupId() + "'), " + max +
				").addElement(document.getElementById('" + upload.getMarkupId() + "'));");
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInputAsArray()
	 */
	public String[] getInputAsArray()
	{
		// fake the input array as if it contained an array of all uploaded file
		// names

		if (inputArrayCache == null)
		{
			// this array will aggregate all input names
			ArrayList names = null;

			final Request request = getRequest();
			if (request instanceof IMultipartWebRequest)
			{
				// retrieve the filename->FileItem map from request
				final Map itemNameToItem = ((IMultipartWebRequest)request).getFiles();
				Iterator it = itemNameToItem.entrySet().iterator();
				while (it.hasNext())
				{
					// iterate over the map and build the list of filenames

					final Entry entry = (Entry)it.next();
					final String name = (String)entry.getKey();
					final FileItem item = (FileItem)entry.getValue();

					if (!Strings.isEmpty(name) &&
							name.startsWith(getInputName() + MAGIC_SEPARATOR) &&
							!Strings.isEmpty(item.getName()))
					{

						// make sure the fileitem belongs to this component and
						// is not empty

						names = (names != null) ? names : new ArrayList();
						names.add(name);
					}
				}
			}

			if (names != null)
			{
				inputArrayCache = (String[])names.toArray(new String[names.size()]);
			}
		}
		return inputArrayCache;

	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(java.lang.String[])
	 */
	protected Object convertValue(String[] value) throws ConversionException
	{
		// convert the array of filenames into a collection of FileItems

		Collection uploads = null;

		final String[] filenames = getInputAsArray();

		if (filenames != null)
		{
			final IMultipartWebRequest request = (IMultipartWebRequest)getRequest();

			uploads = new ArrayList(filenames.length);

			for (int i = 0; i < filenames.length; i++)
			{
				uploads.add(new FileUpload(request.getFile(filenames[i])));
			}
		}

		return uploads;

	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
		final Object object = getModelObject();

		// figure out if there is an existing model object collection for us to
		// reuse
		if (object == null)
		{
			// no existing collection, push the one we created
			setModelObject(getConvertedInput());
		}
		else
		{
			if (!(object instanceof Collection))
			{
				// fail early if there is something interesting in the model
				throw new IllegalStateException("Model object of " + getClass().getName() +
						" component must be of type `" + Collection.class.getName() + "<" +
						FileUpload.class.getName() + ">` but is of type `" +
						object.getClass().getName() + "`");
			}
			else
			{
				// refresh the existing collection
				Collection collection = (Collection)object;
				collection.clear();
				if (getConvertedInput() != null)
				{
					collection.addAll((Collection)getConvertedInput());
				}

				// push the collection in case the model is listening to
				// setobject calls
				setModelObject(collection);
			}
		}
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		// cleanup any opened filestreams
		Collection uploads = (Collection)getConvertedInput();
		if (uploads != null)
		{
			Iterator it = uploads.iterator();
			while (it.hasNext())
			{
				final FileUpload upload = (FileUpload)it.next();
				upload.closeStreams();
			}
		}

		// cleanup any caches
		inputArrayCache = null;

		// clean up the model because we dont want FileUpload objects in session
		Object modelObject = getModelObject();
		if (modelObject != null && (modelObject instanceof Collection))
		{
			((Collection)modelObject).clear();
		}

		super.onDetach();
	}

	/**
	 * Model that will construct the caption string
	 * 
	 * @author ivaynberg
	 */
	private class CaptionModel extends AbstractReadOnlyModel
	{

		private static final long serialVersionUID = 1L;

		/**
		 * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
		 */
		public Object getObject()
		{
			if (max == UNLIMITED)
			{
				return getString(RESOURCE_UNLIMITED);
			}
			else
			{
				return getString(RESOURCE_LIMITED, Model.valueOf(Collections.singletonMap("max",
						new Integer(max))));
			}
		}

	}
}
