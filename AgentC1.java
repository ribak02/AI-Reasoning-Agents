import org.logicng.datastructures.Assignment;
import org.logicng.formulas.Formula;
import org.logicng.formulas.Variable;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;
import org.logicng.datastructures.Tristate;

import java.util.ArrayList;
import java.util.List;

public class AgentC1 {

    FormulaFactory f = new FormulaFactory();
    PropositionalParser p = new PropositionalParser(f);

    private Formula knowledgeBase;

    /**
     * Implements AgentC1, a logic-based agent that uses propositional logic and SAT
     * solving to determine the next moves in a Mosaic game.
     * 
     * This class encapsulates the logic for converting Mosaic game clues into a set
     * of propositional logic formulas, which are then solved using a SAT solver.
     * The agent iterates over the game board, encoding the clues into formulas that
     * represent the game's constraints. These formulas are combined into a
     * knowledge base, which is then converted into Conjunctive Normal Form (CNF)
     * and solved using the MiniSat solver from the LogicNG library.
     * 
     * Upon finding a solution, the agent updates the game board by painting or
     * clearing cells based on the model returned by the SAT solver. This approach
     * allows for the efficient solving of Mosaic puzzles by leveraging the power of
     * SAT solvers to handle complex logical deductions.
     * 
     * @param board   The game board object containing the current state of the
     *                puzzle, including clues and cell states. This object is
     *                modified directly to reflect the moves made by the agent.
     * @param verbose A flag to enable detailed output during the solving process,
     *                useful for debugging or demonstration purposes.
     * 
     * @throws ParserException If there is an error in parsing the formulas
     *                         generated from the game clues.
     */
    public void run(Game board, boolean verbose) {

        knowledgeBase = f.verum(); // Initialize the knowledge base with True

        // encode the clues and construct the knowledge base
        for (int i = 0; i < board.size; i++) {
            for (int j = 0; j < board.size; j++) {

                int clue = board.board[i][j];
                if (clue != -1) { // if it has a clue
                    try {
                        encodeClue(i, j, clue, board); // encode all possibilites for the clue
                    } catch (ParserException e) {
                        System.out.println("Error encoding clue: " + e.getMessage());
                    }
                }
            }
        }

        Formula cnf = knowledgeBase.cnf(); // Convert the knowledge base to CNF

        SATSolver solver = MiniSat.miniSat(f);
        solver.add(cnf);

        Tristate result = solver.sat();
        if (result == Tristate.TRUE) {
            processModel(solver.model(), board, verbose);
        } else {
            System.err.println("No solution found, or puzzle is unsolvable under current constraints.");
        }
    }

    /**
     * Processes the model returned by the SAT solver to update the game board with
     * the solution.
     * 
     * This method iterates over the variables in the model returned by the SAT
     * solver. For each variable, it determines whether the variable represents a
     * cell to be painted or cleared based on its truth value. It then updates the
     * game board accordingly.
     * 
     * Positive variables in the model indicate cells that should be painted, while
     * negative variables indicate cells that should be cleared. The method extracts
     * the coordinates of each cell from the variable names and updates the game
     * board to reflect the solution found by the SAT solver.
     * 
     * @param model   The model returned by the SAT solver, containing the truth
     *                values of variables representing the state of each cell in the
     *                puzzle.
     * @param board   The game board object that will be updated based on the model.
     *                This object contains the current state of the puzzle and will
     *                be directly modified to reflect the solution.
     * @param verbose A boolean flag that, when true, enables the printing of
     *                detailed messages about the cells being updated. This is
     *                useful for debugging or visualizing the steps the agent takes
     *                to solve the puzzle.
     */
    private void processModel(Assignment model, Game board, boolean verbose) {
        model.positiveVariables().forEach(variable -> {
            String varName = variable.name();
            // Extract coordinates and state from variable name
            String[] parts = varName.split("_");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            // Update the board state based on the variable
            board.state[x][y] = board.PAINTED;

            if (verbose) {
                System.out
                        .println("Updating cell [" + x + ", " + y + "] to PAINTED");
            }

        });
        model.negativeVariables().forEach(variable -> {
            String varName = variable.name();
            // Extract coordinates and state from variable name
            String[] parts = varName.split("_");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            // Update the board state based on the variable
            board.state[x][y] = board.CLEARED;

            if (verbose) {
                System.out.println("Updating cell [" + x + ", " + y + "] to COVERED");
            }
        });
    }

    /**
     * Encodes a puzzle clue into the knowledge base using logical formulas.
     * 
     * This method takes a clue from the puzzle, represented by the number of
     * neighboring cells that must be painted, and encodes it as a logical formula.
     * It first identifies the neighbor cells of a given cell, then generates all
     * possible combinations of these cells being painted that satisfy the clue.
     * These combinations are then combined into a disjunctive normal form (DNF)
     * formula representing all valid solutions for the clue.
     * 
     * The resulting formula for the clue is added to the knowledge base, a logical
     * conjunction of all clue formulas, representing the entire puzzle's
     * constraints. This method uses logical operations to construct the formulas
     * and relies on the game board to identify neighbor cells and the clue's value
     * to determine the required conditions.
     * 
     * @param i     The row index of the cell for which the clue is being encoded.
     * @param j     The column index of the cell for which the clue is being
     *              encoded.
     * @param clue  The value of the clue, indicating the number of neighboring
     *              cells that must be painted.
     * @param board The game board object, used to access the puzzle's layout and
     *              state.
     * @throws ParserException If there is an error in parsing the logical formula,
     *                         indicating a problem in the encoding process.
     */

