import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class AgentBTest {
    private Game board;
    private AgentB agentB;

    @Before
    public void setUp() {
        agentB = new AgentB();
    }

    @Test
    public void testSimpleDeductionPaint() {
        board = new Game();
        board.setGame("2.,2*;2.,1*"); // Initial board setup
        agentB.run(board, false);
        int[][] expectedState = {
                { board.PAINTED, board.PAINTED },
                { board.COVERED, board.PAINTED }
        };
        assertArrayEquals("Cells should be painted based on clues", expectedState, board.state);
    }

    @Test
    public void testSimpleDeductionClear() {
        board = new Game();
        board.setGame("0.,2_;2.,1*"); // Initial board setup
        agentB.run(board, false);
        int[][] expectedState = {
                { board.CLEARED, board.CLEARED },
                { board.COVERED, board.PAINTED }
        };
        assertArrayEquals("Cells should be cleared based on clues", expectedState, board.state);
    }

    @Test
    public void testNoMovePossible() {
        board = new Game();
        board.setGame("1.,2_;2.,1*"); // Initial board setup with no obvious moves
        agentB.run(board, false);
        int[][] expectedState = {
                { board.COVERED, board.COVERED },
                { board.COVERED, board.PAINTED }
        };
        assertArrayEquals("No moves should be made if not deducible", expectedState, board.state);
    }

    // Additional test for complex deduction where multiple moves are made
    @Test
    public void testComplexDeduction() {
        board = new Game();
        board.setGame("1.,2_;2.,1*;2.,1*"); // Initial board setup with complex clues
        agentB.run(board, false);
        int[][] expectedState = {
                { board.COVERED, board.CLEARED },
                { board.PAINTED, board.PAINTED },
                { board.PAINTED, board.CLEARED }
        };
        assertArrayEquals("Multiple moves should be made based on complex clues", expectedState, board.state);
    }

    // Test to ensure the agent stops when no moves can be made
    @Test
    public void testAgentStopsWhenStuck() {
        board = new Game();
        board.setGame("1.,2_;2.,1*;2.,1*"); // Initial board setup that leads to a dead end
        agentB.run(board, false);
        boolean anyCovered = false;
        for (int i = 0; i < board.size; i++) {
            for (int j = 0; j < board.size; j++) {
                if (board.state[i][j] == board.COVERED) {
                    anyCovered = true;
                    break;
                }
            }
        }
        assertEquals("Agent should stop when no more moves can be made", true, anyCovered);
    }
}
