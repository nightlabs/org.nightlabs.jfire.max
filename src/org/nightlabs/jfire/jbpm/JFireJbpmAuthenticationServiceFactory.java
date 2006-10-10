package org.nightlabs.jfire.jbpm;

import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

public class JFireJbpmAuthenticationServiceFactory implements ServiceFactory {
	
	private static final long serialVersionUID = 1L;

	public Service openService() {
		return new JFireJbpmAuthenticationService();
	}
	
	public void close() {
		//Empty for now
	}
}
