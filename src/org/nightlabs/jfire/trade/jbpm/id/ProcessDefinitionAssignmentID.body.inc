
	public static ProcessDefinitionAssignmentID create(Class statableClass, org.nightlabs.jfire.trade.TradeSide tradeSide)
	{
		return create(statableClass.getName(), tradeSide.toString());
	}
	public static ProcessDefinitionAssignmentID create(String statableClass, org.nightlabs.jfire.trade.TradeSide tradeSide)
	{
		return create(statableClass, tradeSide.toString());
	}