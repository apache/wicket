package wicket.xtree.table;

import java.io.Serializable;

import wicket.Response;

public interface IRenderable extends Serializable {

	public void render(Response response);
	
}
