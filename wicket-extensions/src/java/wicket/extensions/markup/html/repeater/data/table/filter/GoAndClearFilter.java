package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.Button;
import wicket.model.IModel;
import wicket.model.Model;

public class GoAndClearFilter extends GoFilter
{
	private static final long serialVersionUID = 1L;

	public static final IModel defaultClearModel=new Model("clear");
	
	private final Button clear;
	
	
	public GoAndClearFilter(String id) {
		this(id, defaultGoModel, defaultClearModel);
	}
	
	public GoAndClearFilter(String id, IModel goModel, IModel clearModel)
	{
		super(id, goModel);
		
		clear=new Button("clear", clearModel) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				onClearSubmit();
			}
		};

		clear.setDefaultFormProcessing(false);
		
		add(clear);
	}
	
	public Button getClearButton() {
		return clear;
	}
	
	protected void onClearSubmit() {
		
	}

}
