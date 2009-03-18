package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.util.Map;
import java.util.TreeMap;

public class PriceCalculationStatsTracker
{
	public static final class AccumulationSummaryIdentifierConstants {
		public static final String calculatePriceCell_dirtyStatus = "calculatePriceCell_dirtyStatus";
		public static final String preparePriceCalculation_createPackagedResultPriceConfigs = "preparePriceCalculation_createPackagedResultPriceConfigs";
		public static final String preparePriceCalculation_createResolvableProductTypesMap = "preparePriceCalculation_createResolvableProductTypesMap";
		public static final String calculatePrices = "calculatePrices";
		public static final String packagePriceConfig_getPriceCells = "packagePriceConfig_getPriceCells";
		public static final String packagingResultPriceConfig_createPriceCell = "packagingResultPriceConfig_createPriceCell";
		public static final String innerFormulaPriceConfig_getFormulaCell = "innerFormulaPriceConfig_getFormulaCell";
		public static final String cellReflector_resolvePriceCells = "cellReflector_resolvePriceCells";

		public static final String createMappedLocalPriceCoordinate = "createMappedLocalPriceCoordinate";
		public static final String createMappedLocalPriceCoordinate_customerGroupMapper_getPartnerCustomerGroupID = "createMappedLocalPriceCoordinate_customerGroupMapper_getPartnerCustomerGroupID";
		public static final String createMappedLocalPriceCoordinate_tariffMapper_getPartnerTariffID = "createMappedLocalPriceCoordinate_tariffMapper_getPartnerTariffID";
		public static final String createMappedLocalPriceCoordinate_cloneLocalPriceCoordinate = "createMappedLocalPriceCoordinate_cloneLocalPriceCoordinate";
	}

	public static class AccumulationSummaryRecursive
	{
		public AccumulationSummaryRecursive(AccumulationSummary accumulationSummary, int recursionLevel) {
			this.accumulationSummary = accumulationSummary;
			this.recursionLevel = recursionLevel;
		}

		private AccumulationSummary accumulationSummary;
		private int recursionLevel;
		private long accumulatedTime = 0;
		private long minTime = Long.MAX_VALUE;
		private long maxTime = Long.MIN_VALUE;
		private int invocationCount;

		public String getIdentifier() {
			return accumulationSummary.getIdentifier();
		}
		public AccumulationSummary getAccumulationSummary() {
			return accumulationSummary;
		}
		public int getRecursionLevel() {
			return recursionLevel;
		}
		public long getAccumulatedTime() {
			return accumulatedTime;
		}
		public long getMinTime() {
			return minTime;
		}
		public long getMaxTime() {
			return maxTime;
		}
		public int getInvocationCount() {
			return invocationCount;
		}

		private long startInvocationTimestamp = 0;

		public void startInvocation()
		{
			if (startInvocationTimestamp != 0)
				throw new IllegalStateException("startInvocation already called! identifier=" + getIdentifier() + " recursionLevel=" + recursionLevel);

			startInvocationTimestamp = System.currentTimeMillis();
		}

		public void stopInvocation()
		{
			if (startInvocationTimestamp == 0)
				throw new IllegalStateException("startInvocation not called or stopInvocation already called! identifier=" + getIdentifier() + " recursionLevel=" + recursionLevel);

			long duration = System.currentTimeMillis() - startInvocationTimestamp;
			startInvocationTimestamp = 0;
			++invocationCount;
			accumulatedTime += duration;
			minTime = Math.min(minTime, duration);
			maxTime = Math.max(maxTime, duration);
		}
	}

	public static class AccumulationSummary
	{
		public AccumulationSummary(String identifier) {
			this.identifier = identifier;
		}

		private Map<Integer, AccumulationSummaryRecursive> recursionLevel2accumulationSummaryRecursive = new TreeMap<Integer, AccumulationSummaryRecursive>();

		protected AccumulationSummaryRecursive createAccumulationSummaryRecursive(int recursionLevel)
		{
			AccumulationSummaryRecursive accumulationSummaryRecursive = recursionLevel2accumulationSummaryRecursive.get(recursionLevel);
			if (accumulationSummaryRecursive == null) {
				accumulationSummaryRecursive = new AccumulationSummaryRecursive(this, recursionLevel);
				recursionLevel2accumulationSummaryRecursive.put(recursionLevel, accumulationSummaryRecursive);
			}
			return accumulationSummaryRecursive;
		}

		private String identifier;
		private int invocationCount;
		private int recursionLevel = -1;

		public String getIdentifier() {
			return identifier;
		}
		public int getInvocationCount() {
			return invocationCount;
		}

		public void startInvocation()
		{
			createAccumulationSummaryRecursive(++recursionLevel).startInvocation();
		}

		public void stopInvocation()
		{
			recursionLevel2accumulationSummaryRecursive.get(recursionLevel--).stopInvocation();
			++invocationCount;
		}

		protected void appendToReport(StringBuilder sb)
		{
			for (AccumulationSummaryRecursive accumulationSummaryRecursive : recursionLevel2accumulationSummaryRecursive.values()) {
//				sb.append(getIndentByRecursionLevel(accumulationSummaryRecursive.getRecursionLevel()));
				sb.append("        ");

				sb.append("recursionLevel=");
				sb.append(accumulationSummaryRecursive.getRecursionLevel());

				sb.append(" invocationCount=");
				sb.append(accumulationSummaryRecursive.getInvocationCount());

				sb.append(" accumulatedTime=");
				sb.append(accumulationSummaryRecursive.getAccumulatedTime());

				sb.append(" minTime=");
				sb.append(accumulationSummaryRecursive.getMinTime());

				sb.append(" maxTime=");
				sb.append(accumulationSummaryRecursive.getMaxTime());

				sb.append('\n');
			}
		}
	}

	private Map<String, AccumulationSummary> identifier2accumulationSummary = new TreeMap<String, AccumulationSummary>();

	public AccumulationSummary createAccumulationSummary(String identifier)
	{
		AccumulationSummary accumulationSummary = identifier2accumulationSummary.get(identifier);
		if (accumulationSummary == null) {
			accumulationSummary = new AccumulationSummary(identifier);
			identifier2accumulationSummary.put(identifier, accumulationSummary);
		}
		return accumulationSummary;
	}

	public void clear()
	{
		identifier2accumulationSummary.clear();
	}

//	private static String getIndentByRecursionLevel(int recursionLevel)
//	{
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < recursionLevel; ++i)
//			sb.append("  ");
//		return sb.toString();
//	}

	public String createReport()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		for (AccumulationSummary accumulationSummary : identifier2accumulationSummary.values()) {
			sb.append("    ");
			sb.append(accumulationSummary.getIdentifier());
			sb.append(" invocationCount=");
			sb.append(accumulationSummary.getInvocationCount());
			sb.append('\n');
			accumulationSummary.appendToReport(sb);
		}

		return sb.toString();
	}
}
