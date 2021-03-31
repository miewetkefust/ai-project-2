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

        fact = JFactory.init(2000000,200000); // Init with recommended nodes and cache
        nVars = size*size;
        fact.setVarNum(nVars); 

        // Set true and false variables
        True = fact.one();
        False = fact.zero();

        bdd = True;

        createRules();
        updateBoard();
    }
   
    public int[][] getBoard() {
        return board;
    }

    // If move is valid, insert queen, update the BDD and update graphics
    public void insertQueen(int col, int row) {
        if (!invalidMove(col, row)) {
            board[col][row] = 1;
        }
        var newBDD = this.bdd.restrict(fact.ithVar(getVarNum(col, row)));
        bdd = newBDD;

        updateBoard();
    }

    // Return variable corresponding to position on the board
    private int getVarNum(int col, int row)  {
        return size * row + col;
    }

    // For each position on the board, create rules for restricted moves
    private void createRules() {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                createColumnRule(col,row);
                createRowRule(col, row);
                createDiagonalRule(col, row);
            }
        }
        createQueensRule();
    }

    // For each row create a rule that ensures at least one queen can be placed in that row
    private void createQueensRule() {
        for(int col = 0; col < size; col++){
            var columnBDD = False;

            for (int row = 0; row < size; row++) {
                columnBDD = columnBDD.or(fact.ithVar(getVarNum(col, row)));
            }

            bdd = bdd.and(columnBDD);
        }
    }

    // Make sure that queens can't be attacked vertically
    private void createColumnRule(int col, int row) {
        var columnBDD = True;

        // Set all other rows in this column to false
        for(int y = 0; y < size; y++){
            if(y != row){
                columnBDD = columnBDD.and(fact.nithVar(getVarNum(col, y)));
            }
        }

        // Not col,row
        var subBDD = fact.nithVar(getVarNum(col, row));

        // (not col,row) or (not (all other rows in this column "anded" together))
        subBDD = subBDD.or(columnBDD);

        bdd = bdd.and(subBDD);
    }

    // Make sure that queens can't be attacked horizontally
    private void createRowRule(int col, int row) {
        var rowBDD = True;

        // Set all other columns in this row to false
        for(int x = 0; x < size; x++){
            if(x != col){
                rowBDD = rowBDD.and(fact.nithVar(getVarNum(x, row)));
            }
        }

        // Not col,row
        var subBDD = fact.nithVar(getVarNum(col, row));

        // (not col,row) or (not (all other columns in this row "anded" together))
        subBDD = subBDD.or(rowBDD);

        bdd = bdd.and(subBDD);
    }

    // Make sure that queens can't be attacked diagonally
    private void createDiagonalRule(int col, int row) {
        var diagonalBDD = True;

        int x = col;
        int y = row;

        while (x < size && y < size) {
            if(x != col && y != row)
                diagonalBDD = diagonalBDD.and(fact.nithVar(getVarNum(x, y)));
            x++;
            y++;
        }

        x = col;
        y = row;

        while (x >= 0 && y >= 0) {
            if(x != col && y != row)
                diagonalBDD = diagonalBDD.and(fact.nithVar(getVarNum(x, y)));
            x--;
            y--;
        }

        x = col;
        y = row;

        while (x >= 0 && y < size) {
            if(x != col && y != row)
                diagonalBDD = diagonalBDD.and(fact.nithVar(getVarNum(x, y)));
            x--;
            y++;
        }

        x = col;
        y = row;

        while (x < size && y >= 0) {
            if(x != col && y != row)
                diagonalBDD = diagonalBDD.and(fact.nithVar(getVarNum(x, y)));
            x++;
            y--;
        }

        // Not col,row
        var subBDD = fact.nithVar(getVarNum(col, row));

        subBDD = subBDD.or(diagonalBDD);

        bdd = bdd.and(subBDD);
    }

    // Check if BDD can be true if we insert a queen on this col and row
    private boolean invalidMove(int col, int row) {
        var testBDD = this.bdd.restrict(fact.ithVar(getVarNum(col, row)));
        return testBDD.isZero();
    }

    // Check if a queen has to be inserted on the given position
    private boolean mustDoMove(int col, int row) {
        var testBDD = this.bdd.restrict(fact.nithVar(getVarNum(col, row)));
        return testBDD.isZero();
    }

    // Update restricted positions, and automatically insert queens that must be inserted
    private void updateBoard() {
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                if (invalidMove(col, row)) board[col][row] = -1;
                if (mustDoMove(col, row)) {
                    // Insert queen automatically
                    board[col][row] = 1;
                    bdd = this.bdd.restrict(fact.ithVar(getVarNum(col, row)));
                }
            }
        }
    }
}