    public void encodeClue(int i, int j, int clue, Game board) throws ParserException {
        List<Variable> neighbors = new ArrayList<>();

        neighbors = getNeighborVariables(i, j, board); // includes the cell itself

        // For a clue of n, find all combinations of n neighbors being painted
        List<Formula> combinations = new ArrayList<>();
        if (clue > 0) {
            // Generating combinations for the clue
            generateCombinations(combinations, neighbors, clue);
        }

        // Combine all combinations with OR to form the DNF formula
        Formula clueFormula = f.or(combinations);

        // Add the clue formula to the knowledge base
        knowledgeBase = f.and(knowledgeBase, clueFormula);

        // System.out.println("Clue Formula for cell (" + i + ", " + j + "): " +
        // clueFormula);
    }

    /**
     * Generates all possible combinations of variables representing neighbors to be
     * painted, given a clue, and adds these combinations as formulas to a list.
     * 
     * This method is used to generate all valid configurations of painted cells
     * around a cell with a given clue. It creates combinations of variables where a
     * subset of variables (representing cells) are set to true (painted) based on
     * the clue number, and the remaining variables are set to false (not painted).
     * Each combination is converted into a conjunction formula (AND of variables)
     * and added to the provided list of formulas.
     * 
     * The method first generates all subsets of neighbor variables that match the
     * clue number using a helper method to generate subsets. For each subset, it
     * creates a formula where the variables in the subset are true, and all other
     * neighbor variables are false. These formulas represent all possible ways the
     * clue can be satisfied by painting certain neighbors.
     * 
     * @param combinations The list of formulas to which the generated combinations
     *                     will be added. Each formula represents a possible way to
     *                     satisfy the clue.
     * @param neighbors    A list of variables representing the neighbors of a cell.
     *                     These variables are used to generate the combinations.
     * @param clue         The clue number indicating how many neighbors should be
     *                     painted. This is used to determine the size of the
     *                     subsets to generate.
     */

    private void generateCombinations(List<Formula> combinations, List<Variable> neighbors, int clue) {
        List<List<Variable>> subsets = new ArrayList<>();
        generateSubsets(neighbors, subsets, new ArrayList<>(), 0, clue);

        // For each subset, create a formula where variables in the subset are true, and
        // others are false
        for (List<Variable> subset : subsets) {
            List<Formula> terms = new ArrayList<>();
            // Add variables in the subset as true
            for (Variable trueVar : subset) {
                terms.add(trueVar);
            }
            // Add variables not in the subset as false
            for (Variable neighbor : neighbors) {
                if (!subset.contains(neighbor)) {
                    terms.add(neighbor.negate());
                }
            }
            combinations.add(f.and(terms));
        }
    }

    /**
     * Generates all possible subsets of a given size from the list of neighbor
     * variables.
     * This method is used to explore all combinations of variables that could
     * represent a solution
     * based on a specific clue in the game. It recursively builds subsets of the
     * specified size,
     * adding them to a list of subsets once the desired size is reached.
     *
     * @param neighbors     The list of neighbor variables surrounding a specific
     *                      cell.
     * @param subsets       The list to store the generated subsets. Each subset is
     *                      a list of variables.
     * @param currentSubset The current subset being built in the recursive calls.
     * @param start         The starting index in the neighbors list to add
     *                      variables from.
     * @param clue          The size of the subsets to generate, determined by the
     *                      clue.
     */

    private void generateSubsets(List<Variable> neighbors, List<List<Variable>> subsets, List<Variable> currentSubset,
            int start, int clue) {
        if (currentSubset.size() == clue) {
            subsets.add(new ArrayList<>(currentSubset));
            return;
        }
        for (int i = start; i < neighbors.size(); i++) {
            currentSubset.add(neighbors.get(i));
            generateSubsets(neighbors, subsets, currentSubset, i + 1, clue);
            currentSubset.remove(currentSubset.size() - 1);
        }
    }

    /**
     * Retrieves a list of logic variables representing the neighbors of a given
     * cell on the board.
     * Each variable corresponds to a possible state (painted or cleared) of a
     * neighboring cell.
     *
     * @param i     the row index of the cell
     * @param j     the column index of the cell
     * @param board the game board containing cell states
     * @return a list of {@link Variable} objects, each representing a neighbor of
     *         the cell at (i, j)
     *         that is currently in the covered state. The variables are named using
     *         a convention
     *         "P_x_y", where x and y are the coordinates of the neighbor.
     */

    private List<Variable> getNeighborVariables(int i, int j, Game board) {
        // This method should return a list of Variables for the neighbors of cell (i,
        // j)
        List<Variable> vars = new ArrayList<>();
        for (int[] dir : new int[][] { { 0, 0 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 },
                { 1, 0 },
                { 1, 1 } }) {
            int ni = i + dir[0], nj = j + dir[1];
            if (ni >= 0 && ni < board.size && nj >= 0 && nj < board.size) {
                if (board.state[ni][nj] == board.COVERED) {
                    vars.add(f.variable("P_" + ni + "_" + nj));
                }
            }
        }
        return vars;
    }

}