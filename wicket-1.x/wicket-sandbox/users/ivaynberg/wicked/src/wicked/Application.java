package wicked;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wicked.markup.Markup;
import wicked.markup.parser.BasicXPPParser;
import wicked.markup.parser.IMarkupParser;

public class Application {
	private static ThreadLocal<Application> LOCAL=new ThreadLocal<Application>();
	
	private Map<Class, Markup> markupCache=new ConcurrentHashMap<Class, Markup>();
	
	public static Application get() {
		return LOCAL.get();
	}
	
	public static void set(Application app) {
		LOCAL.set(app);
	}
	
	public Markup getMarkup(Component component) {
		
		Class clazz=component.getClass();
		
		if (!markupCache.containsKey(clazz)) {
			String resource=clazz.getName().replaceAll("[.]", "/");
			resource=resource+".html";
			
			InputStream markupStream = Thread.currentThread()
			.getContextClassLoader().getResourceAsStream(resource);
	
			if (markupStream==null) {
				throw new RuntimeException("cannot load markup for ["+resource+"]");
			}
			
			
			IMarkupParser parser=new BasicXPPParser();
	
			Markup markup=parser.parse(markupStream);
			
			markupCache.put(clazz, markup);
		}
		
		return markupCache.get(clazz);
		
	}
	
}
