/**
 * 
 */
package org.nightlabs.jfire.web.webshop;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * The WebCustomer assembles datas like username and password that are only used for the web
 * and a legalEntity within the core system.
 * 
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
 *
 * @jdo.fetch-group name="WebCustomer.legalEntity" fields="legalEntity"
 * @jdo.fetch-group name="WebCustomer.this" fields="legalEntity"
 * 
 * @jdo.query name="getWebCustomerWithRegexField" query="
 * 		SELECT
 * 			DISTINCT this
 * 		WHERE
 * 			this.legalEntity != null &&
 * 			this.legalEntity.person != null &&
 * 			this.legalEntity.person.dataFields.contains(regexField) &&
 * 			(
 * 				regexField.structBlockOrganisationID == :regexFieldStructBlockOrganisationID &&
 * 				regexField.structBlockID == :regexFieldStructBlockID &&
 * 				regexField.structFieldOrganisationID == :regexFieldStructFieldOrganisationID &&
 * 				regexField.structFieldID == :regexFieldStructFieldID &&
 * 				regexField.text.toLowerCase() == :regexFieldValue
 * 			)
 * 		VARIABLES org.nightlabs.jfire.prop.datafield.RegexDataField regexField
 * "
 */
public class WebCustomer
{
	
	public static final String FETCH_GROUP_LEGAL_ENTITY = "WebCustomer.legalEntity";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_WEB_CUSTOMER = "WebCustomer.this";
	
	/**
	 * @deprecated only for JDO
	 */
	@Deprecated
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
	 * The second password used and stored temporarly when the customer triggers
	 * the lostPassword procedure
	 * @jdo.field persistence-modifier="persistent"
	 * jdo.column length="100"
	 */
	private String secondPassword;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * jdo.column length="100"
	 */
	private Date secondPasswordDate ;
	
	/**
	 * The confirmation String that will be sent to the customer via E-mail
	 * @jdo.field persistence-modifier="persistent"
	 * jdo.column length="100"
	 */
	private String confirmationString ;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * jdo.column length="100"
	 */
	private Date confirmationStringDate ;
	
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

	/**
	 * @return the secondPassword
	 */
	public String getSecondPassword() {
		return secondPassword;
	}

	/**
	 * @param secondPassword  to set
	 */
	public void setSecondPassword(String secondPassword) {
			
		this.secondPassword = secondPassword;
	}

	public Date getSecondPasswordDate() {
		return secondPasswordDate;
	}

	public void setSecondPasswordDate(Date secondPasswordAge) {
		this.secondPasswordDate = secondPasswordAge;
	}

	public String getConfirmationString() {
		return confirmationString;
	}

	public void setConfirmationString(String confirmationString) {
		this.confirmationString = confirmationString;
	}

	public Date getConfirmationStringDate() {
		return confirmationStringDate;
	}

	public void setConfirmationStringDate(Date confirmationStringDate) {
		this.confirmationStringDate = confirmationStringDate;
	}
	

	public static Collection<WebCustomer> getWebCustomersWithEmail(PersistenceManager pm, String email) {
		Query q = pm.newNamedQuery(WebCustomer.class, "getWebCustomerWithRegexField");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("regexFieldStructBlockOrganisationID", PersonStruct.INTERNET_EMAIL.structBlockOrganisationID);
		params.put("regexFieldStructBlockID", PersonStruct.INTERNET_EMAIL.structBlockID);
		params.put("regexFieldStructFieldOrganisationID", PersonStruct.INTERNET_EMAIL.structFieldOrganisationID);
		params.put("regexFieldStructFieldID", PersonStruct.INTERNET_EMAIL.structFieldID);
		params.put("regexFieldValue", email.toLowerCase());
		return (Collection<WebCustomer>) q.executeWithMap(params);
	}
}
