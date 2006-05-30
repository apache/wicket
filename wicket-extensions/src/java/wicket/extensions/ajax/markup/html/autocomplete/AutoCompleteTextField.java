package wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;

/**
 * An implementation of a textfield with the autoassist ajax behavior
 * 
 * @see AutoCompleteBehavior
 * @see IAutoCompleteRenderer
 * @param <T>
 *            The type
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AutoCompleteTextField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id)
	{
		this(parent, id, (IModel)null);

	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param type
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, Class type)
	{
		this(parent, id, (IModel)null, type);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param type
	 * @param renderer
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, Class type,
			IAutoCompleteRenderer renderer)
	{
		this(parent, id, null, type, renderer);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param renderer
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id,
			IAutoCompleteRenderer renderer)
	{
		this(parent, id, (IModel)null, renderer);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param object
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, IModel object)
	{
		this(parent, id, object, (Class)null);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param type
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, IModel model, Class type)
	{
		this(parent, id, model, type, StringAutoCompleteRenderer.INSTANCE);

	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, IModel model, Class type,
			IAutoCompleteRenderer renderer)
	{
		super(parent, id, model, type);

		add(new AutoCompleteBehavior(renderer)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator getChoices(String input)
			{
				return AutoCompleteTextField.this.getChoices(input);
			}

		});

	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public AutoCompleteTextField(MarkupContainer parent, final String id, IModel model,
			IAutoCompleteRenderer renderer)
	{
		this(parent, id, model, (Class)null, renderer);
	}

	/**
	 * Callback method that should return an iterator over all possible assist
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @see AutoCompleteBehavior#getChoices(String)
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible choice objects
	 */
	protected abstract Iterator getChoices(String input);
}
