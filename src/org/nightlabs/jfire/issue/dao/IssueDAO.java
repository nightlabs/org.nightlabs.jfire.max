package org.nightlabs.jfire.issue.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDODetachedFieldAccessException;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.progress.ProgressMonitor;

public class IssueDAO extends BaseJDOObjectDAO<IssueID, Issue>{

	private static IssueDAO sharedInstance = null;

	public static IssueDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (IssueDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<Issue> retrieveJDOObjects(Set<IssueID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask("Loading Issues", 1);
		try {
			IssueManager am = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return null;//am.getAccounts(objectIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(issue == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issue: "+ issue.getIssueID(), 3);
		try {
			IssueManager im = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Issue result = im.storeIssue(issue, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing Issue!\n" ,e);
		}
	}
}
