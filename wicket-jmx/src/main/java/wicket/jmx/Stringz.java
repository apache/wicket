package wicket.jmx;

class Stringz
{
	static String className(Object o) {
		return (o != null) ? o.getClass().getName() : null;
	}
}
