package org.nightlabs.jfire.trade.recurring;

import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.ProductTypeActionHandlerNotFoundException;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Segment;

import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Element;
import org.nightlabs.jfire.trade.recurring.id.RecurringTradeProductTypeActionHandlerID;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * {@link RecurringTradeProductTypeActionHandler}s are used by the {@link RecurringTrader}
 * to create {@link Article}s for {@link RecurredOffer}s on the basis of Articles in a {@link RecurringOffer}.
 * <p>
 * {@link RecurringTradeProductTypeActionHandler} are stored with and resolved by the class-name 
 * of the {@link ProductType} they can handle. It will then be used only for article with 
 * {@link ProductType}s of this class (or superclasses if no other could be found). 
 * </p>
 * 
 * @author Fitas Amine- fitas at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.recurring.id.RecurringTradeProductTypeActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_RecurringTradeProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, recurringTradeProductTypeActionHandlerID"
 *
 * @jdo.query name="getRecurringTradeProductTypeActionHandlerByProductTypeClassName" query="
 *		SELECT UNIQUE
 *		WHERE this.productTypeClassName == :productTypeClassName
 *		import java.lang.String"
 *
 */
@PersistenceCapable(
	objectIdClass=RecurringTradeProductTypeActionHandlerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_RecurringTradeProductTypeActionHandler")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getRecurringTradeProductTypeActionHandlerByProductTypeClassName",
		value=" SELECT UNIQUE WHERE this.productTypeClassName == :productTypeClassName import java.lang.String")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class RecurringTradeProductTypeActionHandler {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;


	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String recurringTradeProductTypeActionHandlerID;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true" null-value="exception"
	 */
	@Element(unique="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String productTypeClassName;



	public String getOrganisationID() {
		return organisationID;
	}


	public String getRecurringTradeProductTypeActionHandlerID() {
		return recurringTradeProductTypeActionHandlerID;
	}


	public String getProductTypeClassName() {
		return productTypeClassName;
	}


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringTradeProductTypeActionHandler() { }	

	/**	
	 * Implement this method to create a new {@link Article} in the given {@link RecurredOffer} for 
	 * each template article passed as parameter. The parameter {@link Article} come from a 
	 * currently processed {@link RecurringOffer}).
	 *
	 * @param offer The {@link RecurredOffer} created upon the processing of a {@link RecurringOffer}. 
	 *              The new Articles should be created for this {@link RecurredOffer}.
	 * @param recurringArticles A collection of the {@link Article}s in the {@link RecurringOffer} this handler
	 *                          should create new {@link Article}s in the RecurredOffer.
	 * @param segment The segment in the offer the new Articles should be placed in.
	 *
	 * @return return a Map where the template article is mapped to the newly created article.
	 */
	public abstract Map<Article, Article> createArticles(RecurredOffer offer, Set<Article> recurringArticles, Segment segment);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * @param organisationID First part of primary key: The identifier of that organisation which defined this handler.
	 *		Use {@link Organisation#DEV_ORGANISATION_ID} if you contribute directly to a JFire project and your own
	 *		organisation's unique identifier (i.e. your domain), if you write an own project.
	 * @param productTypeActionHandlerID The ID within the scope of the <code>organisationID</code>
	 * @param productTypeClass The class for which this handler shall be responsible. It will apply to all
	 *		inherited classes as well, except if there is another handler registered for the extended type.
	 */
	public RecurringTradeProductTypeActionHandler(
			String organisationID, String recurringProductTypeActionHandlerID,
			Class<? extends ProductType> productTypeClass)
	{

		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(organisationID, "productTypeActionHandlerID");

		if (productTypeClass == null)
			throw new IllegalArgumentException("productTypeClass must not be null!");

		if (!productTypeClass.isInterface()) {
			if (!ProductType.class.isAssignableFrom(productTypeClass))
				throw new IllegalArgumentException("productTypeClass is a class, but does not extend " + ProductType.class.getName() + "!");
		}

		this.organisationID = organisationID;
		this.recurringTradeProductTypeActionHandlerID = recurringProductTypeActionHandlerID;
		this.productTypeClassName = productTypeClass.getName();

	}

	/**
	 * This method finds the right handler for the given class (which must extend {@link ProductType}).
	 * Therefore, the method traverses the inheritance and searches for all parent classes and for
	 * all interfaces.
	 * <p>
	 * The search order is like this:
	 * <ul>
	 * <li>class</li>
	 * <li>interfaces in declaration order</li>
	 * <li>superclass</li>
	 * <li>interfaces of superclass in declaration order</li>
	 * <li>...and so on for all super-super-[...]-classes...</li>
	 * </ul>
	 * </p>
	 *
	 * @param pm The <code>PersistenceManager</code> to be used for accessing the datastore.
	 * @param productTypeClass The class (must be an inheritent of {@link ProductType}) for which to find a handler.
	 * @return Returns an instance of {@link ProductTypeActionHandler}. Never returns <code>null</code>.
	 * @throws ProductTypeActionHandlerNotFoundException If no handler is registered for the given class or one of its
	 */	
	public static RecurringTradeProductTypeActionHandler getRecurringTradeProductTypeActionHandler(PersistenceManager pm, Class<? extends ProductType> productTypeClass)
	{
		Class<?> searchClass = productTypeClass;
		Query q = pm.newNamedQuery(RecurringTradeProductTypeActionHandler.class, "getRecurringTradeProductTypeActionHandlerByProductTypeClassName");

		while (searchClass != null) {
			RecurringTradeProductTypeActionHandler res = (RecurringTradeProductTypeActionHandler) q.execute(searchClass.getName());
			if (res != null)
				return res;

			Class<?>[] interfaces = searchClass.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				Class<?> intf = interfaces[i];
				res = (RecurringTradeProductTypeActionHandler) q.execute(intf.getName());
				if (res != null)
					return res;
			}

			searchClass = searchClass.getSuperclass();
		}

		throw new ProductTypeActionHandlerNotFoundException(productTypeClass, "There is no handler registered for " + productTypeClass.getName());
	}

}
