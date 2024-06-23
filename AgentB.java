import java.util.List;
import java.util.ArrayList;

/**
 * AgentB.java
 * 
 */
public class AgentB {

    /**
     * Executes the AgentB strategy on a given Mosaic game board by making moves
     * based on simple logical deductions.
     *
     * This method iterates over the game board, applying a straightforward strategy
     * to either paint or clear cells based on the clues and the current state of
     * their neighbors. The strategy seeks to make moves that are directly
     * deductible without resorting to complex logical inferences or backtracking.
     *
     * @param board   The game board instance to be manipulated, containing the
     *                current state of each cell and clues. The method directly
     *                modifies this object to reflect the moves made.
     * @param verbose A boolean flag that, when true, enables the printing of
     *                detailed logs for each move made, useful for debugging or
     *                understanding the agent's decisions.
     */

    public void run(Game board, boolean verbose) {
        boolean moveMade;

        do {
            moveMade = false;
            for (int i = 0; i < board.size; i++) {
                for (int j = 0; j < board.size; j++) {

                    int clue = board.board[i][j];

                    if (clue != -1) { // if it has a clue

                        int painted = 0, covered = 0;
                        List<int[]> coveredCells = new ArrayList<>();

                        for (int[] dir : new int[][] {
                                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }, { 0, 0 }
                        }) {
                            int ni = i + dir[0], nj = j + dir[1];
                            if (ni >= 0 && ni < board.size && nj >= 0 && nj < board.size) {
                                int state = board.state[ni][nj];
                                if (state == board.PAINTED) {
                                    painted++;
                                } else if (state == board.COVERED) {
                                    covered++;
                                    coveredCells.add(new int[] { ni, nj });
                                }
                            }
                        }

                        if (clue == painted) {
                            for (int[] cell : coveredCells) {

                                board.clear(cell[0], cell[1]);
                                moveMade = true;
                            }

                        } else if (clue - painted == covered) {
                            for (int[] cell : coveredCells) {
                                board.paint(cell[0], cell[1]);
                                moveMade = true;
                            }
                        }

                        if (verbose && moveMade) {
                            System.out.println("Move made at: " + i + ", " + j);
                            board.printBoard();
                        }
                    }
                }
            }
        } while (moveMade);
    }
}