package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.dunning.id.DunningRunID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * 
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@PersistenceCapable(
		objectIdClass=DunningRunID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_Run"
)
public class DunningRun
	implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@PrimaryKey
	private long dunningRunID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private DunningProcess dunningProcess;
	
	private Date startDate;
	
	private Date endDate;
	
	/**
	 * All DunningLetters that have been created so far within the scope of this dunning process.
	 * There's only one active DunningLetter and when a new DunningLetter is finalized & booked, the
	 * previous DunningLetter needs to be booked out.
	 */
	@Persistent(mappedBy="dunningRun")
	private List<DunningLetter> dunningLetters;
	
	public DunningRun(DunningProcess process)
	{
		assert process != null : "The given process must NOT be null1";
		
		this.organisationID = process.getOrganisationID();
		this.dunningRunID = IDGenerator.nextID(DunningRun.class);
		this.dunningProcess = process;
		this.startDate = new Date();
		this.dunningLetters = new ArrayList<DunningLetter>();
	}

	/**
	 * @return the dunningProcess
	 */
	public DunningProcess getDunningProcess()
	{
		return dunningProcess;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * @return the dunningLetters
	 */
	public List<DunningLetter> getDunningLetters()
	{
		return Collections.unmodifiableList(dunningLetters);
	}
	
	public void addDunningLetter(DunningLetter dunningLetter)
	{
		if (dunningLetter == null)
			throw new IllegalArgumentException("dunningLetter may NOT be null!");
		
		DunningLetter previousLetter = getLastDunningLetter();
		if (previousLetter != null)
			previousLetter.setOutstanding(false);
		
		dunningLetters.add(dunningLetter);
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return the dunningProcessID
	 */
	public long getDunningRunID()
	{
		return dunningRunID;
	}
	
	public boolean isCompleted()
	{
		return endDate != null;
	}

	public DunningLetter getLastDunningLetter()
	{
		if (dunningLetters == null || dunningLetters.size() == 0)
			return null;
		
		return dunningLetters.get(dunningLetters.size() - 1);		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result	+ (int) (dunningRunID ^ (dunningRunID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningRun other = (DunningRun) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningRunID, other.dunningRunID)	)
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DunningRun [organisationID=");
		builder.append(organisationID);
		builder.append(", dunningRunID=");
		builder.append(dunningRunID);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", dunningLetters=");
		builder.append(dunningLetters != null ? dunningLetters.subList(0, Math.min(dunningLetters.size(), maxLen)) : null);
		builder.append("]");
		return builder.toString();
	}

}
