package org.nightlabs.jfire.base.security.integration.ldap;
import javax.ejb.Remote;

/**
 * Remote interface for LDAPManagerBean
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Remote
public interface LDAPManagerRemote {

	void initialise();

}
