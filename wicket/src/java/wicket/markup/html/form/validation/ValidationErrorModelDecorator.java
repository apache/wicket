package wicket.markup.html.form.validation;

import java.io.Serializable;

import wicket.Component;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Decorates a model, using the given input (that was the cause of a validation error) as
 * the model object instead of the 'real' model object. NOT INTENDED TO BE USED BY
 * FRAMEWORK CLIENTS.
 * <p>
 * Method getObject() will return the input, setObject(Object) will do nothing at all.
 * </p>
 * IMPORTANT NOTE: AFTER THE PAGE RENDERING IS DONE, THE ORIGINAL MODEL MUST BE SET BACK
 * ON THE COMPONENT. THIS DECORATOR IS HERE FOR THE SOLE PURPOSE OF BEING ABLE TO RENDER
 * THE INPUT ON THE COMPONENT, AND TO AVOID POSSIBLE EXCEPTIONS THAT CAN OCCUR WHEN
 * INVALID VALUES ARE SET INTO THE MODEL.
 * </p>
 * @author Eelco Hillenius
 */
public final class ValidationErrorModelDecorator extends Model
{
	/** the decorated model. */
	private IModel originalModel;

	/** the reporting component. */
	private Component reporter;

	/**
	 * Construct.
	 * @param reporter the reporting component
	 * @param input the input that caused to validation error
	 */
	public ValidationErrorModelDecorator(Component reporter, Serializable input)
	{
		super(input);
		this.originalModel = reporter.getModel();
		this.reporter = reporter;
	}

	/**
	 * Will do nothing.
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		// ignore. This method *will* be called by the framework, but as we
		// just want to return the input that caused the validation error,
		// it's best to ignore it alltogether
	}

	/**
	 * Gets the decorated model.
	 * @return the decorated model.
	 */
	public IModel getOriginalModel()
	{
		return originalModel;
	}

	/**
	 * Gets the reporting component.
	 * @return the reporting component
	 */
	public Component getReporter()
	{
		return reporter;
	}
}