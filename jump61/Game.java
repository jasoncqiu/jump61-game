package jump61;

import java.io.Reader;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Random;

import static jump61.Color.*;
import static jump61.GameException.error;

/** Main logic for playing (a) game(s) of Jump61.
 *  @author Jason Qiu
 */
class Game {

    /** Name of resource containing help message. */
    private static final String HELP = "jump61/Help.txt";

    /** A new Game that takes command/move input from INPUT, prints
     *  normal output on OUTPUT, prints prompts for input on PROMPTS,
     *  and prints error messages on ERROROUTPUT. The Game now "owns"
     *  INPUT, PROMPTS, OUTPUT, and ERROROUTPUT, and is responsible for
     *  closing them when its play method returns. */
    Game(Reader input, Writer prompts, Writer output, Writer errorOutput) {
        _board = new MutableBoard(Defaults.BOARD_SIZE);
        _readonlyBoard = new ConstantBoard(_board);
        _prompter = new PrintWriter(prompts, true);
        _inp = new Scanner(input);
        _inp.useDelimiter("(?m)\\p{Blank}*$|^\\p{Blank}*|\\p{Blank}+");
        _out = new PrintWriter(output, true);
        _err = new PrintWriter(errorOutput, true);
        redPlayer = new HumanPlayer(this, RED);
        bluePlayer = new AI(this, BLUE);
    }

    /** Returns a readonly view of the game board.  This board remains valid
     *  throughout the session. */
    Board getBoard() {
        return _readonlyBoard;
    }

    /** Play a session of Jump61.  This may include multiple games,
     *  and proceeds until the user exits.  Returns an exit code: 0 is
     *  normal; any positive quantity indicates an error.  */
    int play() {
        _out.println("Welcome to " + Defaults.VERSION);
        _out.flush();
        boolean loop = true;
        while (loop) {
            try {
                if (promptForNext()) {
                    readExecuteCommand();
                } else {
                    break;
                }
            } catch (GameException e) {
                _inp.nextLine();
                reportError(e.getMessage());
                _err.flush();
            }
        }
        return 0;
    }

    /** Get a move from my input and place its row and column in
     *  MOVE.  Returns true if this is successful, false if game stops
     *  or ends first. */
    boolean getMove(int[] move) {
        if (_move[0] > 0) {
            move[0] = _move[0];
            move[1] = _move[1];
            _move[0] = 0;
            return true;
        } else {
            return false;
        }
    }

    /** Add a spot to R C, if legal to do so. */
    void makeMove(int r, int c) {
        if (_board.isLegal(_board.whoseMove(), r, c)) {
            _board.addSpot(_board.whoseMove(), r, c);
            checkForWin();
        } else {
            throw error("invalid move: %1$s %2$s", r, c);
        }
    }

    /** Add a spot to square #N, if legal to do so. */
    void makeMove(int n) {
        makeMove(_board.row(n), _board.col(n));
    }

    /** Return a random integer in the range [0 .. N), uniformly
     *  distributed.  Requires N > 0. */
    int randInt(int n) {
        return _random.nextInt(n);
    }

    /** Send a message to the user as determined by FORMAT and ARGS, which
     *  are interpreted as for String.format or PrintWriter.printf. */
    void message(String format, Object... args) {
        _out.printf(format, args);
    }

    /** Returns the player with the other color of C. */
    Player getOtherPlayer(Color c) {
        if (c == RED) {
            return bluePlayer;
        } else {
            return redPlayer;
        }
    }

    /** Check whether we are playing and there is an unannounced winner.
     *  If so, announce and stop play. */
    private void checkForWin() {
        if (_playing && _board.getWinner() != null) {
            announceWinner();
            _playing = false;
        }
    }

    /** Send announcement of winner to my user output. */
    private void announceWinner() {
        _out.println(_board.getWinner().toCapitalizedString() + " wins.");
        _out.flush();
    }

    /** Make PLAYER an AI for subsequent moves. */
    private void setAuto(Color player) {
        _playing = false;
        if (redPlayer.getColor() == player) {
            redPlayer = new AI(this, player);
        } else {
            bluePlayer = new AI(this, player);
        }
    }

    /** Make PLAYER take manual input from the user for subsequent moves. */
    private void setManual(Color player) {
        _playing = false;
        if (redPlayer.getColor() == player) {
            redPlayer = new HumanPlayer(this, player);
        } else {
            bluePlayer = new HumanPlayer(this, player);
        }
    }

    /** Stop any current game and clear the board to its initial
     *  state. */
    private void clear() {
        _playing = false;
        _board.clear(_board.size());
    }

    /** Print the current board using standard board-dump format. */
    private void dump() {
        _out.print(_board);
        _out.flush();
    }

    /** Print a help message. */
    private void help() {
        Main.printHelpResource(HELP, _out);
    }

    /** Stop any current game and set the move number to N. */
    private void setMoveNumber(int n) {
        if (n == 0) {
            throw error("syntax error.");
        }
        _playing = false;
        _board.setMoves(n);
    }

    /** Seed the random-number generator with SEED. */
    private void setSeed(long seed) {
        _random.setSeed(seed);
    }

