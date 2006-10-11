package org.nightlabs.jfire.jbpm;

import org.jbpm.security.AuthenticationService;
import org.nightlabs.jfire.security.SecurityReflector;

public class JFireJbpmAuthenticationService implements AuthenticationService {
	
	private static final long serialVersionUID = 1L;

	public String getActorId() {
		// TODO @KermitTheFragger: IMHO this should always be the complete user name with organisationID (not just userID).
		// To make the code cleaner and life easier for you, I added the method getCompleteUserID() to UserDescriptor.
		// You can delete this TO-DO note, once you've read it ;-) If you used getUserID() alone somewhere else, please change it there, too.
		// And in case, you need to get a user from the actorId somewhere else, you can use code like this:
		// SecurityReflector.UserDescriptor.parseLogin(actorId).getUser(pm); // where pm is your current PersistenceManager
		// Marco :-)
		// P.S.: Thanks for your contribution!!!
		return SecurityReflector.getUserDescriptor().getCompleteUserID();
	}

	public void close() {
		//Empty for now
	}
}
