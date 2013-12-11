package com.dianping.phoenix.session.requestid;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.phoenix.session.RequestEventDelegate;

public class EventDelegateManager extends ContainerHolder implements Initializable {

	private RequestEventDelegate in;

	private RequestEventDelegate out;

	public RequestEventDelegate getIn() {
		return in;
	}

	public RequestEventDelegate getOut() {
		return out;
	}

	@Override
	public void initialize() throws InitializationException {
		in = lookup(RequestEventDelegate.class);
		out = lookup(RequestEventDelegate.class);
	}

	public void setIn(RequestEventDelegate in) {
		this.in = in;
	}

	public void setOut(RequestEventDelegate out) {
		this.out = out;
	}

}
