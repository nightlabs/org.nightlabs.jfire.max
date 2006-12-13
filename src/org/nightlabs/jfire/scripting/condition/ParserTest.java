package org.nightlabs.jfire.scripting.condition;


public class ParserTest 
{

	public static void main(String[] args) 
	{
//		String script = "(($variable==Value)&&($variable2!=Value2)&&($variable3>Value3))||($variable4==Value4)";
		String script = "(($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3))))";
//		String script = "($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3)))";		
//		String script = "$variable4==Value4";
//		String script = "($variable4==Value4)";
		
//		ConstrainedConditionGenerator generator = new JavaScriptConditionGenerator();
//		ConstrainedConditionScriptParser parser = new ConstrainedConditionScriptParser();
//		
//		ICondition condition = parser.getCondition(generator, script);
//		String conditionText = generator.getScriptText(condition);		
//		System.out.println("scriptText = "+script);
//		System.out.println("conditionText = "+conditionText);
//		boolean equals = script.equals(conditionText);
//		System.out.println("equals = "+equals);		
	}

}
