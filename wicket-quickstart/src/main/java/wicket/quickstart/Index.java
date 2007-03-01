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
package wicket.quickstart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import wicket.PageParameters;
import wicket.extensions.markup.html.form.MultiFileUpload;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.upload.FileUpload;
import wicket.model.CompoundPropertyModel;

/**
 * Basic bookmarkable index page.
 * 
 * NOTE: You can get session properties from QuickStartSession via
 * getQuickStartSession()
 */
public class Index extends QuickStartPage
{
	Collection uploads = new ArrayList();


	public Collection getUploads()
	{
		return uploads;
	}


	public void setUploads(Collection uploads)
	{
		this.uploads = uploads;
	}


	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Index(final PageParameters parameters)
	{
		Form form = new Form("form", new CompoundPropertyModel(this))
		{
			protected void onSubmit()
			{
				System.out.println("UPLOADED: " + uploads.size() + " FILES");
				Iterator it = uploads.iterator();
				while (it.hasNext())
				{
					FileUpload upload = (FileUpload)it.next();
					System.out.println("UPLOAD: " + upload.getClientFileName() + " SIZE: "
							+ upload.getSize());
				}

			}
		};
		add(form);

		form.add(new MultiFileUpload("uploads", 2));
	}
}
