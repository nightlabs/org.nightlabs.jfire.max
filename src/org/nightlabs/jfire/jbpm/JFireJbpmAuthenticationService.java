package org.nightlabs.jfire.jbpm;

import org.jbpm.security.AuthenticationService;
import org.nightlabs.jfire.security.SecurityReflector;

public class JFireJbpmAuthenticationService implements AuthenticationService {
	
	private static final long serialVersionUID = 1L;
	
	public String getActorId() {
		return SecurityReflector.getUserDescriptor().getUserID();
	}

	public void close() {
		//Empty for now
	}
}
