package org.nightlabs.jfire.pbx;

import javax.jdo.PersistenceManager;

/**
 * Simple {@link Call} implementation that will simply return the number it was created with.
 * 
 * @author abieber
 */
public class SimpleCall implements Call {

	private static final long serialVersionUID = 20111109L;
	
	private String dialPhoneNumber;
	
	public SimpleCall(String dialPhoneNumber) {
		this.dialPhoneNumber = dialPhoneNumber;
	}
	
	@Override
	public String getDialPhoneNumber(PersistenceManager pm, PhoneSystem phoneSystem) throws UnsupportedCallException {
		return dialPhoneNumber;
	}

}
