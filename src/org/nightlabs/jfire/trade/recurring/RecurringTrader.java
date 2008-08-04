package org.nightlabs.jfire.trade.recurring;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
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


/**
 * RecurringTrader is responsible for purchase and sale of recurring orders and offers
 *
 * @author Fitas Amine <fitas[AT]nightlabs[DOT]de>
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
			// TODO remove this debug stuff
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(recurringTrader.getOrganisationID()))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but Trader.organisationID=" + recurringTrader.getOrganisationID());
			// TODO end debug
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
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
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
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
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
	 */
	public RecurredOffer createRecurredOffer(RecurringOffer recurringOffer) throws ModuleException
	{

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);

		Order order = trader.createOrder(recurringOffer.getVendor(),
				recurringOffer.getCustomer(), null, recurringOffer.getCurrency());

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		String offerIDPrefix = recurringOffer.getOfferIDPrefix();

		// create the new segment for the order
		for (Segment segment : recurringOffer.getSegments()) { 		
			trader.createSegment(order, segment.getSegmentType());
		}
		
		RecurredOffer recurredOffer = new RecurredOffer(
				user, order,
				offerIDPrefix, IDGenerator.nextID(RecurringOffer.class, offerIDPrefix));

		recurredOffer = getPersistenceManager().makePersistent(recurredOffer);
		trader.validateOffer(recurredOffer);


		// Loop through all the segments
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
		
		// put the Segments of the new RecurredOffer in a map (only fore easy lookup later)
		Map<SegmentType, Segment> recurredSegments = new HashMap<SegmentType, Segment>();
		for (Segment segment : recurredOffer.getSegments()) {
			recurredSegments.put(segment.getSegmentType(), segment);
		}
		
		// iterate the cummulated articles by segment type
		for (Map.Entry<SegmentType, Map<Class<? extends ProductType>, Set<Article>>> segmentEntry : segmentTypes2PTClass2Articles.entrySet()) {
			for (Map.Entry<Class<? extends ProductType>, Set<Article>> classEntry : segmentEntry.getValue().entrySet()) {
				// for each segment type
				// for each class of ProductType find the corresponding RecurringTradeProductTypeActionHandler
				RecurringTradeProductTypeActionHandler handler = 
					RecurringTradeProductTypeActionHandler.getRecurringTradeProductTypeActionHandler(pm, classEntry.getKey());
				if (handler == null)
					throw new IllegalStateException("Could not find a " + RecurringTradeProductTypeActionHandler.class.getName() + 
							" for the ProductType class " + classEntry.getKey());
				// and let the handler create the articles based on the Articles in the RecurringOffer
				handler.createArticles(recurredOffer, classEntry.getValue(), recurredSegments.get(segmentEntry.getKey()));
			}
			
		}

		return recurredOffer;
	}



	public RecurringOffer createRecurringOffer(User user, RecurringOrder recurringOrder, String offerIDPrefix) throws ModuleException
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
					offerIDPrefix, IDGenerator.nextID(RecurringOffer.class, offerIDPrefix));

			new OfferLocal(recurringOffer); // OfferLocal registers itself in Offer

			recurringOffer = getPersistenceManager().makePersistent(recurringOffer);
			trader.validateOffer(recurringOffer);

			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(RecurringOffer.class, tradeSide));
			processDefinitionAssignment.createProcessInstance(null, user, recurringOffer);

			return recurringOffer;
		}
	}


	public RecurringOrder createRecurringOrder(LegalEntity vendor,
			LegalEntity customer, String orderIDPrefix, Currency currency)
//	throws ModuleException
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
			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_CREATED,
					"created",
					"The Offer has been newly created. This is the first state in the Offer related workflow.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_ABORTED,
					"aborted",
					"The Offer has been aborted by the vendor (before finalization). A new Offer needs to be created in order to continue the interaction.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED,
					"finalized",
					"The Offer has been finalized. After that, it cannot be modified anymore. A modification would require revocation and recreation.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED,
					"accepted",
					"The Offer has been accepted by the customer. That turns the offer into a binding contract.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_REJECTED,
					"rejected",
					"The Offer has been rejected by the customer. A new Offer needs to be created in order to continue the interaction.",
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
