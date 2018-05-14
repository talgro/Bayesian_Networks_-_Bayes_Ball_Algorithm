import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class Variable {
	//members
	private String _name;
	private String[] _values;
	private LinkedList<Variable> _parents;
	private LinkedList<Variable> _children;
	private CPT _cpt;
	private boolean _isVisitedByParent;
	private boolean _isVisitedByChild;
	private boolean _isFromParent;
	private boolean _isObserved;
	private String _givenValue;
	private int _color;

	//constructors
	public Variable (String name) {
		setNotVisitedByParent();
		setNotVisitedByChild();
		_name = name;
		_children = new LinkedList<>();
	}
	//methods
	public void set_color(int color) {
		_color = color;
	}
	
	public int get_color() {
		return _color;
	}
	
	public void set_givenValue(String givenValue) {
		_givenValue = givenValue;
	}
	
	public String get_givenValue() {
		return _givenValue;
	}
	
	public String get_name() {
		return _name;
	}

	public String[] get_values() {
		return _values;
	}
	
	public boolean isObserved() {
		return _isObserved;
	}
	
	public void setObserved() {
		_isObserved = true;
	}
	
	public void setNotObserved() {
		_isObserved = false;
	}
	
	public void setVisitedByParent() {
		_isVisitedByParent = true;
	}

	public void setNotVisitedByParent() {
		_isVisitedByParent = false;
	}
	
	public boolean isVisitedByParent() {
		return _isVisitedByParent;
	}
	
	
	public void setVisitedByChild() {
		_isVisitedByChild = true;
	}

	public void setNotVisitedByChild() {
		_isVisitedByChild = false;
	}
	
	public boolean isVisitedByChild() {
		return _isVisitedByChild;
	}
	
	public void setIsFromParent() {
		_isFromParent = true;
	}
	
	public void setFromChild() {
		_isFromParent = false;
	}
	
	public boolean isFromParent() {
		return _isFromParent;
	}

	public LinkedList<Variable> get_parents() {
		return _parents;
	}

	public CPT get_cpt() {
		return _cpt;
	}

	public LinkedList<Variable> get_children() {
		return _children;
	}

	public void set_values(String[] _values) {
		this._values = _values;
	}

	public void set_parents(LinkedList<Variable> _parents) {
		this._parents = _parents;
		VariableFactory.setChild(_parents, this);
	}

	public void set_cpt(CPT _cpt) {
		this._cpt = _cpt;
	}
	
	public void addChild(Variable child) {
		_children.add(child);
	}
	
	@Override
	public boolean equals(Object other) {
		Variable otherVar = (Variable)other;
		return _name.equals(otherVar.get_name());
	}
	
	@Override
	public String toString() {
		return _name;
	}
}
