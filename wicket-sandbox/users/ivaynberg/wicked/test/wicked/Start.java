package wicked;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Start {

	public static void main(String[] args) throws IOException {
		
		Application app=new Application();
		Application.set(app);
		
		TestPage page=new TestPage();
		ByteArrayOutputStream out=new ByteArrayOutputStream();

		// any component in the tree can be rendered at any point, even before a full page render
		page.get("container1:panel3").render(out);
		
		out.write("\n\n\n".getBytes());
		
		
		page.render(out);
		String str=new String(out.toByteArray());
		System.out.println(str);
	}

}
