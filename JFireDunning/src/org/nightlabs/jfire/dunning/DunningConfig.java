package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * A DunningConfig contains all settings for a dunning process. 
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
@FetchGroups({
	@FetchGroup(
			fetchGroups={"default"},
			name=DunningConfig.FETCH_GROUP_NAME,
			members=@Persistent(name="name")
	),
	@FetchGroup(
			fetchGroups={"default"},
			name=DunningConfig.FETCH_GROUP_DESCRIPTION,
			members=@Persistent(name="description")
	),
	@FetchGroup(
			name=DunningConfig.FETCH_GROUP_DUNNING_STEPS,
			members=@Persistent(name="dunningSteps")
	)
})
@Queries(
	@javax.jdo.annotations.Query(
			name=DunningConfig.QUERY_GET_DEFAULT_DUNNING_CONFIG,
			value="SELECT WHERE this.organisationID == :organisationID && this.dunningConfigID == " + 
			DunningConfig.DUNNINGCONFIG_DEFAULT_DUNNINGCONFIGID 
	)		
)
@PersistenceCapable(
	objectIdClass=DunningConfigID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_Config")
public class DunningConfig
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DunningConfig.name";
	public static final String FETCH_GROUP_DESCRIPTION = "DunningConfig.description";
	public static final String FETCH_GROUP_DUNNING_AUTO_MODE = "DunningConfig.dunningAutoMode";
	public static final String FETCH_GROUP_DUNNING_STEPS = "DunningConfig.dunningSteps";
	
	public static final long DUNNINGCONFIG_DEFAULT_DUNNINGCONFIGID = 0l;

	public static final String QUERY_GET_DEFAULT_DUNNING_CONFIG = "DunningConfig.getDefaultDunningConfig";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long dunningConfigID;

	@Persistent(dependent="true")
	private DunningConfigName name;

	@Persistent(dependent="true") // , embedded="true"
//	TODO: test embedding to reduce the number of tables.
//	@Embedded(members={
//			@Persistent(name="")
//		})
	private DunningConfigDescription description;

	/**
	 * The time (in milliseconds) within which the payment is due. 
	 * It is copied to the Invoice, when a new invoice is created. 
	 * We might need new a hook in JFire to be able to react on the 
	 * invoice creation – or maybe we can simply use some default 
	 * JDO lifecycle listener.
	 */
	private long defaultTermOfPaymentMSec;

	/**
	 * Whether and what to do automatically.
	 */
	private DunningAutoMode autoMode;

	/**
	 * The steps that need to be performed for one particular 
	 * invoice of a dunning process. The level (or severeness) 
	 * specifies the ordering of the dunning steps.
	 */
	@Persistent(dependentElement="true", mappedBy="dunningConfig")
	private List<DunningStep> dunningSteps;

	/**
	 * The calculator implementation used for the calculation of 
	 * interests – there are many different ways to calculate them.
	 */
	@Persistent(dependent="true")
	private DunningInterestCalculator interestCalculator;

	/**
	 * Encapsulates the logic for deciding what fees to add to 
	 * a newly created DunningLetter.
	 */
	@Persistent(dependent="true")
	private DunningFeeAdder feeAdder;

	@Join
	@Persistent(table="JFireDunning_Config_letterNotifiers", dependentElement="true")
	private List<DunningLetterNotifier> letterNotifiers;

	/**
	 * Maps from a dunning level to the notifers that want to be triggered 
	 * if a letter with the corresponding dunning level is created.
	 */
	@NotPersistent
	private transient Map<Integer, List<DunningLetterNotifier>> level2LetterNotifiers;
	
	/**
	 * The configuration of the accounts  the fees and interests are booked to.
	 */
	@Persistent(dependent="true")
	private DunningMoneyFlowConfig moneyFlowConfig;

	/**
	 * Create an instance of <code>DunningConfig</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningConfigID second part of the primary key. A local identifier within the namespace of the organisation.
	 */
	public DunningConfig(String organisationID, long dunningConfigID, DunningAutoMode dunningAutoMode)
	{
		Organisation.assertValidOrganisationID(organisationID);

		this.organisationID = organisationID;
		this.dunningConfigID = dunningConfigID;
		this.autoMode = dunningAutoMode;

		this.defaultTermOfPaymentMSec = TimeUnit.DAYS.toMillis(31);

		this.name = new DunningConfigName(this);
		this.description = new DunningConfigDescription(this);

		this.dunningSteps = new ArrayList<DunningStep>();
		letterNotifiers = new LinkedList<DunningLetterNotifier>();
		level2LetterNotifiers = null;
	}
	
	public String getOrganisationID()
	{
		return organisationID;
	}

	public long getDunningConfigID()
	{
		return dunningConfigID;
	}

	public DunningConfigDescription getDescription()
	{
		return description;
	}

	public void setDunningAutoMode(DunningAutoMode dunningAutoMode)
	{
		this.autoMode = dunningAutoMode;
	}

	public DunningAutoMode getAutoMode()
	{
		return autoMode;
	}

	public void setDefaultTermOfPaymentMSec(long defaultTermOfPaymentMSec)
	{
		this.defaultTermOfPaymentMSec = defaultTermOfPaymentMSec;
	}

	public void setDefaultTermOfPaymentDay(int defaultTermOfPaymentDay)
	{
		this.defaultTermOfPaymentMSec = TimeUnit.DAYS.toMillis(defaultTermOfPaymentDay);
	}

	public long getDefaultTermOfPaymentMSec()
	{
		return defaultTermOfPaymentMSec;
	}

	public int getDefaultTermOfPaymentDay()
	{
		// TODO: This will give unexpected results if the defaultTemOfPaymentMSec is not exactly dividable by a day. Should we round up? (Marius)
		return (int)(defaultTermOfPaymentMSec / (24l * 60l * 60l * 1000l));
	}

	public void setInterestCalculator(DunningInterestCalculator interestCalculator)
	{
		this.interestCalculator = interestCalculator;
	}

	public DunningInterestCalculator getInterestCalculator()
	{
		return interestCalculator;
	}

	public void setFeeAdder(DunningFeeAdder feeAdder)
	{
		this.feeAdder = feeAdder;
	}

	public DunningFeeAdder getDunningFeeAdder()
	{
		return feeAdder;
	}

	public boolean addDunningStep(DunningStep dunningStep)
	{
		if (dunningSteps.contains(dunningStep))
			return false;
		
		return dunningSteps.add(dunningStep);
	}

	public boolean removeDunningStep(DunningStep dunningStep)
	{
		return dunningSteps.remove(dunningStep);
	}

	public List<DunningStep> getDunningSteps()
	{
		return Collections.unmodifiableList(dunningSteps);
	}

	public DunningStep getDunningStep(int level)
	{
		if (dunningSteps == null)
			return null;
		
		return dunningSteps.get(level);
	}
	
	public List<DunningLetterNotifier> getLetterNotifiers()
	{
		return Collections.unmodifiableList(letterNotifiers);
	}
	
	public void addDunningLetterNotifier(DunningLetterNotifier notifier)
	{
		if (notifier == null)
			return;
		
		if (dunningSteps.contains(notifier))
			return;

		level2LetterNotifiers = null;
		letterNotifiers.add(notifier);
	}
	
	public void removeDunningLetterNotifier(DunningLetterNotifier notifier)
	{
		if (notifier == null)
			return;
		
		if (! letterNotifiers.contains(notifier))
			return;
		
		level2LetterNotifiers = null;
		letterNotifiers.remove(notifier);
	}
	
	public Map<Integer, List<DunningLetterNotifier>> getLevel2LetterNotifiers()
	{
		if (level2LetterNotifiers == null)
		{
			level2LetterNotifiers = new HashMap<Integer, List<DunningLetterNotifier>>(letterNotifiers.size());
			for (DunningLetterNotifier notifier : letterNotifiers)
			{
				List<DunningLetterNotifier> notifierList = level2LetterNotifiers.get(notifier.getDunningLevel());
				if (notifierList == null)
				{
					notifierList = new LinkedList<DunningLetterNotifier>();
					level2LetterNotifiers.put(notifier.getDunningLevel(), notifierList);
				}
				
				notifierList.add(notifier);
			}
		}
		
		return level2LetterNotifiers;
	}

	public void setMoneyFlowConfig(DunningMoneyFlowConfig moneyFlowConfig)
	{
		this.moneyFlowConfig = moneyFlowConfig;
	}

	public DunningMoneyFlowConfig getMoneyFlowConfig()
	{
		return moneyFlowConfig;
	}

	public DunningConfigName getName()
	{
		return name;
	}

	public static DunningConfig getDefaultDunningConfig(PersistenceManager pm, String organisationID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		Query query = pm.newNamedQuery(DunningConfig.class, QUERY_GET_DEFAULT_DUNNING_CONFIG);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("organisationID", organisationID);
		@SuppressWarnings("unchecked")
		Collection<? extends DunningConfig> result = (Collection<? extends DunningConfig>) query.executeWithMap(params);
		if (result == null || result.isEmpty())
			return null;
		
		return result.iterator().next(); 
	}
	
	@Override
	public DunningConfig clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//	DunningConfig clone = (DunningConfig) super.clone();
		DunningConfig clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
		clone.autoMode = autoMode;
		clone.defaultTermOfPaymentMSec = defaultTermOfPaymentMSec;
//	END OF WORKAROUND
		
		clone.dunningConfigID = IDGenerator.nextID(DunningConfig.class);
		if (cloneReferences)
		{
			clone.name = new DunningConfigName(clone);
			clone.name.copyFrom(getName());
			clone.description = new DunningConfigDescription(clone);
			clone.description.copyFrom(getDescription());
			
			context.createClone(this.feeAdder);
			context.createClone(this.interestCalculator);
			clone.dunningSteps = new ArrayList<DunningStep>();
			if (dunningSteps != null)
			{
				for (DunningStep step : dunningSteps)
				{
					context.createClone(step);
				}
			}

			context.createClone(moneyFlowConfig);

			clone.letterNotifiers = new ArrayList<DunningLetterNotifier>();
			if (letterNotifiers != null)
			{
				for (DunningLetterNotifier letterNotifier : letterNotifiers)
				{
					context.createClone(letterNotifier);
				}
			}
		}
		return clone;
	}

	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
		DunningConfig cloneConfig = (DunningConfig) clone;
		cloneConfig.feeAdder = context.getClone(feeAdder);
		cloneConfig.interestCalculator = context.getClone(interestCalculator);
		cloneConfig.moneyFlowConfig = context.getClone(moneyFlowConfig);
		if (dunningSteps != null)
		{
			cloneConfig.dunningSteps.clear();
			for (DunningStep dunningStep : dunningSteps)
			{
				cloneConfig.dunningSteps.add(context.getClone(dunningStep, true));
			}
		}
		if (letterNotifiers != null)
		{
			for (DunningLetterNotifier notifier : letterNotifiers)
			{
				cloneConfig.letterNotifiers.add(context.getClone(notifier, true));
			}
		}
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (dunningConfigID ^ (dunningConfigID >>> 32));
		result = prime * result	+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningConfig other = (DunningConfig) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningConfigID, other.dunningConfigID))
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
		return "DunningConfig [organisationID=" + organisationID + ", dunningConfigID=" + dunningConfigID
				+ ", dunningAutoMode=" + autoMode + ", dunningInterestCalculator=" + interestCalculator
				+ ", dunningFeeAdder=" + feeAdder + ", processDunningSteps="
				+ ", invoiceDunningSteps=" + (dunningSteps != null ? toString(dunningSteps, maxLen) : null) + "]";
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