package org.nightlabs.jfire.dynamictrade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.dynamictrade.DynamicTradeManagerRemote;
import org.nightlabs.jfire.dynamictrade.template.DynamicProductTemplate;
import org.nightlabs.jfire.dynamictrade.template.id.DynamicProductTemplateID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class DynamicProductTemplateDAO
extends BaseJDOObjectDAO<DynamicProductTemplateID, DynamicProductTemplate>
implements IJDOObjectDAO<DynamicProductTemplate>
{
	private static DynamicProductTemplateDAO sharedInstance = null;
	public static DynamicProductTemplateDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized(DynamicProductTemplateDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DynamicProductTemplateDAO();
			}
		}
		return sharedInstance;
	}

	private DynamicTradeManagerRemote ejb = null;

	@Override
	protected Collection<DynamicProductTemplate> retrieveJDOObjects(Set<DynamicProductTemplateID> dynamicProductTemplateIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		DynamicTradeManagerRemote ejb = this.ejb;
		if (ejb == null)
			ejb = JFireEjb3Factory.getRemoteBean(DynamicTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return ejb.getDynamicProductTemplates(dynamicProductTemplateIDs, fetchGroups, maxFetchDepth);
	}

	public List<DynamicProductTemplate> getChildDynamicProductTemplates(DynamicProductTemplate parentCategory, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		DynamicProductTemplateID parentCategoryID = (DynamicProductTemplateID) JDOHelper.getObjectId(parentCategory);
		if (parentCategoryID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(parentCategory) returned null! " + parentCategory);

		return getChildDynamicProductTemplates(parentCategoryID, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<DynamicProductTemplate> getChildDynamicProductTemplates(DynamicProductTemplateID parentCategoryID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading templates", 100);
		try {
			ejb = JFireEjb3Factory.getRemoteBean(DynamicTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
			try {
				Collection<DynamicProductTemplateID> dynamicProductTemplateIDs = ejb.getChildDynamicProductTemplateIDs(parentCategoryID);
				monitor.worked(30);

				return getJDOObjects(null, dynamicProductTemplateIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 70));
			} finally {
				ejb = null;
			}
		} finally {
			monitor.done();
		}
	}

	public List<DynamicProductTemplate> getDynamicProductTemplates(Collection<DynamicProductTemplateID> dynamicProductTemplateIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading templates", 100);
		try {
			return getJDOObjects(null, dynamicProductTemplateIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			monitor.done();
		}
	}

	@Override
	public DynamicProductTemplate storeJDOObject(DynamicProductTemplate dynamicProductTemplate, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		DynamicTradeManagerRemote ejb = JFireEjb3Factory.getRemoteBean(DynamicTradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
		dynamicProductTemplate = ejb.storeDynamicProductTemplate(dynamicProductTemplate, get, fetchGroups, maxFetchDepth);
		if (dynamicProductTemplate != null)
			getCache().put(null, dynamicProductTemplate, fetchGroups, maxFetchDepth);

		return dynamicProductTemplate;
	}

}
