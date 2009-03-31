package org.apache.wicket.devutils.inspector;

import org.apache.wicket.Session;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.Bytes;

public class SessionTotalSizeModel extends LoadableDetachableModel<Bytes> {

	private static final long serialVersionUID = 1L;

	private Session session;

	public SessionTotalSizeModel(Session session) {
		this.session = session;
	}

	@Override
	protected Bytes load() {
		return Bytes.bytes(session.getSizeInBytes());
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		this.session = null;
	}
}
