import java.util.LinkedList;

import javax.swing.plaf.synth.SynthSpinnerUI;

public class QueriesFactory {

	public static LinkedList<BNQuery> createQueriesFromText(LinkedList<String> text, Bnetwork bNetwork) throws Exception {
		LinkedList<BNQuery> queries = new LinkedList<>();
		String currentLine;
		while (!text.isEmpty()) {
			currentLine = text.poll();
			if (!currentLine.equals("")) {
				BNQuery currQuery = readQuery(currentLine, bNetwork);
				queries.add(currQuery);
			}
		}
		return queries;
	}

	public static BNQuery readQuery(String line, Bnetwork bNetwork) throws Exception {
		boolean isBayesBallQuery = !line.startsWith("P(");
		
		BNQuery currQuery;
		if (isBayesBallQuery) {
			currQuery = new BayesBallQ(line, bNetwork);
			return currQuery; 
		}
		else {
			currQuery = new VarElimiQ(line, bNetwork);
			return currQuery;
		}
	}

}
