import java.util.LinkedList;

public class BNFactory {
	/**
	 * static function that initiates Bnetwork object from text file input
	 * @param text - all lines from input file in an linked-list
	 * @return Bnetwork object
	 * @throws Exception - if something goes wrong...
	 */
	public static Bnetwork createBNFromText(LinkedList<String> text) throws Exception {
		//initiating vars with only names
		LinkedList<Variable> varieables;
		String currentLine = text.poll();
		while (!currentLine.startsWith("Variables:"))
			currentLine = text.poll();
		int numOfCharsTillVars = 11;
		currentLine = currentLine.substring(numOfCharsTillVars);
		String[] varsNames = StrAndTxtManipulator.splitAndLoseSpaces(currentLine);
		varieables = VariableFactory.namesToVars(varsNames);
		//adding values to all vars
		while (!text.isEmpty()){
			//pass on all text find "Var" blocks			
			LinkedList<String> varText = new LinkedList<>();
			if (currentLine.startsWith("Var ")){
				varText.add(currentLine);
				currentLine = text.poll();
				while (currentLine != null && !currentLine.startsWith("Var ")) { 
					varText.add(currentLine);
					currentLine = text.poll();
				}
				
				Variable currVar = VariableFactory.setVarByText(varText, varieables);
				varieables.add(currVar);
			}
			else
				currentLine = text.poll();
		}
		Bnetwork currBN = new Bnetwork(varieables);
		return currBN;
	}
}

