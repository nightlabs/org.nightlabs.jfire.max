/**
 *
 */
package org.nightlabs.jfire.web.webshop;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.webshop.id.WebCustomerID;

/**
 * @author khaled
 *
 * @ejb.bean name="jfire/ejb/JFireWebShopBase/WebShop"
 *           jndi-name="jfire/ejb/JFireWebShopBase/WebShop" type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class WebShopBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final int secondPasswordExpirationTimeInHours = 6;
	public static final int confirmationStringExpirationTimeInHours = 24;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(WebShopBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	public void ejbActivate() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbActivate()");
	}

	public void ejbPassivate() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public List<WebCustomer> getWebCustomers(Set<WebCustomerID> webCustomerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, webCustomerIDs, WebCustomer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<WebCustomerID> getWebCustomerIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(WebCustomer.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<WebCustomerID>((Collection<? extends WebCustomerID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public WebCustomer getWebCustomer(WebCustomerID webCustomerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			return pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public WebCustomer getPerson(WebCustomerID webCustomerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
//			AnchorID anchorID = (AnchorID) (webCustomer.getLegalEntity() != null ? JDOHelper.getObjectId(webCustomer.getLegalEntity()) : null);
			return pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public AnchorID getWebCustomerLegalEntityID(WebCustomerID webCustomerID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			return (AnchorID) (webCustomer.getLegalEntity() != null ? JDOHelper.getObjectId(webCustomer.getLegalEntity()) : null);
		} finally {
			pm.close();
		}
	}

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
	public WebCustomer createWebCustomer(WebCustomerID webCustomerID, String password, Person person, boolean get, String[] fetchGroups, int maxFetchDepth) throws DuplicateIDException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (isWebCustomerIDExisting(webCustomerID, pm))
				throw new DuplicateIDException("webCustomerID \"" + webCustomerID + "\" already exists!");

			if(get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}
			WebCustomer webCustomer = new WebCustomer(getOrganisationID(), webCustomerID.webCustomerID);
			webCustomer.setPassword(UserLocal.encryptPassword(password));
			person = pm.makePersistent(person);
			LegalEntity legalEntity = Trader.getTrader(pm).setPersonToLegalEntity(person, true);
			webCustomer.setLegalEntity(legalEntity);
			webCustomer = pm.makePersistent(webCustomer);
			if(!get)
				return null;
			return pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public boolean isWebCustomerIDExisting(WebCustomerID webCustomerID, PersistenceManager pm)
	{
		try {
			pm.getObjectById(webCustomerID);
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		return true;
	}
	/**
	 * @throws Exception
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<WebCustomer> getWebCustomerByEmail(String email)
	{
		return WebCustomer.getWebCustomerWithEmail(getPersistenceManager(), email);
	}

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
	public boolean tryCustomerLogin(WebCustomerID webCustomerID, String password)
	{
		logger.debug("Trying authentication for web customer: "+webCustomerID);
		if(password == null) {
			logger.info("Customer authentication failed: No password given.");
			return false;
		}
		String encryptedPassword = UserLocal.encryptPassword(password);
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			if(wbc.getConfirmationString() != null || wbc.getConfirmationStringDate() != null) {
				logger.info("Account has not been confirmed yet");
				return false;
			}
			if(wbc.getPassword().equals(encryptedPassword)) {
				// login succeeded - reset second password
				wbc.setSecondPassword(null);
				wbc.setSecondPasswordDate(null);
				logger.debug("Customer authentication successful.");
				return true;
			}
			else {
				// try second password:
				String secondPassword = wbc.getSecondPassword();
				if(secondPassword != null) {
					logger.debug("Trying authentication (2nd pwd) for web customer: "+webCustomerID);
					if(secondPassword.equals(encryptedPassword))  {
						// login with second password succeeded - replace first password with second
						logger.debug("Customer authentication (2nd pwd) successful.");
						logger.debug("Replacing first password with second.");
						wbc.setPassword(wbc.getSecondPassword());
						wbc.setSecondPassword(null);
						return true;
					} else {
						logger.debug("Customer authentication (2nd pwd) failed: Passwords don't match.");
						return false;
					}
				} else {
					logger.debug("No second password found");
				}
				logger.debug("Customer authentication failed: Passwords don't match.");
				return false;
			}
		}	catch (JDOObjectNotFoundException e) {
			logger.error("Customer authentication failed: Customer not found: "+webCustomerID, e);
			return false;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public boolean checkEmailConfirmation(WebCustomerID webCustomerID, String checkString)
	{
		if(checkString == null ) return false;
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			// this will avoid a NullPointerException if no Email confirmation is open
			if(wbc.getConfirmationString() == null) return false;
			if(wbc.getConfirmationString().equals(checkString)) return true;
			else return false;
		}
		catch (JDOObjectNotFoundException e) {
			logger.error("Customer not found: "+webCustomerID, e);
			return false;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public boolean hasEmailConfirmationExpired(WebCustomerID webCustomerID) throws JDOObjectNotFoundException
	{
		long expirationTime = 1000 * 60 * 60 * confirmationStringExpirationTimeInHours;
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			Date now = new Date();
			if((now.getTime() - wbc.getConfirmationStringDate().getTime()) > expirationTime)
				return true;
			else return false;
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public boolean isWebCustomerIDExisting(WebCustomerID webCustomerID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getObjectById(webCustomerID);
			return true;
		} catch (JDOObjectNotFoundException e) {
			return false;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public boolean isEmailExisting(String email)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<WebCustomer> customer = WebCustomer.getWebCustomerWithEmail(pm, email);

			return (customer.size()>0);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public WebCustomer storeWebCustomer(WebCustomer webCustomer, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, webCustomer, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeWebCustomerPassword(WebCustomerID webCustomerID, String newPassword)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			wbc.setPassword(UserLocal.encryptPassword(newPassword));
			pm.makePersistent(wbc);
		} finally {
			pm.close();
		}
	}

	/**
	 * This will send E-mails to every address found for that webCustomers
	 * @param webCustomer
	 * @param subject of the mail
	 * @param message Mail content
	 * @return atLeastOneMailSent Turns false when no mail was sent
	 * @throws DataBlockGroupNotFoundException
	 *
	 */
	public boolean sendBlockGroupMails(WebCustomerID webCustomerID, String subject, String message)
	{
		WebCustomer webCustomer = getWebCustomer(
				webCustomerID,
				new String[] {
						FetchPlan.DEFAULT, WebCustomer.FETCH_GROUP_LEGAL_ENTITY,
						LegalEntity.FETCH_GROUP_PERSON, PropertySet.FETCH_GROUP_FULL_DATA
				},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);

		DataBlockGroup dataBlockGroupInternet = null;
		try {
			dataBlockGroupInternet = webCustomer.getLegalEntity().getPerson().getDataBlockGroup(PersonStruct.INTERNET);
		} catch(DataBlockGroupNotFoundException e) {
			logger.error("Cannot send email - no Internet Block Group found for customer: "+webCustomerID);
			return false;
		}

		// Now sending mails to every E-Mail address found
		boolean atLeastOneMailSent = false;
		for (DataBlock dataBlock : dataBlockGroupInternet.getDataBlocks()) {
			try {
				RegexDataField mailAddress = (RegexDataField)dataBlock.getDataField(PersonStruct.INTERNET_EMAIL);
				sendMail(mailAddress.getText(), subject, message);
				atLeastOneMailSent = true;
			} catch (Exception e) {
				logger.error("Sending email failed for customer: "+webCustomerID, e);
			}
		}

		if(!atLeastOneMailSent)
			logger.error("No email could be sent to customer: "+webCustomerID);

		return atLeastOneMailSent;
	}

	/**
	 * This is no Bean Method!!
	 * @throws NamingException
	 * @throws MessagingException
	 */
	public void sendMail(String recipientAddress, String subject, String message) throws NamingException, MessagingException
	{
		InitialContext ctx = new InitialContext();
		Session mailSession = (Session) ctx.lookup("java:/Mail");
		Message mailMessage = new MimeMessage(mailSession);
		mailMessage.setSubject(subject);
		mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddress));
		mailMessage.setText(message);
		Transport.send(mailMessage);

		ctx.close();
	}

	/**
	 * Creates, sends and if it succeeds stores it with an expiration Date.
	 * @throws
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void createPassword(WebCustomerID webCustomerID) throws DataBlockGroupNotFoundException
	{
		String newPassword = UserLocal.createHumanPassword(8,10);
		String subject =  "New Password for the JFire-demoshop";
		String message = "This is your new Password: "+ newPassword;
		// sending the mail
		boolean atLeastOneMailSent = sendBlockGroupMails(webCustomerID, subject, message);
		if(atLeastOneMailSent) {
			PersistenceManager pm = getPersistenceManager();
			try {
				WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
				wbc.setPassword(UserLocal.encryptPassword(newPassword));
				pm.makePersistent(wbc);
			} finally {
				pm.close();
			}
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeAndSendConfirmation(WebCustomerID webCustomerID, String subject, String messageText, String confirmationString) throws Exception
	{
			if(sendBlockGroupMails(webCustomerID, subject, messageText))
				// At least 1 mail has been sent succesfully
				setConfirmationString(webCustomerID, confirmationString);
			else
				// sending all mails failed
				throw new Exception("Sending mail failed");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void setConfirmationString(WebCustomerID webCustomerID, String confirmationString)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			// if the password doesnt get erased we have to set the current Time for a possible expiration
			wbc.setConfirmationString(confirmationString);
			wbc.setConfirmationStringDate(confirmationString == null ? null : new Date());
		} finally {
			pm.close();
		}
	}

}
