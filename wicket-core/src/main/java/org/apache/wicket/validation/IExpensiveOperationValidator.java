package org.apache.wicket.validation;


/**
 * Marker interface for validators that will perform expensive operations (database call for example).
 * With implementing this interface validator won't be called via
 * {@link  org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onEvent(org.apache.wicket.Component, org.apache.wicket.event.IEvent)}.
 * 
 * @author Artur Michalowski
 * @param <T>
 *            type of validatable
 * @see IValidator
 * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior
 */

public interface IExpensiveOperationValidator<T> extends IValidator<T> {

}
