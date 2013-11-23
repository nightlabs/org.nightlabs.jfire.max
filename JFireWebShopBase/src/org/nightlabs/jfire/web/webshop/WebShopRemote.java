package org.nightlabs.jfire.web.webshop;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.webshop.id.WebCustomerID;

@Remote
public interface WebShopRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	List<WebCustomer> getWebCustomers(Set<WebCustomerID> webCustomerIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<WebCustomerID> getWebCustomerIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	WebCustomer getWebCustomer(WebCustomerID webCustomerID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	WebCustomer getPerson(WebCustomerID webCustomerID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	AnchorID getWebCustomerLegalEntityID(WebCustomerID webCustomerID);

	/**
	 * Create a new web customer.
	 *
	 * @param webCustomerID The customer to create
	 * @param password The plain text password for the customer
	 * @param person The person for the new customer
	 * @param get when set to <code>true</code> this method will return the
	 * 		newly created customer
	 * @param fetchGroups The fetch groups to use when <code>get</code> is set to <code>true</code>.
	 * @param maxFetchDepth The fetch depth to use when <code>get</code> is set to <code>true</code>.
	 * @return The newly created customer if the <code>get</code> parameter is <code>true</code> -
	 * 		<code>null</code> otherwise.
	 * @throws DuplicateIDException If a customer with the same id already exists.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	WebCustomer createWebCustomer(WebCustomerID webCustomerID, String password,
			Person person, boolean get, String[] fetchGroups, int maxFetchDepth)
			throws DuplicateIDException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	boolean isWebCustomerIDExisting(WebCustomerID webCustomerID,
			PersistenceManager pm);

	/**
	 * @throws Exception
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<WebCustomerID> getWebCustomerIDsByEmail(String email);

	/**
	 * Check if a customer can login. This is done by checking
	 * the first and second password for the customer. If authentication
	 * with the second password succeeds, the firsat password will be
	 * replaced by the second and the second password will be removed.
	 * @param webCustomerID the customer to log in
	 * @param password The plain text password to check
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	boolean tryCustomerLogin(WebCustomerID webCustomerID, String password);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	boolean checkEmailConfirmation(WebCustomerID webCustomerID,
			String checkString);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	boolean hasEmailConfirmationExpired(WebCustomerID webCustomerID)
			throws JDOObjectNotFoundException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	boolean isWebCustomerIDExisting(WebCustomerID webCustomerID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	boolean isEmailExisting(String email);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	WebCustomer storeWebCustomer(WebCustomer webCustomer, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeWebCustomerPassword(WebCustomerID webCustomerID,
			String newPassword);

	/**
	 * Creates, sends and if it succeeds stores it with an expiration Date.
	 * @throws
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void createPassword(WebCustomerID webCustomerID)
			throws DataBlockGroupNotFoundException;

	/**
	 * Creates, sends and if it succeeds stores it with an expiration Date.
	 * @param mailTemplate The email template. This text will be used as e-mail text. All occurencies
	 * of <code>${password}</code> in the text will be replaced by the newly created password.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void createPassword(WebCustomerID webCustomerID, String subject, String mailTemplate) throws DataBlockGroupNotFoundException;
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeAndSendConfirmation(WebCustomerID webCustomerID, String subject,
			String messageText, String confirmationString) throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void setConfirmationString(WebCustomerID webCustomerID,
			String confirmationString);

}