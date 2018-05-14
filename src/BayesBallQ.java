import java.util.LinkedList;

public class BayesBallQ implements BNQuery{
	//members
	private Variable _var1, _var2;
	private LinkedList<Variable> _observeds;
	private Bnetwork _bNetwork;
	private String _queryText;
	private boolean DEBUG = true;
	//constructors
	/**
	 * 
	 * @param line - query text line from input file
	 * @param bNetwork - Bnetwork object built by input parameters
	 * @throws Exception - if something goes wrong...
	 */
	public BayesBallQ(String line, Bnetwork bNetwork) throws Exception {
		_observeds = new LinkedList<>();
		_queryText = line;
		String[] queryParts = line.split("\\|");
		_bNetwork = bNetwork;
		try {
			readVars(queryParts[0]);
			if (queryParts.length == 2) {	//if there are observed vars
				readObservds(queryParts[1]);	//read them
			}
		}
		catch (Exception e) {
			throw (new Exception("Error Creating BBquery. "+e));
		}
	}

	//methods	
	/**
	 * initiating query variables 
	 * @param varPartFromText - string of query variables
	 * @throws Exception - if variables were not declared in input file
	 */
	private void readVars(String varPartFromText) throws Exception {
		try {
			String[] vars = varPartFromText.split("-");
			String var1Name = vars[0];
			String var2Name = vars[1];
			LinkedList<Variable> existingVars = _bNetwork.get_varieables();
			_var1 = VariableFactory.lookForVar(var1Name, existingVars);
			_var2 = VariableFactory.lookForVar(var2Name, existingVars);
		}
		catch(Exception e) {
			throw(new Exception("Error reading variables to BNquery."));
		}
	}
	/**
	 * initiating observed variables 
	 * @param varPartFromText - string of observed variables
	 * @throws Exception - if variables were not declared in input file
	 */
	private void readObservds(String varPartFromText) throws Exception {
		LinkedList<Variable> existingVars = _bNetwork.get_varieables();
		if (!varPartFromText.equals("")) {
			String[] givenVars = StrAndTxtManipulator.splitAndLoseSpaces(varPartFromText);
			for (String givenVar : givenVars) {
				String[] varAndItsValue = givenVar.split("=");
				String currentGivenVarName = varAndItsValue[0];
				Variable currentGivenVar = VariableFactory.lookForVar(currentGivenVarName, existingVars);
				_observeds.add(currentGivenVar);
			}
		}
	}
	/**
	 * calculates bayes-ball query ans return a string represnting the result.
	 */
	public String calcQuery() {
		LinkedList<Variable> queue = new LinkedList<>();
		LinkedList<Variable> children = _var1.get_children();
		LinkedList<Variable> parents = _var1.get_parents();
		if (DEBUG) System.out.println("first var: "+_var1.get_name());
		if (DEBUG) System.out.println("target var: "+_var2.get_name());
		//adding first var childern to queue
		for (Variable child : children) {
			if (DEBUG)System.out.println("add "+child.get_name()+" to Queue as child of "+_var1.get_name());
			if (child == _var2) {
				if (DEBUG) System.out.println("first var is parent of: "+child.get_name()+" which is var2!");
				resetSettings();
				if (DEBUG) System.out.println("var2 found, return <NO>");
				return "no";
			}
			queue.add(child);
			child.setIsFromParent();
			if (DEBUG) System.out.println(_var1.get_name()+ " child: "+ child.get_name() + "was set as isFromParent");
		}
		//adding first var parents to queue
		for (Variable parent : parents) {
			if (parent == _var2) {
				if (DEBUG) System.out.println("first var is child of: "+parent.get_name()+" which is var2!");
				resetSettings();
				if (DEBUG) System.out.println("var2 found, return <NO>");
				return "no";
			}
			if (DEBUG) System.out.println("add "+parent.get_name()+" to Queue  as parent of "+_var1.get_name());
			queue.add(parent);
			parent.setFromChild();
			if (DEBUG) System.out.println(_var1.get_name()+ " parent: "+ parent.get_name() + "was set as isFromChild");
		}
		//adding observed vars
		for (Variable observed : _observeds) {
			observed.setObserved();
			if (DEBUG) System.out.println(observed.get_name()+ " was set as observed");
		}
		//queue loop
		if (DEBUG) System.out.println("starting queue loop");
		while (!queue.isEmpty()) {
			Variable currVar = queue.poll();
			if (DEBUG) System.out.println("deque var from queue: "+currVar.get_name());
			if (currVar.isObserved()) {
			//Observed node
				if (DEBUG) System.out.println("curr var is observed");
				if (currVar.isFromParent()) {
					if (DEBUG) System.out.println("curr var is from parent");
					//visit all parents
					if (DEBUG) System.out.println("var is observed and came from parent, so adding his parents.");
					parents = currVar.get_parents();
					for (Variable parent : parents) {
						if (DEBUG) System.out.println("curr var parent: "+parent.get_name());
						if (parent == _var2) {
							if (DEBUG) System.out.println("curr var parent is var2: "+_var2.get_name());
							resetSettings();
							if (DEBUG) System.out.println("var2 found, return <NO>");
							return "no";
						}
						if (!parent.isVisitedByChild()) {
							if (DEBUG) System.out.println(parent.get_name()+" is parent of "+currVar.get_name()+" and was NOT been visited by his children yet, so we add it to queue.");
							addParentToQueue(parent, queue);
						}
						else {
							if (DEBUG) System.out.println(parent.get_name()+" is parent of "+currVar.get_name()+" but was visited by his children before, so we dont add it to queue.");
						}
					}
				}
				//else: do nothing.
			}
			else {
			//Unobserved node
				if (DEBUG) System.out.println(currVar.get_name()+" is unobserved so we add its children (that hasnt been visited by their parents) to queue.");
				children = currVar.get_children();					
				for (Variable child : children) {
					if (DEBUG) System.out.println("curr var child: "+child.get_name());
					if (child == _var2) {
						if (DEBUG) System.out.println("curr var child is var2: "+_var2.get_name());
						resetSettings();
						if (DEBUG) System.out.println("var2 found, return <NO>");
						return "no";
					}
					if (!child.isVisitedByParent()) {
						if (DEBUG) System.out.println(child.get_name()+" is child of "+currVar.get_name()+" and was NOT been visited by his parent yet, so we add it to queue.");
						addChildToQueue(child, queue);
					}
					else {
						if (DEBUG) System.out.println(child.get_name()+" is child of "+currVar.get_name()+" but was visited by his parents before, so we dont add it to queue.");
					}
				}
				if (!currVar.isFromParent()) {
					//currVar is from child
					//visit all parents
					if (DEBUG) System.out.println("var is NOT observed and came from child, so adding his parents.");
					parents = currVar.get_parents();
					for (Variable parent : parents) {
						if (parent == _var2) {
							resetSettings();
							return "no";
						}
						if (!parent.isVisitedByChild()) {
							if (DEBUG) System.out.println(parent.get_name()+" is parent of "+currVar.get_name()+" and was NOT been visited by his children yet, so we add it to queue.");
							addParentToQueue(parent, queue);
						}
						else {
							if (DEBUG) System.out.println(parent.get_name()+" is parent of "+currVar.get_name()+" but was visited by his children before, so we dont add it to queue.");
						}
					}
				}
			}
		}
		resetSettings();
		return "yes";
	}
	/**
	 * reset falgs for the next query 
	 */
	private void resetSettings() {
		while (!_observeds.isEmpty()) {
			_observeds.poll();
		}
		LinkedList<Variable> vars= _bNetwork.get_varieables();
		for (Variable var : vars) {
			var.setNotObserved();
			var.setNotVisitedByChild();
			var.setNotVisitedByParent();
			var.setIsFromParent();
		}
	}
	/**
	 * before adding a parent to queue, we need to flag it as -From Child- and -Visited by Child-
	 * @param parent - variable need to add to queue as parent
	 * @param q - queue itself
	 */
	private void addParentToQueue(Variable parent, LinkedList<Variable> q) {
		parent.setFromChild();
		parent.setVisitedByChild();
		q.add(parent);
	}
	/**
	 * before adding a child to queue, we need to flag it as -From Parent- and -Visited by Parent-
	 * @param child
	 * @param q
	 */
	private void addChildToQueue(Variable child, LinkedList<Variable> q) {
		child.setIsFromParent();
		child.setVisitedByParent();
		q.add(child);
	}

	@Override
	public String getText() {
		return _queryText;
	}

}
