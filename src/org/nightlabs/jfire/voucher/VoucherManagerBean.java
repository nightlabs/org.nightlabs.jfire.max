package org.nightlabs.jfire.voucher;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.voucher.accounting.ModeOfPaymentConst;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.accounting.pay.ServerPaymentProcessorVoucher;
import org.nightlabs.jfire.voucher.store.VoucherDeliveryNoteActionHandler;
import org.nightlabs.jfire.voucher.store.VoucherStore;
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

			VoucherTypeActionHandler voucherTypeActionHandler = new VoucherTypeActionHandler(
					Organisation.DEVIL_ORGANISATION_ID, VoucherTypeActionHandler.class.getName(), VoucherType.class);
			pm.makePersistent(voucherTypeActionHandler);

			VoucherDeliveryNoteActionHandler voucherDeliveryNoteActionHandler = new VoucherDeliveryNoteActionHandler(Organisation.DEVIL_ORGANISATION_ID, VoucherDeliveryNoteActionHandler.class.getName());
			pm.makePersistent(voucherDeliveryNoteActionHandler);

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

			ModeOfPayment modeOfPayment = new ModeOfPayment(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_VOUCHER);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");
			ModeOfPaymentFlavour modeOfPaymentFlavour = modeOfPayment.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_VOUCHER);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");
			modeOfPaymentFlavour.loadIconFromResource(
					ModeOfPaymentConst.class,
					"resource/" + ModeOfPaymentConst.class.getSimpleName() + '-' + ModeOfPaymentFlavour.class.getSimpleName() + '-' + modeOfPaymentFlavour.getModeOfPaymentFlavourID() + ".16x16.png");
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(modeOfPayment);

			ServerPaymentProcessorVoucher serverPaymentProcessorVoucher = ServerPaymentProcessorVoucher.getServerPaymentProcessorVoucher(pm);
			serverPaymentProcessorVoucher.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
			serverPaymentProcessorVoucher.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");
			serverPaymentProcessorVoucher.addModeOfPayment(modeOfPayment);

			// FIXME We should obtain our numeric voucherOrganisationID from the root-organisation as soon as the root-organisation-feature works!!!
			VoucherStore.getVoucherStore(pm).setVoucherOrganisationID(VoucherStore.MAX_VOUCHER_ORGANISATION_ID);

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

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<PriceConfigID> getVoucherPriceConfigIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(VoucherPriceConfig.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<PriceConfigID>((Collection)q.execute());
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
	public List<VoucherPriceConfig> getVoucherPriceConfigs(Collection<PriceConfigID> voucherPriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherPriceConfigIDs, VoucherPriceConfig.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public VoucherType storeVoucherType(VoucherType voucherType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		if (voucherType == null)
			throw new IllegalArgumentException("voucherType must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups == null)
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroups);

			try {
				VoucherLocalAccountantDelegate delegate = (VoucherLocalAccountantDelegate) voucherType.getLocalAccountantDelegate();
				if (delegate != null) {
					OrganisationLegalEntity organisationLegalEntity = null;

					for (Account account : delegate.getAccounts().values()) {
						try {
							if (account.getOwner() == null) {
								if (organisationLegalEntity == null)
									organisationLegalEntity = OrganisationLegalEntity.getOrganisationLegalEntity(pm, getOrganisationID(), OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true);

								account.setOwner(organisationLegalEntity);
							}
						} catch (JDODetachedFieldAccessException x) {
							// ignore
						}
					}
				}
			} catch (JDODetachedFieldAccessException x) {
				// ignore
			}

//		 we don't need any price calculation as we have fixed prices only

			if (NLJDOHelper.exists(pm, voucherType)) {
				voucherType = (VoucherType) pm.makePersistent(voucherType);
			}
			else {
				Store.getStore(pm).addProductType(
						User.getUser(pm, getPrincipal()),
						voucherType,
						VoucherTypeActionHandler.getDefaultHome(pm, voucherType));
			}

			// take care about the inheritance
			voucherType.applyInheritance();

			if (!get)
				return null;

			return (VoucherType) pm.detachCopy(pm.getObjectById(JDOHelper.getObjectId(voucherType)));
		} finally {
			pm.close();
		}
	}

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException in case there are not enough <tt>Voucher</tt>s available and the <tt>Product</tt>s cannot be created (because of a limit). 
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<Article> createArticles(
			SegmentID segmentID,
			OfferID offerID,
			ProductTypeID productTypeID,
			int quantity,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			Store store = Store.getStore(pm);
			Segment segment = (Segment) pm.getObjectById(segmentID);
			Order order = segment.getOrder();

			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(VoucherType.class);
			ProductType pt = (ProductType) pm.getObjectById(productTypeID);
			if (!(pt instanceof VoucherType))
				throw new IllegalArgumentException("productTypeID \""+productTypeID+"\" specifies a ProductType of type \""+pt.getClass().getName()+"\", but must be \""+VoucherType.class.getName()+"\"!");
			
			VoucherType voucherType = (VoucherType)pt;

			// find an Offer within the Order which is not finalized - or create one
			Offer offer;
			if (offerID == null) {
				Collection offers = Offer.getNonFinalizedOffers(pm, order);
				if (!offers.isEmpty()) {
					offer = (Offer) offers.iterator().next();
				}
				else {
					offer = trader.createOffer(user, order, null); // TODO offerIDPrefix ???
				}
			}
			else {
				pm.getExtent(Offer.class);
				offer = (Offer) pm.getObjectById(offerID);
			}

			// find / create Products
			NestedProductType pseudoNestedPT = null;
			if (quantity != 1)
				pseudoNestedPT = new NestedProductType(null, voucherType, quantity);

			Collection products = store.findProducts(user, voucherType, pseudoNestedPT, null);

			Collection articles = trader.createArticles(
					user, offer, segment,
					products,
					new ArticleCreator(null),
					true, false, true);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Set<LocalAccountantDelegateID> getVoucherLocalAccountantDelegateIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(VoucherLocalAccountantDelegate.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<LocalAccountantDelegateID>((Collection<? extends LocalAccountantDelegateID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherLocalAccountantDelegate> getVoucherLocalAccountantDelegates(
			Collection<LocalAccountantDelegateID> voucherLocalAccountantDelegateIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherLocalAccountantDelegateIDs, VoucherLocalAccountantDelegate.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
