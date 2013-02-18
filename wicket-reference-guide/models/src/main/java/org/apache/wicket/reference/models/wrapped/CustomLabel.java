package org.apache.wicket.reference.models.wrapped;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

//#classOnly
public class CustomLabel extends Label
{
	private final IConverter<?> converter;

	/**
	 * @param id
	 * @param label
	 */
	public <T> CustomLabel(String id, IModel<T> labelModel, IConverter<T> converter)
	{
		super(id, labelModel);
		this.converter = converter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.Component#getConverter(java.lang.Class)
	 */
	@Override
	public <C> IConverter<C> getConverter(Class<C> type)
	{
		return (IConverter<C>)this.converter;
	}
}
//#classOnly
