import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Predicate;

public class calculateBNqueries {
	//members
	public static BufferedReader _reader;
	private Bnetwork _bNetwork;
	private LinkedList<BNQuery> _queries;
	//constructors
	public calculateBNqueries(String inaputName) throws Exception{
		try {
			initReader(inaputName);
			LinkedList<String> BNPartFromText = readTextUntill("Queries");
			LinkedList<String> QPartFromText = readTextUntill(null);
			createBN(BNPartFromText);
			createQueries(QPartFromText);
			_reader.close();
		}
		catch(Exception e) {
			throw (new Exception("Error creating \"calculateBNqueries\" object." + e));
		}
	}
	//methods
	public LinkedList<String> calculate() {
		LinkedList<String> ans = new LinkedList<>();
		for (BNQuery query : _queries) {
			String queryResult = query.calcQuery();
			ans.add(queryResult);
		}
		return ans;
	}

	//*****************create-BN
	private void createQueries (LinkedList<String> QPart) throws Exception {
		_queries = QueriesFactory.createQueriesFromText(QPart, _bNetwork);
	}

	//*****************create-BN
	private void createBN (LinkedList<String> BNPart) throws Exception {
		_bNetwork = BNFactory.createBNFromText(BNPart); 
	}

	//*****************reader
	private void initReader(String inputName) throws Exception {
		File inputFile = findInputFile(inputName);
		try {
			_reader = new BufferedReader(new FileReader(inputFile));
		}
		catch(Exception e) {
			throw (new Exception("Error creating _reader."));
		}
	}

	private File findInputFile(String inputName) throws Exception {
		String path = System.getProperty("user.dir");
		inputName = inputName + ".txt";
		File currentPath = new File(path);
		File[] listOfFiles = currentPath.listFiles();
		for (File file : listOfFiles) {
			if (!file.isDirectory() && file.getName().equals(inputName)){
				return file;
			}
		}
		throw (new Exception("Error: input File was not found."));
	}

	private LinkedList<String> readTextUntill(String endLine) throws Exception {
		LinkedList<String> ans = new LinkedList<>();
		String currentLine = _reader.readLine();
		if (endLine != null) {	//read till a word
			while (!currentLine.equals(endLine)) {
				ans.add(currentLine);
				currentLine = _reader.readLine();
			}
		}

		else {	//read till the end of the text
			while (currentLine != null) {
				ans.add(currentLine);
				currentLine = _reader.readLine();
			}
		}
		return ans;
	}
}
