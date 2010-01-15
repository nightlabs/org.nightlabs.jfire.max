package org.nightlabs.jfire.reporting.scheduled;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportDeliveryDelegateEMailID;

/**
 * Delivery-delegate for ScheduledReports that sends the report by email. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	objectIdClass=ScheduledReportDeliveryDelegateEMailID.class
)
public class ScheduledReportDeliveryDelegateEMail implements IScheduledReportDeliveryDelegate {

	/** organisationID pk-part */
	@PrimaryKey
	private String organisationID;
	
	/** deliveryDelegateID pk-part */
	@PrimaryKey
	private long deliveryDelegateID;
	
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
	public void deliverReportOutput(ScheduledReport scheduledReport, RenderedReportLayout renderedReportLayout) {
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
	
}
