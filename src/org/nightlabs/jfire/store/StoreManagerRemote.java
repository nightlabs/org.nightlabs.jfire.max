package org.nightlabs.jfire.store;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.CheckRequirementsEnvironment;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryQueue;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroupCarrier;
import org.nightlabs.jfire.store.deliver.config.ModeOfDeliveryConfigModule;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.store.query.ProductTransferIDQuery;
import org.nightlabs.jfire.store.query.ProductTransferQuery;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.transfer.id.TransferID;

public interface StoreManagerRemote {

	/**
	 * Initialisation method called by the organisation-init framework to set up essential things for JFireTrade's store.
	 *
	 * @throws Exception While loading an icon from a local resource, an {@link IOException} might happen; in
	 *	{@link #initTimerTaskCalculateProductTypeAvailabilityPercentage(PersistenceManager)} timepattern-related
	 *	exceptions might occur; and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;

	/**
	 * Get the object-ids of all {@link Unit}s known to the organisation.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<UnitID> getUnitIDs();

	/**
	 * Get the {@link Unit}s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because {@link Unit}s are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	List<Unit> getUnits(Collection<UnitID> unitIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the {@link DeliveryNote}s for the specified object-ids.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	List<DeliveryNote> getDeliveryNotes(Set<DeliveryNoteID> deliveryNoteIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the {@link DeliveryNote}s' object-ids that match the criteria specified by the given queries.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	@SuppressWarnings("unchecked")
	Set<DeliveryNoteID> getDeliveryNoteIDs(
			QueryCollection<? extends AbstractJDOQuery> queries);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	List<ProductType> getProductTypes(Set<ProductTypeID> productTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @throws CannotPublishProductTypeException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed( { "org.nightlabs.jfire.store.editUnconfirmedProductType",
			"org.nightlabs.jfire.store.editConfirmedProductType" })
	ProductTypeStatusHistoryItem setProductTypeStatus_published(
			ProductTypeID productTypeID, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws CannotPublishProductTypeException;

	/**
	 * @throws CannotConfirmProductTypeException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed( { "org.nightlabs.jfire.store.editUnconfirmedProductType",
			"org.nightlabs.jfire.store.editConfirmedProductType" })
	ProductTypeStatusHistoryItem setProductTypeStatus_confirmed(
			ProductTypeID productTypeID, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws CannotConfirmProductTypeException;

	/**
	 * @throws CannotMakeProductTypeSaleableException
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editUnconfirmedProductType, org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed( { "org.nightlabs.jfire.store.editUnconfirmedProductType",
			"org.nightlabs.jfire.store.editConfirmedProductType" })
	ProductTypeStatusHistoryItem setProductTypeStatus_saleable(
			ProductTypeID productTypeID, boolean saleable, boolean get,
			String[] fetchGroups, int maxFetchDepth)
			throws CannotMakeProductTypeSaleableException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editConfirmedProductType"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editConfirmedProductType")
	ProductTypeStatusHistoryItem setProductTypeStatus_closed(
			ProductTypeID productTypeID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get mode of delivery flavours available to the given customer-group(s) and the specified product-types.
	 * <p>
	 * This method can be called by everyone, because mode of delivery flavours are not considered confidential.
	 * </p>
	 *
	 * @param productTypeIDs Instances of {@link ProductTypeID}.
	 * @param customerGroupIDs Instances of {@link org.nightlabs.jfire.trade.id.CustomerGroupID}.
	 * @param mergeMode One of {@link ModeOfDeliveryFlavour#MERGE_MODE_SUBTRACTIVE} or {@link ModeOfDeliveryFlavour#MERGE_MODE_ADDITIVE}
	 * @param filterByConfig
	 * 		If this is <code>true</code> the flavours available found for the given product-types and customer-groups will also be filtered by the
	 * 		intersection of the entries configured in the {@link ModeOfDeliveryConfigModule} for the current user and the
	 * 		workstation he is currently loggen on.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	ModeOfDeliveryFlavourProductTypeGroupCarrier getModeOfDeliveryFlavourProductTypeGroupCarrier(
			Collection<ProductTypeID> productTypeIDs,
			Collection<CustomerGroupID> customerGroupIDs, byte mergeMode,
			boolean filterByConfig, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the modes of delivery that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because modes of delivery are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<ModeOfDelivery> getModeOfDeliverys(
			Set<ModeOfDeliveryID> modeOfDeliveryIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the object-ids of all mode of delivery flavours.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<ModeOfDeliveryFlavourID> getAllModeOfDeliveryFlavourIDs();

	/**
	 * Get the object-ids of all modes of delivery.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Set<ModeOfDeliveryID> getAllModeOfDeliveryIDs();

	/**
	 * Get the mode of delivery flavours that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because mode of delivery flavours are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours(
			Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the server delivery processors that are available for the specified mode of delivery flavour.
	 * <p>
	 * This method can be called by everyone, because server delivery processors do not contain confidential information.
	 * </p>
	 *
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<ServerDeliveryProcessor> getServerDeliveryProcessorsForOneModeOfDeliveryFlavour(
			ModeOfDeliveryFlavourID modeOfDeliveryFlavourID,
			CheckRequirementsEnvironment checkRequirementsEnvironment,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Creates a <code>DeliveryNote</code> for all specified <code>Article</code>s. If
	 * get is true, a detached version of the new DeliveryNote will be returned.
	 *
	 * @param articleIDs The {@link ArticleID}s of those {@link Article}s that shall be added to the new <code>DeliveryNote</code>.
	 * @param get Whether a detached version of the created DeliveryNote should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the deliveryNote should be detached with.
	 * @return Detached DeliveryNote or null.
	 * @throws DeliveryNoteEditException
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	DeliveryNote createDeliveryNote(Collection<ArticleID> articleIDs,
			String deliveryNoteIDPrefix, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws DeliveryNoteEditException;

	/**
	 * Creates an DeliveryNote for all <tt>Article</tt>s the Offer identified by
	 * the given offerID. If get is true a detached version of the
	 * DeliveryNote will be returned.
	 *
	 * @param offerID OfferID of the offer to be delivered.
	 * @param get Whether a detached version of the created DeliveryNote should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the deliveryNote should be detached with.
	 * @return Detached DeliveryNote or null.
	 * @throws DeliveryNoteEditException
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	DeliveryNote createDeliveryNote(ArticleContainerID articleContainerID,
			String deliveryNoteIDPrefix, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws DeliveryNoteEditException;

	/**
	 * @throws DeliveryNoteEditException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	DeliveryNote addArticlesToDeliveryNote(DeliveryNoteID deliveryNoteID,
			Collection<ArticleID> articleIDs, boolean validate, boolean get,
			String[] fetchGroups, int maxFetchDepth)
			throws DeliveryNoteEditException;

	/**
	 * @throws DeliveryNoteEditException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	DeliveryNote removeArticlesFromDeliveryNote(DeliveryNoteID deliveryNoteID,
			Collection<ArticleID> articleIDs, boolean validate, boolean get,
			String[] fetchGroups, int maxFetchDepth)
			throws DeliveryNoteEditException;

	/**
	 * @param deliveryDataList A <tt>List</tt> of {@link DeliveryData}.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryData} objects passed in
	 *		<tt>deliveryDataList</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	List<DeliveryResult> deliverBegin(List<DeliveryData> deliveryDataList);

	/**
	 * Perform all delivery-steps in one single transaction. This is used for fast
	 * cross-organisation-deliveries (internally within the JFire network).
	 * @throws DeliveryException if the delivery fails. Note that this (like any other exception) causes the complete transaction
	 * 		to be rolled back and (in contrast to the usual multi-step-delivery) all traces in the database to be deleted.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	DeliveryResult[] deliverInSingleTransaction(DeliveryData deliveryData)
			throws DeliveryException;

	/**
	 * @param serverDeliveryProcessorID Might be <tt>null</tt>.
	 * @param deliveryDirection Either
	 *		{@link ServerDeliveryProcessor#DELIVERY_DIRECTION_INCOMING}
	 *		or {@link ServerDeliveryProcessor#DELIVERY_DIRECTION_OUTGOING}.
	 *
	 * @see Store#deliverBegin(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	DeliveryResult deliverBegin(DeliveryData deliveryData);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param deliveryIDs Instances of {@link DeliveryID}
	 * @param deliverEndClientResults Instances of {@link DeliveryResult} corresponding
	 *		to the <tt>deliveryIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all deliveries will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryID} objects passed in
	 *		<tt>deliveryIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<DeliveryResult> deliverEnd(List<DeliveryID> deliveryIDs,
			List<DeliveryResult> deliverEndClientResults, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param deliveryIDs Instances of {@link DeliveryID}
	 * @param deliverDoWorkClientResults Instances of {@link DeliveryResult} corresponding
	 *		to the <tt>deliveryIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all deliveries will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link DeliveryResult} in the same
	 *		order as and corresponding to the {@link DeliveryID} objects passed in
	 *		<tt>deliveryIDs</tt>.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<DeliveryResult> deliverDoWork(List<DeliveryID> deliveryIDs,
			List<DeliveryResult> deliverDoWorkClientResults,
			boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	DeliveryResult deliverDoWork(DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#deliverEnd(User, DeliveryData)
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	DeliveryResult deliverEnd(DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult, boolean forceRollback);

	/**
	 * This method queries all <code>DeliveryNote</code>s which exist between the given vendor and customer.
	 * They are ordered by deliveryNoteID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link DeliveryNote}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	List<DeliveryNoteID> getDeliveryNoteIDs(AnchorID vendorID,
			AnchorID customerID, AnchorID endCustomerID, long rangeBeginIdx,
			long rangeEndIdx);

	/**
	 * This method returns the delivery with the respective ID.
	 * <p>
	 * Because this method is (currently) only used when performing a delivery, it requires
	 * the role <code>org.nightlabs.jfire.store.deliver</code> to be granted.
	 * </p>
	 *
	 * @param deliveryID The ID of the delivery to be retrieved.
	 * @param fetchGroups The fetch groups to be used.
	 * @param maxFetchDepth The max fetch depth to be used.
	 * @return A detached copy of the delivery with the respective ID.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.deliver"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.deliver")
	Delivery getDelivery(DeliveryID deliveryID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer and
	 * are not yet finalized. They are ordered by invoiceID descending (means newest first).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryDeliveryNotes"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.queryDeliveryNotes")
	List<DeliveryNote> getNonFinalizedDeliveryNotes(AnchorID vendorID,
			AnchorID customerID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryNote"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryNote")
	void signalDeliveryNote(DeliveryNoteID deliveryNoteID,
			String jbpmTransitionName);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	@SuppressWarnings("unchecked")
	Set<ProductTypeID> getProductTypeIDs(
			QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	ProductTypeGroupIDSearchResult getProductTypeGroupIDSearchResultForProductTypeQueries(
			QueryCollection<? extends AbstractProductTypeQuery> productTypeQueries);

	/**
	 * Returns the {@link DeliveryQueue}s identified by the given IDs.
	 * <p>
	 * Because the delivery queues used (and even visible) to a user are configured in a config module,
	 * there is at the moment no authorization required for this method (though, authentication is
	 * necessary - i.e this method cannot be called anonymously). This may change later (we may implement
	 * {@link SecuredObject} in {@link DeliveryQueue}).
	 * </p>
	 *
	 * @param deliveryQueueIds The IDs of the DeliveryQueues to be returned.
	 * @param fetchGroups The fetch groups to be used to detach the DeliveryQueues
	 * @param fetchDepth The fetch depth to be used to detach the DeliveryQueues (-1 for unlimited)
	 * @return the {@link DeliveryQueue}s identified by the given IDs.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<DeliveryQueue> getDeliveryQueuesById(
			Set<DeliveryQueueID> deliveryQueueIds, String[] fetchGroups,
			int fetchDepth);

	/**
	 * Stores the given DeliveryQueues in the data store.
	 *
	 * @param deliveryQueues The {@link DeliveryQueue} to be stored
	 * @param get Indicates whether this method should return a collection containing the detached copies of the stored DeliveryQueues.
	 * @param fetchGroups The fetchGroups to be used when get == true
	 * @param fetchDepth The fetchDepth to be used when get == true
	 * @return A collection of the detached copies of the stored {@link DeliveryQueue}s
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editDeliveryQueue"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.editDeliveryQueue")
	Collection<DeliveryQueue> storeDeliveryQueues(
			Collection<DeliveryQueue> deliveryQueues, boolean get,
			String[] fetchGroups, int fetchDepth);

	/**
	 * Returns the {@link DeliveryQueueID}s of all {@link DeliveryQueue}s available.
	 * @return The {@link DeliveryQueueID}s of all {@link DeliveryQueue}s available.
	 *
	 * @param includeDefunct Sets whether defunct delivery queues should be included in the returned collection.
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<DeliveryQueueID> getAvailableDeliveryQueueIDs(
			boolean includeDefunct);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryRepositories"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryRepositories")
	@SuppressWarnings("unchecked")
	Set<AnchorID> getRepositoryIDs(
			QueryCollection<? extends AbstractJDOQuery> queries);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryRepositories"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryRepositories")
	List<Repository> getRepositories(Collection<AnchorID> repositoryIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.editRepository"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.editRepository")
	Repository storeRepository(Repository repository, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Unlike {@link #getProductTransferIDs(ProductTransferIDQuery)}, this method allows
	 * for cascading multiple queries. It is slower than {@link #getProductTransferIDs(ProductTransferIDQuery)}
	 * and should therefore only be used, if it's essentially necessary.
	 *
	 * @param productTransferQueries A <code>Collection</code> of {@link ProductTransferQuery}. They will be executed
	 *		in the given order (if it's a <code>List</code>) and the result of the previous query will be passed as candidates
	 *		to the next query.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryProductTransfers"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryProductTransfers")
	List<TransferID> getProductTransferIDs(
			QueryCollection<? extends ProductTransferQuery> productTransferQueries);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.queryProductTransfers"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.queryProductTransfers")
	List<ProductTransfer> getProductTransfers(
			Collection<TransferID> productTransferIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the object-ids of all {@link RepositoryType}s.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	Set<RepositoryTypeID> getRepositoryTypeIDs();

	/**
	 * Get the <code>RepositoryType</code>s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because the <code>RepositoryType</code>s are not confidential.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<RepositoryType> getRepositoryTypes(
			Collection<RepositoryTypeID> repositoryTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param productTypeGroupIDs Either <code>null</code> in order to return all or instances of {@link ProductTypeGroupID}
	 *		specifying a subset.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Collection<ProductTypeGroup> getProductTypeGroups(
			Collection<ProductTypeGroupID> productTypeGroupIDs,
			String[] fetchGroups, int maxFetchDepth);

	Set<ReceptionNoteID> getReceptionNoteIDs(QueryCollection<? extends AbstractJDOQuery> queries);

	List<ReceptionNote> getReceptionNotes(Set<ReceptionNoteID> receptionNoteIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Set<OfferID> getReservations(ProductTypeID productTypeID,
			String fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_System_"
//	 */
//	@RolesAllowed("_System_")
//	void calculateProductTypeAvailabilityPercentage(TaskID taskID)
//			throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	Set<ProductTypePermissionFlagSetID> getMyProductTypePermissionFlagSetIDs(
			Collection<? extends ProductTypeID> productTypeIDs);

	/**
	 * We allow this method to be executed by everyone, because it currently filters out (silently!) all {@link ProductTypePermissionFlagSet}s
	 * that have a differen user than the current principal. We might later extend this method to silently filter only then,
	 * when the current principal does not have the right to see access right configurations.
	 * <p>
	 * For security reasons, it is not possible to specify fetch-groups or max-fetch-depth. This method returns
	 * the {@link ProductTypePermissionFlagSet}s being detached with {@link FetchPlan#DEFAULT} only. Hence, the methods
	 * {@link ProductTypePermissionFlagSet#getProductType()} and {@link ProductTypePermissionFlagSet#getUser()} cannot be used
	 * (use {@link ProductTypePermissionFlagSet#getProductTypeObjectID()} and {@link ProductTypePermissionFlagSet#getUserObjectID()}
	 * instead!).
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	Collection<ProductTypePermissionFlagSet> getProductTypePermissionFlagSets(
			Collection<? extends ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="org.nightlabs.jfire.store.seeProductType"
	 */
	@RolesAllowed("org.nightlabs.jfire.store.seeProductType")
	Set<ProductTypeID> getChildProductTypeIDs(ProductTypeID parentProductTypeID);

}