
import java.util.LinkedList;

public class Bnetwork {
	//members
	private LinkedList<Variable> _varieables;

	//constructors
	/**
	 * 
	 * @param varieables - all variables initiated from text input file
	 */
	public Bnetwork(LinkedList<Variable> varieables) {
		_varieables = varieables;
	}
	public LinkedList<Variable> get_varieables() {
		return _varieables;
	}

}
