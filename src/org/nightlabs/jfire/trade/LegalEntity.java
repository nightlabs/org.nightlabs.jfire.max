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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.Accountant;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductReference;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.book.Storekeeper;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
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
 * @jdo.fetch-group name="LegalEntity.person" fields="person"
 * @jdo.fetch-group name="LegalEntity.this" fetch-groups="default" fields="person, accountant"
 */
public class LegalEntity extends Anchor
{
	private static final long serialVersionUID = 1L;

	public static final String ANCHOR_ID_ANONYMOUS = "LegalEntity-anonymous";

	public static final String FETCH_GROUP_ACCOUNTANT = "LegalEntity.accountant";
	public static final String FETCH_GROUP_STOREKEEPER = "LegalEntity.storekeeper";
	public static final String FETCH_GROUP_PERSON = "LegalEntity.person";
	public static final String FETCH_GROUP_THIS_LEGAL_ENTITY = "LegalEntity.this";

	public static final String ANCHOR_TYPE_ID_PARTNER = "Partner";

	public static final String PROPERY_SCOPE = StructLocal.DEFAULT_SCOPE;
	
	/**
	 * @param pm The <tt>PersistenceManager</tt> to use.
	 * @return the <tt>LegalEntity</tt> which represents the anonymous customer. If this
	 * <tt>LegalEntity</tt> does not yet exist, it will be created.
	 */
	public static LegalEntity getAnonymousCustomer(PersistenceManager pm)
	{
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		try {
			pm.getExtent(LegalEntity.class);
			LegalEntity anonymousCustomer = (LegalEntity) pm.getObjectById(AnchorID.create(organisationID, ANCHOR_TYPE_ID_PARTNER, ANCHOR_ID_ANONYMOUS));
			return anonymousCustomer;
		} catch (JDOObjectNotFoundException x) {
			// nothing if it's not existent - we'll create it afterwards
		}

		CustomerGroup anonymousCustomerGroup;
		try {
			pm.getExtent(CustomerGroup.class);
			anonymousCustomerGroup = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS));
		} catch (JDOObjectNotFoundException x) {
			anonymousCustomerGroup = new CustomerGroup(organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS);
			anonymousCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Anonym");
			anonymousCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Anonyme");
			anonymousCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Anonymous");
			pm.makePersistent(anonymousCustomerGroup);
		}

		// It's better to have a Person for EVERY LegalEntity
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		IStruct struct = StructLocal.getStructLocal(Person.class, PROPERY_SCOPE, pm);
		struct.explodeProperty(person);
		try {
			((TextDataField)person.getDataField(PersonStruct.PERSONALDATA_NAME)).setText("Anonymous");
			person.setDisplayName("Anonymous");
			person.setAutoGenerateDisplayName(true);
		} catch (Exception e) { // this should not happen, if there's not a programming error
			throw new RuntimeException(e);
		}
		struct.implodeProperty(person);

		LegalEntity anonymousCustomer = new LegalEntity(organisationID, ANCHOR_TYPE_ID_PARTNER, ANCHOR_ID_ANONYMOUS);
		anonymousCustomer.setPerson(person);
		anonymousCustomer.setAnonymous(true);
		anonymousCustomer.setDefaultCustomerGroup(anonymousCustomerGroup);
		anonymousCustomer = (LegalEntity) pm.makePersistent(anonymousCustomer);
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
	public LegalEntity(String organisationID, String anchorTypeID, String anchorID)
	{
		super(organisationID, anchorTypeID, anchorID);
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
	 * Manages {@link Map}s:
	 * key: String currencyID
	 * value: {@link Balance}
	 */
	private static ThreadLocal balancesTL = new ThreadLocal() {
		protected Object initialValue()
		{
			return new HashMap();
		}
	};

	/**
	 * Manages {@link Map}s:
	 * key: String productPK (see {@link Product#getPrimaryKey()})
	 * value: {@link ProductReference} productReference
	 */
	private static ThreadLocal productReferencesTL = new ThreadLocal() {
		protected Object initialValue()
		{
			return new HashMap();
		}
	};

	public void checkIntegrity(Collection<Transfer> containers)
	{
		Transfer firstContainer = (Transfer) containers.iterator().next();

		if (firstContainer instanceof MoneyTransfer) {
			Map balances = (Map) balancesTL.get();
			for (Iterator it = balances.values().iterator(); it.hasNext(); ) {
				Balance balance = (Balance) it.next();
				if (balance.amount != 0)
					throw new IllegalStateException("Balance for LegalEntity \""+getPrimaryKey()+"\" must be 0, but is "+balance.amount+" for currency \""+balance.currencyID+"\"!");
			}
		}
		else if (firstContainer instanceof ProductTransfer) {
			Map productReferences = (Map) productReferencesTL.get();
			for (Iterator it = productReferences.values().iterator(); it.hasNext(); ) {
				ProductReference productReference = (ProductReference) it.next();
				if (productReference.getQuantity() != 0)
					throw new IllegalStateException("LegalEntity \""+getPrimaryKey()+"\" has the ProductReference \""+productReference.getPrimaryKey()+"\" with quantity = " + productReference.getQuantity() + "! The quantity MUST be 0 at the end of a transaction!");
			}
		}
		else
			throw new IllegalArgumentException("I know only MoneyTransfer and ProductTransfer! Your container of type " + containers.getClass() + " cannot be processed!");
	}

	public void resetIntegrity(Collection<Transfer> containers)
	{
		Transfer firstContainer = (Transfer) containers.iterator().next();

		if (firstContainer instanceof MoneyTransfer) {
			Map balances = (Map) balancesTL.get();
			balances.clear();
		}
		else if (firstContainer instanceof ProductTransfer) {
			Map productReferences = (Map) productReferencesTL.get();
			productReferences.clear();
		}
		else
			throw new IllegalArgumentException("I know only MoneyTransfer and ProductTransfer! Your container of type " + containers.getClass() + " cannot be processed!");
	}

	protected void bookMoneyTransfer(MoneyTransfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		if (accountant == null)
			throw new NullPointerException("There is no accountant existing in this LegalEntity (\""+this.getPrimaryKey()+"\")!");

		Map balances = (Map) balancesTL.get();
		String currencyID = transfer.getCurrency().getCurrencyID();
		Balance balance = (Balance) balances.get(currencyID);
		if (balance == null) {
			balance = new Balance(currencyID);
			balances.put(currencyID, balance);
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

		Map balances = (Map) balancesTL.get();
//		synchronized (balancesMutex) {
			String currencyID = transfer.getCurrency().getCurrencyID();
			Balance balance = (Balance) balances.get(currencyID);
			if (balance == null) {
				balance = new Balance(currencyID);
				balances.put(currencyID, balance);
			}
			if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
				balance.amount += transfer.getAmount();
			else
				balance.amount -= transfer.getAmount();
//		}
	}

	protected ProductReference createProductReference(Map productReferences, Product product)
	{
		String productPK = product.getPrimaryKey();
		ProductReference productReference = (ProductReference) productReferences.get(productPK);
		if (productReference == null) {
			productReference = new ProductReference(this, product);
			productReferences.put(productPK, productReference);
		}
		return productReference;
	}

	protected void bookProductTransfer(ProductTransfer transfer, org.nightlabs.jfire.security.User user, Set<Anchor> involvedAnchors)
	{
		if (storekeeper == null)
			throw new NullPointerException("There is no storekeeper existing in this LegalEntity (\""+this.getPrimaryKey()+"\")!");

		Map productReferences = (Map) productReferencesTL.get();

		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		// after we delegated to the storekeeper, we handle the product transfer and update ProductReference.quantity
		for (Iterator it = transfer.getProducts().iterator(); it.hasNext(); ) {
			Product product = (Product) it.next();
			if (thisIsFrom)
				createProductReference(productReferences, product).decQuantity();
			else if (thisIsTo)
				createProductReference(productReferences, product).incQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");
		}

		storekeeper.bookTransfer(user, this, transfer, involvedAnchors);
	}

	protected void rollbackProductTransfer(ProductTransfer transfer, User user,
			Set<Anchor> involvedAnchors)
	{
		Map productReferences = (Map) productReferencesTL.get();

		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		for (Iterator it = transfer.getProducts().iterator(); it.hasNext(); ) {
			Product product = (Product) it.next();
			if (thisIsFrom)
				createProductReference(productReferences, product).incQuantity();
			else if (thisIsTo)
				createProductReference(productReferences, product).decQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");
		}
	}

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

	protected void internalRollbackTransfer(Transfer transfer, User user,
			Set<Anchor> involvedAnchors)
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
	 * @see #getAnonymousCustomer(PersistenceManager)
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
	public Collection getCustomerGroups()
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
}
