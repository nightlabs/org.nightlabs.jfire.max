/**
 * 
 */
package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class LocalAccountantDelegateDAO 
extends BaseJDOObjectDAO<LocalAccountantDelegateID, LocalAccountantDelegate>
{
	private static LocalAccountantDelegateDAO sharedInstance = null;

	public static LocalAccountantDelegateDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (LocalAccountantDelegateDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new LocalAccountantDelegateDAO();
			}
		}
		return sharedInstance;
	}
	
	private LocalAccountantDelegateDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<LocalAccountantDelegate> retrieveJDOObjects(Set<LocalAccountantDelegateID> delegateIDs, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception 
	{
		monitor.beginTask("Loading LocalAccountDelegates", 1);
		try {
			AccountingManager am = JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());  
			return am.getLocalAccountantDelegates(delegateIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * Get the LocalAccountantDelegate for the given delegateID.
	 * 
	 * @param delegateID The delegateID of the desired LocalAccountantDelegate.
	 * @param fetchGroups The fetchGroups to detach the delegate with.
	 * @return A cached version of the LocalAccountantDelegate with the given id.
	 */
	public LocalAccountantDelegate getDelegate(LocalAccountantDelegateID delegateID, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObject(null, delegateID, fetchGroups, maxFetchDepth, monitor);
	}
	
	public Collection<LocalAccountantDelegate> getTopLevelDelegates(Class delegateClass, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			AccountingManager am = JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
			return getJDOObjects(
					null, am.getTopLevelAccountantDelegates(delegateClass),
					fetchGroups, maxFetchDepth, monitor
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get all LocalAccountantDelegates that have the delegate with the given
	 * id as extendedLocalAccountantDelegate.
	 * 
	 * @param delegateID The parent delegate id.
	 * @param fetchGroups The fetchGroups to detach the children with.
	 */
	public Collection<LocalAccountantDelegate> getChildDelegates(LocalAccountantDelegateID delegateID, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) 
	{
		try {
			AccountingManager am = JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
			return getJDOObjects(
					null, am.getChildAccountantDelegates(delegateID),
					fetchGroups, maxFetchDepth, monitor
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
}
