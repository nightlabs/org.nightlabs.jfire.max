package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.dunning.id.DunningStepID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningStep.FETCH_GROUP_DUNNING_FEE_TYPE,
		members=@Persistent(name="feeTypes")
	),
	@FetchGroup(
		fetchGroups={"default"},
		name=DunningStep.FETCH_GROUP_LETTER_LAYOUT,
		members=@Persistent(name="letterLayout")
	)
})
@PersistenceCapable(
		objectIdClass=DunningStepID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_Step"
)
public class DunningStep
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_DUNNING_FEE_TYPE = "ProcessDunningStep.feeTypes";
	public static final String FETCH_GROUP_LETTER_LAYOUT = "ProcessDunningStep.letterLayout";
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningStepID;
	
	@Persistent(nullValue=NullValue.EXCEPTION)
	private DunningConfig dunningConfig;

	/**
	 * The percentage that should be applied when calculating the interest.
	 */
	@Column(length=10, scale=6)
	private BigDecimal interestPercentage;
	
	/**
	 * The time (in milliseconds) after this DunningStep, before continuing 
	 * the DunningProcess (i.e. how long to wait before performing further action).
	 */
	private Long periodOfGraceMSec;
	
	/**
	 * The fees that should be applied to the new DunningLetter. 
	 * 
	 * Note, that all fees from the previous DunningLetter need to be 
	 * copied into the new DunningLetter, additionally.
	 */
	@Persistent(table="JFireDunning_Step_feeTypes")
	@Join
	private Set<DunningFeeType> feeTypes;
	
	/**
	 * The description of how the DunningLetter should look like.
	 */
	private ReportLayout letterLayout;
	
	/**
	 * The coolDownPeriod is the timespan from the last DunningLetter, 
	 * in which no new DunningLetters are to be created.
	 * 
	 * Therefore, manual triggering of a DunningProcess check does not 
	 * flood the customer with DunningLetters.
	 */
	private Long coolDownPeriod;
	
	public DunningStep(DunningConfig config)
	{
		this(config, IDGenerator.nextID(DunningStep.class));
	}

	public DunningStep(DunningConfig config, long dunningStepID)
	{
		this(config.getOrganisationID(), dunningStepID);
		this.dunningConfig = config;
	}

	/**
	 * Create an instance of <code>AbstractDunningStep</code>.
	 */
	public DunningStep(String organisationID, long dunningStepID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningStepID = dunningStepID;
		this.feeTypes = new HashSet<DunningFeeType>();
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningStepID()
	{
		return dunningStepID;
	}

	/**
	 * @return the dunningConfig
	 */
	public DunningConfig getDunningConfig()
	{
		return dunningConfig;
	}

	/**
	 * @param dunningConfig the dunningConfig to set
	 */
	public void setDunningConfig(DunningConfig dunningConfig)
	{
		this.dunningConfig = dunningConfig;
	}

	public void setInterestPercentage(BigDecimal interestPercentage)
	{
		this.interestPercentage = interestPercentage;
	}
	
	/**
	 * Returns the interest percentage of the step.
	 * @return
	 */
	public BigDecimal getInterestPercentage()
	{
		return interestPercentage;
	}
	
	/**
	 * Sets the time (in milliseconds) that specify how long to wait
	 * before continuing the DunningProcess.
	 * 
	 * @return long time
	 */
	public void setPeriodOfGraceMSec(long periodOfGraceMSec)
	{
		this.periodOfGraceMSec = periodOfGraceMSec;
	}
	
	/**
	 * Returns the time (in milliseconds) that specify how long to wait
	 * before continuing the DunningProcess.
	 * 
	 * @return long time
	 */
	public long getPeriodOfGraceMSec()
	{
		return periodOfGraceMSec;
	}

	public void addFeeType(DunningFeeType feeType)
	{
		feeTypes.add(feeType);
	}
	
	public void addFeeTypes(Collection<DunningFeeType> feeTypes)
	{
		feeTypes.addAll(feeTypes);
	}
	
	public void removeFeeType(DunningFeeType feeType)
	{
		feeTypes.remove(feeType);
	}
	
	public void removeFeeTypes(Collection<DunningFeeType> feeTypes)
	{
		feeTypes.removeAll(feeTypes);
	}

	public Set<DunningFeeType> getFeeTypes()
	{
		return Collections.unmodifiableSet(feeTypes);
	}
	
	public void setLetterLayout(ReportLayout letterLayout)
	{
		this.letterLayout = letterLayout;
	}
	
	public ReportLayout getLetterLayout()
	{
		return letterLayout;
	}
	
	public void setCoolDownPeriod(long coolDownPeriod)
	{
		this.coolDownPeriod = coolDownPeriod;
	}
	
	public long getCoolDownPeriod()
	{
		return coolDownPeriod;
	}
	
	@Override
	public DunningStep clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//	AbstractDunningStep clone = (AbstractDunningStep) super.clone();
		DunningStep clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND
		
		clone.dunningStepID = IDGenerator.nextID(DunningStep.class);
		clone.interestPercentage = interestPercentage;
		clone.periodOfGraceMSec = periodOfGraceMSec;
		clone.coolDownPeriod = coolDownPeriod;
		
		if (cloneReferences)
		{
			clone.feeTypes = new HashSet<DunningFeeType>(feeTypes.size());
			for (DunningFeeType feeType : feeTypes)
			{
				context.createClone(feeType);
			}
		}
		
		// TODO: once we know how we can clone ReportLayouts or store the rendered result.
		clone.letterLayout = letterLayout;

		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
		DunningStep clonedStep = (DunningStep) clone;
		if (feeTypes != null)
		{
			for (DunningFeeType feeType : feeTypes)
			{
				clonedStep.feeTypes.add(context.getClone(feeType));
			}
		}
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (dunningStepID ^ (dunningStepID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningStep other = (DunningStep) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningStepID, other.dunningStepID))
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
		return "DunningStep [organisationID=" + organisationID + ", dunningStepID=" + dunningStepID
				+ ", periodOfGraceMSec=" + periodOfGraceMSec + ", interestPercentage=" + interestPercentage
				+ ", coolDownPeriod=" + coolDownPeriod + ", letterLayout=" + letterLayout + ", feeTypes="
				+ (feeTypes != null ? toString(feeTypes, maxLen) : null) + "]";
	}

	private String toString(Collection<?> collection, int maxLen)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++)
		{
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

}