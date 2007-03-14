package org.nightlabs.jfire.voucher.store;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTypeActionHandler"
 *		detachable="true"
 *		table="JFireVoucher_VoucherTypeActionHandler"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class VoucherTypeActionHandler
		extends ProductTypeActionHandler
{
	/**
	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which becomes the factory-output-repository for all
	 * newly created {@link SimpleProduct}s.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = VoucherType.class.getName() + ".local";

	/**
	 * This is the {@link org.nightlabs.jfire.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = VoucherType.class.getName() + ".foreign";

	public static Repository getDefaultHome(PersistenceManager pm, VoucherType voucherType)
	{
		Store store = Store.getStore(pm);
		if (store.getOrganisationID().equals(voucherType.getOrganisationID()))
			return getDefaultLocalHome(pm, store);
		else
			return getDefaultForeignHome(pm, store);
	}

	private static AnchorID localHomeID = null;

	protected static Repository getDefaultLocalHome(PersistenceManager pm, Store store)
	{
		if (localHomeID == null) {
			localHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_LOCAL);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(localHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					localHomeID.organisationID,
					localHomeID.anchorTypeID,
					localHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}

	private static AnchorID foreignHomeID = null;

	protected static Repository getDefaultForeignHome(PersistenceManager pm, Store store)
	{
		if (foreignHomeID == null) {
			foreignHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_FOREIGN);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(foreignHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					foreignHomeID.organisationID,
					foreignHomeID.anchorTypeID,
					foreignHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherTypeActionHandler() { }

	public VoucherTypeActionHandler(String organisationID,
			String productTypeActionHandlerID, Class productTypeClass)
	{
		super(organisationID, productTypeActionHandlerID, productTypeClass);
	}

	@Implement
	public Collection<Product> findProducts(User user, ProductType productType,
			NestedProductType nestedProductType, ProductLocator productLocator)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented!");
	}

}
