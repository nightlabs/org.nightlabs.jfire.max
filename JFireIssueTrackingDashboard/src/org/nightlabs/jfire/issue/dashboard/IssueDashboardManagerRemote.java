/**
 * 
 */
package org.nightlabs.jfire.issue.dashboard;

import javax.ejb.Remote;

/**
 * @author Daniel Mazurek
 *
 */
@Remote
public interface IssueDashboardManagerRemote {
	void initialise() throws Exception;
}
