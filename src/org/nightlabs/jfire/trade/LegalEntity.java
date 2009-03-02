/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.Accountant;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductReference;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.book.Storekeeper;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * A JFire organisation manages all its business partners as instances of {@link LegalEntity}.
 * This includes local customers and vendors/suppliers that are not other JFire organisations.
 * These {@link LegalEntity}s will have the id of the local organisation as {@link #getOrganisationID()},
 * {@link LegalEntity#ANCHOR_TYPE_ID_LEGAL_ENTITY} as {@link #getAnchorTypeID()} and
 * a unique {@link #getAnchorID()}.
 * <p>
 * Note that other JFire organisations are represented as instances of {@link OrganisationLegalEntity}
 * and will have their organisationID as {@link OrganisationLegalEntity#getOrganisation()}. This also
 * includes the local organisation.
 * </p>
 *
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Anchor"
 *		detachable="true"
 *		table="JFireTrade_LegalEntity"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="LegalEntity.accountant" fields="accountant"
 * @jdo.fetch-group name="LegalEntity.storekeeper" fields="storekeeper"
 * @jdo.fetch-group name="LegalEntity.person" fields="person[-1]"
 * @jdo.fetch-group name="LegalEntity.customerGroups" fields="customerGroups"
 * @jdo.fetch-group name="LegalEntity.defaultCustomerGroup" fields="defaultCustomerGroup"
 * @jdo.fetch-group name="LegalEntity.this" fetch-groups="default" fields="person, accountant"
 *
 * @jdo.query name="getLegalEntityForPerson" query="SELECT UNIQUE WHERE this.person == :person"
 */
public class LegalEntity extends Anchor
{
	private static final long serialVersionUID = 1L;

	public static final String ANCHOR_ID_ANONYMOUS = "LegalEntity-anonymous";

	public static final String FETCH_GROUP_ACCOUNTANT = "LegalEntity.accountant";
	public static final String FETCH_GROUP_STOREKEEPER = "LegalEntity.storekeeper";
	public static final String FETCH_GROUP_PERSON = "LegalEntity.person";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_LEGAL_ENTITY = "LegalEntity.this";
	public static final String FETCH_GROUP_CUSTOMER_GROUPS = "LegalEntity.customerGroups";
	public static final String FETCH_GROUP_DEFAULT_CUSTOMER_GROUP = "LegalEntity.defaultCustomerGroup";

	public static final String ANCHOR_TYPE_ID_LEGAL_ENTITY = "LegalEntity";

	public static LegalEntity getLegalEntityForPerson(PersistenceManager pm, PropertySetID personID)
	{
		Person person = (Person) pm.getObjectById(personID);
		return getLegalEntity(pm, person);
	}

	public static LegalEntity getLegalEntity(PersistenceManager pm, Person person)
	{
		Query q = pm.newNamedQuery(LegalEntity.class, "getLegalEntityForPerson");
		return (LegalEntity) q.execute(person);
	}

//	/**
//	 * When creating a new {@link LegalEntity} for a {@link Person}, you should use this method to
//	 * generate the ID. Note, however, that the LegalEntity doesn't necessarily uses this ID and
//	 * you must always search via {@link #getLegalEntity(PersistenceManager, Person)} or {@link #getLegalEntityForPerson(PersistenceManager, PropertySetID)}
//	 * first.
//	 *
//	 * @param personID
//	 * @return
//	 */
//	public static AnchorID createDefaultLegalEntityIDForPerson(PropertySetID personID)
//	{
//		return AnchorID.create(IDGenerator.getOrganisationID(), ANCHOR_TYPE_ID_LEGAL_ENTITY, personID.organisationID + '#' + ObjectIDUtil.longObjectIDFieldToString(personID.propertySetID));
//	}

	public static String nextAnchorID()
	{
		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Anchor.class, ANCHOR_TYPE_ID_LEGAL_ENTITY));
	}

	/**
	 * @param pm The <tt>PersistenceManager</tt> to use.
	 * @return the <tt>LegalEntity</tt> which represents the anonymous customer. If this
	 * <tt>LegalEntity</tt> does not yet exist, it will be created.
	 */
	public static LegalEntity getAnonymousLegalEntity(PersistenceManager pm)
	{
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		try {
			pm.getExtent(LegalEntity.class);
			LegalEntity anonymousCustomer = (LegalEntity) pm.getObjectById(AnchorID.create(organisationID, ANCHOR_TYPE_ID_LEGAL_ENTITY, ANCHOR_ID_ANONYMOUS));
			return anonymousCustomer;
		} catch (JDOObjectNotFoundException x) {
			// nothing if it's not existent - we'll create it afterwards
		}

		CustomerGroup anonymousCustomerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS));

		// It's better to have a Person for EVERY LegalEntity
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
//		IStruct struct = StructLocal.getStructLocal(Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, pm);
		IStruct struct = PersonStruct.getPersonStructLocal(pm);
		person.inflate(struct);
		try {
			// TODO: name should be multi language
			person.getDataField(PersonStruct.PERSONALDATA_NAME).setData("Anonymous");
			person.setDisplayName("Anonymous");
			person.setAutoGenerateDisplayName(true);
		} catch (Exception e) { // this should not happen, if there's not a programming error
			throw new RuntimeException(e);
		}
		person.deflate();

		LegalEntity anonymousCustomer = new LegalEntity(organisationID, ANCHOR_ID_ANONYMOUS);
		anonymousCustomer.setPerson(person);
		anonymousCustomer.setAnonymous(true);
		anonymousCustomer.setDefaultCustomerGroup(anonymousCustomerGroup);
		anonymousCustomer = pm.makePersistent(anonymousCustomer);
		return anonymousCustomer;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean anonymous = false;

	/**
	 * This is the default <tt>CustomerGroup</tt> which is used if the client
	 * doesn't specify another one. It may only specify one within {@link #customerGroups}.
	 * The <tt>defaultCustomerGroup</tt> must be registered in that <tt>Map</tt> as well,
	 * hence it's automatically added if missing.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CustomerGroup defaultCustomerGroup;

//	/**
//	 * This <tt>Map</tt> stores all {@link CustomerGroup}s as which this
//	 * <tt>LegalEntity</tt> might buy sth. It must include the
//	 * {@link #defaultCustomerGroup}, hence this is always added.
//	 * <p>
//	 * key: String customerGroupPK<br/>
//	 * value: CustomerGroup customerGroup
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="String"
//	 *		value-type="CustomerGroup"
//	 *		table="JFireTrade_LegalEntity_customerGroups"
//	 *
//	 * @jdo.join
//	 */
//	private Map customerGroups = new HashMap();

	/**
	 * This <tt>Set</tt> stores all {@link CustomerGroup}s as which this
	 * <tt>LegalEntity</tt> might buy sth. It must include the
	 * {@link #defaultCustomerGroup}, hence this is always added.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="CustomerGroup"
	 *		table="JFireTrade_LegalEntity_customerGroups"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set<CustomerGroup> customerGroups;

	public LegalEntity() { }
	/**
	 * @param organisationID
	 * @param anchorID
	 */
	public LegalEntity(String organisationID, String anchorID)
	{
		super(organisationID, ANCHOR_TYPE_ID_LEGAL_ENTITY, anchorID);
		this.customerGroups = new HashSet<CustomerGroup>();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Person person;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Accountant accountant;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Storekeeper storekeeper;

	protected static class Balance
	{
		public Balance(String currencyID) {
			this.currencyID = currencyID;
		}

		public String currencyID;
		public long amount = 0;
	}

//	/**
//	 * key: String currencyID
//	 * value: Balance balance
//	 *
//	 * @jdo.field persistence-modifier="transactional"
//	 */
//	private transient Map balances = new HashMap();

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	private transient Object balancesMutex = new Object();

	/**
	 * Manages the balance of a legal entity in the scope of an organisation.
	 */
	private static ThreadLocal<Map<String, Map<AnchorID, Map<CurrencyID,Balance>>>> balancesMapTL = new ThreadLocal<Map<String,Map<AnchorID,Map<CurrencyID,Balance>>>>() {
		@Override
		protected Map<String, Map<AnchorID, Map<CurrencyID,Balance>>> initialValue()
		{
			return new HashMap<String, Map<AnchorID, Map<CurrencyID,Balance>>>();
		}
	};

	private Map<CurrencyID,Balance> getBalanceMap()
	{
		Map<String, Map<AnchorID, Map<CurrencyID,Balance>>> organisationID2anchorID2balanceMap = balancesMapTL.get();
		String currentOrganisationID = IDGenerator.getOrganisationID();
		Map<AnchorID, Map<CurrencyID,Balance>> anchorID2balanceMap = organisationID2anchorID2balanceMap.get(currentOrganisationID);
		if (anchorID2balanceMap == null) {
			anchorID2balanceMap = new HashMap<AnchorID, Map<CurrencyID,Balance>>();
			organisationID2anchorID2balanceMap.put(currentOrganisationID, anchorID2balanceMap);
		}

		AnchorID anchorID = (AnchorID) JDOHelper.getObjectId(this);
		if (anchorID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(this) returned null!");

		Map<CurrencyID,Balance> balanceMap = anchorID2balanceMap.get(anchorID);
		if (balanceMap == null) {
			balanceMap = new HashMap<CurrencyID, Balance>();
			anchorID2balanceMap.put(anchorID, balanceMap);
		}

		return balanceMap;
	}

	/**
	 * Manages {@link Map}s:
	 * key: String productPK (see {@link Product#getPrimaryKey()})
	 * value: {@link ProductReference} productReference
	 */
	private static ThreadLocal<Map<String, Map<AnchorID, Map<ProductID, ProductReference>>>> productReferencesMapTL = new ThreadLocal<Map<String,Map<AnchorID,Map<ProductID,ProductReference>>>>() {
		@Override
		protected Map<String, Map<AnchorID, Map<ProductID, ProductReference>>> initialValue()
		{
			return new HashMap<String, Map<AnchorID,Map<ProductID,ProductReference>>>();
		}
	};

	private Map<ProductID, ProductReference> getProductReferenceMap()
	{
		Map<String, Map<AnchorID, Map<ProductID, ProductReference>>> organisationID2anchorID2productReferenceMap = productReferencesMapTL.get();
		String currentOrganisationID = IDGenerator.getOrganisationID();
		Map<AnchorID, Map<ProductID, ProductReference>> anchorID2productReferenceMap = organisationID2anchorID2productReferenceMap.get(currentOrganisationID);
		if (anchorID2productReferenceMap == null) {
			anchorID2productReferenceMap = new HashMap<AnchorID, Map<ProductID,ProductReference>>();
			organisationID2anchorID2productReferenceMap.put(currentOrganisationID, anchorID2productReferenceMap);
		}

		AnchorID anchorID = (AnchorID) JDOHelper.getObjectId(this);
		if (anchorID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(this) returned null!");

		Map<ProductID, ProductReference> productReferenceMap = anchorID2productReferenceMap.get(anchorID);
		if (productReferenceMap == null) {
			productReferenceMap = new HashMap<ProductID, ProductReference>();
			anchorID2productReferenceMap.put(anchorID, productReferenceMap);
		}

		return productReferenceMap;
	}

	@Override
	public void checkIntegrity(Collection<? extends Transfer> containers)
	{
		Transfer firstContainer = containers.iterator().next();

		if (firstContainer instanceof MoneyTransfer) {
			Map<CurrencyID,Balance> balanceMap = getBalanceMap();
			for (Balance balance : balanceMap.values()) {
				if (balance.amount != 0)
					throw new IllegalStateException("Balance for LegalEntity \""+getPrimaryKey()+"\" must be 0, but is "+balance.amount+" for currency \""+balance.currencyID+"\"!");
			}
		}
		else if (firstContainer instanceof ProductTransfer) {
			Map<ProductID, ProductReference> productReferenceMap = getProductReferenceMap();
			for (ProductReference productReference : productReferenceMap.values()) {
				if (productReference.getQuantity() != 0)
					throw new IllegalStateException("LegalEntity \""+getPrimaryKey()+"\" has the ProductReference \""+productReference.getPrimaryKey()+"\" with quantity = " + productReference.getQuantity() + "! The quantity MUST be 0 at the end of a transaction!");
			}
		}
		else
			throw new IllegalArgumentException("I know only MoneyTransfer and ProductTransfer! Your container of type " + containers.getClass() + " cannot be processed!");
	}

	@Override
	public void resetIntegrity(Collection<? extends Transfer> containers)
	{
		Transfer firstContainer = containers.iterator().next();

		if (firstContainer instanceof MoneyTransfer) {
			Map<CurrencyID,Balance> balanceMap = getBalanceMap();
			balanceMap.clear();
		}
		else if (firstContainer instanceof ProductTransfer) {
			Map<ProductID, ProductReference> productReferenceMap = getProductReferenceMap();
			productReferenceMap.clear();
		}
		else
			throw new IllegalArgumentException("I know only MoneyTransfer and ProductTransfer! Your container of type " + containers.getClass() + " cannot be processed!");
	}

	/**
	 * The bookMoneyTransfer method of a {@link LegalEntity} adjusts the balance-map (balance per Currency)
	 * and more importantly delegates to the {@link Account} set for the {@link LegalEntity}.
	 * It gives the {@link Accountant} the possibility to perform further actions.
	 *
	 * @see Accountant#bookTransfer(User, LegalEntity, MoneyTransfer, Set)
	 *
	 * @param transfer The transfer to book.
	 * @param user The user that initiated the transfer.
	 * @param involvedAnchors All {@link Anchor}s involved in the booking process.
	 */
	protected void bookMoneyTransfer(MoneyTransfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		if (accountant == null)
			throw new NullPointerException("There is no accountant existing in this LegalEntity (\""+this.getPrimaryKey()+"\")!");

		Map<CurrencyID,Balance> balanceMap = getBalanceMap();
		CurrencyID currencyID = (CurrencyID) JDOHelper.getObjectId(transfer.getCurrency());
		Balance balance = balanceMap.get(currencyID);
		if (balance == null) {
			balance = new Balance(currencyID.currencyID);
			balanceMap.put(currencyID, balance);
		}

		if (isTransferFrom(transfer)) {
			transfer.setFromBalanceBeforeTransfer(balance.amount);
		} else if (isTransferTo(transfer)) {
			transfer.setToBalanceBeforeTransfer(balance.amount);
		}

		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			balance.amount -= transfer.getAmount();
		else
			balance.amount += transfer.getAmount();

		accountant.bookTransfer(user, this, transfer, involvedAnchors);
	}

	protected void rollbackMoneyTransfer(MoneyTransfer transfer, User user,
			Set<Anchor> involvedAnchors)
	{
		if (accountant == null)
			throw new NullPointerException("There is no accountant existing in this LegalEntity (\""+this.getPrimaryKey()+"\")!");

		Map<CurrencyID,Balance> balanceMap = getBalanceMap();
		CurrencyID currencyID = (CurrencyID) JDOHelper.getObjectId(transfer.getCurrency());
		Balance balance = balanceMap.get(currencyID);
		if (balance == null) {
			balance = new Balance(currencyID.currencyID);
			balanceMap.put(currencyID, balance);
		}
		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			balance.amount += transfer.getAmount();
		else
			balance.amount -= transfer.getAmount();
	}

	protected ProductReference createProductReference(Map<ProductID, ProductReference> productReferenceMap, Product product)
	{
		ProductID productID = (ProductID) JDOHelper.getObjectId(product);
		if (productID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(product) returned null!");

		ProductReference productReference = productReferenceMap.get(productID);
		if (productReference == null) {
			productReference = new ProductReference(this, product);
			productReferenceMap.put(productID, productReference);
		}
		return productReference;
	}

	protected void bookProductTransfer(ProductTransfer transfer, org.nightlabs.jfire.security.User user, Set<Anchor> involvedAnchors)
	{
		if (storekeeper == null)
			throw new NullPointerException("There is no storekeeper existing in this LegalEntity (\""+this.getPrimaryKey()+"\")!");

		Map<ProductID, ProductReference> productReferenceMap = getProductReferenceMap();

		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		// after we delegated to the storekeeper, we handle the product transfer and update ProductReference.quantity
		for (Product product : transfer.getProducts()) {
			if (thisIsFrom)
				createProductReference(productReferenceMap, product).decQuantity();
			else if (thisIsTo)
				createProductReference(productReferenceMap, product).incQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");
		}

		storekeeper.bookTransfer(user, this, transfer, involvedAnchors);
	}

	protected void rollbackProductTransfer(ProductTransfer transfer, User user,
			Set<Anchor> involvedAnchors)
	{
		Map<ProductID, ProductReference> productReferenceMap = getProductReferenceMap();

		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		for (Product product : transfer.getProducts()) {
			if (thisIsFrom)
				createProductReference(productReferenceMap, product).incQuantity();
			else if (thisIsTo)
				createProductReference(productReferenceMap, product).decQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");
		}
	}

	@Override
	protected void internalBookTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of LegalEntity is not linked to a PersistenceManager! Cannot call this method while being detached.");

		if (transfer instanceof MoneyTransfer) {
			bookMoneyTransfer((MoneyTransfer)transfer, user, involvedAnchors);
		}
		else if (transfer instanceof ProductTransfer) {
			bookProductTransfer((ProductTransfer)transfer, user, involvedAnchors);
		}
		else
			throw new IllegalArgumentException("unsupported Transfer type: "+transfer.getClass().getName());
	}

	@Override
	protected void internalRollbackTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors)
	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new NullPointerException("This instance of LegalEntity is not linked to a PersistenceManager! Cannot call this method while being detached.");

		if (transfer instanceof MoneyTransfer) {
			rollbackMoneyTransfer((MoneyTransfer)transfer, user, involvedAnchors);
		}
		else if (transfer instanceof ProductTransfer) {
			rollbackProductTransfer((ProductTransfer)transfer, user, involvedAnchors);
		}
		else
			throw new IllegalArgumentException("unsupported Transfer type: "+transfer.getClass().getName());
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @param anonymous Whether or not this <tt>LegalEntity</tt> is anonymous.
	 *
	 * @see #getAnonymousLegalEntity(PersistenceManager)
	 * @see #isAnonymous()
	 */
	protected void setAnonymous(boolean anonymousCustomer)
	{
		this.anonymous = anonymousCustomer;
	}

	/**
	 * Maybe we will support multiple anonymous customers (to group anonymous sales) later,
	 * hence a <tt>LegalEntity</tt> which represents an anonymous customer is tagged and
	 * returns <tt>true</tt>.
	 *
	 * @return <tt>true</tt> if this <tt>LegalEntity</tt> represents the (or one of the)
	 * anonymous customer(s).
	 */
	public boolean isAnonymous()
	{
		return anonymous;
	}
	/**
	 * @return Returns the accountant.
	 */
	public Accountant getAccountant()
	{
		return accountant;
	}
	/**
	 * @param accountant The accountant to set.
	 */
	public void setAccountant(Accountant accountant)
	{
		this.accountant = accountant;
	}

	public Storekeeper getStorekeeper()
	{
		return storekeeper;
	}
	public void setStorekeeper(Storekeeper storekeeper)
	{
		this.storekeeper = storekeeper;
	}

	/**
	 * @return Returns the defaultCustomerGroup.
	 */
	public CustomerGroup getDefaultCustomerGroup()
	{
		return defaultCustomerGroup;
	}
	/**
	 * This method sets the default <tt>CustomerGroup</tt> for this <tt>LegalEntity</tt>.
	 * In case, it is not yet in the <tt>Map</tt> {@link #customerGroups}, it will be
	 * added.
	 *
	 * @param defaultCustomerGroup The new defaultCustomerGroup.
	 */
	public void setDefaultCustomerGroup(CustomerGroup defaultCustomerGroup)
	{
		addCustomerGroup(defaultCustomerGroup);
		this.defaultCustomerGroup = defaultCustomerGroup;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<CustomerGroup> unmodifiableCustomerGroups = null;

	/**
	 * @return Returns the {@link CustomerGroup}s as which this <tt>LegalEntity</tt>
	 *		is allowed to buy sth.
	 */
	public Collection<CustomerGroup> getCustomerGroups()
	{
		if (unmodifiableCustomerGroups == null)
			unmodifiableCustomerGroups = Collections.unmodifiableSet(customerGroups);

		return unmodifiableCustomerGroups;
	}

	public void addCustomerGroup(CustomerGroup customerGroup)
	{
		if (!customerGroups.contains(customerGroup))
			customerGroups.add(customerGroup);
	}
	public void removeCustomerGroup(CustomerGroup customerGroup)
	{
		customerGroups.remove(customerGroup);
	}
	public boolean containsCustomerGroup(CustomerGroup customerGroup)
	{
		return customerGroups.contains(customerGroup);
	}

	@Override
	protected String internalGetDescription() {
		return getPerson().getDisplayName();
	}
}
