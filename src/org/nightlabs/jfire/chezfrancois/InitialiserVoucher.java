package org.nightlabs.jfire.chezfrancois;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.JFirePrincipal;

public class InitialiserVoucher
extends Initialiser
{
	private static final Logger logger = Logger.getLogger(InitialiserVoucher.class);

	public InitialiserVoucher(PersistenceManager pm, JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws ModuleException 
	{
		
	}
}
