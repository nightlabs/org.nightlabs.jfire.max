package org.nightlabs.jfire.voucher;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorManual;
import org.nightlabs.jfire.store.deliver.id.DeliveryConfigurationID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleCreator;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LayoutMapForArticleIDSet;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.voucher.accounting.ModeOfPaymentConst;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.accounting.pay.ServerPaymentProcessorVoucher;
import org.nightlabs.jfire.voucher.scripting.PreviewParameterSet;
import org.nightlabs.jfire.voucher.scripting.PreviewParameterValuesResult;
import org.nightlabs.jfire.voucher.scripting.ScriptingInitialiser;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.scripting.VoucherScriptingConstants;
import org.nightlabs.jfire.voucher.scripting.id.VoucherLayoutID;
import org.nightlabs.jfire.voucher.store.Voucher;
import org.nightlabs.jfire.voucher.store.VoucherDeliveryNoteActionHandler;
import org.nightlabs.jfire.voucher.store.VoucherKey;
import org.nightlabs.jfire.voucher.store.VoucherStore;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.VoucherTypeActionHandler;
import org.nightlabs.jfire.voucher.store.deliver.ServerDeliveryProcessorClientSideVoucherPrint;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;

/**
 * @ejb.bean name="jfire/ejb/JFireVoucher/VoucherManager"
 *           jndi-name="jfire/ejb/JFireVoucher/VoucherManager" type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class VoucherManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(VoucherManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException {
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
	public void ejbCreate() throws CreateException {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Implement
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws Exception {
		PersistenceManager pm = getPersistenceManager();
		JFireServerManager jsm = getJFireServerManager();
		try {
			// init scripts
			new ScriptingInitialiser(jsm, pm,
					Organisation.DEV_ORGANISATION_ID).initialise(); // this is a
																														// throw-away-instance

			DeliveryConfiguration deliveryConfiguration = checkDeliveryConfiguration(pm);
			// check each time for ticketPrinter module, to register corresponding
			// modeOfDeliveryFlavour if necessary
			checkModeOfDeliveryFlavourTicketPrinter(pm);

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm,
					"JFireVoucher");
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireVoucher started...");

			Trader trader = Trader.getTrader(pm);
			Store store = Store.getStore(pm);

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData("JFireVoucher", "0.9.3-0-beta", // TODO use constant for "JFireVoucher" like in other EARs
					"0.9.3-0-beta");
			moduleMetaData = pm.makePersistent(moduleMetaData);

			User user = User.getUser(pm, getPrincipal());

			AccountType accountType;
			accountType = pm.makePersistent(new AccountType(JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER, false));
			accountType.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
			accountType.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");


			// create the ProductTypeActionHandler for VoucherTypes
			VoucherTypeActionHandler voucherTypeActionHandler = new VoucherTypeActionHandler(
					Organisation.DEV_ORGANISATION_ID, VoucherTypeActionHandler.class
							.getName(), VoucherType.class);
			pm.makePersistent(voucherTypeActionHandler);

			VoucherDeliveryNoteActionHandler voucherDeliveryNoteActionHandler = new VoucherDeliveryNoteActionHandler(
					Organisation.DEV_ORGANISATION_ID,
					VoucherDeliveryNoteActionHandler.class.getName());
			pm.makePersistent(voucherDeliveryNoteActionHandler);

//			DeliveryConfiguration deliveryConfiguration = checkDeliveryConfiguration(pm);

			// create root-VoucherType (if not yet existing)
			pm.getExtent(VoucherType.class);
			try {
				VoucherType vt = (VoucherType) pm.getObjectById(ProductTypeID.create(
						getOrganisationID(), VoucherType.class.getName()));
				vt.getDeliveryConfiguration(); // JPOX bug (sometimes it recognises
																				// only that the object isn't there when
																				// accessing a field)
			} catch (JDOObjectNotFoundException x) {
				VoucherType rootVoucherType = new VoucherType(getOrganisationID(),
						VoucherType.class.getName(), null,
						ProductType.INHERITANCE_NATURE_BRANCH,
						ProductType.PACKAGE_NATURE_OUTER);
				rootVoucherType.setOwner(trader.getMandator());
				rootVoucherType.getName().setText(Locale.ENGLISH.getLanguage(), LocalOrganisation.getLocalOrganisation(pm).getOrganisation().getPerson().getDisplayName());
				rootVoucherType.setDeliveryConfiguration(deliveryConfiguration);
				store.addProductType(user, rootVoucherType); // , VoucherTypeActionHandler.getDefaultHome(pm, rootVoucherType));
				store.setProductTypeStatus_published(user, rootVoucherType);
			}

			LegalEntity anonymousCustomer = LegalEntity.getAnonymousLegalEntity(pm);
			CustomerGroup anonymousCustomerGroup = anonymousCustomer
					.getDefaultCustomerGroup();

			ModeOfPayment modeOfPayment = new ModeOfPayment(
					ModeOfPaymentConst.MODE_OF_PAYMENT_ID_VOUCHER);
			modeOfPayment.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
			modeOfPayment.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");
			ModeOfPaymentFlavour modeOfPaymentFlavour = modeOfPayment
					.createFlavour(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_VOUCHER);
			modeOfPaymentFlavour.getName().setText(Locale.ENGLISH.getLanguage(),
					"Voucher");
			modeOfPaymentFlavour.getName().setText(Locale.GERMAN.getLanguage(),
					"Gutschein");
			modeOfPaymentFlavour
					.loadIconFromResource(ModeOfPaymentConst.class, "resource/"
							+ ModeOfPaymentConst.class.getSimpleName() + '-'
							+ ModeOfPaymentFlavour.class.getSimpleName() + '-'
							+ modeOfPaymentFlavour.getModeOfPaymentFlavourID() + ".16x16.png");
			pm.makePersistent(modeOfPayment);
			trader.getDefaultCustomerGroupForKnownCustomer().addModeOfPayment(
					modeOfPayment);
			anonymousCustomerGroup.addModeOfPayment(modeOfPayment);

			ServerPaymentProcessorVoucher serverPaymentProcessorVoucher = ServerPaymentProcessorVoucher
					.getServerPaymentProcessorVoucher(pm);
			serverPaymentProcessorVoucher.getName().setText(
					Locale.ENGLISH.getLanguage(), "Voucher");
			serverPaymentProcessorVoucher.getName().setText(
					Locale.GERMAN.getLanguage(), "Gutschein");
			serverPaymentProcessorVoucher.addModeOfPayment(modeOfPayment);

			// FIXME We should obtain our numeric voucherOrganisationID from the
			// root-organisation as soon as the root-organisation-feature works!!!
			VoucherStore.getVoucherStore(pm).setVoucherOrganisationID(
					VoucherStore.MAX_VOUCHER_ORGANISATION_ID);

			logger.info("Initialization of JFireVoucher done!");
		} finally {
			pm.close();
			jsm.close();
		}
	}

	protected void checkModeOfDeliveryFlavourTicketPrinter(PersistenceManager pm)
	{
		try {
			pm.getObjectById(JFireVoucherEAR.MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_TICKET_PRINTER);
			return; // fine - it is already persistent and we don't need to do it
		} catch (JDOObjectNotFoundException e) {
			// the object is not persisted => ignore this exception and continue creating + persisting it
		}

		String ticketPrinterClassName = "org.nightlabs.ticketprinter.TicketPrinter"; // check whether this class exists and only do things, if it does
		try {
			Class.forName(ticketPrinterClassName);
		} catch (ClassNotFoundException e2) {
			// Tobias: I don't think it is necessary to "pollute" the server log with the exception

//			logger.info("Class "+ticketPrinterClassName+" could not be resolved, means TicketPrinter Module " +
//					"is not deployed, will skip registering of ModeOfDeliveryFlavour " +
//					JFireVoucherEAR.MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_TICKET_PRINTER, e2);
			logger.info("Class "+ticketPrinterClassName+" could not be resolved, means TicketPrinter Module " +
					"is not deployed, will skip registering of ModeOfDeliveryFlavour " +
					JFireVoucherEAR.MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_TICKET_PRINTER);

			// the class does not exist => no need for this ModeOfDelivery[Flavour]
			return;
		}

		ModeOfDelivery modeOfDelivery = getModeOfDeliveryVoucherPrint(pm);
		ModeOfDeliveryFlavour modeOfDeliveryFlavour = modeOfDelivery.createFlavour(JFireVoucherEAR.MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_TICKET_PRINTER);
		modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Print Voucher To Ticket Printer");
		modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein-Druck via Ticket-Drucker");
	}

	protected ModeOfDelivery getModeOfDeliveryVoucherPrint(PersistenceManager pm)
	{
		ModeOfDelivery modeOfDelivery;
		try {
			modeOfDelivery = (ModeOfDelivery) pm.getObjectById(JFireVoucherEAR.MODE_OF_DELIVERY_ID_VOUCHER_PRINT);
		} catch (JDOObjectNotFoundException x) {
			modeOfDelivery = new ModeOfDelivery(JFireVoucherEAR.MODE_OF_DELIVERY_ID_VOUCHER_PRINT);
			modeOfDelivery.getName().setText(Locale.ENGLISH.getLanguage(), "Print Voucher");
			modeOfDelivery.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein-Druck");
			modeOfDelivery = pm.makePersistent(modeOfDelivery);
		}
		return modeOfDelivery;
	}

	protected DeliveryConfiguration checkDeliveryConfiguration(PersistenceManager pm)
	{
		DeliveryConfiguration deliveryConfiguration = null;
		try {
			deliveryConfiguration = (DeliveryConfiguration) pm.getObjectById(DeliveryConfigurationID.create(getOrganisationID(), "JFireVoucher.default"));
			return deliveryConfiguration;
		}
		catch (JDOObjectNotFoundException jdoonfe)
		{
				// create a default DeliveryConfiguration with all default ModeOfDeliverys
				deliveryConfiguration = new DeliveryConfiguration(
						getOrganisationID(), "JFireVoucher.default");
				deliveryConfiguration.getName().setText(Locale.ENGLISH.getLanguage(),
						"Default Delivery Configuration for Vouchers");
				deliveryConfiguration.getName().setText(Locale.GERMAN.getLanguage(),
						"Standard-Liefer-Konfiguration für Gutscheine");
				pm.getExtent(ModeOfDelivery.class);

				try {
					ModeOfDelivery modeOfDelivery;
					ModeOfDeliveryFlavour modeOfDeliveryFlavour;

					modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
					deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

					modeOfDelivery = getModeOfDeliveryVoucherPrint(pm);

					for (Iterator<CustomerGroup> it = pm.getExtent(CustomerGroup.class).iterator(); it.hasNext(); ) {
						CustomerGroup customerGroup = it.next();
						customerGroup.addModeOfDelivery(modeOfDelivery);
					}

					modeOfDeliveryFlavour = modeOfDelivery.createFlavour(JFireVoucherEAR.MODE_OF_DELIVERY_FLAVOUR_ID_VOUCHER_PRINT_VIA_OPERATING_SYSTEM_PRINTER);
					modeOfDeliveryFlavour.getName().setText(Locale.ENGLISH.getLanguage(), "Print To Operating System Printer");
					modeOfDeliveryFlavour.getName().setText(Locale.GERMAN.getLanguage(), "Druck via Betriebssystem-Drucker");

					deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

					ServerDeliveryProcessorClientSideVoucherPrint.getServerDeliveryProcessorClientSideVoucherPrint(pm).addModeOfDelivery(modeOfDelivery);


					modeOfDelivery = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_DELIVER_TO_DELIVERY_QUEUE);
					deliveryConfiguration.addModeOfDelivery(modeOfDelivery);

					pm.makePersistent(deliveryConfiguration);
				} catch (JDOObjectNotFoundException x) {
					logger
							.warn(
									"Could not populate default DeliveryConfiguration for JFireVoucher with ModeOfDelivery s!",
									x);
				}
		}
		return deliveryConfiguration;
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.VoucherManager.VoucherType.read"
	 */
	@SuppressWarnings("unchecked")
	public Set<ProductTypeID> getChildVoucherTypeIDs(
			ProductTypeID parentVoucherTypeID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(VoucherType.getChildVoucherTypes(pm,
					parentVoucherTypeID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.voucher.VoucherManager.VoucherType.read"
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherType> getVoucherTypes(Collection<ProductTypeID> voucherTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherTypeIDs,
					VoucherType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<PriceConfigID> getVoucherPriceConfigIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(VoucherPriceConfig.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<PriceConfigID>((Collection) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherPriceConfig> getVoucherPriceConfigs(Collection<PriceConfigID> voucherPriceConfigIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherPriceConfigIDs,
					VoucherPriceConfig.class, fetchGroups, maxFetchDepth);
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
				if (voucherType.getProductTypeLocal() != null) {
					VoucherLocalAccountantDelegate delegate = (VoucherLocalAccountantDelegate) voucherType.getProductTypeLocal().getLocalAccountantDelegate();
					if (delegate != null) {
						OrganisationLegalEntity organisationLegalEntity = null;

						for (Account account : delegate.getAccounts().values()) {
							try {
								if (account.getOwner() == null) {
									if (organisationLegalEntity == null)
										organisationLegalEntity = OrganisationLegalEntity
										.getOrganisationLegalEntity(pm, getOrganisationID());

									account.setOwner(organisationLegalEntity);
								}
							} catch (JDODetachedFieldAccessException x) {
								// ignore
							}
						}
					}
				} // if (voucherType.getProductTypeLocal() != null)
			} catch (JDODetachedFieldAccessException x) {
				// ignore
			}

			// we don't need any price calculation as we have fixed prices only

			if (NLJDOHelper.exists(pm, voucherType)) {
				voucherType = pm.makePersistent(voucherType);
			} else {
				voucherType = (VoucherType) Store.getStore(pm).addProductType(User.getUser(pm, getPrincipal()), voucherType);
			}

			if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) { // TODO JPOX WORKAROUND
				pm.flush();
				ProductTypeID vtid = (ProductTypeID) JDOHelper.getObjectId(voucherType);
				pm.evictAll();
				voucherType = (VoucherType) pm.getObjectById(vtid);
			}

			// take care about the inheritance
			voucherType.applyInheritance();

			if (!get)
				return null;

			VoucherType detachedVoucherType = pm.detachCopy(voucherType);
			return detachedVoucherType;
		} finally {
			pm.close();
		}
	}

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException
	 *           in case there are not enough <tt>Voucher</tt>s available and
	 *           the <tt>Product</tt>s cannot be created (because of a limit).
	 */
	protected Collection<? extends Article> createArticles(PersistenceManager pm,
			SegmentID segmentID, OfferID offerID, ProductTypeID productTypeID,
			int quantity) throws ModuleException {
		Trader trader = Trader.getTrader(pm);
		Store store = Store.getStore(pm);
		Segment segment = (Segment) pm.getObjectById(segmentID);
		Order order = segment.getOrder();

		User user = User.getUser(pm, getPrincipal());

		pm.getExtent(VoucherType.class);
		ProductType pt = (ProductType) pm.getObjectById(productTypeID);
		if (!(pt instanceof VoucherType))
			throw new IllegalArgumentException("productTypeID \"" + productTypeID
					+ "\" specifies a ProductType of type \"" + pt.getClass().getName()
					+ "\", but must be \"" + VoucherType.class.getName() + "\"!");

		VoucherType voucherType = (VoucherType) pt;

		// find an Offer within the Order which is not finalized - or create one
		Offer offer;
		if (offerID == null) {
			Collection<Offer> offers = Offer.getNonFinalizedNonEndedOffers(pm, order);
			if (!offers.isEmpty()) {
				offer = offers.iterator().next();
			} else {
				offer = trader.createOffer(user, order, String.valueOf(Calendar
						.getInstance().get(Calendar.YEAR)));
			}
		} else {
			pm.getExtent(Offer.class);
			offer = (Offer) pm.getObjectById(offerID);
		}

		// find / create Products
		NestedProductTypeLocal pseudoNestedPT = null;
		if (quantity != 1)
			pseudoNestedPT = new NestedProductTypeLocal(null, voucherType.getProductTypeLocal(), quantity);

		Collection<? extends Product> products = store.findProducts(
				user,
				voucherType,
				pseudoNestedPT,
				null
		);

		Collection<? extends Article> articles = trader.createArticles(user, offer, segment, products,
				new ArticleCreator(null), true, false);

		return articles;
	}

	/**
	 * @return <tt>Collection</tt> of {@link org.nightlabs.jfire.trade.Article}
	 * @throws org.nightlabs.jfire.store.NotAvailableException
	 *           in case there are not enough <tt>Voucher</tt>s available and
	 *           the <tt>Product</tt>s cannot be created (because of a limit).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<? extends Article> createArticles(SegmentID segmentID,
			OfferID offerID, ProductTypeID productTypeID, int quantity,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<? extends Article> articles = createArticles(
					pm, segmentID, offerID,
					productTypeID, quantity
			);

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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<LocalAccountantDelegateID> getVoucherLocalAccountantDelegateIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(VoucherLocalAccountantDelegate.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<LocalAccountantDelegateID>(
					(Collection<? extends LocalAccountantDelegateID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherLocalAccountantDelegate> getVoucherLocalAccountantDelegates(
			Collection<LocalAccountantDelegateID> voucherLocalAccountantDelegateIDs,
			String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm,
					voucherLocalAccountantDelegateIDs,
					VoucherLocalAccountantDelegate.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public VoucherKeyID getVoucherKeyID(String voucherKeyString) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return VoucherKey.getVoucherKeyID(pm, voucherKeyString);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherKey> getVoucherKeys(
			Collection<VoucherKeyID> voucherKeyIDs, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, voucherKeyIDs,
					VoucherKey.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @param voucherIDs
	 *          Specifies which vouchers shall be evaluated.
	 * @param allScripts
	 *          If <code>false</code>, only those scripts are evaluated that
	 *          are imported into the voucher design. If <code>true</code>, all
	 *          scripts with the
	 *          {@link ScriptRegistryItem#getScriptRegistryItemType()}
	 *          {@link VoucherScriptingConstants#SCRIPT_REGISTRY_ITEM_TYPE_VOUCHER}
	 *          will be executed and included in the result.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Map<ProductID, Map<ScriptRegistryItemID, Object>> getVoucherScriptingResults(
			Collection<ProductID> voucherIDs, boolean allScripts)
			throws ModuleException {
		return getVoucherScriptingResults((PersistenceManager) null, voucherIDs,
				allScripts);
	}

	// TODO we need to pass ArticleIDs instead of ProductIDs, because products can be resold
	// after having been reversed and we should be able to print duplicates at any time with
	// the correct data.
	protected Map<ProductID, Map<ScriptRegistryItemID, Object>> getVoucherScriptingResults(
			PersistenceManager pm, Collection<ProductID> voucherIDs,
			boolean allScripts) throws ModuleException
	{
		allScripts = true; // TODO remove this line!
		// TODO obtain the scripts via the voucher-layout-file,
		try {
			boolean closePM = false;
			if (pm == null) {
				closePM = true;
				pm = getPersistenceManager();
			}
			try {
				ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
				pm.getExtent(Voucher.class);
				Map<ProductID, Map<ScriptRegistryItemID, Object>> res = new HashMap<ProductID, Map<ScriptRegistryItemID, Object>>();
				for (ProductID voucherID : voucherIDs) {
					Voucher voucher = (Voucher) pm.getObjectById(voucherID);
					// obtain list of scripts for current voucher
					VoucherType voucherType = (VoucherType) voucher.getProductType();
					List<Script> scripts = new ArrayList<Script>();
					if (allScripts) {
						Query q = pm.newQuery("SELECT FROM " + Script.class.getName()
								+ " \n" + "WHERE \n"
								+ "  this.scriptRegistryItemType == pScriptRegistryItemType \n"
								+ "PARAMETERS java.lang.String pScriptRegistryItemType");

						// scripts.addAll((Collection)q.execute(VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_VOUCHER));
						scripts
								.addAll((Collection<? extends Script>) q
										.execute(VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER));
					} else {
						// TODO obtain the scripts via the voucher-layout-file,
						// scriptRegistryItemIDs from scriptDrawComponents as well as from
						// visibleScripts
						if (voucherType.getVoucherLayout() == null)
							throw new IllegalStateException(
									"voucher.getVoucherLayout() == null");
					}

					// TODO this way, we cannot really print duplicates as a duplicate will always be printed with the
					// most current values. This causes problems after an article has been reversed and the same product
					// resold. We need to create a special VoucherArticle which
					// snapshots the VoucherKey at the moment it is assigned to the Voucher instance.
					VoucherKey voucherKey = voucher.getVoucherKey();
					if (voucherKey == null) {
//						logger.error(
//								"voucher.voucherKey == null! voucher.getPrimaryKey()=" + voucher.getPrimaryKey(),
//								new IllegalStateException("The voucher does not have a voucherKey assigned! " + voucher.getPrimaryKey()));
						throw new IllegalStateException("The voucher does not have a voucherKey assigned! " + voucher.getPrimaryKey());

//						Query q = pm.newQuery(VoucherKey.class);
//						q.setFilter("this.voucher == :voucher");
//						q.setOrdering("this.voucherNumber DESC");
//						Collection<VoucherKey> voucherKeys = (Collection<VoucherKey>) q.execute(voucher);
//						if (voucherKeys.isEmpty())
//							throw new IllegalStateException("The voucher does not have a voucherKey assigned and even a query on the datastore did not return any VoucherKey! " + voucher.getPrimaryKey());
//
//						for (VoucherKey vk : voucherKeys) {
//							voucherKey = vk; break;
//						}
					}

					VoucherKeyID voucherKeyID = (VoucherKeyID) JDOHelper.getObjectId(voucherKey);
					if (voucherKeyID == null)
						throw new IllegalStateException("The voucherKey does not have an ID assigned! " + voucherKey.getVoucherKey());

					Map<String, Object> paramValues = new HashMap<String, Object>();
					paramValues.put(
							VoucherScriptingConstants.PARAMETER_ID_PERSISTENCE_MANAGER, pm);
					// paramValues.put(VoucherScriptingConstants.PARAMETER_ID_VOUCHER_ID,
					// voucherID);
					paramValues.put(
							VoucherScriptingConstants.PARAMETER_ID_VOUCHER_KEY_ID,
							voucherKeyID);
					res.put(voucherID, scriptRegistry.execute(scripts, paramValues));
				}
				return res;
			} finally {
				if (closePM)
					pm.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	private static class PreviewParameterSetExtension {
		public VoucherType voucherType;

		public Currency currency;
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PreviewParameterValuesResult getPreviewParameterValues(
			ProductTypeID voucherTypeID) throws ModuleException {
		try {
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
//					Variables are never used locally
//					User user = User.getUser(pm, getPrincipal());
//					PreviewParameterSet previewParameterSet = new PreviewParameterSet(voucherTypeID);

//					PreviewParameterSetExtension previewParameterSetExtension = ensureFinishedConfiguration(
//							pm, user, previewParameterSet);

					pm.getExtent(VoucherType.class);
					VoucherType voucherType = (VoucherType) pm.getObjectById(voucherTypeID);
					return new PreviewParameterValuesResult(voucherType);
				} finally {
					pm.close();
				}
			} finally {
				// This must be done at the end (*NOT* before), because it will immediately close the DB connection managed by the container.
				sessionContext.setRollbackOnly();
			}
		} catch (RuntimeException x) {
			throw x;
//		} catch (ModuleException x) {
//			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * This method ensures that the event specified by
	 * <code>previewParameterSet</code> is completely configured. If it's not,
	 * it will be configured using the other IDs in
	 * <code>previewParameterSet</code>. All of them except voucherTypeID can
	 * be <code>null</code>. They'll be set to an arbitrary value then.
	 * <p>
	 * <b>Warning:</b> This method modifies the database with arbitrary data!
	 * Thus, it requires the bean method to have the
	 * <code>transaction type="RequiresNew"</code> and to rollback the
	 * transaction using <code>sessionContext.setRollbackOnly();</code>.
	 * </p>
	 *
	 * @param pm
	 *          The PersistenceManager used for the datastore access.
	 * @param user
	 *          Who is responsible?
	 * @param previewParameterSet
	 *          Various parameters necessary for generating preview data. If a
	 *          property is <code>null</code>, it will be set to a meaningful
	 *          value. Therefore, this object may be modified during execution.
	 *          Note, that all properties are non-<code>null</code> afterwards.
	 * @throws ModuleException
	 * @throws NamingException
	 * @throws IOException
	 * @throws CannotConfirmProductTypeException
	 */
	private PreviewParameterSetExtension ensureFinishedConfiguration(
			PersistenceManager pm, User user, PreviewParameterSet previewParameterSet)
			throws ModuleException, NamingException, IOException, CannotConfirmProductTypeException {
		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		if (previewParameterSet == null)
			throw new IllegalArgumentException(
					"previewParameterSet must not be null!");

		if (previewParameterSet.getVoucherTypeID() == null)
			throw new IllegalArgumentException(
					"previewParameterSet.voucherTypeID must not be null!");

		// configure the given event, create an order, add an article and query the
		// ticket data
		// finally, rollback the transaction

		String organisationID = user.getOrganisationID();
//		Accounting accounting = Accounting.getAccounting(pm);
		Store store = Store.getStore(pm);

		PreviewParameterSetExtension previewParameterSetExtension = new PreviewParameterSetExtension();

		// ensure the event is correctly configured and confirmed
		pm.getExtent(VoucherType.class);
		previewParameterSetExtension.voucherType = (VoucherType) pm
				.getObjectById(previewParameterSet.getVoucherTypeID());

		// Currency
		Extent extent = pm.getExtent(Currency.class);
		if (previewParameterSet.getCurrencyID() == null) {
			Iterator<Currency> it = extent.iterator();
			if (it.hasNext())
				previewParameterSetExtension.currency = it.next();
			else {
				previewParameterSetExtension.currency = new Currency("EUR", "EUR", 2);
				pm.makePersistent(previewParameterSetExtension.currency);
			}
			previewParameterSet.setCurrencyID((CurrencyID) JDOHelper
					.getObjectId(previewParameterSetExtension.currency));
		} else
			previewParameterSetExtension.currency = (Currency) pm
					.getObjectById(previewParameterSet.getCurrencyID());

		if (!previewParameterSetExtension.voucherType.isConfirmed()) {
			if (previewParameterSetExtension.voucherType.getDeliveryConfiguration() == null) {
				pm.getExtent(DeliveryConfiguration.class);
				previewParameterSetExtension.voucherType
						.setDeliveryConfiguration((DeliveryConfiguration) pm
								.getObjectById(DeliveryConfigurationID.create(organisationID,
										JFireVoucherEAR.DEFAULT_DELIVERY_CONFIGURATION_ID)));
			}

			boolean calculatePrices = false;
			if (previewParameterSetExtension.voucherType.getPackagePriceConfig() == null) {
				calculatePrices = true;
				VoucherPriceConfig packagePriceConfig = new VoucherPriceConfig(
						IDGenerator.getOrganisationID(), PriceConfig.createPriceConfigID()); // TODO do we really need to consume IDs here - hmmm... shouldn't be such a big problem
				pm.makePersistent(packagePriceConfig);
				previewParameterSetExtension.voucherType
						.setPackagePriceConfig(packagePriceConfig);
			}

			if (previewParameterSetExtension.voucherType.getInnerPriceConfig() == null) {
				// Ignore as vouchers don't have innerPriceConfig
			}

			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig) previewParameterSetExtension.voucherType
					.getPackagePriceConfig();

			if (voucherPriceConfig.getCurrency(
					previewParameterSet.getCurrencyID().currencyID, false) == null) {
				calculatePrices = true;
				voucherPriceConfig.addCurrency(previewParameterSetExtension.currency);
			}

			if (calculatePrices) {
				long defaultValue = 5000;
				voucherPriceConfig.setPrice(previewParameterSetExtension.currency,
						defaultValue);
				// the PriceCalculator may do some detaching stuff
				pm.getFetchPlan().setMaxFetchDepth(1);
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			}

			store.setProductTypeStatus_confirmed(user,
					previewParameterSetExtension.voucherType);
		} // if (!previewParameterSetExtension.voucherType.isConfirmed())

		return previewParameterSetExtension;
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Map<ProductID, Map<ScriptRegistryItemID, Object>> getPreviewVoucherData(
			PreviewParameterSet previewParameterSet) throws ModuleException {
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getFetchPlan().setMaxFetchDepth(1);
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

				User user = User.getUser(pm, getPrincipal());
				Store store = Store.getStore(pm);

				PreviewParameterSetExtension previewParameterSetExtension = ensureFinishedConfiguration(
						pm, user, previewParameterSet);

				// find or create an Order
				Trader trader = Trader.getTrader(pm);

				IStruct personStruct = StructLocal.getStructLocal(getOrganisationID(),
						Person.class.getName(), 
						Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, pm);
				Person person = new Person(getOrganisationID(), IDGenerator
						.nextID(PropertySet.class));
				person.inflate(personStruct);
				((TextDataField) person.getDataField(PersonStruct.PERSONALDATA_COMPANY))
						.setText("NightLabs GmbH");
				((TextDataField) person.getDataField(PersonStruct.PERSONALDATA_NAME))
						.setText("Schulze");
				((TextDataField) person
						.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME))
						.setText("Marco");
				person.deflate();

				LegalEntity customer = new LegalEntity(getOrganisationID(),
						ObjectIDUtil.makeValidIDString(
								null, true));
				customer.setPerson(person);
				customer.setDefaultCustomerGroup(trader
						.getDefaultCustomerGroupForKnownCustomer());
				pm.makePersistent(customer);

				Order order = trader.createOrder(trader.getMandator(), customer, null,
						previewParameterSetExtension.currency);
				trader.createOffer(user, order, null);
				// String lockKey = ObjectIDUtil.makeValidIDString(null, true);

				SegmentType segmentType = SegmentType.getDefaultSegmentType(pm);
				Segment segment = trader.createSegment(order, segmentType);
				SegmentID segmentID = (SegmentID) JDOHelper.getObjectId(segment);
				Collection<? extends Article> articles = createArticles(pm, segmentID, null,
						previewParameterSet.getVoucherTypeID(), 1);

				Article article = articles.iterator().next();
				Voucher voucher = (Voucher) article.getProduct();

				trader.acceptOfferImplicitely(article.getOffer());

				// Perform a pseudo delivery...
				// We cannot use the "real" delivery process, because it uses
				// sub-transactions and we
				// want to rollback everything.

				// first the DeliveryNote
				DeliveryNote deliveryNote = store.createDeliveryNote(user, articles,
						String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				store.validateDeliveryNote(deliveryNote);
				store.finalizeDeliveryNote(user, deliveryNote);

				// now the delivery itself
				Delivery delivery = new Delivery(IDGenerator.getOrganisationID(), IDGenerator.nextID(Delivery.class));
				delivery.setDeliveryDirection(Delivery.DELIVERY_DIRECTION_OUTGOING);

				delivery.setPartner(
						trader.getMandator().equals(deliveryNote.getCustomer()) ?
								deliveryNote.getVendor() : deliveryNote.getCustomer());
				Set<ArticleID> articleIDs = new HashSet<ArticleID>(1);
				articleIDs.add((ArticleID) JDOHelper.getObjectId(article));
				delivery.setArticleIDs(articleIDs);
				ModeOfDeliveryFlavourID modeOfDeliveryFlavourID = ModeOfDeliveryFlavourID
						.create(Organisation.DEV_ORGANISATION_ID,
								"mailing.physical.default"); // TODO should be a constant!
				delivery.setModeOfDeliveryFlavourID(modeOfDeliveryFlavourID);
				delivery.setClientDeliveryProcessorFactoryID("dummy"); // should not
																																// matter
				ServerDeliveryProcessorID serverDeliveryProcessorID = (ServerDeliveryProcessorID) JDOHelper
						.getObjectId(ServerDeliveryProcessorManual
								.getServerDeliveryProcessorManual(pm));
				delivery.setServerDeliveryProcessorID(serverDeliveryProcessorID);
				DeliveryData deliveryData = new DeliveryData(delivery);
				deliveryData = pm.makePersistent(deliveryData);
				delivery.setDeliverBeginClientResult(new DeliveryResult(
						DeliveryResult.CODE_APPROVED_NO_EXTERNAL,
						null, null));
				store.deliverBegin(user, deliveryData);
				delivery.setDeliverDoWorkClientResult(new DeliveryResult(
						DeliveryResult.CODE_DELIVERED_NO_EXTERNAL,
						null, null));
				store.deliverDoWork(user, deliveryData);
				delivery.setDeliverEndClientResult(new DeliveryResult(
						DeliveryResult.CODE_COMMITTED_NO_EXTERNAL,
						null, null));
				store.deliverEnd(user, deliveryData);
				// ...pseudo delivery done

				Collection<ProductID> voucherIDs = new LinkedList<ProductID>();
				voucherIDs.add((ProductID) JDOHelper.getObjectId(voucher));
				Map<ProductID, Map<ScriptRegistryItemID, Object>> voucherScriptingResult = getVoucherScriptingResults(
						pm, voucherIDs, true);
				return voucherScriptingResult;
			} finally {
				sessionContext.setRollbackOnly();
				pm.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public LayoutMapForArticleIDSet getVoucherLayoutMapForArticleIDSet(
			Collection<ArticleID> articleIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LayoutMapForArticleIDSet res = new LayoutMapForArticleIDSet();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Article.class);
			for (ArticleID articleID : articleIDs) {
				Article article = (Article) pm.getObjectById(articleID);
				ProductID productID = (ProductID) JDOHelper.getObjectId(article.getProduct());
				res.getArticleID2ProductIDMap().put(articleID, productID);
				VoucherType voucherType = (VoucherType)article.getProduct().getProductType();
				if (voucherType.getVoucherLayout() == null)
					throw new IllegalStateException("voucherType.getVoucherLayout() == null! voucherType: " + voucherType.getPrimaryKey());

				res.getProductID2LayoutMap().put(productID, pm.detachCopy(voucherType.getVoucherLayout()));
			}
			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<VoucherLayout> getVoucherLayouts(Set<VoucherLayoutID> voucherLayoutIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return NLJDOHelper.getDetachedObjectList(pm, voucherLayoutIDs, VoucherLayout.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * This method checks, whether all specified <code>Article</code>s have a VoucherLayout assigned and thus can be printed.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type="Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public Map<ArticleID, PrintabilityStatus> getArticleID2PrintabilityStatusMap(Set<ArticleID> articleIDs)
//	{
//		if (articleIDs == null)
//			throw new IllegalArgumentException("articleIDs must not be null!");
//
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(Article.class);
//			Map<ArticleID, PrintabilityStatus> res = new HashMap<ArticleID, PrintabilityStatus>(articleIDs.size());
//
//			for (ArticleID articleID : articleIDs) {
//				Article article = (Article) pm.getObjectById(articleID);
//				if (!(article.getProductType() instanceof VoucherType))
//					res.put(articleID, PrintabilityStatus.NOT_A_VOUCHER_TYPE);
//				else {
//					VoucherType voucherType = (VoucherType) article.getProductType();
//					if (voucherType.getVoucherLayout() == null)
//						res.put(articleID, PrintabilityStatus.MISSING_VOUCHER_LAYOUT);
//					else
//						res.put(articleID, PrintabilityStatus.OK);
//				}
//			} // for (ArticleID articleID : articleIDs) {
//
//			return res;
//		} finally {
//			pm.close();
//		}
//	}
}
