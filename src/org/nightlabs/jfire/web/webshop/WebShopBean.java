/**
 * 
 */
package org.nightlabs.jfire.web.webshop;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
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
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
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
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
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
 */
public abstract class WebShopBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int secondPasswordExpirationTimeInHours = 6;
	public static final int confirmationStringExpirationTimeInHours = 24;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(WebShopBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
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
	 * @ejb.transaction type="Supports"
	 */	
	public List<WebCustomer> getWebCustomers(Set<WebCustomerID> webCustomerIDs, 
			String[] fetchGroups, int maxFetchDepth) 
			{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, webCustomerIDs, WebCustomer.class, 
					fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}		
			}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */	
	public WebCustomer getWebCustomer(WebCustomerID webCustomerID, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			return (WebCustomer) pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}	
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */	
	public WebCustomer getPerson(WebCustomerID webCustomerID, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		AnchorID anchorID = null;
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			anchorID = (AnchorID) (webCustomer.getLegalEntity() != null ? JDOHelper.getObjectId(webCustomer.getLegalEntity()) : null);

			return (WebCustomer) pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}	
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */		
	public WebCustomer createWebCustomer(
			String webCustomerID, String password, Person person,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DuplicateIDException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (isWebCustomerIDExisting(webCustomerID, pm))
				throw new DuplicateIDException("webCustomerID \""+webCustomerID+"\" already exists!");

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}
			WebCustomer webCustomer = new WebCustomer(getOrganisationID(), webCustomerID);
			webCustomer.setPassword(password);
			LegalEntity legalEntity;
			// TODO the following lines work only locally - are we always in the core server here?!
			person = (Person) pm.makePersistent(person);
			legalEntity = Trader.getTrader(pm).setPersonToLegalEntity(person, true);
