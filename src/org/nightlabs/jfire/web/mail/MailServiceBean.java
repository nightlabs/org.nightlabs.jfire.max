/**
 * 
 */
package org.nightlabs.jfire.web.mail;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
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
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @author khaled
 * 
 * @ejb.bean name="jfire/ejb/JFireWebShopBase/MailService"
 *           jndi-name="jfire/ejb/JFireWebShopBase/MailService" type="Stateless"
 *           transaction-type="Container"
 * 
 * @ejb.util generate="physical"
 */
public abstract class MailServiceBean
extends BaseSessionBeanImpl 
implements SessionBean {

	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(MailServiceBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }	/**

	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
}
