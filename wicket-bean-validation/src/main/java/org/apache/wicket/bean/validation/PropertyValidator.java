package org.apache.wicket.bean.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

/**
 * Validator that delegates to the bean validation framework. The integration has to be first
 * configured using {@link BeanValidationConfiguration}.
 * 
 * <p>
 * The validator must be provided a {@link Property}, unless one can be resolved from the component
 * implicitly. By default the configuration contains the {@link DefaultPropertyResolver} so
 * {@link PropertyModel}s are supported out of the box - when attached to a component with a
 * property model the property does not need to be specified explicitly.
 * </p>
 * 
 * <p>
 * The validator will set the required flag on the form component it is attached to based on the
 * presence of the @NotNull annotation. Notice, the required flag will only be set to {@code true},
 * components with the required flag already set to {@code true} will not have the flag set to
 * {@code false} by this validator.
 * </p>
 * 
 * <p>
 * The validator will allow {@link ITagModifier}s configured in {@link BeanValidationConfiguration}
 * to mutate the markup tag of the component it is attached to.
 * </p>
 * 
 * <p>
 * The validator specifies default error messages in the {@code PropertyValidator.properties} file.
 * These values can be overridden in the application subclass' property files globally or in the
 * page or panel properties locally. See this file for the default messages supported.
 * </p>
 * 
 * @author igor
 * 
 * @param <T>
 */
public class PropertyValidator<T> extends Behavior implements IValidator<T>
{
	private static final Class<?>[] EMPTY = new Class<?>[0];

	private FormComponent<T> component;

	// the trailing underscore means that these members should not be used
	// directly. ALWAYS use the respective getter instead.
	private Property property_;
	private final IModel<Class<?>[]> groups_;

	/**
	 * A flag that indicates whether {@linkplain #setComponentRequiredFlag()} has been called for
	 * this behavior.
	 */
	private boolean requiredFlagSet;

	public PropertyValidator(Class<?>... groups)
	{
		this(null, groups);
	}

	public PropertyValidator(IModel<Class<?>[]> groups)
	{
		this(null, groups);
	}

	public PropertyValidator(Property property, Class<?>... groups)
	{
		this(property, new GroupsModel(groups));
	}

	public PropertyValidator(Property property, IModel<Class<?>[]> groups)
	{
		this.property_ = property;
		this.groups_ = groups;
	}

	private Property getProperty()
	{
		if (property_ == null)
		{
			property_ = BeanValidationConfiguration.get().resolveProperty(component);
			if (property_ == null)
			{
				throw new IllegalStateException(
					"Could not resolve Property from component: " + component
						+ ". Either specify the Property in the constructor or use a model that works in combination with a "
						+ IPropertyResolver.class.getSimpleName()
						+ " to resolve the Property automatically");
			}
		}
		return property_;
	}

	private Class<?>[] getGroups()
	{
		if (groups_ == null)
		{
			return EMPTY;
		}
		return groups_.getObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bind(Component component)
	{
		if (this.component != null)
		{
			throw new IllegalStateException( //
				"This validator has already been added to component: "
					+ this.component
					+ ". This validator does not support reusing instances, please create a new one");
		}

		if (!(component instanceof FormComponent))
		{
			throw new IllegalStateException(getClass().getSimpleName()
				+ " can only be added to FormComponents");
		}

		// TODO add a validation key that appends the type so we can have
		// different messages for
		// @Size on String vs Collection - done but need to add a key for each
		// superclass/interface

		this.component = (FormComponent<T>)component;
	}

	@Override
	public void onConfigure(Component component)
	{
		super.onConfigure(component);
		if (requiredFlagSet == false)
		{
			// "Required" flag is calculated upon component's model property, so
			// we must ensure,
			// that model object is accessible (i.e. component is already added
			// in a page).
			requiredFlagSet = true;
			if (isRequired())
			{
				this.component.setRequired(true);
			}
		}
	}

	@Override
	public void detach(Component component)
	{
		super.detach(component);
		if (groups_ != null)
		{
			groups_.detach();
		}
	}

	private List<NotNull> findNotNullConstraints()
	{
		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();
		Property property = getProperty();

		List<NotNull> constraints = new ArrayList<NotNull>();

		Iterator<ConstraintDescriptor<?>> it = new ConstraintIterator(validator, property);

		while (it.hasNext())
		{
			ConstraintDescriptor<?> desc = it.next();
			if (desc.getAnnotation().annotationType().equals(NotNull.class))
			{
				constraints.add((NotNull)desc.getAnnotation());
			}
		}

		return constraints;
	}

	boolean isRequired()
	{
		List<NotNull> constraints = findNotNullConstraints();

		if (constraints.isEmpty())
		{
			return false;
		}

		HashSet<Class<?>> validatorGroups = new HashSet<Class<?>>();
		validatorGroups.addAll(Arrays.asList(getGroups()));

		for (NotNull constraint : constraints)
		{
			if (canApplyToDefaultGroup(constraint) && validatorGroups.isEmpty())
			{
				return true;
			}

			for (Class<?> constraintGroup : constraint.groups())
			{
				if (validatorGroups.contains(constraintGroup))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean canApplyToDefaultGroup(NotNull constraint)
	{
		List<Class<?>> groups = Arrays.asList(constraint.groups());
		//the constraint can be applied to default group either if its group array is empty
		//or if it contains javax.validation.groups.Default
		return groups.size() == 0 || groups.contains(Default.class);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);

		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();
		Property property = getProperty();

		// find any tag modifiers that apply to the constraints of the property
		// being validated
		// and allow them to modify the component tag

		Iterator<ConstraintDescriptor<?>> it = new ConstraintIterator(validator, property,
			getGroups());

		while (it.hasNext())
		{
			ConstraintDescriptor<?> desc = it.next();

			ITagModifier modifier = config.getTagModifier(desc.getAnnotation().annotationType());

			if (modifier != null)
			{
				modifier.modify((FormComponent<?>)component, tag, desc.getAnnotation());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(IValidatable<T> validatable)
	{
		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();

		Property property = getProperty();

		// validate the value using the bean validator

		Set<?> violations = validator.validateValue(property.getOwner(), property.getName(),
			validatable.getValue(), getGroups());

		// iterate over violations and report them

		for (ConstraintViolation<?> violation : (Set<ConstraintViolation<?>>)violations)
		{
			validatable.error(config.getViolationTranslator().convert(violation));
		}
	}

}
