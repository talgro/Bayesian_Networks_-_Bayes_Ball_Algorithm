import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class BFS {
	final static int WHITE = 0;
	final static int GRAY = 0;
	final static int BLACK = 0;

	/**
	 * BFS algorithm to find whether var1 is ancentor of var2
	 * @param startFrom - variable to start the search from
	 * @param allVars - all vars initiated in this binary network
	 * @param probVar - variable to find in bfs search
	 * @param observds - all observds initiated in this binary network
	 * @return TRUE if var1 is ancentor of var2, else FALSE
	 */
	public static boolean isAncentor(Variable startFrom, LinkedList<Variable>allVars, Variable probVar, ArrayList<Variable> observds) {
		LinkedList<Variable> vars = new LinkedList<>();
		for (Variable variable : allVars) {
			variable.set_color(WHITE);
			vars.add(variable);
		}
		LinkedList<Variable> queue = new LinkedList<>();
		queue.add(startFrom);
		startFrom.set_color(GRAY);
		while (!queue.isEmpty()) {
			Variable currVar = queue.pop();
			for (Variable child : currVar.get_children()) {
				if (child.get_color() == WHITE) {
					if (observds.contains(child) || probVar.equals(child)) {
						return true;
					}
					child.set_color(GRAY);
					queue.add(child);
				}
			}
			currVar.set_color(BLACK);
		}
		return false;
	}

}
