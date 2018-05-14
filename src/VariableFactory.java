import java.util.Arrays;
import java.util.LinkedList;

public class VariableFactory {

	public static LinkedList<Variable> namesToVars(String[] varsNames) {
		LinkedList<Variable> ans = new LinkedList<>();
		for (String varName : varsNames) {
			Variable currrentVar = new Variable(varName);
			ans.add(currrentVar);
		}
		return ans;
	}

	public static Variable setVarByText(LinkedList<String> varText, LinkedList<Variable> vars) throws Exception {
		//get var name
		String currentLine = varText.poll();
		while (!currentLine.startsWith("Var "))
			currentLine = varText.poll();
		String[] varHeader = currentLine.split(" ");
		String varName = varHeader[1];
		Variable currVar = lookForVar(varName, vars);
		currentLine = varText.poll();
		//get & set var values
		int charsTillEndOfValsHeader = 8;
		currentLine = currentLine.substring(charsTillEndOfValsHeader);
		String[] varValues= StrAndTxtManipulator.splitAndLoseSpaces(currentLine);
		currVar.set_values(varValues);
		//get & set var parents
		currentLine = varText.poll();
		LinkedList<Variable> varParents = readParents(currentLine, vars);
		//System.out.println(currVar.get_name()+" was set with new parents: "+varParents.toString());
		currVar.set_parents(varParents);
		currentLine = varText.poll();
		//get var CPT
		while (!currentLine.equals("CPT:"))
			currentLine = varText.poll();
		CPT varCPT = new CPT(varText, currVar);
		//System.out.println(currVar.get_name()+" was set with new CPT");
		currVar.set_cpt(varCPT);
		return currVar;
	}

	private static LinkedList<Variable> readParents(String line, LinkedList<Variable> vars) throws Exception {		
		LinkedList<Variable> parents = new LinkedList<>();
		int spaceFromParentsHeader = 8;
		line = line.substring(spaceFromParentsHeader);
		String[] parentsNames = StrAndTxtManipulator.splitAndLoseSpaces(line);
		for (String parentName : parentsNames) {
			if (parentName.equals("none"))
				break;
			Variable currentParent = lookForVar(parentName, vars);
			parents.add(currentParent);
		}
		return parents;
	}

	public static Variable lookForVar(String name, LinkedList<Variable> vars) throws Exception {
		for (Variable var : vars) {
			String currName = var.get_name();
			if (currName.equals(name))
				return var;
		}
		throw new Exception("Error: there is no variable with the name <"+name+">.");
	}
	
	public static void setChild(LinkedList<Variable> parents, Variable child) {
		for (Variable parent : parents) {
			parent.addChild(child);
		}
	}

}