//			try {
//			TradeManagerLocal tm = TradeManagerUtil.getLocalHome().create(); // TODO are we in the core server here?!
//			legalEntity = tm.storePersonAsLegalEntity(person, false, 
//			new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA}, 
//			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			} catch (Exception x) {
//			throw new RuntimeException(x);
//			}
			webCustomer.setLegalEntity(legalEntity);
			webCustomer = (WebCustomer) pm.makePersistent(webCustomer);
			if (!get)
				return null;
			return (WebCustomer) pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */			
	public boolean isWebCustomerIDExisting(String webCustomerID, PersistenceManager pm) 
	{
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		return true;
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public boolean checkPassword(String webCustomerID, String password) 
	{
		if(password == null ) return false;
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
			if(wbc.getPassword().equals(password)) return true;
			else return false;
		}	
		catch (JDOObjectNotFoundException e) {
			e.printStackTrace();
		} finally {
			pm.close();
		}	
		return false;
	}
	/**
	 * The second password that can be set if a customer triggers the lostPassword procedure
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public boolean checkSecondPassword(String webCustomerID, String password) 
	{
		if(password == null ) return false;
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
			if(wbc.getSecondPassword().equals(password)) return true;
			else return false;
		}	
		catch (JDOObjectNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			pm.close();
		}	
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
			e.printStackTrace();
			return false;
		} finally {
			pm.close();
		}	
	}
	/**
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public boolean hasEmailConfirmationExpired(WebCustomerID webCustomerID) throws JDOObjectNotFoundException 
	{
		long expirationTime = 1000 * 60 * 60 * confirmationStringExpirationTimeInHours;
		PersistenceManager pm = getPersistenceManager();
		//	WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
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
	 * The second password that can be set if a customer triggers the lostPassword procedure
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public boolean hasSecondPasswordDateExpired(WebCustomerID webCustomerID)  
	{
		long expirationTime = 1000 * 60 * 60 * secondPasswordExpirationTimeInHours;
		PersistenceManager pm = getPersistenceManager();
		//	WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			Date now = new Date();
			if((now.getTime() - wbc.getSecondPasswordDate().getTime()) > expirationTime)
				return true;
			else return false;
		} finally {
			pm.close();
		}	

	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
	 * @ejb.transaction type="Supports"
	 */			
	public boolean isEmailExisting(String email) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection<WebCustomer> customers = WebCustomer.getWebCustomersWithEmail(pm, email);

			return customers.size() > 0;
		} finally {
			pm.close();
		}		
	}
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public WebCustomer storeWebCustomer(WebCustomer webCustomer, boolean get, 
			String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (WebCustomer) NLJDOHelper.storeJDO(pm, webCustomer, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
//	/**
//	* @ejb.interface-method
//	* @ejb.permission role-name="_Guest_"
//	* @ejb.transaction type="Supports"
//	*/
//	public DataBlockGroup getCustomersDataBlockGroup(WebCustomer webCustomer,StructBlockID structBlockID) throws DataBlockGroupNotFoundException {

//	PersistenceManager pm = getPersistenceManager();
//	DataBlockGroup dataBlockGroup;
//	try {
//	//	dataBlockGroup = (DataBlockGroup) pm.getObjectId(webCustomer.getLegalEntity().getPerson().getDataBlockGroup(structBlockID));
//	//	AnchorID anchorID = (AnchorID) (webCustomer.getLegalEntity() != null ? JDOHelper.getObjectId(webCustomer.getLegalEntity()) : null);
//	dataBlockGroup = webCustomer.getLegalEntity().getPerson().getDataBlockGroup(structBlockID);
//	return (DataBlockGroup) pm.detachCopy(dataBlockGroup);
//	} finally {
//	pm.close();
//	}
//	}
	/**
	 * This will send E-mails to every address found for that webCustomers 
	 * @param webCustomer
	 * @param subject of the mail
	 * @param message Mail content 
	 * @return atLeastOneMailSent Turns false when no mail was sent 
	 * @throws DataBlockGroupNotFoundException
	 *
	 */
	public boolean sendBlockGroupMails(WebCustomerID webCustomerID, String subject ,String message) throws DataBlockGroupNotFoundException
	{
		WebCustomer webCustomer = getWebCustomer(
				webCustomerID, 
				new String[] {
						FetchPlan.DEFAULT, WebCustomer.FETCH_GROUP_LEGAL_ENTITY,
						LegalEntity.FETCH_GROUP_PERSON, Person.FETCH_GROUP_FULL_DATA
				}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);

		DataBlockGroup dataBlockGroupInternet = null;
		dataBlockGroupInternet = webCustomer.getLegalEntity().getPerson().getDataBlockGroup(PersonStruct.INTERNET);
		RegexDataField mailAddress = null;
		// Now sending mails to every E-Mail address found
		boolean atLeastOneMailSent = false;
		//String newPassword = UserLocal.createPassword(8,10);
		for (DataBlock dataBlock : dataBlockGroupInternet.getDataBlocks()) {
			try {
				mailAddress = (RegexDataField)dataBlock.getDataField(PersonStruct.INTERNET_EMAIL);
				sendMail(mailAddress.getText(), subject, message);
				atLeastOneMailSent = true;
			} catch (Exception e) {
				// hoping that at least one mail is going out
				e.printStackTrace();
			}
		}
		
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
				mailMessage.setRecipient(Message.RecipientType.TO,
						new InternetAddress(recipientAddress));
				mailMessage.setText(message);
				Transport.send(mailMessage);

			ctx.close();
		
	}

	/**
	 * Creates,sends and if it succeeds stores it with an expiration Date.
	 * @throws  
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void createPassword(WebCustomerID webCustomerID) throws DataBlockGroupNotFoundException
	{
		String newPassword = UserLocal.createPassword(8,10);
		String subject =  "New Password for the JFire-demoshop";
		String message = "This is your new Password: "+ newPassword;
		// sending the mail
		boolean atLeastOneMailSent = sendBlockGroupMails(webCustomerID,subject,message);
		if(atLeastOneMailSent)
			setSecondPassword(webCustomerID,UserLocal.encryptPassword(newPassword));
	}
	/**
	 * @param customerId
	 * @param encryptedPassword Set it to null if you want to erase the Password
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void setPassword(String customerId,String encryptedPassword)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(WebCustomerID.create(getOrganisationID(), customerId));
			webCustomer.setPassword(encryptedPassword);
		} finally {
			pm.close();
		}	
	}
	/**
	 * @param customerId
	 * @param encryptedPassword Set it to null if you want to erase the Password
	 *		 This way the secondPasswordDate will be erased to	
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void setSecondPassword(WebCustomerID webCustomerID,String encryptedPassword)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			webCustomer.setSecondPassword(encryptedPassword);
			// if the password doesnt get erased we have to set the current Time for a possible expiration
			if(encryptedPassword != null) 
				webCustomer.setSecondPasswordDate(new Date());
			else 
				webCustomer.setSecondPasswordDate(null);
		} finally {
			pm.close();
		}	
	}
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void storeAndSendConfirmation(WebCustomerID webCustomerID)
	{
		try {
			// the confirmation String is random encrypted String
			String randomEncrypted = UserLocal.encryptPassword(UserLocal.createPassword(8,10));
			String subject = "Account confirmation for the JFire demo webshop";
			// very temporarly
			String message = "Welcome to the JFire Web Shop . You can activate your account with" +
			"this link: http://127.0.0.1:8080/jfire-webshop/customer/?customerId="+webCustomerID.webCustomerID +"&action=confirm&cf="+ randomEncrypted;  

			if(sendBlockGroupMails(webCustomerID,subject,message) == true) {
				// Mail has been sent succesfully 
				setConfirmationUrl(webCustomerID, randomEncrypted);
			}		 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setConfirmationUrl(WebCustomerID webCustomerID,String encryptedConfirmation)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(webCustomerID);
			// if the password doesnt get erased we have to set the current Time for a possible expiration
			wbc.setConfirmationString(encryptedConfirmation);
			if(encryptedConfirmation != null) {
				wbc.setConfirmationStringDate(new Date());
			}
			else 
				wbc.setConfirmationStringDate(null);
		} finally {
			pm.close();
		}	
	}
	/*
	   public void erasePasswordByReflection(String customerId, String setter)
	   {
			PersistenceManager pm = getPersistenceManager();
			try {
				WebCustomer webCustomer = (WebCustomer)pm.getObjectById(WebCustomerID.create(getOrganisationID(), customerId));

				try {
					Method setterMethod = WebCustomer.class.getDeclaredMethod(setter, new Class[] { String.class });
					setterMethod.invoke(webCustomer, new Object[] {null});
				} catch(Exception e) {
					// TODO
				}
				webCustomer.setPassword(null);
			} finally {
				pm.close();
			}	
	   }
 */ 
}
