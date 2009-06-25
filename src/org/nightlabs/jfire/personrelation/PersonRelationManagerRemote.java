package org.nightlabs.jfire.personrelation;

import javax.ejb.Remote;

@Remote
public interface PersonRelationManagerRemote {

	void initialise() throws Exception;

}
