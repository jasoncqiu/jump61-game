package jump61;

import java.util.ArrayList;

/** An automated Player.
 *  @author Jason Qiu
 */
class AI extends Player {

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically.
     */
    AI(Game game, Color color) {
        super(game, color);
    }

    @Override
    void makeMove() {
        Game game = getGame();
        Board board = getBoard();
        if (board.isLegal(getColor())) {
            ArrayList<Move> moves = new ArrayList<Move>();
            Board copy = new MutableBoard(board);
            minmax(getColor(), copy, 5, Integer.MAX_VALUE, moves);
            if (moves.isEmpty()) {
                return;
            }
            Move best = moves.get(0);
            for (Move move : moves) {
                if (move.value() > best.value()) {
                    best = move;
                }
            }
            game.message("%1$s moves %2$s %3$s.%n"
                    , getColor().toCapitalizedString()
                    , board.row(best.move()), board.col(best.move()));
            game.makeMove(best.move());
            game.getOtherPlayer(getColor()).makeMove();
        }
    }

    /** Return the minimum of CUTOFF and the minmax value of board B
     *  (which must be mutable) for player P to a search depth of D
     *  (where D == 0 denotes evaluating just the next move).
     *  If MOVES is not null and CUTOFF is not exceeded, set MOVES to
     *  a list of all highest-scoring moves for P; clear it if
     *  non-null and CUTOFF is exceeded. the contents of B are
     *  invariant over this call. */
    private int minmax(Color p, Board b, int d, int cutoff,
                       ArrayList<Move> moves) {
        if (b.getWinner() == p) {
            return Integer.MAX_VALUE;
        } else if (b.getWinner() == p.opposite()) {
            return -Integer.MAX_VALUE;
        } else if (d == 0) {
            return staticEval(p, b);
        }
        int bestSoFar = -Integer.MAX_VALUE;
        for (int i = 0; i < b.size() * b.size(); i += 1) {
            if (b.isLegal(p, i)) {
                b.addSpot(p, i);
                int response = minmax(p.opposite()
                        , b, d - 1, -bestSoFar, new ArrayList<Move>());
                b.undo();
                moves.add(new Move(i, -response));
                if (-response > bestSoFar) {
                    bestSoFar = -response;
                    if (-response >= cutoff) {
                        break;
                    }
                }
            }
        }
        return bestSoFar;
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    private int staticEval(Color p, Board b) {
        return b.numOfColor(p)
                - b.numOfColor(p.opposite());
    }

    /** Move class.*/
    private class Move {
        /** Value of a move. */
        private int value;
        /** The move itself. */
        private int move;

        /** Constructs a move with move M and value V. */
        Move(int m, int v) {
            move = m;
            value = v;
        }

        /** Returns this move's value. */
        int value() {
            return value;
        }

        /** Returns this move's move. */
        int move() {
            return move;
        }
    }
}


