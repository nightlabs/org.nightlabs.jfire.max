/**
 * 
 */
package org.nightlabs.jfire.webshop;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * @author Khaled
 *
 * @jdo.persistence-capable
 *		identity-type="application"		
 *		detachable="true"
 *		objectid-class="org.nightlabs.jfire.webshop.id.WebCustomerID"
 *		table="JFireWebShopBase_WebCustomer"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, webCustomerID"
 */
public class WebCustomer
{
	/**
	 * @deprecated only for JDO
	 */
	protected WebCustomer() {}

	/**
	 * 
	 * @param organisationID the organisationID
	 * @param webCustomerID the webCustomerID
	 */
	public WebCustomer(String organisationID, String webCustomerID) {
		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(webCustomerID, "webCustomerID");

		this.organisationID = organisationID;
		this.webCustomerID = webCustomerID;
	}

	/**
	 * This is the organisationID to which the customer belongs.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * The webCustomerID of the webcustomer
	 * 
	 * @jdo.field primary-key="true"
	 * jdo.column length="100"
	 */
	private String webCustomerID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * jdo.column length="100"
	 */
	private String password;
	
	/**
	 * @jdo.field persistence-modifier="persistent" 
	 */
	private LegalEntity legalEntity = null;

	/**
	 * @return the legalEntity
	 */
	public LegalEntity getLegalEntity()
	{
		return legalEntity;
	}

	/**
	 * @param legalEntity the legalEntity to set
	 */
	public void setLegalEntity(LegalEntity legalEntity)
	{
		this.legalEntity = legalEntity;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the webCustomerID
	 */
	public String getWebCustomerID()
	{
		return webCustomerID;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	
}
