import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.sat4j.specs.ISolver;
import org.sat4j.minisat.SolverFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import org.sat4j.core.VecInt;
import java.io.BufferedReader;

public class AgentC2 {

    private List<List<String>> knowledgeBase = new ArrayList<>();

    private Map<String, Integer> varMap = new HashMap<>(); // Maps variables to integers
    private int varCount = 0; // Tracks the total number of unique variables

    public void run(Game board, boolean verbose) {
        for (int i = 0; i < board.size; i++) {
            for (int j = 0; j < board.size; j++) {

                int clue = board.board[i][j];
                if (clue != -1) { // if it has a clue
                    try {
                        encodeClueToCNF(i, j, clue, board); // encode all possibilites for the clu
                    } catch (Exception e) {
                        System.out.println("Error encoding clue: " + e.getMessage());
                    }
                }
            }
        }

        String dimacs = convertKnowledgeBaseToDIMACS();
        System.out.println(dimacs);
        System.out.println(varMap);
        solveWithSAT4J(dimacs, board);
    }

    private List<List<Integer>> generateCombinations(int n, int k) {
        List<List<Integer>> combinations = new ArrayList<>();
        backtrack(combinations, new ArrayList<>(), 1, n, k);
        return combinations;
    }

    private void backtrack(List<List<Integer>> combinations, List<Integer> current, int start, int n, int k) {
        if (k == 0) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i <= n; i++) {
            current.add(i);
            backtrack(combinations, current, i + 1, n, k - 1);
            current.remove(current.size() - 1);
        }
    }

    private void encodeClueToCNF(int i, int j, int clue, Game board) {
        List<String> cnfClauses = new ArrayList<>();
        List<int[]> neighbors = getNeighborPositions(i, j, board); // Assume implementation from previous instructions

        // For simplicity, variables are named based on their indices (e.g., "1" for the
        // first neighbor)
        int variablesCount = neighbors.size();
        // Generate CNF for "at least N"
        if (clue > 0) {
            List<List<Integer>> atLeastCombinations = generateCombinations(variablesCount, clue);
            for (List<Integer> combination : atLeastCombinations) {
                StringBuilder clause = new StringBuilder();
                for (Integer idx : combination) {
                    clause.append("D").append(neighbors.get(idx - 1)[0]).append("_").append(neighbors.get(idx - 1)[1])
                            .append(" ");
                }
                cnfClauses.add(clause.toString().trim() + " 0"); // Append "0" to denote the end of a clause
            }
        }

        // Generate CNF for "at most N"
        // This is more complex and involves generating negations for combinations of
        // size (N+1) to enforce an upper limit
        if (clue < variablesCount) {
            List<List<Integer>> atMostCombinations = generateCombinations(variablesCount, clue + 1);
            for (List<Integer> combination : atMostCombinations) {
                StringBuilder clause = new StringBuilder();
                for (Integer idx : combination) {
                    clause.append("-D").append(neighbors.get(idx - 1)[0]).append("_").append(neighbors.get(idx - 1)[1])
                            .append(" ");
                }
                cnfClauses.add(clause.toString().trim() + " 0"); // Append "0" to denote the end of a clause
            }
        }

        System.out.println("cell: " + i + "," + j + ": " + cnfClauses);
        knowledgeBase.add(cnfClauses);
    }

    private List<int[]> getNeighborPositions(int i, int j, Game board) {
        List<int[]> neighbors = new ArrayList<>();

        // Define the relative positions of all possible neighbors
        int[][] directions = {
                { -1, -1 }, // top-left
                { -1, 0 }, // top
                { -1, 1 }, // top-right
                { 0, -1 }, // left
                { 0, 0 }, // current cell
                { 0, 1 }, // right
                { 1, -1 }, // bottom-left
                { 1, 0 }, // bottom
                { 1, 1 } // bottom-right
        };

        // Check each possible direction to ensure it's within the grid bounds
        for (int[] dir : directions) {
            int neighborI = i + dir[0];
            int neighborJ = j + dir[1];

            // Check if the neighbor position is within the bounds of the board
            if (neighborI >= 0 && neighborI < board.size && neighborJ >= 0 && neighborJ < board.size) {
                if (board.state[neighborI][neighborJ] == board.COVERED) { // check if cell is covered
                    neighbors.add(new int[] { neighborI, neighborJ });
                }
            }
        }

        return neighbors;
    }

    private String convertKnowledgeBaseToDIMACS() {
        StringBuilder dimacs = new StringBuilder();
        int clauseCount = knowledgeBase.size();
        // Reset varMap and varCount for each conversion
        varMap.clear();
        varCount = 0;

        // Unique integer assignment for variables
        for (List<String> clause : knowledgeBase) {
            for (String literal : clause) {
                String var = literal.startsWith("-") ? literal.substring(1) : literal;
                if (!varMap.containsKey(var)) {
                    varMap.put(var, ++varCount);
                }
            }
        }

        // DIMACS preamble
        dimacs.append("p cnf ").append(varCount).append(" ").append(clauseCount).append("\n");

        // Convert clauses to DIMACS
        for (List<String> clause : knowledgeBase) {
            for (String literal : clause) {
                int varNum = varMap.get(literal.startsWith("-") ? literal.substring(1) : literal);
                dimacs.append(literal.startsWith("-") ? "-" : "").append(varNum).append(" ");
            }
            dimacs.append("0\n"); // Clause end
        }

        return dimacs.toString();
    }

    public void solveWithSAT4J(String dimacs, Game board) {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1 hour timeout

        try (ByteArrayInputStream in = new ByteArrayInputStream(dimacs.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("p") || line.startsWith("c")) {
                    // Skip preamble and comments
                    continue;
                }
                VecInt clause = new VecInt();
                for (String part : line.split("\\s")) {
                    int literal = Integer.parseInt(part);
                    if (literal == 0) {
                        // 0 denotes the end of a clause in DIMACS format
                        break;
                    }
                    clause.push(literal);
                }
                solver.addClause(clause);
            }

            if (solver.isSatisfiable()) {
                System.out.println("Satisfiable");
                int[] model = solver.model();
                processModel(model, board); // Ensure this method is correctly implemented to update the board
            } else {
                System.out.println("Unsatisfiable");
            }
        } catch (Exception e) {
            System.err.println("Error during SAT solving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processModel(int[] model, Game board) {
        System.out.println("Model: " + Arrays.toString(model));
        // Inverting varMap to map variable IDs back to their identifiers
        Map<Integer, String> reverseVarMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : varMap.entrySet()) {
            reverseVarMap.put(entry.getValue(), entry.getKey());
        }

        for (int varId : model) {
            boolean isTrue = varId > 0;
            String varKey = reverseVarMap.get(Math.abs(varId));
            if (varKey != null) {
                // Ensure we only deal with the variable identifier part
                String[] keyParts = varKey.split(" ");
                if (keyParts.length > 0) {
                    varKey = keyParts[0]; // Use only the first part, ignoring anything after a space
                }

                if (varKey.startsWith("D")) {
                    try {
                        String[] parts = varKey.substring(1).split("_"); // Remove 'D' and split
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        // Update the board based on the truth value of the variable
                        if (isTrue) {
                            board.state[x][y] = board.PAINTED;
                            System.out.println("Painting cell [" + x + ", " + y + "]");
                        } else {
                            board.state[x][y] = board.CLEARED;
                            System.out.println("Clearing cell [" + x + ", " + y + "]");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing cell coordinates from key: " + varKey);
                    }
                }
            }
        }
    }
}
