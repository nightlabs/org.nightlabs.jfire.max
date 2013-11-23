package org.nightlabs.jfire.numorgid;

import javax.jdo.JDOObjectNotFoundException;

public class UnknownOrganisationException
extends JDOObjectNotFoundException
{
	private static final long serialVersionUID = 1L;

	private String organisationID;

	public UnknownOrganisationException(String organisationID) {
		super("The organisation \"" + organisationID + "\" is unknown!");
		this.organisationID = organisationID;
	}

	public String getOrganisationID() {
		return organisationID;
	}
}
