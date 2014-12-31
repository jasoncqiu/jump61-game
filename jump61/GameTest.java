package jump61;

import static jump61.Color.*;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;

import static org.junit.Assert.*;

/** Unit tests of Game. It has few public methods, so its correctness
 * is mostly tested through blackbox testing.
 *  @author Jason Qiu
 */
public class GameTest {

    @Test
    public void testMakeMove() {
        Writer output = new OutputStreamWriter(System.out);
        Game game = new Game(new InputStreamReader(System.in),
                output, output,
                new OutputStreamWriter(System.err));
        Board board = game.getBoard();
        game.makeMove(1, 1);
        game.makeMove(1);
        assertEquals("makeMove error", RED, board.color(1, 1));
        assertEquals("makeMove error", BLUE, board.color(1));
        game.makeMove(1, 1);
        game.makeMove(1);
        assertEquals("makeMove error", 2, board.spots(1, 1));
        assertEquals("makeMove error", 2, board.spots(1));
    }
}
