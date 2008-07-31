package org.nightlabs.jfire.trade.recurring;

import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandlerNotFoundException;
import org.nightlabs.jfire.trade.Article;

/**
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
 */
public abstract class RecurringTradeProductTypeActionHandler {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;


	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String recurringTradeProductTypeActionHandlerID;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true" null-value="exception"
	 */
	private String recurringTradeproductTypeClassName;


	
	public String getOrganisationID() {
		return organisationID;
	}


	public String getRecurringTradeProductTypeActionHandlerID() {
		return recurringTradeProductTypeActionHandlerID;
	}


	public String getRecurringTradeproductTypeClassName() {
		return recurringTradeproductTypeClassName;
	}

		
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringTradeProductTypeActionHandler() { }	


	public abstract Map<Article, Article> createArticles(RecurredOffer offer, Set<Article> recurringArticles);


	public RecurringTradeProductTypeActionHandler(
			String organisationID, String recurringProductTypeActionHandlerID,
			Class<? extends ProductType> recurringProductTypeClass)
	{

		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(organisationID, "productTypeActionHandlerID");

		if (recurringProductTypeClass == null)
			throw new IllegalArgumentException("productTypeClass must not be null!");

		if (!recurringProductTypeClass.isInterface()) {
			if (!ProductType.class.isAssignableFrom(recurringProductTypeClass))
				throw new IllegalArgumentException("productTypeClass is a class, but does not extend " + ProductType.class.getName() + "!");
		}
		
		this.organisationID = organisationID;
		this.recurringTradeProductTypeActionHandlerID = recurringProductTypeActionHandlerID;
		this.recurringTradeproductTypeClassName = recurringProductTypeClass.getName();

	}


	public static RecurringTradeProductTypeActionHandler getRecurringTradeProductTypeActionHandler(PersistenceManager pm, Class<? extends ProductType> productTypeClass)
	{
		Class<?> searchClass = productTypeClass;
		
		
		while (searchClass != null) {
			RecurringTradeProductTypeActionHandler res = _getProductTypeActionHandler(pm, searchClass);
			if (res != null)
				return res;

			searchClass = searchClass.getSuperclass();
		}

		throw new ProductTypeActionHandlerNotFoundException(productTypeClass, "There is no handler registered for " + productTypeClass.getName());
	}


	private static RecurringTradeProductTypeActionHandler _getProductTypeActionHandler(PersistenceManager pm, Class<?> searchClass)
	{
		Query q = pm.newNamedQuery(RecurringTradeProductTypeActionHandler.class, "getProductTypeActionHandlerByProductTypeClassName");
		RecurringTradeProductTypeActionHandler res = (RecurringTradeProductTypeActionHandler) q.execute(searchClass.getName());
		if (res != null)
			return res;

		Class<?>[] interfaces = searchClass.getInterfaces();
		if (interfaces.length > 1) {
			for (int i = 0; i < interfaces.length; i++) {
				Class<?> intf = interfaces[i];
				res = (RecurringTradeProductTypeActionHandler) q.execute(intf.getName());
				if (res != null)
					return res;
			}
		}

		return null;
	}
	
}
