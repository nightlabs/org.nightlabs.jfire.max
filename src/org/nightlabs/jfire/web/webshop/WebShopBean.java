/**
 * 
 */
package org.nightlabs.jfire.web.webshop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import java.util.Calendar;
import java.util.Date;
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
//				TradeManagerLocal tm = TradeManagerUtil.getLocalHome().create(); // TODO are we in the core server here?!
//				legalEntity = tm.storePersonAsLegalEntity(person, false, 
//						new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA}, 
//						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			} catch (Exception x) {
//				throw new RuntimeException(x);
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
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
			if(wbc.getPassword()== null) return false;
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
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
			if(wbc.getSecondPassword()== null) return false;
			if(wbc.getSecondPassword().equals(password)) return true;
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
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public boolean hasSecondPasswordDateExpired(String webCustomerID) throws JDOObjectNotFoundException 
	{
		long expirationTime = 1000 * 60 * 60 * secondPasswordExpirationTimeInHours;
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
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
	public boolean isWebCustomerIDExisting(String webCustomerID) 
	{
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			return false;
		} finally {
			pm.close();
		}		
		return true;
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
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Supports"
//	 */
//	public DataBlockGroup getCustomersDataBlockGroup(WebCustomer webCustomer,StructBlockID structBlockID) throws DataBlockGroupNotFoundException {
//
//		PersistenceManager pm = getPersistenceManager();
//		DataBlockGroup dataBlockGroup;
//		try {
//		//	dataBlockGroup = (DataBlockGroup) pm.getObjectId(webCustomer.getLegalEntity().getPerson().getDataBlockGroup(structBlockID));
//		//	AnchorID anchorID = (AnchorID) (webCustomer.getLegalEntity() != null ? JDOHelper.getObjectId(webCustomer.getLegalEntity()) : null);
//			dataBlockGroup = webCustomer.getLegalEntity().getPerson().getDataBlockGroup(structBlockID);
//			return (DataBlockGroup) pm.detachCopy(dataBlockGroup);
//		} finally {
//			pm.close();
//		}
//	}
	
	/*
	 * This is no Bean Method!!
	 */
	   public void sendMail(String recipientAddress, String subject, String message)
	   
	    {
	        try {
	            InitialContext ctx = new InitialContext();
	            Session mailSession = (Session) ctx.lookup("java:/Mail");
	            Message mailMessage = new MimeMessage(mailSession);
	            try {
	                mailMessage.setSubject(subject);
	                mailMessage.setRecipient(Message.RecipientType.TO,
	                        new InternetAddress(recipientAddress));
	                mailMessage.setText(message);
	                Transport.send(mailMessage);

	            } catch (AddressException e1) {
	                e1.printStackTrace();
	            } catch (MessagingException e1) {
	                e1.printStackTrace();
	            }

	            ctx.close();
	        } catch (NamingException e) {
	            e.printStackTrace();
	        }
	    }
		/**
		 * @ejb.interface-method
		 * @ejb.transaction type="Required"
		 * @ejb.permission role-name="_Guest_"
		 */	
	   public void createPassword(String customerId) throws DataBlockGroupNotFoundException,DataFieldNotFoundException
	   {
		   WebCustomer webCustomer = getWebCustomer(
				   WebCustomerID.create(getOrganisationID(), customerId), 
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
		   String newPassword = UserLocal.createPassword(8,10);
		   for (DataBlock dataBlock : dataBlockGroupInternet.getDataBlocks()) {
			   mailAddress = (RegexDataField)dataBlock.getDataField(PersonStruct.INTERNET_EMAIL);
			   if(Util.isValidEmailAddress(mailAddress.getText())) {
				   sendMail(mailAddress.getText(), "New Password for the JFire-demoshop", 
						   "This is your new Password: "+newPassword);
				   atLeastOneMailSent = true;
			   } else if (mailAddress.getText().length() != 0 ) {
				   logger.warn("The E-mail address: "+ mailAddress.getText()+ " for the customer: "+customerId +" seems not to be valid");
			   }
		   }
		   if(atLeastOneMailSent)
			   setSecondPassword(customerId,UserLocal.encryptPassword(newPassword));
	   }

	   /**
	    * @param customerId
	    * @param encryptedPassword Set it to null if you want to erase the Password
	    * 
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
	    *
	    * @ejb.interface-method
	    * @ejb.transaction type="Required"
	    * @ejb.permission role-name="_Guest_"
	    */	
	   public void setSecondPassword(String customerId,String encryptedPassword)
	   {
		   PersistenceManager pm = getPersistenceManager();
		   try {
			   WebCustomer webCustomer = (WebCustomer)pm.getObjectById(WebCustomerID.create(getOrganisationID(), customerId));
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
