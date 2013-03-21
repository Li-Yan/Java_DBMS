package Exec_Tree;

public class Exec_Condition_AND extends Exec_Condition {
	public Exec_Condition leftCondition, righCondition;
	
	public Exec_Condition_AND(Exec_Condition Left, Exec_Condition Right) {
		leftCondition = Left;
		righCondition = Right;
	}
}
