/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html.form.upload;

import org.apache.commons.fileupload.FileItem;

import wicket.model.DetachableModel;

/**
 * <p>
 * Special purpose model for use with {@link wicket.markup.html.form.upload.UploadTextField}.
 * </p>
 * <p>
 * WARNING: this model is defensive in that on detach, the references to name
 * and file are set to null. That way, we avoid holding the file resource for
 * longer than actually needed.
 * </p>
 * @author Eelco Hillenius
 */
public class UploadModel extends DetachableModel
{
	/**
	 * The name of the file from user input.
	 */
	private String name;

	/**
	 * The uploaded file.
	 */
	private FileItem file;

	/**
	 * Construct.
	 */
	public UploadModel()
	{
	}

	/**
	 * Gets the name of the file that was provided by user input.
	 * @return the name of the file
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the file that was provided by user input.
	 * @param fileName the name of the file
	 */
	public void setName(String fileName)
	{
		this.name = fileName;
	}

	/**
	 * Gets the uploaded file.
	 * @return the uploaded file
	 */
	public FileItem getFile()
	{
		return file;
	}

	/**
	 * Sets the uploaded file
	 * @param file the uploaded file
	 */
	public void setFile(FileItem file)
	{
		this.file = file;
	}

	/**
	 * @see wicket.model.DetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	}

	/**
	 * @see wicket.model.DetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		this.file = null;
		this.name = null;
	}
}
