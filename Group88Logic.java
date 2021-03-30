import net.sf.javabdd.*;
import java.util.*;

public class Group88Logic implements IQueensLogic {
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDD bdd;
    private BDDFactory fact;
    private int nVars;
    
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];

        fact = JFactory.init(2000000,200000);
        nVars = size*size;
        fact.setVarNum(nVars); 

        createRules();
    }
   
    public int[][] getBoard() {
        return board;
    }

    public void insertQueen(int column, int row) {
        board[column][row] = 1;
    }

    private void createRules() {
        bdd.noget();
    }
}
