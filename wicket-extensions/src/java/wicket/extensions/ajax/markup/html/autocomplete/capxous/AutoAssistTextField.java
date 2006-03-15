package wicket.extensions.ajax.markup.html.autocomplete.capxous;

import java.util.Iterator;

import wicket.markup.html.form.TextField;
import wicket.model.IModel;

/**
 * An implementation of a textfield with the autoassist ajax behavior provided
 * by the autoassist javascript library that can be found here:
 * http://capxous.com/autoassist/
 * 
 * @see AutoAssistBehavior
 * @see IAutoAssistRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AutoAssistTextField extends TextField
{

	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 * @param type
	 */
	public AutoAssistTextField(String id, Class type)
	{
		this(id, (IModel)null, type);
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 */
	public AutoAssistTextField(String id, IModel model, Class type)
	{
		this(id, model, type, StringAutoAssistRenderer.INSTANCE);

	}

	/**
	 * @param id
	 * @param object
	 */
	public AutoAssistTextField(String id, IModel object)
	{
		this(id, object, (Class)null);
	}

	/**
	 * @param id
	 */
	public AutoAssistTextField(String id)
	{
		this(id, (IModel)null);

	}

	/**
	 * @param id
	 * @param renderer
	 */
	public AutoAssistTextField(String id, IAutoAssistRenderer renderer)
	{
		this(id, (IModel)null, renderer);
	}

	/**
	 * @param id
	 * @param type
	 * @param renderer
	 */
	public AutoAssistTextField(String id, Class type, IAutoAssistRenderer renderer)
	{
		this(id, null, type, renderer);
	}

	/**
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public AutoAssistTextField(String id, IModel model, IAutoAssistRenderer renderer)
	{
		this(id, model, (Class)null, renderer);
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 */
	public AutoAssistTextField(String id, IModel model, Class type, IAutoAssistRenderer renderer)
	{
		super(id, model, type);

		add(new AutoAssistBehavior(renderer)
		{

			private static final long serialVersionUID = 1L;

			protected Iterator getAssists(String input)
			{
				return AutoAssistTextField.this.getAssists(input);
			}

		});

	}

	/**
	 * Callback method that should return an iterator over all possible assist
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @see AutoAssistBehavior#getAssists(String)
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible assist choice objects
	 */
	protected abstract Iterator getAssists(String input);


}
