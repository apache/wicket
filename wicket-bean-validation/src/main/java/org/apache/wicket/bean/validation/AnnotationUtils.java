package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;

class AnnotationUtils {

	static List<ConstraintDescriptor<?>> findConstraints(Property property, Collection<Class<? extends Annotation>> annotationTypes)
	{
		BeanValidationContext config = BeanValidationConfiguration.get();
		Validator validator = config.getValidator();

		List<ConstraintDescriptor<?>> constraints = new ArrayList<>();

		Iterator<ConstraintDescriptor<?>> it = new ConstraintIterator(validator, property);

		while (it.hasNext())
		{
			ConstraintDescriptor<?> desc = it.next();
			Annotation annotation = desc.getAnnotation();
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationTypes.contains(annotationType))
			{
				constraints.add(desc);
			}
		}

		return constraints;
	}

	static boolean canApplyToDefaultGroup(ConstraintDescriptor<?> constraint)
	{
		Set<Class<?>> groups = constraint.getGroups();
		//the constraint can be applied to default group either if its group array is empty
		//or if it contains javax.validation.groups.Default
		return groups.size() == 0 || groups.contains(Default.class);
	}
}
