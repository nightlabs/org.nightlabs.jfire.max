package org.nightlabs.jfire.trade;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.store.reverse.IReverseProductError;
import org.nightlabs.jfire.store.reverse.ReverseProductException;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.CustomerGroupMappingID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.trade.query.AbstractArticleContainerQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.version.MalformedVersionException;

public interface TradeManagerLocal {

	/**
	 * This method only creates a new <code>Order</code>, if there is no unlocked, empty
	 * quick-sale-order existing. If it can re-use a previously created <code>Order</code>,
	 * it is locked and its ID returned.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	OrderID createQuickSaleWorkOrder(AnchorID customerID, String orderIDPrefix,
			CurrencyID currencyID, SegmentTypeID[] segmentTypeIDs)
			throws ModuleException;

	/**
	 * Creates a new Purchase order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param vendorID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Order createPurchaseOrder(AnchorID vendorID, String orderIDPrefix,
			CurrencyID currencyID, SegmentTypeID[] segmentTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Creates a new Sale order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param customerID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Order createSaleOrder(AnchorID customerID, String orderIDPrefix,
			CurrencyID currencyID, SegmentTypeID[] segmentTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method is called, if the cross-trade-{@link Order} already exists (from a previous call to {@link #createCrossTradeOrder(String, String, CustomerGroupID, Set)}),
	 * but additional Segments are required.
	 *
	 * @param orderID The ID of the {@link Order} for which the new {@link Segment}s shall be created.
	 * @param segmentTypeIDs For each {@link SegmentTypeID} in this {@link Collection}, a new instance of {@link Segment} will be created and returned. If you pass
	 *		a {@link List} here with multiple references to the same {@link SegmentTypeID}, multiple instances of <code>Segment</code> will be created for this
	 *		same {@link SegmentType}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Collection<Segment> createCrossTradeSegments(OrderID orderID,
			Collection<SegmentTypeID> segmentTypeIDs);

	/**
	 * Creates a new order. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the customer for the new Order.
	 *
	 * @param customerGroupID Either <code>null</code> (then the default will be used) or an ID of a {@link CustomerGroup} which is allowed to the customer.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Order createCrossTradeOrder(String orderIDPrefix, String currencyID,
			CustomerGroupID customerGroupID,
			Collection<SegmentTypeID> segmentTypeIDs);

	/**
	 * Creates a new Offer within a given Order. This method is only usable, if the user (principal)
	 * is an organisation.
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer createCrossTradeOffer(OrderID orderID, String offerIDPrefix)
			throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer createCrossTradeReverseOffer(
			Collection<ArticleID> reversedArticleIDs, String offerIDPrefix)
			throws ModuleException;

	/**
	 * Creates a new Offer within a given Order.
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer createOffer(OrderID orderID, String offerIDPrefix,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @param orderID The orderID defining the Order for which to find all non-finalized offers.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	List<Offer> getNonFinalizedNonEndedOffers(OrderID orderID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "reversing"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * the specified {@link Offer} reversing all the specified <code>Article</code>s.
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 *		Note, that there are no special requirements for this <code>Offer</code> (it can either be created by
	 *		{@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} or by
	 *		{@link TradeManager#createReverseOffer(java.util.Collection, boolean, java.lang.String[])} (or other create-methods)).
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.reverseProductType"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.reverseProductType")
	Collection<Article> reverseArticles(OfferID offerID,
			Collection<ArticleID> reversedArticleIDs, boolean get,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "inversed"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * a newly created {@link Offer} reversing all the specified <code>Article</code>s.
	 * <p>
	 * This method is a shortcut for
	 * {@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} combined
	 * with
	 * {@link TradeManager#reverseArticle(org.nightlabs.jfire.trade.id.OfferID, org.nightlabs.jfire.trade.id.ArticleID, boolean, java.lang.String[])}
	 * or
	 * {@link TradeManager#reverseArticles(org.nightlabs.jfire.trade.id.OfferID, java.util.Collection, boolean, java.lang.String[])}.
	 * </p>
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer createReverseOffer(Collection<ArticleID> reversedArticleIDs,
			String offerIDPrefix, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws ModuleException;

	/**
	 * This method delegates to
	 * {@link LegalEntity#getAnonymousLegalEntity(PersistenceManager)}.
	 * <p>
	 * It's OK if everyone can read the anonymous business partner, because that's not confidential.
	 * Therefore, this method can be called by every authenticated user.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	LegalEntity getAnonymousLegalEntity(String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	/**
	 * This method delegates to
	 * {@link OrganisationLegalEntity#getOrganisationLegalEntity(PersistenceManager, String)}.
	 * <p>
	 * At the moment, this method can be called by every authenticated user. We believe that the revealed
	 * information is not so extremely confidential - the same
	 * information (address, VAT number, phone number and the like) can ususally be found on a web site, too.
	 * We might change this later, and filter some of the {@link Person}'s data blocks as well as maybe
	 * some information of the {@link LegalEntity}.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	OrganisationLegalEntity getOrganisationLegalEntity(String organisationID,
			boolean throwExceptionIfNotExistent, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * This method queries all <code>Order</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Order}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	List<OrderID> getOrderIDs(Class<? extends Order> orderClass,
			boolean subclasses, AnchorID vendorID, AnchorID customerID,
			AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	List<Order> getOrders(Set<OrderID> orderIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Stores the given Person to a LegalEntity. If no LegalEntity with the right
	 * AnchorID is found a new one will be created and made persistent.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException} on an
	 * attempt to change the person of an anonymous {@link LegalEntity}.
	 * </p>
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @param person The person to be set to the LegalEntity
	 * @param get If true the created LegalEntity will be returned else null
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	LegalEntity storePersonAsLegalEntity(Person person, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the {@link LegalEntity} for the given {@link Person} or <code>null</code> if none could be found.
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @param personID The object-id of the {@link Person} for which a {@link LegalEntity} is to be returned.
	 * @return the {@link LegalEntity} for the given {@link Person} or <code>null</code> if none could be found.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports"
	 */
	@RolesAllowed("_Guest_")
	LegalEntity getLegalEntityForPerson(PropertySetID personID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores the given LegalEntity.
	 * <p>
	 * Note that this method will throw an {@link IllegalArgumentException} on an
	 * attempt to change the person of an anonymous {@link LegalEntity}.
	 * </p>
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 * @param legalEntity The LegalEntity to be stored
	 * @param get Whether the stored instance or null should be returned.
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 * @return The stored LegalEntity or null
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	LegalEntity storeLegalEntity(LegalEntity legalEntity, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	Order getOrder(OrderID orderID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * <p>
	 * TODO https://www.jfire.org/modules/bugs/view.php?id=896
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Collection<LegalEntity> getLegalEntities(Set<AnchorID> anchorIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	Offer getOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	@SuppressWarnings("unchecked")
	List<Offer> getOffers(Set<OfferID> offerIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @param articleIDs Instances of {@link ArticleID}.
	 * @return Returns instances of {@link Article}
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders, org.nightlabs.jfire.trade.queryOffers, org.nightlabs.jfire.store.queryDeliveryNotes, org.nightlabs.jfire.accounting.queryInvoices"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed( { "org.nightlabs.jfire.trade.queryOrders",
			"org.nightlabs.jfire.trade.queryOffers",
			"org.nightlabs.jfire.store.queryDeliveryNotes",
			"org.nightlabs.jfire.accounting.queryInvoices" })
	Collection<Article> getArticles(Collection<ArticleID> articleIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Creates a new <tt>Segment</tt> within the given <tt>Order</tt>
	 * for the given <tt>SegmentType</tt>. Note, that you can create
	 * many <tt>Segment</tt>s with the same <tt>SegmentType</tt>.
	 *
	 * @param orderID The ID of the {@link Order} in which to create the new <tt>Segment</tt>.
	 * @param segmentTypeID The ID of the {@link SegmentType} of which a <tt>Segment</tt>
	 * "instance" will be created. This may be <tt>null</tt>. If undefined, the default
	 * segment type will be used.
	 * @param fetchGroups A <tt>String</tt> array defining what fields to detach.
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Segment createSegment(OrderID orderID, SegmentTypeID segmentTypeID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @return <code>null</code> if <code>get == false</code>, otherwise those {@link Article}s that were <b>not</b> yet removed, because
	 *		they are released asynchronously first.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 *
	 * @deprecated use {@link #deleteArticles(Collection, boolean, String[], int)} instead.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	@Deprecated
	Collection<Article> deleteArticles(Collection<ArticleID> articleIDs,
			boolean validate, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws ModuleException;

	/**
	 * @return <code>null</code> if <code>get == false</code>, otherwise those {@link Article}s that were <b>not</b> yet removed, because
	 *		they are released asynchronously first.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<Article> deleteArticles(Collection<ArticleID> articleIDs,
			boolean get, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<Article> releaseArticles(Collection<ArticleID> articleIDs,
			boolean synchronously, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws ModuleException;

	/**
	 *
	 * signal a given Jbpm transition to the offer
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	void signalOffer(OfferID offerID, String jbpmTransitionName);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws IOException, MalformedVersionException,
			TimePatternFormatException;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void releaseExpiredUnfinalizedOffers(TaskID taskID) throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void releaseExpiredFinalizedOffers(TaskID taskID);

	/**
	 * This method assigns a customer to an {@link Order}. This fails with
	 * an {@link IllegalStateException}, if the <code>Order</code> contains
	 * at least one finalized {@link Offer}.
	 *
	 * @param orderID The ID of the {@link Order} that shall be linked to another customer.
	 * @param customerID The ID of the {@link LegalEntity} which shall be the new customer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	Order assignCustomer(OrderID orderID, AnchorID customerID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer setOfferExpiry(OfferID offerID, Date expiryTimestampUnfinalized,
			boolean expiryTimestampUnfinalizedAutoManaged,
			Date expiryTimestampFinalized,
			boolean expiryTimestampFinalizedAutoManaged, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the object-ids of all mappings between customer-groups. These mappings are necessary for cross-organisation-trade.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any confidential data.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	Set<CustomerGroupMappingID> getCustomerGroupMappingIDs();

	/**
	 * Get the mappings between customer-groups specified by their object-ids.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any highly confidential data.
	 * We will add filtering of CustomerGroups later, though, because this information is slightly confidential.
	 * </p>
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	Collection<CustomerGroupMapping> getCustomerGroupMappings(
			Collection<CustomerGroupMappingID> customerGroupMappingIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editCustomerGroupMapping"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editCustomerGroupMapping")
	CustomerGroupMapping createCustomerGroupMapping(
			CustomerGroupID localCustomerGroupID,
			CustomerGroupID partnerCustomerGroupID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the object-ids of all customer-groups matching a certain <code>organisationID</code> or all, if
	 * the specified <code>organisationID</code> is <code>null</code>.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any confidential data.
	 * </p>
	 *
	 * @param organisationID <code>null</code> in order to get all customerGroups (no filtering). non-<code>null</code> to filter by <code>organisationID</code>.
	 * @param inverse This applies only if <code>organisationID != null</code>. If <code>true</code>, it will return all {@link CustomerGroupID}s where the <code>organisationID</code>
	 *		is NOT the one passed as parameter <code>organisationID</code>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	Set<CustomerGroupID> getCustomerGroupIDs(String organisationID,
			boolean inverse);

	/**
	 * Get customer-groups specified by their object-ids.
	 * <p>
	 * This method can be called by all authenticated users, because it does not reveal any highly confidential data.
	 * We will add filtering of CustomerGroups later, though, because this information is slightly confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	Collection<CustomerGroup> getCustomerGroups(
			Collection<CustomerGroupID> customerGroupIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editCustomerGroup"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editCustomerGroup")
	CustomerGroup storeCustomerGroup(CustomerGroup customerGroup, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName);

	/**
	 * @param queries the QueryCollection containing all queries that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders, org.nightlabs.jfire.trade.queryOffers, org.nightlabs.jfire.store.queryDeliveryNotes, org.nightlabs.jfire.accounting.queryInvoices"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed( { "org.nightlabs.jfire.trade.queryOrders",
			"org.nightlabs.jfire.trade.queryOffers",
			"org.nightlabs.jfire.store.queryDeliveryNotes",
			"org.nightlabs.jfire.accounting.queryInvoices" })
	@SuppressWarnings("unchecked")
	<R extends ArticleContainer> Set<ArticleContainerID> getArticleContainerIDs(
			QueryCollection<? extends AbstractArticleContainerQuery> queries);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	@SuppressWarnings("unchecked")
	Set<OfferID> getOfferIDs(QueryCollection<? extends AbstractJDOQuery> queries);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	@SuppressWarnings("unchecked")
	Set<OrderID> getOrderIDs(QueryCollection<? extends AbstractJDOQuery> queries);

	/**
	 * Assign a tariff to the specified articles.
	 *
	 * @param articleIDs the object-ids of the articles to be changed.
	 * @param tariffID the object-id of the tariff to be assigned.
	 * @param get <code>false</code> if no result is desired (this method will return <code>null</code>) or <code>true</code> to get the specified articles
	 * @param fetchGroups the fetch-groups in case the affected articles shall be detached and returned. This is ignored, if <code>get</code> is <code>false</code>.
	 * @param maxFetchDepth the maximum fetch-depth - ignored, if <code>get</code> is <code>false</code>.
	 * @return either <code>null</code>, if <code>get</code> is <code>false</code> or the articles identified by <code>articleIDs</code>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Collection<Article> assignTariff(Set<ArticleID> articleIDs,
			TariffID tariffID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void crossOrganisationRegistrationCallback(Context context)
			throws Exception;

	/**
	 * Returns the reversing {@link Offer} which defines the reverse for the {@link Product}
	 * of the given {@link ProductID}.
	 * If the Article where the {@link Product} with the given {@link ProductID} is referenced,
	 * can not be reversed, a {@link ReverseProductException} is thrown which contains {@link IReverseProductError}s
	 * which describe why not.
	 *
	 * @param productID the {@link ProductID} to get the reversing {@link Offer} for.
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	Offer createReverseOfferForProduct(ProductID productID,
			boolean completeOffer, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws ReverseProductException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<ProcessDefinitionID> getProcessDefinitionIDs(String statableClassName,
			TradeSide tradeSide);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	void createReservation(OrderID orderID, AnchorID customerID);

	/**
	 * Get all {@link ProductTypePermissionFlagSet}s assigned to the current user and the specified ProductTypeIDs.
	 *
	 * @param productTypeIDs the {@link ProductTypeID}s of those {@link ProductType}s for which to obtain {@link ProductTypePermissionFlagSet}s.
	 * @return the object-ids of the found {@link ProductTypePermissionFlagSet}s.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<ProductTypePermissionFlagSetID> getMyProductTypePermissionFlagSetIDs(
			Set<ProductTypeID> productTypeIDs);

	/**
	 * Get the {@link ProductTypePermissionFlagSet}s specified by their object-ids.
	 *
	 * @param productTypePermissionFlagSetIDs the object-ids of the {@link ProductTypePermissionFlagSet}s to retrieve.
	 * @param fetchGroups the JDO-fetch-groups to use for detaching.
	 * @param maxFetchDepth the maximum depth of the object-graphs to be detached.
	 * @return the {@link ProductTypePermissionFlagSet}s for the specified object-ids.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<ProductTypePermissionFlagSet> getProductTypePermissionFlagSets(
			Collection<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<EndCustomerReplicationPolicyID> getEndCustomerReplicationPolicyIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<EndCustomerReplicationPolicy> getEndCustomerReplicationPolicies(
			Collection<EndCustomerReplicationPolicyID> endCustomerReplicationPolicyIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeEndCustomer(LegalEntity endCustomer, Set<OrderID> assignOrderIDs);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

}