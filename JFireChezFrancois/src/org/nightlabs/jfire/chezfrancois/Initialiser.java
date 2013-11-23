package org.nightlabs.jfire.chezfrancois;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.JFirePrincipal;

public abstract class Initialiser
{
	protected PersistenceManager pm;
	private JFirePrincipal principal;

	public Initialiser(PersistenceManager pm, JFirePrincipal principal)
	{
		this.pm = pm;
		this.principal = principal;
	}

	public String getOrganisationID()
	{
		return principal.getOrganisationID();
	}

	public JFirePrincipal getPrincipal()
	{
		return principal;
	}
}
