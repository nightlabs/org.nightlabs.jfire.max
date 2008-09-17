package org.nightlabs.jfire.trade.recurring;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.AccountingManagerLocal;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.jbpm.JbpmConstantsInvoice;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.config.TradeConfigModule;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.trade.recurring.jbpm.JbpmConstantsRecurringOffer;


/**
 * RecurringTrader is responsible for the purchase and the sale of the {@link RecurringOrder}  {@link RecurringOffer} , {@link RecurredOffer}
 * It manages orders, offers and delegates to the Trader and the Store and to the Accounting.
 *
 *
 * @author Fitas Amine <fitas[AT]nightlabs[DOT]de>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.recurring.id.RecurringTraderID"
 *		detachable="true"
 *		table="JFireTrade_RecurringTrader"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class RecurringTrader {

	private static final Logger logger = Logger.getLogger(RecurringTrader.class);

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;


	/**
	 * This method returns the singleton instance of {@link RecurringTrader}. If there is no
	 * instance of {@link RecurringTrader} in the datastore, yet, it will be created.
	 *
	 * @param pm The PersistenceManager to retrieve the {@link RecurringTrader} with.
	 * @return The {@link RecurringTrader} of the datastore of the given PersistenceManager.
	 */
	public static RecurringTrader getRecurringTrader(PersistenceManager pm)
	{
		Iterator<?> it = pm.getExtent(RecurringTrader.class).iterator();
		if (it.hasNext()) {
			RecurringTrader recurringTrader = (RecurringTrader) it.next();
			return recurringTrader;
		}

		RecurringTrader recurringTrader = new RecurringTrader();
		recurringTrader.organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		recurringTrader = pm.makePersistent(recurringTrader);

		return recurringTrader;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		String res = organisationID;
		if (res == null)
			SecurityReflector.getUserDescriptor().getOrganisationID();

		return res;
	}

	/**
	 * @return the {@link PersistenceManager} associated with this {@link RecurringTrader}
	 * will fail if the instance is not attached.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of Trader is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}


	public String getOrderIDPrefix(User user, String orderIDPrefix)
	{
		if (orderIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			orderIDPrefix = tradeConfigModule.getActiveIDPrefixCf(RecurringOrder.class.getName()).getDefaultIDPrefix();
		}
		return orderIDPrefix;
	}


	public String getOfferIDPrefix(User user, String orderIDPrefix)
	{
		if (orderIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			orderIDPrefix = tradeConfigModule.getActiveIDPrefixCf(RecurringOffer.class.getName()).getDefaultIDPrefix();
		}
		return orderIDPrefix;
	}


	/**
	 * This method creates a new {@link RecurredOffer} from an existing {@link RecurringOffer}
	 *
	 * @param recurringOffer the {@link RecurringOffer}
	 * @return newly created {@link RecurredOffer}
	 * @throws ModuleException If creating the recurred articles fails.
	 * @throws NamingException
	 * @throws CreateException
	 */
	public RecurredOffer processRecurringOffer(RecurringOffer recurringOffer) throws ModuleException, CreateException, NamingException
	{
		boolean priceDiffer = false;

		Authority organisationAuthority = Authority.getOrganisationAuthority(getPersistenceManager());
		// userID references the principal, i.e. the currently logged-in user.
		UserID userID = SecurityReflector.getUserDescriptor().getUserObjectID();

		// TODO set problem key instead of assert (which throws an exception and thus rolls the whole transaction back).
		organisationAuthority.assertContainsRoleRef(userID, org.nightlabs.jfire.trade.RoleConstants.editOrder);
		organisationAuthority.assertContainsRoleRef(userID, org.nightlabs.jfire.trade.RoleConstants.editOffer);

		String nodeName = recurringOffer.getState().getStateDefinition().getJbpmNodeName();
		if (!JbpmConstantsRecurringOffer.Vendor.NODE_NAME_RECURRENCE_STARTED.equals(nodeName)) {
			throw new IllegalStateException("The recurrence for RecurringOffer " + JDOHelper.getObjectId(recurringOffer) + " is not started, it is in the state '" + nodeName + "'.");
		}
		logger.debug("Starting creation of RecurredOffer (with new Order) for RecurringOffer: " + JDOHelper.getObjectId(recurringOffer));
		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);

		Order order = trader.createOrder(recurringOffer.getVendor(),
				recurringOffer.getCustomer(), null, recurringOffer.getCurrency());
		logger.debug("Created Order: " + JDOHelper.getObjectId(order));

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		String offerIDPrefix = recurringOffer.getOfferIDPrefix();

		// create the new segment for the order
		for (Segment segment : recurringOffer.getSegments()) {
			trader.createSegment(order, segment.getSegmentType());
		}

		RecurredOffer recurredOffer = createRecurredOffer(recurringOffer,user, order, offerIDPrefix);

		logger.debug("Created RecurredOffer: " + JDOHelper.getObjectId(recurredOffer));

		// Loop over all articles in the given offer and group
		// them by SegmentType and ProductType-class
		Map<SegmentType, Map<Class<? extends ProductType>, Set<Article>>> segmentTypes2PTClass2Articles =  new HashMap<SegmentType, Map<Class<? extends ProductType>, Set<Article>>>();

		for (Article recurringArticle : recurringOffer.getArticles()) {
			SegmentType segmentType = recurringArticle.getSegment().getSegmentType();
			Map<Class<? extends ProductType>, Set<Article>> ptClass2Articles = segmentTypes2PTClass2Articles.get(segmentType);
			if (ptClass2Articles == null) {
				ptClass2Articles = new HashMap<Class<? extends ProductType>, Set<Article>>();
				segmentTypes2PTClass2Articles.put(segmentType, ptClass2Articles);
			}
			Class<? extends ProductType> productTypeClass = recurringArticle.getProductType().getClass();
			Set<Article> articles = ptClass2Articles.get(productTypeClass);
			if (articles == null) {
				articles = new HashSet<Article>();
				ptClass2Articles.put(productTypeClass, articles);
			}
			articles.add(recurringArticle);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Grouped articles in RecurringOffer:");
			for (Map.Entry<SegmentType, Map<Class<? extends ProductType>, Set<Article>>> segmentTypeEntry : segmentTypes2PTClass2Articles.entrySet()) {
				logger.debug("  SegmentType: " + JDOHelper.getObjectId(segmentTypeEntry.getKey()));
				for (Map.Entry<Class<? extends ProductType>, Set<Article>> productTypeEntry : segmentTypeEntry.getValue().entrySet()) {
					logger.debug("    ProductType class: " + productTypeEntry.getKey());
					for (Article article : productTypeEntry.getValue()) {
						logger.debug("      Article: " + JDOHelper.getObjectId(article));
					}
				}
			}
		}

		// loop over the segments added to the order
		for (Segment segment : order.getSegments()) {
			logger.debug("Creating articles for RecurredOffer for SegmentType " + JDOHelper.getObjectId(segment.getSegmentType()));
			Map<Class<? extends ProductType>, Set<Article>> collected = segmentTypes2PTClass2Articles.get(segment.getSegmentType());
			if (collected != null) { // it is possible that there are segments with no articles in the RecurringOffer
				// add each segment to the RecurredOffer
				recurredOffer.addSegment(segment);
				for (Iterator< Class<? extends ProductType>> it = collected.keySet().iterator(); it.hasNext();)
				{
					// now for each ProductType class find the handler and let him create the articles
					Class<? extends ProductType> pt = it.next();
					logger.debug("  Creating articles for RecurredOffer for ProductType class " + pt);

					Set<Article> articles = collected.get(pt);

					RecurringTradeProductTypeActionHandler handler = RecurringTradeProductTypeActionHandler.getRecurringTradeProductTypeActionHandler(pm, pt);
					if (handler == null)
						throw new IllegalStateException("Could not find a " + RecurringTradeProductTypeActionHandler.class.getName() +
								" for the ProductType class " + pt);
					logger.debug("  Found handler " + handler.getClass().getName() + " ProductType class " + pt);

					Map<Article, Article> recurredArticles = handler.createArticles(recurredOffer, articles, segment);

					for (Map.Entry<Article, Article> articleEntry : recurredArticles.entrySet()) {
						//	Compare Prices to check if they the Differ
						if(!articleEntry.getValue().getPrice().equals(articleEntry.getKey().getPrice()))
							priceDiffer = true;

						if (logger.isDebugEnabled()) {
							if (!articleEntry.getValue().isAllocated()) {
								logger.debug("    An Article was created which was NOT allocated: " + JDOHelper.getObjectId(articleEntry.getValue()));
							} else {
								logger.debug("    An allocated Article was created: " + JDOHelper.getObjectId(articleEntry.getValue()));
							}

							logger.debug("  Finished creatingArticles");
						}

					}
				}
			}
		}
		// TODO: Check/Compare article prices from RecurringOffer and crated RecurredOffer
		/* Depending on the configuration of the RecurringOffer the following strageties should be supported:
		 * * Always use offered prices (Meaning that the process will enforce the
		 *   old prices for the newly created RecurredOffer)
		 * * Use offered prices but don't finalize (Meaning that the old prices will
		 *   be used, but the new offer will not be finalized, it would be best if this
		 *   could somehow notify the user then, but this for later)
		 * * Use current prices (Meaning that the process will always take the
		 *   current prices for the new offers, this is what currently happens)
		 * * Use current prices but don't finalize (Meaning that the new prices will
		 *   be used, but the new offer will not be finalized)
		 *   How to enforce old prices is not absolutely clear to me now, but I know
		 *   that it's possible somehow
		 *
		 * Alex
		 */
		// finished creating articles

		if(!priceDiffer)
		{
			// For now, as long as the different strategies are not present, we directly accept the offer.
			trader.acceptOfferImplicitely(recurredOffer);

			if(recurringOffer.getRecurringOfferConfiguration().isCreateInvoice()) {
				// If the configuration says so, automatically create an invoice
				logger.debug("Creating invoice for new RecurredOffer");

				// TODO set problem key instead of assert (which throws an exception and thus rolls the whole transaction back).
				organisationAuthority.assertContainsRoleRef(userID, org.nightlabs.jfire.accounting.RoleConstants.editInvoice);

				Accounting accounting = Accounting.getAccounting(pm);
				Invoice invoice = accounting.createInvoice(user, recurredOffer.getArticles(), null);

				logger.debug("Successfully created Invoice " + JDOHelper.getObjectId(invoice));
				accounting.validateInvoice(invoice);

				AccountingManagerLocal aml = AccountingManagerUtil.getLocalHome().create();

				if(recurringOffer.getRecurringOfferConfiguration().isBookInvoice())
					aml.signalInvoice((InvoiceID)JDOHelper.getObjectId(invoice), JbpmConstantsInvoice.Vendor.TRANSITION_NAME_BOOK_IMPLICITELY);

			}
		}
		else
			//Mark the Error
			recurringOffer.setStatusKey(RecurringOffer.STATUS_KEY_PRICES_NOT_EQUAL);

		// Increment the recurredOffer Counter
		recurringOffer.setRecurredOfferCount(recurringOffer.getRecurredOfferCount()+1);
		return recurredOffer;
	}


	/**
	 * Creates a new {@link RecurringOffer}.
	 * A {@link RecurringOffer} has a workflow differing from normal
	 * offers as it serves as a template for the creation of {@link RecurredOffer}s.
	 *
	 * @param user The user that created the Offer.
	 * @param recurringOrder The {@link RecurringOrder} the new {@link RecurringOffer} should be part of.
	 * @param offerIDPrefix The prefix for the id of the new offer. This might be <code>null</code>.
	 * @return The newly create {@link RecurringOffer}.
	 */
	public RecurringOffer createRecurringOffer(User user, RecurringOrder recurringOrder, String offerIDPrefix)
	{
		TradeSide tradeSide;

		LegalEntity vendor = recurringOrder.getVendor();
		if (vendor == null)
			throw new IllegalStateException("order.getVendor() returned null!");

		LegalEntity customer = recurringOrder.getCustomer();
		if (customer == null)
			throw new IllegalStateException("order.getCustomer() returned null!");

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);
		OrganisationLegalEntity mandator = trader.getMandator();

		if (mandator.equals(customer) && (vendor instanceof OrganisationLegalEntity)) {
			tradeSide = TradeSide.customerCrossOrganisation;
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			if (mandator.equals(vendor))
				tradeSide = TradeSide.vendor;
			else if (mandator.equals(customer))
				tradeSide = TradeSide.customerLocal;
			else
				throw new IllegalStateException("mandator is neither customer nor vendor! order=" + recurringOrder + " mandator=" + mandator);

			offerIDPrefix = getOfferIDPrefix(user, offerIDPrefix);

			RecurringOffer recurringOffer = new RecurringOffer(
					user, recurringOrder,
					offerIDPrefix, IDGenerator.nextID(Offer.class, offerIDPrefix));

			new OfferLocal(recurringOffer); // OfferLocal registers itself in Offer

			recurringOffer = getPersistenceManager().makePersistent(recurringOffer);
			trader.validateOffer(recurringOffer);

			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(RecurringOffer.class, tradeSide));
			processDefinitionAssignment.createProcessInstance(null, user, recurringOffer);

			return recurringOffer;
		}
	}

	/**
	 * Creates a new {@link RecurredOffer} for the given {@link RecurringOffer}.
	 *
	 * @param user The user that created the Offer.
	 * @param order The {@link Order} the new {@link RecurredOffer} should be part of.
	 * @param offerIDPrefix The prefix for the id of the new offer. This might be <code>null</code>.
	 * @return The newly created {@link RecurredOffer}·
	 */
	public RecurredOffer createRecurredOffer(RecurringOffer recurringOffer,User user, Order order, String offerIDPrefix)
	{
		TradeSide tradeSide;

		LegalEntity vendor = order.getVendor();
		if (vendor == null)
			throw new IllegalStateException("order.getVendor() returned null!");

		LegalEntity customer = order.getCustomer();
		if (customer == null)
			throw new IllegalStateException("order.getCustomer() returned null!");

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);
		OrganisationLegalEntity mandator = trader.getMandator();

		if (mandator.equals(customer) && (vendor instanceof OrganisationLegalEntity)) {
			tradeSide = TradeSide.customerCrossOrganisation;
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			if (mandator.equals(vendor))
				tradeSide = TradeSide.vendor;
			else if (mandator.equals(customer))
				tradeSide = TradeSide.customerLocal;
			else
				throw new IllegalStateException("mandator is neither customer nor vendor! order=" + order + " mandator=" + mandator);

			offerIDPrefix = getOfferIDPrefix(user, offerIDPrefix);

			RecurredOffer recurredOffer = new RecurredOffer(recurringOffer,
					user, order,
					offerIDPrefix, IDGenerator.nextID(Offer.class, offerIDPrefix));

			new OfferLocal(recurredOffer); // OfferLocal registers itself in Offer

			recurredOffer = getPersistenceManager().makePersistent(recurredOffer);
			trader.validateOffer(recurredOffer);

			// RecurredOffer has the same workflow definition as other offers,
			// thus we persist a workflow from the assignment to Offer.class
			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(Offer.class, tradeSide));
			processDefinitionAssignment.createProcessInstance(null, user, recurredOffer);

			return recurredOffer;
		}
	}


	public RecurringOrder createRecurringOrder(LegalEntity vendor,
			LegalEntity customer, String orderIDPrefix, Currency currency)
	{
		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);

		if (!trader.getMandator().equals(vendor) && (vendor instanceof OrganisationLegalEntity)) {
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			// local: the vendor is the local organisation (owning this datastore) OR it is a locally managed non-organisation-LE
			User user = SecurityReflector.getUserDescriptor().getUser(pm);

			orderIDPrefix = getOrderIDPrefix(user, orderIDPrefix);

			RecurringOrder recurringOrder = new RecurringOrder(
					vendor, customer,
					orderIDPrefix, IDGenerator.nextID(Order.class, orderIDPrefix),
					currency, user);

			recurringOrder = getPersistenceManager().makePersistent(recurringOrder);
			return recurringOrder;
		}
	}

	public RecurringOfferConfiguration storeRecurringOfferConfiguration(RecurringOfferConfiguration configuration, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		return NLJDOHelper.storeJDO(pm, configuration, get, fetchGroups, maxFetchDepth);
	}

	/**
	 * TODO: Copied from Trader and modified. All the Process initialization should be done from the definition files not from code.
	 */
	public ProcessDefinition storeProcessDefinitionRecurringOffer(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();

		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// The ActionHandlerNodeEnter is added for all nodes!
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);
		// All other handlers are configured in the process definition file

		// store the process definition
		ProcessDefinition processDefinition = ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		ProcessDefinitionID processDefinitionID = (ProcessDefinitionID) JDOHelper.getObjectId(processDefinition);

		// The stuff below should be done generically from the process definition!

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_SENT,
				"sent",
				"The Offer has been sent from the vendor to the customer.",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_REVOKED,
				"revoked",
				"The Offer has been revoked by the vendor. The result is the same as if the customer had rejected the offer. A new Offer needs to be created in order to continue the interaction.",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_EXPIRED,
				"expired",
				"The Offer has expired - the customer waited too long. A new Offer needs to be created in order to continue the interaction.",
				true);

		switch (tradeSide) {
		case vendor:
		{
			// give known StateDefinitions a name and a description
			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_CREATED,
					"created",
					"The Offer has been newly created. This is the first state in the Offer related workflow.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_ABORTED,
					"aborted",
					"The Offer has been aborted by the vendor (before finalization). A new Offer needs to be created in order to continue the interaction.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_FINALIZED,
					"finalized",
					"The Offer has been finalized. After that, it cannot be modified anymore. A modification would require revocation and recreation.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_ACCEPTED,
					"accepted",
					"The Offer has been accepted by the customer. That turns the offer into a binding contract.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_REJECTED,
					"rejected",
					"The Offer has been rejected by the customer. A new Offer needs to be created in order to continue the interaction.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsRecurringOffer.Vendor.NODE_NAME_RECURRENCE_STARTED,
					"recurrence started",
					"The timed creation of recurred offers has been started.",
					true);

			// give known Transitions a name
			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_IMPLICITELY)) {
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_CUSTOMER_ACCEPTED)) {
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_CUSTOMER_REJECTED)) {
				transition.setUserExecutable(false);
			}

		}
		break;
		default:
			throw new IllegalStateException("Unknown TradeSide: " + tradeSide);
		}

		return processDefinition;
	}


	/**
	 * TODO: Copied from Trader. All the Process initialization should be done from the definition files not from code.
	 */
	private static void setStateDefinitionProperties(
			ProcessDefinition processDefinition, String jbpmNodeName,
			String name, String description, boolean publicState)
	{
		StateDefinition stateDefinition;
		try {
			stateDefinition = StateDefinition.getStateDefinition(processDefinition, jbpmNodeName);
		} catch (JDOObjectNotFoundException x) {
			return;
		}
		stateDefinition.getName().setText(Locale.ENGLISH.getLanguage(), name);
		stateDefinition.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		stateDefinition.setPublicState(publicState);
	}

}
