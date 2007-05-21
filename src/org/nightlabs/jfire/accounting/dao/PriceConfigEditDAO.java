package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class PriceConfigEditDAO
extends BaseJDOObjectDAO<ProductTypeID, ProductType>
{
	private static PriceConfigEditDAO sharedInstance;

	public static PriceConfigEditDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PriceConfigEditDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PriceConfigEditDAO();
			}
		}
		return sharedInstance;
	};

	@Implement
	protected Collection<ProductType> retrieveJDOObjects(
			Set<ProductTypeID> objectIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception {
		throw new UnsupportedOperationException("NI");
	}

	@Override
	protected ProductType retrieveJDOObject(ProductTypeID objectID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		AccountingManager am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return am.getProductTypeForPriceConfigEditing(objectID);
	}

	public ProductType getProductTypeForPriceConfigEditing(ProductTypeID productTypeID, ProgressMonitor monitor)
	{
		return getJDOObject("getProductTypeForPriceConfigEditing", productTypeID, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	// public void storePriceConfigForProductType(ProductType productType,
	// String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) {
	// // TODO: implement this
	// GridPriceConfigManager gridPriceConfigManager =
	// GridPriceConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
	// gridPriceConfigManager.storePriceConfigs(_priceConfigs, get, fetchGroups,
	// maxFetchDepth);
	// }
}
