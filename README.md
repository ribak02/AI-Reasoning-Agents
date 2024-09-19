# AI-Reasoning-Agents

This project implements and evaluates various agents capable of playing and solving a logic-based Mosaic puzzle game.

## Game Infrastructure

- **Puzzle Parser**: Translates puzzle inputs into a workable data structure. Converts input files into a 2D array representing the game board and associated clues.
- **Game Board Representation**: Stores the puzzle's current state, including cell statuses and clues. Uses a matrix to track each cell's state (painted, clear, or covered).
- **Game State Management**: Manages game state transitions, checks win conditions, and validates board updates based on agent actions.
- **Solving Agents Interface**: Defines interfaces/classes for agents to interact with the game board, including methods for making moves and assessing the board state.
- **Visualization and Output**: Displays the game board and solution to users, ranging from text-based to graphical UIs for dynamic state visualization.

## Agents Overview

### Agent A
- **Description**: A rule-based agent that evaluates the puzzle board based on clues and makes logical determinations. It checks if the puzzle is solved, unsolvable, or incomplete but consistent.
- **Operation**: 
  - Iterates over each cell to check if the clues are satisfied based on the neighboring painted and covered cells.
  - Returns status codes: solved, unsolvable, or incomplete.

### Agent B
- **Description**: An iterative agent that makes moves based on the direct implications of clues. It modifies the game board dynamically by applying clues to neighboring cells.
- **Operation**: 
  - Continuously analyzes cells until no further actions can be deduced.
  - Tracks whether any moves are made during each iteration, controlling its decision-making loop.

### Agent C1
- **Description**: A higher complexity agent using propositional logic and a SAT solver to deduce the puzzle solution.
- **Operation**:
  - Converts game rules and clues into logical formulas in Conjunctive Normal Form (CNF).
  - Solves the CNF using the MiniSat solver and updates the game board based on the returned solution.

### Agent C2
- **Description**: An extension of Agent C1, Agent C2 also uses SAT solvers to handle Mosaic puzzles by navigating complex constraints derived from clues.
- **Operation**:
  - Translates puzzle clues into logical statements in CNF.
  - Uses the SAT solver to determine if a solution exists and updates the game board accordingly.

## Test Summary
- **JUnit Tests**:
  - `AgentATest`: 5 tests passed.
  - `AgentBTest`: 5 tests passed.
  - `AgentC1Test`: 5 tests passed.

## Evaluation

### Evaluation of Agent A
- **Strengths**: Efficient for simple puzzles with direct clues, providing quick determination of the puzzle's state.
- **Limitations**: Struggles with complex puzzles requiring indirect reasoning, making it less effective in larger or more intricate puzzles.

### Evaluation of Agent B
- **Strengths**: Dynamic reasoning allows it to handle puzzles of moderate complexity, efficiently processing clues and making logical deductions.
- **Limitations**: Limited in handling puzzles requiring advanced problem-solving techniques, such as puzzles with indirect clues or requiring hypothesis testing.

### Evaluation of Agent C1
- **Strengths**: Excels at solving complex puzzles by using SAT solvers and logical reasoning. Efficient in handling intricate puzzles with interwoven clues.
- **Limitations**: Computationally intensive for simple puzzles, where simpler heuristic methods may be faster.

### Evaluation of Agent C2
- **Strengths**: Combines game heuristics with logical reasoning to handle well-defined puzzles efficiently. Performs well even in complex puzzles requiring SAT solving.
- **Limitations**: Struggles with puzzles that require dynamic heuristic adjustments or probabilistic clues, and its reliance on SAT solver performance can limit its efficiency.

## Comparison

- **Agent A**: Best suited for simple puzzles but lacks depth for complex scenarios.
- **Agent B**: An effective balance for simple to moderately complex puzzles but falls short in more intricate scenarios.
- **Agent C1**: A powerful solver for highly complex puzzles, utilizing SAT solvers and logical reasoning to systematically find solutions.

## Conclusion

This project successfully implemented and evaluated various agents (Agent A, B, C1, and C2) for solving Mosaic puzzles. Each agent was tested rigorously to ensure correctness and functionality across a range of puzzle complexities. While Agent A and B excel at handling straightforward puzzles, Agent C1 demonstrates superior performance in solving intricate puzzles through logical deductions and SAT solving. Agent C2, though capable, requires further optimization to handle edge cases effectively.
