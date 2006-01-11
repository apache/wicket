package wicked;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Start {

	public static void main(String[] args) throws IOException {
		
		Application app=new Application();
		Application.set(app);

		repeaterTest();
		//pageTest();
	}
	
	public static void repeaterTest() {
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		RepeaterPage page=new RepeaterPage();
		page.render(out);
		String str=new String(out.toByteArray());
		System.out.println(str);
	}
	
	
	public static void pageTest() throws IOException {
		ByteArrayOutputStream out=new ByteArrayOutputStream();

		TestPage page=new TestPage();

		// any component in the tree can be rendered at any point, even before a full page render
		page.get("container1:panel3").render(out);
		
		out.write("\n\n\n".getBytes());
		
		
		page.render(out);
		String str=new String(out.toByteArray());
		System.out.println(str);
	}

}
