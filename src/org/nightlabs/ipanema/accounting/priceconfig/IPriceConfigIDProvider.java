/*
 * Created on Feb 27, 2005
 */
package org.nightlabs.ipanema.accounting.priceconfig;

import org.nightlabs.ModuleException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IPriceConfigIDProvider
{
	String getOrganisationID();
	long createPriceConfigID() throws ModuleException;
}
