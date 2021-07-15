package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.lang.Args;

/**
 * Configures bean validation and integrates it with Wicket
 * 
 * @author igor
 * 
 */
public class BeanValidationConfiguration implements BeanValidationContext
{
	private static final MetaDataKey<BeanValidationConfiguration> KEY = new MetaDataKey<>()
	{
	};

	/**
	 * Default list of annotations that make a component required.
	 */
	static final List<Class<? extends Annotation>> REQUIRED_ANNOTATIONS;
	static
	{
		List<Class<? extends Annotation>> tmp = new ArrayList<>();
		tmp.add(NotNull.class);
		try
		{
			tmp.add(Class.forName("jakarta.validation.constraints.NotBlank")
				.asSubclass(Annotation.class));
			tmp.add(Class.forName("jakarta.validation.constraints.NotEmpty")
				.asSubclass(Annotation.class));
		}
		catch (ClassNotFoundException e)
		{
			// ignore exception, we are using bean validation 1.1
		}
		REQUIRED_ANNOTATIONS = Collections.unmodifiableList(tmp);
	}

	private Supplier<Validator> validatorProvider = new DefaultValidatorProvider();

	private IViolationTranslator violationTranslator = new DefaultViolationTranslator();

	private List<IPropertyResolver> propertyResolvers = new CopyOnWriteArrayList<>();

	private Map<Class<?>, ITagModifier<? extends Annotation>> tagModifiers = new ConcurrentHashMap<>();

	public BeanValidationConfiguration()
	{
		add(new DefaultPropertyResolver());
		register(Size.class, new SizeTagModifier());
	}

	/**
	 * Registers a tag modifier for a specific constraint annotation.
	 * <p>
	 * By default {@link Size} constraints are automatically mapped to <code>maxlength</code> of text inputs,
	 * this can be disabled by registering a {@link ITagModifier#NoOp} instead:
	 * <code>
	 * configuration.register(Size.class, ITagModifier.NoOp});
	 * </code>
	 * 
	 * @param annotationType
	 *            constraint annotation such as {@link Size}
	 * @param modifier
	 *            tag modifier to use
	 * @return {@code this}
	 */
	public <T extends Annotation> BeanValidationConfiguration register(Class<T> annotationType,
		ITagModifier<T> modifier)
	{
		Args.notNull(annotationType, "annotationType");
		Args.notNull(modifier, "modifier");

		tagModifiers.put(annotationType, modifier);

		return this;
	}

	/**
	 * Get the registered modifier for the given annotation.
	 * 
	 * @see #register(Class, ITagModifier)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> ITagModifier<T> getTagModifier(Class<T> annotationType)
	{
		Args.notNull(annotationType, "annotationType");

		return (ITagModifier<T>)tagModifiers.get(annotationType);
	}

	/**
	 * Adds a property resolver to the configuration. Property resolvers registered last are the
	 * first to be allowed to resolve the property.
	 * 
	 * @param resolver
	 * @return {@code this}
	 */
	public BeanValidationConfiguration add(IPropertyResolver resolver)
	{
		Args.notNull(resolver, "resolver");

		// put newly added resolvers in the beginning so its possible to override previously added
		// resolvers with newer ones
		propertyResolvers.add(0, resolver);

		return this;
	}

	/**
	 * @return validator
	 */
	@Override
	public Validator getValidator()
	{
		return validatorProvider.get();
	}

	/**
	 * Sets the provider used to retrieve {@link Validator} instances
	 * 
	 * @param validatorProvider
	 */
	public void setValidatorProvider(Supplier<Validator> validatorProvider)
	{
		Args.notNull(validatorProvider, "validatorProvider");

		this.validatorProvider = validatorProvider;
	}

	/**
	 * Binds this configuration to the application instance
	 * 
	 * @param application
	 */
	public void configure(Application application)
	{
		application.setMetaData(KEY, this);
	}

	/** @return registered violation translator */
	@Override
	public IViolationTranslator getViolationTranslator()
	{
		return violationTranslator;
	}

	/**
	 * Registers a violation translator
	 *
	 * @param violationTranslator
	 *            A violation translator that will convert {@link jakarta.validation.ConstraintViolation}s into Wicket's
	 *            {@link org.apache.wicket.validation.ValidationError}s
	 */
	public void setViolationTranslator(IViolationTranslator violationTranslator)
	{
		Args.notNull(violationTranslator, "violationTranslator");

		this.violationTranslator = violationTranslator;
	}

	/**
	 * Retrieves the validation context (read only version of the configuration). This is how
	 * components retrieve the configuration.
	 * 
	 * @return validation context
	 */
	public static BeanValidationContext get()
	{
		BeanValidationConfiguration config = Application.get().getMetaData(KEY);
		if (config == null)
		{
			throw new IllegalStateException(
				"Application instance has not yet been configured for bean validation. See BeanValidationConfiguration#configure(Application)");
		}
		return config;
	}

	@Override
	public Property resolveProperty(FormComponent<?> component)
	{
		for (IPropertyResolver resolver : propertyResolvers)
		{
			Property property = resolver.resolveProperty(component);
			if (property != null)
			{
				return property;
			}
		}
		return null;
	}

	/**
	 * By default {@link NotNull} and {@link NotEmpty} constraints make a component required.
	 * 
	 * @param constraint
	 *            constraint 
	 */
	@Override
	public boolean isRequiredConstraint(ConstraintDescriptor<?> constraint)
	{
		return REQUIRED_ANNOTATIONS.contains(constraint.getAnnotation().annotationType());
	}
}
