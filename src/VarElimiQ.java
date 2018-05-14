import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class VarElimiQ implements BNQuery{
	//members
	private int _nuOfSums, _numOfMuls;
	private Variable _probVar;
	private ArrayList<Variable> _observds, _hidden;
	private Bnetwork _bNetwork;
	private String _queryText;
	private String _ProbGivenValue; 
	static private boolean DEBUG = true;
	//constructors
	public VarElimiQ(String line, Bnetwork bNetwork) throws Exception {
		_nuOfSums = _numOfMuls = 0;
		_observds = new ArrayList<>();
		_hidden = new ArrayList<>();
		_bNetwork = bNetwork;
		_queryText = line;
	}

	//methods	
	private void initByLine(String queryLine) throws Exception {
		queryLine = queryLine.substring(2);		//lose P(
		String query_Hidden[] = queryLine.split("\\)");	//Separate query and hidden
		String query = query_Hidden[0];
		String hidden = query_Hidden[1].substring(1);
		String ProbVar_Observeds[] = query.split("\\|");
		String ProbVarWithVal = ProbVar_Observeds[0];
		String ObservedsWithVals = ProbVar_Observeds[1];
		String ProbVar_Val[] = ProbVarWithVal.split("\\=");
		String ProbVarName = ProbVar_Val[0];
		String PVarVal = ProbVar_Val[1];
		_probVar = VariableFactory.lookForVar(ProbVarName, _bNetwork.get_varieables());
		_probVar.set_givenValue(PVarVal);
		_ProbGivenValue = PVarVal;
		String observds[] = ObservedsWithVals.split(",");
		for (String observd : observds) {
			String separateObservdAndVal[] = observd.split("=");
			String observdName = separateObservdAndVal[0];
			String observdVal = separateObservdAndVal[1];
			Variable currObserved = VariableFactory.lookForVar(observdName, _bNetwork.get_varieables());
			currObserved.set_givenValue(observdVal);
			_observds.add(currObserved);
		}

		String separateHidden[] = hidden.split("-");
		for (String hiddenVar : separateHidden) {
			Variable currHidden = VariableFactory.lookForVar(hiddenVar, _bNetwork.get_varieables());
			_hidden.add(currHidden);
		}
	}

	@Override
	public String calcQuery() {
		try {
			initByLine(_queryText);
		} catch (Exception e) {}
		if (DEBUG) System.out.println("quert: "+_queryText);
		if (DEBUG) System.out.println("_hidden before loseUnnecessaryHidden(): "+_hidden.toString());
		loseUnnecessaryHidden();
		if (DEBUG) System.out.println("_hidden adter loseUnnecessaryHidden(): "+_hidden.toString());
		LinkedList<CPT> factors = new LinkedList<>();
		factors.add(_probVar.get_cpt());
		//init factors list
		for (Variable observd : _observds) {
			factors.add(observd.get_cpt());
		}
		for (Variable hidden : _hidden) {
			factors.add(hidden.get_cpt());
		}
		if (DEBUG) System.out.println("all factors: "+factors.toString());
		//for each hidden variable, eliminate it
		while (!_hidden.isEmpty()) {
			Variable currHidden = _hidden.remove(0);
			if (DEBUG) System.out.println("currHidden: "+currHidden.get_name());

			LinkedList<CPT> relevantFactors = new LinkedList<>();
			//collect all factors contains current hidden
			LinkedList<CPT> factorsToRemove = new LinkedList<>();
			for (CPT factor : factors)
				if (factor.get_variables().contains(currHidden)) {
					relevantFactors.add(factor);
					factorsToRemove.add(factor);					
				}
			for (CPT factor : factorsToRemove)
				factors.remove(factor);
			//sort factors by their num of rows, and start elimination process 
			Collections.sort(relevantFactors);
			if (DEBUG) System.out.println("relevantFactors: "+relevantFactors.toString());
			while (relevantFactors.size() > 1) {
				CPT factor1 = relevantFactors.pop();
				CPT factor2 = relevantFactors.pop();
				CPT newFactor = join(factor1, factor2, currHidden);
				relevantFactors.add(newFactor);
				Collections.sort(relevantFactors);
			}
			if (DEBUG) System.out.println("Start elimination on: "+currHidden.get_name());
			CPT eliminatedFactor = eliminate(relevantFactors.pop(), currHidden);
			if (DEBUG) System.out.println("elimination result: "+eliminatedFactor.toString());
			factors.add(eliminatedFactor);
			if (DEBUG) System.out.println("all factors: "+factors.toString());
		}
		//last join
		while (factors.size() > 1) {
			if (DEBUG) System.out.println("done with hidden vars. join on left CPTs containing "+_probVar.get_name());
			Collections.sort(factors);
			CPT factor1 = factors.pop();
			CPT factor2 = factors.pop();
			if (DEBUG) System.out.println("join: "+factor1.toString()+factor2.toString());
			CPT newFactor = join(factor1, factor2, _probVar);
			if (DEBUG) System.out.println("join result : "+newFactor.toString());
			factors.add(newFactor);
			if (DEBUG) System.out.println("all factors: "+factors.toString());
			Collections.sort(factors);
		}
		//normalize
		CPT normalizedFactor = normalize(factors.pop());
		if (DEBUG) System.out.println("normalizedFactor: "+normalizedFactor.toString());
		for (ArrayList<String> line : normalizedFactor.get_table()) {
			if (line.get(line.size()-2).equals(_probVar.get_givenValue())){
				String finalValSTR = line.get(line.size()-1);
				double ans = Double.parseDouble(finalValSTR);
				ans = Math.floor(ans * 100000) / 100000;
				resetSettings();
				return (ans + "," + _nuOfSums + "," + _numOfMuls);
			}
		}
		System.out.println("problem calulating query.");
		resetSettings();
		return null;
	}

	private CPT normalize(CPT factorToNormalize) {
		if (DEBUG) System.out.println("start normalization process on: "+factorToNormalize.toString());
		double sumOfProbs = 0;
		for (ArrayList<String> line : factorToNormalize.get_table()) {
			double currProb = Double.parseDouble(line.get(line.size()-1));
			sumOfProbs = sumOfProbs + currProb;
			_nuOfSums++;
		}
		_nuOfSums--;	//because first summong above is from zero, so no need to count it
		if (DEBUG) System.out.println("sumOfProbs: "+sumOfProbs);
		double x = 1 / sumOfProbs;
		if (DEBUG) System.out.println("alpha = "+x);
		for (ArrayList<String> line : factorToNormalize.get_table()) {
			if (DEBUG) System.out.println("line before normalization: "+line.toString());
			double currProb = Double.parseDouble(line.get(line.size()-1));
			currProb = currProb * x;
			String normalizedProb = String.valueOf(currProb);
			line.set(line.size()-1, normalizedProb);
			if (DEBUG) System.out.println("line after normalization: "+line.toString());
		}
		_numOfMuls++;	//my implementation of the algorithm does the full process including return factor after normalization, not only normal the wanted value. that is the reason i put "_numOfMuls++;" in this line and not in the loop- so it count only the MUL of the wanted line for the query.
		if (DEBUG) System.out.println("factor after normalization process: "+factorToNormalize.toString());
		return factorToNormalize;
	}

	private CPT eliminate(CPT factorToEliminateFrom, Variable eliminatedVar) {
		//copy input factor
		ArrayList<Variable> varHeaders = new ArrayList<>(factorToEliminateFrom.get_variables());
		ArrayList<ArrayList<String>> varTable = new ArrayList<>(factorToEliminateFrom.get_table());
		int indexOfEliminatedVar = varHeaders.indexOf(eliminatedVar);
		//remove eliminated Var from factor
		varHeaders.remove(indexOfEliminatedVar);
		for (ArrayList<String> line : varTable) {
			line.remove(indexOfEliminatedVar);
		}
		ArrayList<String> lineSignitures = new ArrayList<>();
		for (ArrayList<String> line : varTable) {
			String lineSigniture = new String();
			for (int valIndex = 0; valIndex < line.size()-1; valIndex++){
				String val = line.get(valIndex);
				lineSigniture = lineSigniture + val;
			}
			lineSignitures.add(lineSigniture);
		}
		//build new table
		int numOfLinesInNewFactor = 1;
		for (Variable var : varHeaders) {
			numOfLinesInNewFactor = numOfLinesInNewFactor * var.get_values().length; 
			_numOfMuls++;
		}
		_numOfMuls--;	//because first MUL is by 1 for the first line - no need to count
		//eliminate var
		while (varTable.size() > numOfLinesInNewFactor) {
			for (int indexOfLineSigniture = 0; indexOfLineSigniture < lineSignitures.size(); indexOfLineSigniture++) {
				String lineSigniture = lineSignitures.get(indexOfLineSigniture);
				for (int otherLineSigniture = indexOfLineSigniture+1; otherLineSigniture < lineSignitures.size(); otherLineSigniture++) {
					if (lineSigniture.equals(lineSignitures.get(otherLineSigniture))) { 	//if (lineSigniture == otherLineSigniture)
						lineSignitures.remove(otherLineSigniture);
						//sum probabilities and remove second line
						ArrayList<String> line1 = varTable.get(indexOfLineSigniture);
						ArrayList<String> line2 = varTable.get(otherLineSigniture);
						double val1 = Double.parseDouble(line1.get(line1.size()-1));
						double val2 = Double.parseDouble(line2.get(line1.size()-1));
						double sumLines = val1 + val2;
						_nuOfSums++;
						String sumSTR = Double.toString(sumLines);
						line1.set(line1.size()-1, sumSTR);
						varTable.remove(line2);
						otherLineSigniture--;
					}
				}
			}
		}
		//build new CPT
		CPT eliminatedFactor = new CPT();
		eliminatedFactor.set_table(varTable);
		eliminatedFactor.set_variables(varHeaders);
		return eliminatedFactor; 
	}

	private boolean areLinesEquale(ArrayList<String> line1, ArrayList<String> line2) {
		if (DEBUG) System.out.println("checking if lines are equal: " + line1 + " , " + line2);
		for (int indexOfVal = 0; indexOfVal < line1.size()-1; indexOfVal++) {	//for-each value in line but the last one (prob...)
			String line1CurrVal = line1.get(indexOfVal);
			String line2CurrVal = line2.get(indexOfVal);
			if (line1CurrVal.equals(line2CurrVal)) {
				if (DEBUG) System.out.println("answer: true");
				return true;
			}
			else
				break;
		}
		if (DEBUG) System.out.println("answer: false");
		return false;
	}

	private CPT copyAndLoseObservdsFromCPT(CPT originFactor) {
		//create copy of input factor
		CPT factorCopy = new CPT();
		ArrayList<ArrayList<String>> copyTable = new ArrayList<>();
		ArrayList<ArrayList<String>> originFactorTable = originFactor.get_table();
		//copy table
		for (ArrayList<String> line : originFactorTable) {
			ArrayList<String> newLine = new ArrayList<>();
			for (String val : line) {
				String copyVal = new String(val);
				newLine.add(copyVal);
			}
			copyTable.add(newLine);
		}
		factorCopy.set_table(copyTable);
		//copy vars
		ArrayList<Variable> vars = new ArrayList<>();
		for (Variable var : originFactor.get_variables()) {
			vars.add(var);
		}
		factorCopy.set_variables(vars);
		//for-each observed var
		ArrayList<ArrayList<String>> linesToRemove = new ArrayList<>();
		ArrayList<Integer> indexesToRemove = new ArrayList<>();
		for (Variable observed : _observds) {	//for-each observed var
			int observedIndexInFactor = factorCopy.get_variables().indexOf(observed);
			if (observedIndexInFactor > -1) {	//if observed exists in this factor
				indexesToRemove.add(observedIndexInFactor);
				for (ArrayList<String> line : factorCopy.get_table()) {	//remove lines with value not equal to given value in query
					String currValue = line.get(observedIndexInFactor);
					String givenVal = observed.get_givenValue();
					if (currValue.equals(givenVal)==false)
						linesToRemove.add(line);	//collect all lines need to be removed
				}
				//remove all lines collected above
				for (ArrayList<String> line : linesToRemove)
					factorCopy.get_table().remove(line);
			}
		}
		//remove all observds and their values
		Collections.sort(indexesToRemove);
		Collections.reverse(indexesToRemove);
		for (int index : indexesToRemove) {
			factorCopy.get_variables().remove(index);
			for (ArrayList<String> line : factorCopy.get_table()) {
				line.remove(index);
			}	
		}
		return factorCopy;
	}

	private CPT join(CPT f1, CPT f2, Variable var) {
		if (DEBUG) System.out.println("join: "+f1.toString()+f2.toString());
		//filter factor1 from known values
		CPT factor1 = copyAndLoseObservdsFromCPT(f1);
		CPT factor2 = copyAndLoseObservdsFromCPT(f2);
		ArrayList<Variable> f1Vars = factor1.get_variables();
		ArrayList<Variable> f2Vars = factor2.get_variables();
		//union all vars f1Vars & f2Vars
		ArrayList<Variable> varHeaders = new ArrayList<>();
		for (Variable variable : f1Vars) {
			varHeaders.add(variable);
		}
		for (Variable variable : f2Vars) {
			if (!varHeaders.contains(variable)) 
				varHeaders.add(variable);	
		}
		//create a new factor, set inside it all values in lines, and then set the right probability for each line
		ArrayList<ArrayList<String>> lines = fillLinesWithVals(varHeaders);
		fillLinesWithProbabilities(lines, varHeaders, factor1, factor2);
		CPT joinedFactor = new CPT();
		joinedFactor.set_table(lines);
		joinedFactor.set_variables(varHeaders);
		if (DEBUG) System.out.println("join result : "+joinedFactor.toString());
		return joinedFactor;
	}

	private boolean areMatchLines(ArrayList<String> line1, ArrayList<String> line2) {
		String lineSigniture1 = new String();
		String lineSigniture2 = new String();
		for (int strIndex = 0; strIndex < line1.size()-2; strIndex++) {
			String value = line1.get(strIndex);
			lineSigniture1 = lineSigniture1 + value;
		}
		for (int strIndex = 0; strIndex < line2.size()-2; strIndex++) {
			String value = line2.get(strIndex);
			lineSigniture2 = lineSigniture2 + value;
		}
		return (lineSigniture1.equals(lineSigniture2));
	}

	private void fillLinesWithProbabilities (ArrayList<ArrayList<String>> lines, ArrayList<Variable> varHeaders, CPT factor1, CPT factor2) {
		for (ArrayList<String> line : lines) {
			line.add("1");
		}
		for (ArrayList<String> line : lines) {
			double currP = Double.parseDouble(line.get(line.size()-1));
			double factor1Prob = getProb(factor1, line, varHeaders);
			double factor2Prob = getProb(factor2, line, varHeaders);
			double updatedProb = currP * factor1Prob * factor2Prob;
			_numOfMuls++;
			//	_numOfMuls++;
			if (DEBUG) System.out.println("currP * factor1Prob * factor2Prob = updatedProb >>> "+currP+"*"+factor1Prob+"*"+factor2Prob+"="+updatedProb);//***
			line.set(line.size()-1, String.valueOf(updatedProb));
		}
	}

	/**
	 *
	 * @param checkedFactor CPT to search a match in
	 * @param inputLine line to compare to in factor
	 * @param varHeaders order of vars in origin factor
	 * @return probability in matching line
	 */
	private double getProb(CPT checkedFactor, ArrayList<String> inputLine, ArrayList<Variable> varHeaders) {
		//find vars in common for input line and input factor
		ArrayList<Variable> relevantVars = new ArrayList<>();
		for (Variable variable : varHeaders) {
			if (checkedFactor.get_variables().contains(variable)) {
				relevantVars.add(variable);
			}
		}
		//
		for (ArrayList<String> lineInFactor : checkedFactor.get_table()) {
			for (Variable relevantVar : relevantVars) {
				int index_Of_relevant_Var_In_Factor_Current_Line = checkedFactor.get_variables().indexOf(relevantVar);
				String val_of_curr_var_in_curr_line = lineInFactor.get(index_Of_relevant_Var_In_Factor_Current_Line);
				int index_Of_relevant_Var_In_Input_Line = varHeaders.indexOf(relevantVar);
				String val_of_curr_var_in_input_line = inputLine.get(index_Of_relevant_Var_In_Input_Line);
				if (val_of_curr_var_in_curr_line.equals(val_of_curr_var_in_input_line) == false) {
					break;
				}
				Variable lastRelevantVar = relevantVars.get(relevantVars.size()-1);
				if (relevantVar.equals(lastRelevantVar)){	//if that is the last relevant var, means line was found
					String valSTR = lineInFactor.get(lineInFactor.size()-1);
					return Double.parseDouble(valSTR);
				}
			}	
		}
		return -1;

	}

	private ArrayList<ArrayList<String>> fillLinesWithVals(ArrayList<Variable> varHeaders) {
		ArrayList<ArrayList<String>> lines= new ArrayList<>();
		int numOfLines = 1;
		//count how many lines should be in table
		for (Variable variable : varHeaders) {		
			int numOfVals;
			if (_observds.contains(variable))
				numOfVals = 1;
			else
				numOfVals = variable.get_values().length;
			numOfLines = numOfLines * numOfVals;
		}
		//initiate table
		for (int line = 0; line < numOfLines; line++) {		
			lines.add(new ArrayList<String>());
		}
		int numOfValues;
		int numOfCellsWithCurrVar = numOfLines;
		//fill table for each column and var values
		for (int varHeader = 0; varHeader < varHeaders.size(); varHeader++) {	
			Variable currVarHeader = varHeaders.get(varHeader);
			if (_observds.contains(currVarHeader))
				numOfValues = 1;
			else
				numOfValues = currVarHeader.get_values().length;
			numOfCellsWithCurrVar = numOfCellsWithCurrVar / numOfValues;

			LinkedList<String> valsQueue = new LinkedList<>();
			String[] relevantVals;
			if (_observds.contains(currVarHeader)) {
				relevantVals = new String[1];
				relevantVals[0] = currVarHeader.get_givenValue();
			}
			else
				relevantVals = currVarHeader.get_values();
			for (String value : relevantVals) {	//for each value, add it to queue as number of time as it should appear
				for (int time = 0; time < numOfCellsWithCurrVar; time++) {
					valsQueue.add(value);
				}
			}

			for (int line = 0; line < numOfLines; line++) {
				String valueToSet = valsQueue.pop();
				ArrayList<String> currLine = lines.get(line);
				currLine.add(valueToSet);
				valsQueue.add(valueToSet);
			}
		}
		return lines;
	}

	private void loseUnnecessaryHidden() {
		LinkedList<Variable> loseFromTheseVars = new LinkedList<>();
		for (Variable hidden : _hidden) {
			if (isAncestor(hidden) == false) {
				loseFromTheseVars.add(hidden);
			}
		}
		for (Variable variable : loseFromTheseVars) {
			_hidden.remove(variable);
		}
	}

	private boolean isAncestor(Variable var) {
		boolean ans = BFS.isAncentor(var, _bNetwork.get_varieables(), _probVar, _observds);
		return ans;
	}

	private void resetSettings() {
		for (Variable observedVar : _observds) {
			observedVar.set_givenValue(null);
		}
	}

	public String getText() {
		return _queryText;
	}
}