    /** Place SPOTS spots on square R:C and color the square red or
     *  blue depending on whether COLOR is "r" or "b".  If SPOTS is
     *  0, clears the square, ignoring COLOR.  SPOTS must be less than
     *  the number of neighbors of square R, C. */
    private void setSpots(int r, int c, int spots, String color) {
        if (_board.neighbors(r, c) < spots) {
            throw error("spots must be less than the number of neighbors");
        } else {
            _playing = false;
            Color player;
            if (color.equals("r")) {
                player = RED;
            } else if (color.equals("b")) {
                player = BLUE;
            } else {
                throw error("syntax error.");
            }
            if (spots == 0) {
                player = WHITE;
            }
            _board.set(r,  c, spots, player);
        }
    }

    /** Stop any current game and set the board to an empty N x N board
     *  with numMoves() == 0.  */
    private void setSize(int n) {
        if (n == 0) {
            throw error("syntax error.");
        }
        _playing = false;
        _board.clear(n);
    }

    /** Begin accepting moves for game.  If the game is won,
     *  immediately print a win message and end the game. */
    private void restartGame() {
        _playing = true;
        checkForWin();
    }

    /** Save move R C in _move.  Error if R and C do not indicate an
     *  existing square on the current board. */
    private void saveMove(int r, int c) {
        if (!_board.exists(r, c)) {
            throw error("move %d %d out of bounds", r, c);
        }
        _move[0] = r;
        _move[1] = c;
    }

    /** Returns a color (player) name from _inp: either RED or BLUE.
     *  Throws an exception if not present. */
    private Color readColor() {
        if (_inp.hasNext("[rR][eE][dD]|[Bb][Ll][Uu][Ee]")) {
            return Color.parseColor(_inp.next("[rR][eE][dD]|[Bb][Ll][Uu][Ee]"));
        } else {
            throw error("syntax error.");
        }
    }

    /** Returns the integer value of an input, if it exists. */
    private int readInt() {
        if (_inp.hasNextInt()) {
            int num = _inp.nextInt();
            if (num < 0) {
                throw error("syntax error");
            }
            return num;
        } else {
            throw error("syntax error.");
        }
    }

    /** Read and execute one command.  Leave the input at the start of
     *  a line, if there is more input. */
    private void readExecuteCommand() {
        if (_inp.hasNextInt()) {
            executeCommand("makeMove");
        } else {
            executeCommand(_inp.next());
        }
    }

    /** Gather arguments and execute command CMND.  Throws GameException
     *  on errors. */
    private void executeCommand(String cmnd) {
        switch (cmnd) {
        case "\n": case "\r\n":
            return;
        case "#":
            break;
        case "clear":
            clear();
            break;
        case "makeMove":
            if (!_playing) {
                throw error("no game in progress.");
            } else {
                saveMove(readInt(), readInt());
                redPlayer.makeMove();
                bluePlayer.makeMove();
            }
            break;
        case "start":
            redPlayer.makeMove();
            bluePlayer.makeMove();
            restartGame();
            break;
        case "quit":
            System.exit(0);
            break;
        case "auto":
            setAuto(readColor());
            break;
        case "manual":
            setManual(readColor());
            break;
        case "size":
            setSize(readInt());
            break;
        case "move":
            setMoveNumber(readInt());
            break;
        case "set":
            setSpots(readInt(), readInt(), readInt(), _inp.next());
            break;
        case "dump":
            dump();
            break;
        case "seed":
            setSeed(readInt());
            break;
        case "help":
            help();
            break;
        default:
            throw error("bad command: '%s'", cmnd);
        }
        _inp.nextLine();
    }

    /** Print a prompt and wait for input. Returns true iff there is another
     *  token. */
    private boolean promptForNext() {
        if (_playing) {
            _prompter.print(_board.whoseMove() + "> ");
        } else {
            _prompter.print("> ");
        }
        _prompter.flush();
        return _inp.hasNext();
    }

    /** Send an error message to the user formed from arguments FORMAT
     *  and ARGS, whose meanings are as for printf. */
    void reportError(String format, Object... args) {
        _err.print("Error: ");
        _err.printf(format, args);
        _err.println();
    }

    /** Writer on which to print prompts for input. */
    private final PrintWriter _prompter;
    /** Scanner from current game input.  Initialized to return
     *  newlines as tokens. */
    private final Scanner _inp;
    /** Outlet for responses to the user. */
    private final PrintWriter _out;
    /** Outlet for error responses to the user. */
    private final PrintWriter _err;

    /** The board on which I record all moves. */
    private final Board _board;
    /** A readonly view of _board. */
    private final Board _readonlyBoard;

    /** A pseudo-random number generator used by players as needed. */
    private final Random _random = new Random();

    /** True iff a game is currently in progress. */
    private boolean _playing;

    /** The Red player. */
    private Player redPlayer;
    /** The Blue player. */
    private Player bluePlayer;

    /** Used to return a move entered from the console.  Allocated
     *  here to avoid allocations. */
    private final int[] _move = new int[2];
}
