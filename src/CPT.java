import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


public class CPT implements Comparable<CPT>{
	//members
	ArrayList<Variable> _variables;
	ArrayList<ArrayList<String>> _table;

	//constructors
	public CPT(LinkedList<String> text, Variable var) {
		_variables = new ArrayList<>();
		for (Variable parent : var.get_parents())
			_variables.add(parent);
		_variables.add(var);

		_table = fillTableWithValues(text, var.get_values());
	}

	public CPT() {
		_table = new ArrayList<>();
	}

	//methods	
	private ArrayList<ArrayList<String>> fillTableWithValues(LinkedList<String> text, String[] values) {
		for (int lineInText = 0; lineInText < text.size(); lineInText++) {
			String line = text.removeFirst();
			if (!line.equals(""))
				text.addLast(line);
		}
		ArrayList<ArrayList<String>> table = new ArrayList<>();
		//add to each line in text the missing value
		int numOfParents = _variables.size()-1;
		String missingValue = values[values.length-1];
		for (int lineInTextIndex = 0; lineInTextIndex < text.size(); lineInTextIndex++){
			String lineInText = text.removeFirst();
			int currentValueProbColunm = numOfParents+1;
				double currProb = 1;
				String[] lineParts = StrAndTxtManipulator.splitAndLoseSpaces(lineInText);
				for (int selfValIndex = 0; selfValIndex < values.length-1; selfValIndex++) {
					String selfVarProbSTR = lineParts[currentValueProbColunm];
					currentValueProbColunm = currentValueProbColunm + 2;
					double selfVarProb = Double.parseDouble(selfVarProbSTR);
					currProb = currProb - selfVarProb;
				}
				lineInText = lineInText + "," + "=" + missingValue + "," + currProb;
				text.addLast(lineInText);
		}
		
		//fill parent vals only in table
		int numOfSelfValues = values.length;
		int numOfLinesShouldBeInTable = text.size() * numOfSelfValues;
		for (int lineIndex = 0; lineIndex < numOfLinesShouldBeInTable; lineIndex++) {
			ArrayList<String> lineInTable = new ArrayList<>();
			if (numOfParents > 0) {
				String line = text.removeFirst();
				text.addLast(line);	//push to the end of the list
				String[] lineParts = StrAndTxtManipulator.splitAndLoseSpaces(line) ;
				//add parents values to current line
				for (int parentIndex = 0; parentIndex < numOfParents; parentIndex++) {
					lineInTable.add(lineParts[parentIndex]);
				}
			}
			table.add(lineInTable);
		}

		//fill self vals
		int currentValueColunm = numOfParents;
		for (int selfVal = 0; selfVal < numOfSelfValues; selfVal++) {
			for (int indexOfLine = selfVal * text.size(); indexOfLine < (selfVal+1) * text.size(); indexOfLine++) {
				String lineFromText = text.removeFirst();
				text.addLast(lineFromText);	//push to the end of the list
				String[] lineParts = StrAndTxtManipulator.splitAndLoseSpaces(lineFromText) ;
				String selfValSTR = lineParts[currentValueColunm];
				String selfValProb = lineParts[currentValueColunm+1];
				ArrayList<String> lineInTable = table.get(indexOfLine);
				lineInTable.add(selfValSTR.replace("=", ""));		//lose "=");
				lineInTable.add(selfValProb);
			}
			currentValueColunm = currentValueColunm + 2;
		}
		return table;
	}

	public ArrayList<ArrayList<String>> get_table(){
		return _table;
	}

	public ArrayList<Variable> get_variables(){
		return _variables;
	}

	public void set_variables(ArrayList<Variable> variables) {
		_variables = variables;
	}

	public void set_table(ArrayList<ArrayList<String>> table) {
		_table = table;
	}

	@Override
	public int compareTo(CPT other) {
		if (_variables.size() < other.get_variables().size())
			return -1;
		else if (_variables.size() == other.get_variables().size()) {
			return 0;
		}
		return 1;
	}

	@Override
	public String toString() {
		String ans = "\n-------------------------------BEGIN\n" + _variables.toString(); 
		for (ArrayList<String> line : _table) {
			ans = ans + "\n" + line.toString();
		}
		ans = ans + "\n-------------------------------END\n";
		return ans;
	}
}
