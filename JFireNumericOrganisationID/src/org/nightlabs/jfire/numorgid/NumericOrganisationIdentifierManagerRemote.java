package org.nightlabs.jfire.numorgid;

import javax.ejb.Remote;

import org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID;

@Remote
public interface NumericOrganisationIdentifierManagerRemote {

	NumericOrganisationIdentifier getNumericOrganisationIdentifier(String organisationID, Integer numericOrganisationID);

	NumericOrganisationIdentifier getNumericOrganisationIdentifier(NumericOrganisationIdentifierID numericOrganisationIdentifierID);

	NumericOrganisationIdentifier getNumericOrganisationIdentifier(int numericOrganisationID);

	void initialise() throws Exception;

}