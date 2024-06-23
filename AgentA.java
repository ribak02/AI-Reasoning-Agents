/**
 * AgentA.java
 * 
 */
public class AgentA {

    /**
     * Executes the AgentA strategy on a given Mosaic game board.
     *
     * This method assesses the game board to make determinations based on the
     * current state of each cell and its neighboring clues. It does not directly
     * modify the board but instead evaluates the completeness and consistency of
     * the game state.
     *
     * @param board   The game board instance to be evaluated, containing the
     *                current state of each cell and clues.
     * @param verbose A boolean flag that, when true, enables the printing of
     *                detailed execution logs for debugging or informational
     *                purposes.
     * @return An integer status code representing the state of the game after
     *         evaluation:
     *         - 0: Inconsistent board state with remaining covered cells.
     *         - 1: Inconsistent board state but all cells are uncovered.
     *         - 2: Consistent board state with remaining covered cells.
     *         - 3: Consistent board state and all cells are uncovered.
     */
    public int run(Game board, boolean verbose) {
        boolean allUncovered = true;
        boolean isConsistent = true;

        for (int i = 0; i < board.size; i++) {
            for (int j = 0; j < board.size; j++) {
                if (board.state[i][j] == board.COVERED) {
                    allUncovered = false; // There are still covered cells
                    // continue; // Skip covered cells for inconsistency checks
                }

                int clue = board.board[i][j];
                if (clue != -1) { // if it has a clue
                    int[] neighbors = board.getNeighborsStates(i, j); // check the neighbor states
                    int painted = 0, covered = 0;

                    if (board.state[i][j] == board.PAINTED) {
                        painted++;
                    }
                    if (board.state[i][j] == board.COVERED) {
                        covered++;
                    }

                    for (int neighbor : neighbors) {
                        if (neighbor == board.PAINTED) {
                            painted++;
                        } else if (neighbor == board.COVERED) {
                            covered++;
                        }
                    }
                    if (painted + covered < clue) {
                        isConsistent = false;
                    }
                }
            }
        }
        if (allUncovered) { // if all cells are uncovered
            if (isConsistent) {
                return 3;
            } else {
                return 1;
            }
        } else {
            if (isConsistent) {
                return 2;
            } else {
                return 0;
            }
        }
    }
}
