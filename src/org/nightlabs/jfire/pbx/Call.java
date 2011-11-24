package org.nightlabs.jfire.pbx;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface Call extends Serializable
{
	/**
	 * @return The phone-number to dial.
	 */
	String getDialPhoneNumber(PersistenceManager pm, PhoneSystem phoneSystem) throws UnsupportedCallException;
}
