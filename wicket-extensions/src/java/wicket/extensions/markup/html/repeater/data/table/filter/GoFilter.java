package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.Button;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

public class GoFilter extends Panel
{
	private static final long serialVersionUID = 1L;
	
	public static final IModel defaultGoModel=new Model("filter");
	
	private final Button go;
	
	public GoFilter(String id) {
		this(id, defaultGoModel);
	}
	
	public GoFilter(String id, IModel goModel)
	{
		super(id);
		
		go=new Button("go", goModel) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				onGoSubmit();
			}
		};
		
		add(go);
	}
	
	public Button getGoButton() {
		return go;
	}
	
	protected void onGoSubmit() {
	}

}
