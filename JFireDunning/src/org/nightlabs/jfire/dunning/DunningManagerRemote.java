package org.nightlabs.jfire.dunning;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.dunning.id.DunningConfigCustomerID;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningFeeAdderID;
import org.nightlabs.jfire.dunning.id.DunningFeeTypeID;
import org.nightlabs.jfire.dunning.id.DunningInterestCalculatorID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface DunningManagerRemote 
{
	DunningConfig storeDunningConfig(DunningConfig dunningConfig, boolean get, String[] fetchGroups, int maxFetchDepth);
	
	Set<DunningConfigID> getDunningConfigIDs();
	DunningConfig getDefaultDunningConfigOfOrganisation(String[] fetchGroups, int maxFetchDepth);
	DunningConfig getDefaultDunningConfigOfOrganisation(String organisationID, String[] fetchGroups, int maxFetchDepth);
	List<DunningConfig> getDunningConfigs(Collection<DunningConfigID> dunningConfigIDs, String[] fetchGroups, int maxFetchDepth);
	
	DunningProcess getDunningProcess(DunningProcessID dunningProcessID, String[] fetchGroups, int maxFetchDepth);
	List<DunningProcess> getDunningProcesses(Collection<DunningProcessID> dunningProcessIDs, String[] fetchGroups, int maxFetchDepth);
	Set<DunningProcessID> getDunningProcessIDs();

	List<DunningInterestCalculator> getDunningInterestCalculators(Set<DunningInterestCalculatorID> dunningInterestCalculatorIDs, String[] fetchGroups, int maxFetchDepth);
	DunningInterestCalculator getDunningInterestCalculator(DunningInterestCalculatorID dunningInterestCalculatorID,	String[] fetchGroups, int maxFetchDepth);

	List<DunningFeeAdder> getDunningFeeAdders(Set<DunningFeeAdderID> dunningFeeAdderIDs, String[] fetchGroups, int maxFetchDepth);
	DunningFeeAdder getDunningFeeAdder(DunningFeeAdderID dunningFeeAdderID,	String[] fetchGroups, int maxFetchDepth);
	

	Set<DunningFeeTypeID> getDunningFeeTypeIDs();
	List<DunningFeeType> getDunningFeeTypes(
			Collection<DunningFeeTypeID> dunningFeeTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	DunningFeeType storeDunningFeeType(DunningFeeType dunningFeeType, boolean get, String[] fetchGroups, int maxFetchDepth);

	Set<DunningConfigCustomerID> getDunningConfigCustomerIDs();
	List<DunningConfigCustomer> getDunningConfigCustomers(
			Collection<DunningConfigCustomerID> dunningConfigCustomerIDs,
			String[] fetchGroups, int maxFetchDepth);

	DunningConfigCustomer storeDunningConfigCustomer(
			DunningConfigCustomer dunningConfigCustomer, boolean get,
			String[] fetchGroups, int maxFetchDepth);
	
	
	void processAutomaticDunning(TaskID taskID)
		throws Exception;

	void initialise() throws Exception;
}