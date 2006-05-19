package wicked.markup;

public class StaticFragment extends Fragment {
	private String text;

	public StaticFragment() {
	}
	
	public StaticFragment(String string) {
		text=string;
	}

	public String toString() {
		return text;
	}

	@Override
	public MarkupType getType() {
		return MarkupType.STATIC;
	}


	
}
