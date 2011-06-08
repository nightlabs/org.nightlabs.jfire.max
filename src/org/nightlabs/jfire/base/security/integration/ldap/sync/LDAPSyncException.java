package org.nightlabs.jfire.base.security.integration.ldap.sync;

/**
 * This exception is thrown if some problem occurs when synchronizing data
 * between JFire and LDAP directory in both directions.  
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPSyncException extends Exception {

	/**
	 * serialVersionUID for this class 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	public LDAPSyncException(){
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public LDAPSyncException(String msg){
		super(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public LDAPSyncException(String msg, Throwable t){
		super(msg, t);
	}

	/**
	 * {@inheritDoc}
	 */
	public LDAPSyncException(Throwable t){
		super(t);
	}

}
