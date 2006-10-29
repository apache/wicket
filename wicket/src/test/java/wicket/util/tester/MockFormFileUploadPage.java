/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket.util.tester;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.upload.FileUpload;
import wicket.markup.html.form.upload.FileUploadField;
import wicket.model.CompoundPropertyModel;
import wicket.util.lang.Bytes;

/**
 * Mock form for use when testing FormTester's addFile functionality.
 * 
 * @author frankbille
 */
public class MockFormFileUploadPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Model object used in this test.
	 * 
	 * @author frankbille
	 */
	public static class MockDomainObjectFileUpload
	{
		private String text;

		/**
		 * @return text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * @param text
		 */
		public void setText(String text)
		{
			this.text = text;
		}
	}

	private MockDomainObjectFileUpload domainObject;

	private FileUploadField fileUploadField;

	private FileUpload fileUpload;

	/**
	 * Construct.
	 */
	public MockFormFileUploadPage()
	{
		domainObject = new MockDomainObjectFileUpload();
		Form<MockDomainObjectFileUpload> form = new Form<MockDomainObjectFileUpload>(this, "form",
				new CompoundPropertyModel<MockDomainObjectFileUpload>(domainObject))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				fileUpload = fileUploadField.getFileUpload();
			}
		};
		form.setMultiPart(true);
		form.setMaxSize(Bytes.kilobytes(100));
		new TextField<String>(form, "text");
		fileUploadField = new FileUploadField(form, "file");
	}

	/**
	 * @return domainObject
	 */
	public MockDomainObjectFileUpload getDomainObject()
	{
		return domainObject;
	}

	/**
	 * @return fileUpload
	 */
	public FileUpload getFileUpload()
	{
		return fileUpload;
	}

}
