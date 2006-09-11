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
package wicket.markup.html.form.validation;

/**
 * A validator that skips validation if {@link IValidatable#getValue()} returns
 * null
 * 
 * @author ivaynberg
 * @param <T>
 */
public abstract class NullIgnoringValidator<T> implements IValidator<T>
{

	public final void validate(IValidatable<T> validatable)
	{
		if (validatable.getValue() != null)
		{
			onValidate(validatable);
		}
	}

	/**
	 * Validates the validatable. This method is only called if
	 * {@link IValidatable#getValue()} is not null
	 * 
	 * @param validatable
	 */
	protected abstract void onValidate(IValidatable<T> validatable);

}
