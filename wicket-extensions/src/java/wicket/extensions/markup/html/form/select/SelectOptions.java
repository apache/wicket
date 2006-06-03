package wicket.extensions.markup.html.form.select;

import java.util.Collection;
import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Component that makes it easy to produce a list of SelectOption components
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class SelectOptions extends RepeatingView
{
	private static final long serialVersionUID = 1L;
	private boolean recreateChoices = false;
	private IOptionRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public SelectOptions(MarkupContainer parent, final String id, IModel model,
			IOptionRenderer renderer)
	{
		super(parent, id, model);
		this.renderer = renderer;
		setRenderBodyOnly(true);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 * @param elements
	 * @param renderer
	 */
	public SelectOptions(MarkupContainer parent, final String id, Collection elements,
			IOptionRenderer renderer)
	{
		this(parent, id, new Model(elements), renderer);
	}

	/**
	 * Controls whether or not SelectChoice objects are recreated every request
	 * 
	 * @param refresh
	 * @return this for chaining
	 */
	public SelectOptions setRecreateChoices(boolean refresh)
	{
		this.recreateChoices = refresh;
		return this;
	}

	@Override
	protected void onAttach()
	{
		if (size() == 0 || recreateChoices)
		{
			// populate this repeating view with SelectOption components
			removeAll();

			Object modelObject = getModelObject();

			if (modelObject != null)
			{
				if (!(modelObject instanceof Collection))
				{
					throw new WicketRuntimeException("Model object " + modelObject
							+ " not a collection");
				}

				// iterator over model objects for SelectOption components
				Iterator it = ((Collection)modelObject).iterator();

				while (it.hasNext())
				{
					// we need a container to represent a row in repeater
					WebMarkupContainer row = new WebMarkupContainer(this, newChildId());
					row.setRenderBodyOnly(true);

					// we add our actual SelectOption component to the row
					Object value = it.next();
					String text = renderer.getDisplayValue(value);
					IModel model = renderer.getModel(value);
					new SimpleSelectOption(row, "option", model, text);
				}
			}
		}
	}

	private static class SimpleSelectOption extends SelectOption
	{

		private String text;

		/**
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 * @param model
		 * @param text
		 */
		public SimpleSelectOption(MarkupContainer parent, final String id, IModel model, String text)
		{
			super(parent, id, model);
			this.text = text;
		}

		@Override
		protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
		{
			replaceComponentTagBody(markupStream, openTag, text);
		}


		private static final long serialVersionUID = 1L;


	}
}
