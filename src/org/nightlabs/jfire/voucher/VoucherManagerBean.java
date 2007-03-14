package org.nightlabs.jfire.voucher;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.VoucherTypeActionHandler;

/**
 * @ejb.bean name="jfire/ejb/JFireVoucher/VoucherManager"	
 *					 jndi-name="jfire/ejb/JFireVoucher/VoucherManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class VoucherManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final Logger logger = Logger.getLogger(VoucherManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException { }

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Implement
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireVoucher");
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireVoucher started...");

			Trader trader = Trader.getTrader(pm);
			Store store = Store.getStore(pm);

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireVoucher", "1.0.0-0-beta", "1.0.0-0-beta");
			moduleMetaData = (ModuleMetaData) pm.makePersistent(moduleMetaData);

			User user = User.getUser(pm, getPrincipal());

//		 create a default DeliveryConfiguration with all default ModeOfDelivery s
			DeliveryConfiguration deliveryConfiguration = new DeliveryConfiguration(
					getOrganisationID(), "JFireVoucher.default");
			deliveryConfiguration.getName().setText(Locale.ENGLISH.getLanguage(), "Default Delivery Configuration");
			deliveryConfiguration.getName().setText(Locale.GERMAN.getLanguage(), "Standard-Liefer-Konfiguration");
			pm.getExtent(ModeOfDelivery.class);

			try {
				ModeOfDelivery modeOfDelivery;
				
				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryID.create(
						Organisation.DEVIL_ORGANISATION_ID, ModeOfDelivery.MODE_OF_DELIVERY_ID_MANUAL));
				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				// TODO this should be a PRINTING ModeOfDelivery!!!
	
//				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryID.create(
//						Organisation.DEVIL_ORGANISATION_ID, ModeOfDelivery.MODE_OF_DELIVERY_ID_MAILING_VIRTUAL));
//				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);
//
//				modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryID.create(
//						Organisation.DEVIL_ORGANISATION_ID, ModeOfDelivery.MODE_OF_DELIVERY_ID_MAILING_PHYSICAL));
//				deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

				pm.makePersistent(deliveryConfiguration);
			} catch (JDOObjectNotFoundException x) {
				logger.warn("Could not populate default DeliveryConfiguration for JFireVoucher with ModeOfDelivery s!", x);
			}

			// create root-VoucherType (if not yet existing)
			pm.getExtent(VoucherType.class);
			try {
				VoucherType vt = (VoucherType) pm.getObjectById(ProductTypeID.create(getOrganisationID(), VoucherType.class.getName()));
				vt.getDeliveryConfiguration(); // JPOX bug (sometimes it recognises only that the object isn't there when accessing a field)
			} catch (JDOObjectNotFoundException x) {
				VoucherType rootVoucherType = new VoucherType(getOrganisationID(), VoucherType.class.getName(), null, trader.getMandator(), ProductType.INHERITANCE_NATURE_BRANCH, ProductType.PACKAGE_NATURE_OUTER);
				rootVoucherType.setDeliveryConfiguration(deliveryConfiguration);
				store.addProductType(user, rootVoucherType, VoucherTypeActionHandler.getDefaultHome(pm, rootVoucherType));
				store.setProductTypeStatus_published(user, rootVoucherType);
			}

			logger.info("Initialization of JFireVoucher done!");
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProductTypeID> getChildVoucherTypeIDs(ProductTypeID parentVoucherTypeID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(VoucherType.getChildVoucherTypes(pm, parentVoucherTypeID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherType> getVoucherTypes(Collection<ProductTypeID> voucherTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherTypeIDs, VoucherType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
