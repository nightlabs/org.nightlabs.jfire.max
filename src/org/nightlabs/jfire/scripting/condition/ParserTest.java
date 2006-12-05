package org.nightlabs.jfire.scripting.condition;

import java.util.List;

public class ParserTest 
{

	public static void main(String[] args) 
	{
//		String script = "(($variable==Value)&&($variable2!=Value2)&&($variable3>Value3))||($variable4==Value4)";
		String script = "(($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3))))";
//		String script = "($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3)))";		
//		String script = "$variable4==Value4";
//		String script = "($variable4==Value4)";
		ConstrainedConditionGenerator generator = new JavaScriptConditionGenerator();
		ConstrainedConditionScriptParser parser = new ConstrainedConditionScriptParser();
		
		ICondition condition = parser.getCondition(generator, script);
		String conditionText = generator.getScriptText(condition);
//		String conditionText = condition.getScriptText();		
		System.out.println("scriptText = "+script);
		System.out.println("conditionText = "+conditionText);
		boolean equals = script.equals(conditionText);
		System.out.println("equals = "+equals);
		
//		getSubContainers(script, generator, parser);
	}

//	private static void getSubContainers(String text, ConstrainedConditionGenerator generator, ConstrainedConditionScriptParser parser) 
//	{
//		List<String> subContainer = parser.getContainerSubStrings(text, 
//				generator.getOpenContainerString(), 
//				generator.getCloseContainerString());		
//		System.out.println("text = "+text);
//		System.out.println("subContainer.size() = "+subContainer.size());
//		for (String string : subContainer) {
//			System.out.println("subContainer = "+string);
//			System.out.println();
//			getSubContainers(string, generator, parser);
//		}
//		System.out.println();
//	}
}
