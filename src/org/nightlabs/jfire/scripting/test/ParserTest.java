package org.nightlabs.jfire.scripting.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.condition.ConstrainedConditionGenerator;
import org.nightlabs.jfire.scripting.condition.DefaultCompareOperatorProvider;
import org.nightlabs.jfire.scripting.condition.ICondition;
import org.nightlabs.jfire.scripting.condition.JavaScriptConditionGenerator;
import org.nightlabs.jfire.scripting.condition.LabelProvider;
import org.nightlabs.jfire.scripting.condition.ScriptConditioner;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;


public class ParserTest 
{

	public static void main(String[] args) 
	{
//		String script = "(($variable==Value)&&($variable2!=Value2)&&($variable3>Value3))||($variable4==Value4)";
//		String script = "(($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3))))";
//		String script = "($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3)))";		
//		String script = "$variable4==Value4";
//		String script = "($variable4==Value4)";
		
		StringBuffer sb = new StringBuffer();
		sb.append("importPackage(Packages.javax.jdo);");
//		sb.append("importPackage(Packages.org.nightlabs.jdo);");
//		sb.append("JDOHelper.getObjectId(Test)==ObjectIDUtil.createObjectID(\"jdo/org.b.MyClassID?fieldA=0\");");
		String scriptIDString = getScriptID().toString();
		sb.append("JDOHelper.getObjectId(Test).toString()=="+scriptIDString+"");		
		String script = sb.toString();		
		
		ConstrainedConditionGenerator generator = new JavaScriptConditionGenerator();
		generator.setScriptConditioner(getScriptConditioner());

		ICondition condition = generator.getCondition(script);
		String conditionText = generator.getScriptText(condition);		
		System.out.println("scriptText = "+script);
		System.out.println("conditionText = "+conditionText);
		boolean equals = script.equals(conditionText);
		System.out.println("equals = "+equals);		
	}

//	private static Collection<ScriptConditioner> getScriptConditioner() 
//	{
//		return Collections.EMPTY_LIST;
//	}

	private static Collection<ScriptConditioner> getScriptConditioner() 
	{
		String organisationID = "devil.nightlabs.org";
		String scriptType = "IpanemaTicketing-Type-Ticket";
		String scriptIDName = "Test";
//		ScriptRegistryItemID scriptID = ScriptRegistryItemID.create(organisationID, scriptType, scriptIDName);
		ScriptRegistryItemID scriptID = getScriptID();
		System.out.println("scriptID = "+scriptID);
		ScriptCategory scriptCategory = new ScriptCategory(organisationID, scriptType, "Ticketing"); 
		Script script = new Script(scriptCategory, organisationID, scriptType, "Test");
		Collection possibleValues = new ArrayList();
//		possibleValues.add(new Tariff(organisationID, 0));
		possibleValues.add(script);
		ScriptConditioner sc = new ScriptConditioner(scriptID, script, scriptIDName, 
				DefaultCompareOperatorProvider.sharedInstance().getEqualOperators(), 
				possibleValues, LabelProvider.class.getName());
		
		Collection<ScriptConditioner> scs = new ArrayList<ScriptConditioner>();
		scs.add(sc);
		return scs;
	}
	
	private static ScriptRegistryItemID getScriptID() 
	{
		String organisationID = "devil.nightlabs.org";
		String scriptType = "IpanemaTicketing-Type-Ticket";
		String scriptIDName = "Test";
		return ScriptRegistryItemID.create(organisationID, scriptType, scriptIDName);
//		System.out.println("scriptID = "+scriptID);		
	}
}
