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
package wicket.markup.html.form;

/**
 * Interface that must be implemented by components that are able to submit
 * form.
 * 
 * @author Matej Knopp
 */
public interface IFormSubmittingComponent
{
	/**
	 * Returns whether form should be processed the default way. When false
	 * (default is true), all validation and formupdating is bypassed and the
	 * onSubmit method of that button is called directly, and the onSubmit
	 * method of the parent form is not called. A common use for this is to
	 * create a cancel button.
	 * 
	 * @return defaultFormProcessing
	 */
	boolean getDefaultFormProcessing();

	/**
	 * Returns the form this component submits.
	 * 
	 * @return form submitted by this component
	 */
	Form getForm();

	/**
	 * Returns the name that is unique to this component, at least within the
	 * form.
	 * 
	 * @return component name
	 */
	String getInputName();

	/**
	 * Override this method to provide special submit handling in a multi-button
	 * form. It is called whenever the user clicks this particular button.
	 */
	void onSubmit();
}
