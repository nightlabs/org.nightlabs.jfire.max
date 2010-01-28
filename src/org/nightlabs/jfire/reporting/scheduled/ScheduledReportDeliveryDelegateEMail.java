package org.nightlabs.jfire.reporting.scheduled;

import java.io.File;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayoutUtil;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportDeliveryDelegateEMailID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.IOUtil;

/**
 * Delivery-delegate for ScheduledReports that sends the report by email. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	objectIdClass=ScheduledReportDeliveryDelegateEMailID.class,
	detachable="true",
	table="JFireReporting_ScheduledReportDeliveryDelegateEMail"
)
public class ScheduledReportDeliveryDelegateEMail implements IScheduledReportDeliveryDelegate {

	private static final long serialVersionUID = 20100128L;
	
	private static final Logger logger = Logger.getLogger(ScheduledReportDeliveryDelegateEMail.class);

	/** organisationID pk-part */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	/** deliveryDelegateID pk-part */
	@PrimaryKey
	private long deliveryDelegateID;
	
	@Persistent
	private String toAddresses;
	
	@Persistent
	private String fromAddress;
	
	@Persistent
	private String subject;
	
	@Persistent
	@Column(jdbcType="CLOB")
	private String mailBody;
	
	/**
	 * @deprecated Only for JDO.
	 */
	protected ScheduledReportDeliveryDelegateEMail() {
	}
	
	/**
	 * Creates a new {@link ScheduledReportDeliveryDelegateEMail}.
	 * 
	 * @param organisationID
	 * @param deliveryDelegateID
	 */
	public ScheduledReportDeliveryDelegateEMail(String organisationID, long deliveryDelegateID) {
		super();
		this.organisationID = organisationID;
		this.deliveryDelegateID = deliveryDelegateID;
	}



	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.scheduled.IScheduledReportDeliveryDelegate#deliverReportOutput(org.nightlabs.jfire.reporting.scheduled.ScheduledReport, org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout)
	 */
	@Override
	public void deliverReportOutput(ScheduledReport scheduledReport, RenderedReportLayout renderedReportLayout) throws Exception {
		
		if (logger.isDebugEnabled())
			logger.debug("Delivering RenderedReportLayout for " + ScheduledReport.describeScheduledReport(scheduledReport) + " via email");
		
		MimeMessage message = null;
		try {
			InitialContext ctx = new InitialContext();
			Session mailSession = (Session) ctx.lookup("java:/Mail");
			message = new MimeMessage(mailSession);
		} catch (NamingException e1) {
			
		}

		if (toAddresses == null && toAddresses.isEmpty()) {
			throw new IllegalStateException("Can't send ScheduledReport output because no toAddresses are configured. "
					+ ScheduledReport.describeScheduledReport(scheduledReport), new Exception());
		}
		String[] addresses = toAddresses.split("\\s*,\\s*");
		if (addresses == null || addresses.length < 1) {
			throw new IllegalStateException("Can't send ScheduledReport output because toAddresses configured are invalid (" + toAddresses + "). "
					+ ScheduledReport.describeScheduledReport(scheduledReport), new Exception());
		}
		int addedAddrs = 0;
		for (String address : addresses) {
			try {
				InternetAddress addr = new InternetAddress(address.trim());
				message.addRecipient(Message.RecipientType.TO, addr);
				addedAddrs++;
			} catch (Exception e) {
				logger.error("Could not parse address to email address: (" + address + "). "
						+ ScheduledReport.describeScheduledReport(scheduledReport));
			}
		}
		if (addedAddrs < 1) {
			throw new IllegalStateException("Can't send ScheduledReport output because no address configured is invalid (" + toAddresses + "). " 
					+ ScheduledReport.describeScheduledReport(scheduledReport), new Exception());
		}
		
		InternetAddress fromAddr = null;
		try {
			fromAddr = new InternetAddress(getFromAddress());
		} catch (Exception e) {
			fromAddr = new InternetAddress("reporting@jfire.org");
		}
		message.setFrom(fromAddr);
		
		message.setSubject(subject);
		
		if (logger.isDebugEnabled())
			logger.debug("Configured mail with toAddresses " + toAddresses + ", fromAddress " + fromAddress + ", subject " + subject);
		
		// create the message part 
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(mailBody);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		if (logger.isDebugEnabled())
			logger.debug("Creating mail-attachment");
		
		// now create the attachment part
		File folder = null;
		try {
			folder = IOUtil.createUniqueRandomFolder(IOUtil.createUserTempDir("jfire.scheduleReports", null), null);
		} catch (IOException e) {
			throw new IllegalStateException("Can't send ScheduledReport output because an IOException occured when creating a temporary folder ("
					+ toAddresses + "). " + ScheduledReport.describeScheduledReport(scheduledReport), e);
		}
		if (logger.isDebugEnabled())
			logger.debug("Write report output to disk: " + folder);
		File file = RenderedReportLayoutUtil.prepareRenderedReportLayout(folder, renderedReportLayout, new NullProgressMonitor());
		if (logger.isDebugEnabled())
			logger.debug("Written: " + file + ", now attaching to mail");
		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(file.getName());
		multipart.addBodyPart(messageBodyPart);				
		message.setContent(multipart);

		// Send the message
		if (logger.isDebugEnabled())
			logger.debug("Sending mail");
		Transport.send( message );		
		if (logger.isDebugEnabled())
			logger.debug("Sending mail done");
	}

	/**
	 * @return organisationID part from pk.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * @return deliveryDelegateID part from pk.
	 */
	public long getDeliveryDelegateID() {
		return deliveryDelegateID;
	}

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMailBody() {
		return mailBody;
	}

	public void setMailBody(String mailBody) {
		this.mailBody = mailBody;
	}
}
