import java.io.PrintWriter;
import java.util.LinkedList;

public class ex1 {

	public static void main(String[] args) throws Exception {
		calculateBNqueries calc = new calculateBNqueries("input");
		LinkedList<String> answers = calc.calculate();
		PrintWriter out = new PrintWriter("output.txt");
		for (String ans : answers) {
			out.println(ans);
		}
		out.close();
	}
}