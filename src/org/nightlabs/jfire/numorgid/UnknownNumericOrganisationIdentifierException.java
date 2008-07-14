package org.nightlabs.jfire.numorgid;

import javax.jdo.JDOObjectNotFoundException;

public class UnknownNumericOrganisationIdentifierException
extends JDOObjectNotFoundException 
{
	private static final long serialVersionUID = 1L;

	private int numericOrganisationID;

	public UnknownNumericOrganisationIdentifierException(int numericOrganisationID) {
		super("The numeric organisationID \"" + numericOrganisationID + "\" is unknown!");
		this.numericOrganisationID = numericOrganisationID;
	}

	public int getNumericOrganisationID() {
		return numericOrganisationID;
	}
}
