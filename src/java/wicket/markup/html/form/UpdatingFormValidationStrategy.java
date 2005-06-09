package wicket.markup.html.form;

import wicket.Component;
import wicket.markup.html.form.validation.IFormValidationStrategy;
import wicket.model.IModel;
import wicket.util.lang.Objects;
import wicket.version.undo.Change;

/**
 * The default form validation strategy.
 */
final class UpdatingFormValidationStrategy implements IFormValidationStrategy
{
	/**
	 * Construct.
	 */
	UpdatingFormValidationStrategy()
	{
	}

	/**
	 * Validates all children of this form and the form itself, recording all messages
	 * that are returned by the validators.
	 * @param form the form that the validation is applied to
	 */
	public void validate(final Form form)
	{
		// Visit all the form components and validate each
		form.visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// Validate form component
				formComponent.validate();

				// If component is not valid (has an error)
				if (!formComponent.isValid())
				{
					// tell component to deal with invalidity
					formComponent.invalid();
				}
				else
				{
					// tell component that it is valid now
					formComponent.valid();
				}
			}
		});

		// record the current model
		ModelChange record = new ModelChange(form);

		// record the versioned property
		boolean wasVersioned = form.isVersioned();

		// set it to false (that won't have any effect for components that just override the method btw)
		form.setVersioned(false);
		try
		{
			// update
			form.updateFormComponentModels();

			// visit any validators of the form itself
			form.validator.validate(form);
		}
		finally
		{
			// rollback the updated model
			if (form.hasError())
			{
				record.undo();
			}

			// set versioned property to what it was before
			form.setVersioned(wasVersioned);
		}
	}

	class ModelChange extends Change
	{
		/** subject. */
		private final Component component;

		/** original model. */
		private IModel originalModel;

		/**
		 * Construct.
		 * @param component subject of the change
		 */
		ModelChange(final Component component)
		{
			// Save component
			this.component = component;

			// Get component model
			final IModel model = component.getModel();

			// If the component has a model, it's about to change!
			if (model != null)
			{
				model.detach();
				originalModel = (IModel)Objects.clone(model);
			}
		}

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public void undo()
		{
			component.setModel(originalModel);
		}
	}

}