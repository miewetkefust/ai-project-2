import net.sf.javabdd.*;

public class Group88Logic implements IQueensLogic {
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    
    private BDDFactory fact;
    private BDD bdd;
    private BDD True;
    private BDD False;
    private int nVars;
    
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];

        fact = JFactory.init(2000000,200000);
        nVars = size*size;
        fact.setVarNum(nVars); 

        True = fact.one();
        False = fact.zero();

        bdd = True;

        createRules();
        updateBoard();
    }
   
    public int[][] getBoard() {
        return board;
    }

    public void insertQueen(int col, int row) {
        if (!invalidMove(col, row)) {
            board[col][row] = 1;
        }
    
        var testBDD = this.bdd.restrict(fact.ithVar(getVarFromPosition(col, row)));
        bdd = testBDD;

        updateBoard();
    }

    private int getVarFromPosition(int col, int row)  {
        return size * row + col;
    }

    private void createRules() {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                createColumnRule(col,row);
                createRowRule(col, row);
                createDiagonalRule(col, row);
            }
        }
        createSizeQueensRule();
    }

    private void createSizeQueensRule() {
        // For each column create a rule that ensures at least one queen in that column
        for(int col = 0; col < size; col++){
            var tempBDDFalse = False;

            for (int row = 0; row < size; row++) {
                tempBDDFalse = tempBDDFalse.or(fact.ithVar(getVarFromPosition(col, row)));
            }

            bdd = bdd.and(tempBDDFalse);
        }
    }

    private void createColumnRule(int col, int row) {
        var tempBDDTrue = True;
        var tempBDDFalse = False;

        // Set all other rows in this column to false
        for(int y = 0; y < size; y++){
            if(y != row){
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(col, y)));
            }
        }

        // Not col,row
        tempBDDFalse = tempBDDFalse.or(fact.nithVar(getVarFromPosition(col, row)));

        // (not col,row) or (not (all other rows in this column "anded" together))
        tempBDDFalse = tempBDDFalse.or(tempBDDTrue);

        bdd = bdd.and(tempBDDFalse);
    }

    private void createRowRule(int col, int row) {
        var tempBDDTrue = True;
        var tempBDDFalse = False;

        // Set all other rows in this column to false
        for(int x = 0; x < size; x++){
            if(x != col){
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(x, row)));
            }
        }

        // Not col,row
        tempBDDFalse = tempBDDFalse.or(fact.nithVar(getVarFromPosition(col, row)));

        // (not col,row) or (not (all other rows in this column "anded" together))
        tempBDDFalse = tempBDDFalse.or(tempBDDTrue);

        bdd = bdd.and(tempBDDFalse);
    }

    private void createDiagonalRule(int col, int row) {
        var tempBDDTrue = True;
        var tempBDDFalse = False;

        int x = col;
        int y = row;

        while (x < size && y < size) {
            if(x != col && y != row)
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(x, y)));
            x++;
            y++;
        }

        x = col;
        y = row;

        while (x >= 0 && y >= 0) {
            if(x != col && y != row)
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(x, y)));
            x--;
            y--;
        }

        x = col;
        y = row;

        while (x >= 0 && y < size) {
            if(x != col && y != row)
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(x, y)));
            x--;
            y++;
        }

        x = col;
        y = row;

        while (x < size && y >= 0) {
            if(x != col && y != row)
                tempBDDTrue = tempBDDTrue.and(fact.nithVar(getVarFromPosition(x, y)));
            x++;
            y--;
        }

        tempBDDFalse = tempBDDFalse.or(fact.nithVar(getVarFromPosition(col, row)));

        tempBDDFalse = tempBDDFalse.or(tempBDDTrue);

        bdd = bdd.and(tempBDDFalse);
    }

    private boolean invalidMove(int col, int row) {
        // Check if BDD can be true if we insert a queen on this col and row
        var testBDD = this.bdd.restrict(fact.ithVar(getVarFromPosition(col, row)));
        return testBDD.isZero();
    }

    private boolean mustDoMove(int col, int row) {
        var testBDD = this.bdd.restrict(fact.nithVar(getVarFromPosition(col, row)));
        return testBDD.isZero();
    }

    private void updateBoard() {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                if (invalidMove(col, row)) board[col][row] = -1;
                if (mustDoMove(col, row)) {
                    board[col][row] = 1;
                    bdd = this.bdd.restrict(fact.ithVar(getVarFromPosition(col, row)));
                }
            }
        }
    }
}
