/*
 * Starter code
 * Game class: used to hold the board, parse the input, print the output
 * CS5011 - P1
 * 
 * author: a.toniolo
 */

import java.util.Arrays;

public class Game {

	public final int COVERED = 0;
	public final int PAINTED = 1;
	public final int CLEARED = 2;

	int[][] board;
	int[][] state;
	int[][] game;
	int size;
	/*
	 * board is the one the agent will play with
	 * state indicates the status of the cells
	 * game is the hidden board, the one the agent has to be able to find
	 */

	public Game() {
		size = 2;
		board = new int[size][size];
		state = new int[size][size];
		game = new int[size][size];
	}

	public boolean setGame(String map) {
		try {
			String[] lines = map.split(";"); // rows
			size = lines.length;
			board = new int[size][size];
			state = new int[size][size];
			game = new int[size][size];
			for (int r = 0; r < size; r++) {
				String line = lines[r];
				String[] set = line.split(","); // columns
				int[] bl = new int[size];
				int[] sl = new int[size];
				int[] gl = new int[size];
				for (int c = 0; c < size; c++) {
					String ch = set[c];
					char i = ch.charAt(0); // status
					char e = ch.charAt(ch.length() - 1); // paint
					String m = ch.substring(1, ch.length() - 1); // clue
					int state = 0;
					int paint = 0;
					int clue = 0;
					switch (i) {
						case '*':
							state = 1;
							break;
						case '_':
							state = 2;
							break;
						case '.':
							state = 0;
					}
					if (m.equals("-")) {
						clue = -1;
					} else {
						clue = Integer.parseInt(m);
					}
					switch (e) {
						case '*':
							paint = 1;
							break;
						case '_':
							paint = 2;
							break;
					}
					bl[c] = clue;
					sl[c] = state;
					gl[c] = paint;
				}
				board[r] = bl;
				state[r] = sl;
				game[r] = gl;

			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void printGame() {
		// this method is used to print the initial game view

		System.out.println();
		// first line
		System.out.format("%4s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.format("%4s", c);
		}
		System.out.println();
		// second line
		System.out.format("%5s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.print("--- ");// separator
		}
		System.out.println();
		// the board
		for (int r = 0; r < board.length; r++) {
			System.out.print(" " + r + "| ");// index+separator
			for (int c = 0; c < board[0].length; c++) {
				String code = "";
				// print clues
				if (board[r][c] == -1) {
					code += " ";
				} else {
					code += board[r][c];
				}
				// print paint
				switch (game[r][c]) {
					case 1:
						code += "*";
						break;
					case 2:
						code += "_";
						break;
				}
				System.out.format("%4s", code);
			}
			System.out.println();
		}
		System.out.println();
	}

	public void printBoard() {
		// this is used to print the agent view

		System.out.println();
		// first line
		System.out.format("%4s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.format("%4s", c);
		}
		System.out.println();
		// second line
		System.out.format("%5s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.print("--- ");// separator
		}
		System.out.println();
		// the board
		for (int r = 0; r < board.length; r++) {
			System.out.print(" " + r + "| ");// index+separator
			for (int c = 0; c < board[0].length; c++) {
				String code = "";
				// print state
				switch (state[r][c]) {
					case 0:
						code += ".";
						break;
					case 1:
						code += "*";
						break;
					case 2:
						code += "_";
						break;
				}
				// print clues
				if (board[r][c] == -1) {
					code += "-";
				} else {
					code += board[r][c];
				}
				System.out.format("%4s", code);
			}
			System.out.println();
		}
		System.out.println();

	}

	public int[] getNeighborsStates(int x, int y) {
		int[] neighbors = new int[8];
		int index = 0;

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0)
					continue; // Skip the cell itself

				int nx = x + i;
				int ny = y + j;

				if (nx >= 0 && nx < size && ny >= 0 && ny < size) {
					neighbors[index++] = state[nx][ny];
				}
			}
		}

		return Arrays.copyOf(neighbors, index); // Return only the valid neighbors
	}

	public void paint(int x, int y) {
		state[x][y] = PAINTED; // Painted cell
	}

	public void clear(int x, int y) {
		state[x][y] = CLEARED; // Cleared cell
	}

}
