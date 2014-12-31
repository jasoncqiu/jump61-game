package jump61;

import static jump61.Color.*;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;

import static org.junit.Assert.*;

/** Unit tests of Player and its subclasses. Better tested in blackbox testing.
 *  @author Jason Qiu
 */
public class PlayerTest {

    @Test
    public void testGets() {
        Writer output = new OutputStreamWriter(System.out);
        Game game = new Game(new InputStreamReader(System.in),
                output, output,
                new OutputStreamWriter(System.err));
        Player player1 = new HumanPlayer(game, RED);
        Player player2 = new AI(game, BLUE);
        player1.setColor(BLUE);
        player2.setColor(RED);
        assertEquals("setColor/getColor error", player1.getColor(), BLUE);
        assertEquals("setColor/getColor error", player2.getColor(), RED);
        assertEquals("getGame error", player1.getGame(), game);
        assertEquals("getGame error", player2.getGame(), game);
    }
}
