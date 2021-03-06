package operators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;
import visitor.JoinExpVisitor;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * JoinOperator class joins two tuples on specified condition
 */
public class JoinOperator extends Operator{

	Operator left;
	Operator right;
	Expression expr;
	Tuple t1, t2;
	JoinExpVisitor jv = new JoinExpVisitor();
	
	/*
	 * Check whether two tuple meets the join condition and get the result after join
	 * @return the tuple after join
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t = null;
		while (t1 != null && t2 != null) {
			if (expr == null) 
				t = combineTuples(t1, t2);
			else {
				jv.readTuple(t1, t2);
				expr.accept(jv);
				if (jv.getCurStatus()) {
					t = combineTuples(t1, t2);
				}
			}
			this.nextPair();
			if (t != null) return t;
		}
		return null;
	}

	/*
	 * Combine two tuples that meet the join condition
	 * @param t1 the first tuple
	 * @param t2 the second tuple
	 * @return the combined tuple
	 */
	private Tuple combineTuples(Tuple t1, Tuple t2) {
		List<Long> value = new ArrayList<>();
		value.addAll(t1.getAllColumn());
		value.addAll(t2.getAllColumn());
		List<String> schemas = new ArrayList<String>();
		schemas.addAll(t1.getAllSchemas());
		schemas.addAll(t2.getAllSchemas());
		return new Tuple(value, schemas);
	}

	/*
	 * Look for next pair of tuples 
	 */
	public void nextPair() {
		if (t1 == null) return;
		if (t2 != null) t2 = right.getNextTuple();
		if (t2 == null) {
			t1 = left.getNextTuple();
			right.reset();
			t2 = right.getNextTuple();
		}
	}
	
	/*
	 * Rest the child operators
	 */
	@Override
	public void reset() {
		left.reset();
		right.reset();
	}
	
	/*
	 * Create a JoinOperator object
	 * @param left the child operator
	 * @param right the child operator
	 * @param expr the join condition
	 */
	public JoinOperator (Operator left, Operator right, Expression expr) {
		this.left = left;
		this.right = right;
		this.expr = expr;
		t1 = left.getNextTuple();
		t2 = right.getNextTuple();
	}

}
