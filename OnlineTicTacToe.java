import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Ashish Nagar
 */
public class OnlineTicTacToe implements ActionListener {

    private final int INTERVAL = 1000;         // 1 second
    private final int NBUTTONS = 9;            // #bottons
    private ObjectInputStream input = null;    // input from my counterpart
    private ObjectOutputStream output = null;  // output from my counterpart
    private JFrame window = null;              // the tic-tac-toe window
    private JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private String myMark = null;              // "O" or "X"
    private String yourMark = null;            // "X" or "O"

    /**
     * Prints out the usage.
     */
    private static void usage() {
        System.err.println("Usage: java OnlineTicTacToe ipAddr ipPort(>=5000) [auto]");
        System.exit(-1);
    }

    /**
     * Prints out the track trace upon a given error and quits the application.
     *
     * @param e an exception
     */
    private static void error(Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Starts the online tic-tac-toe game.
     *
     * @param args args[0]: my counterpart's ip address, args[1]: his/her port, (arg[2]: "auto") if args.length == 0,
     *             this Java program is remotely launched by JSCH.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            // if no arguments, this process was launched through JSCH
            try {
                OnlineTicTacToe game = new OnlineTicTacToe();
            }
            catch (IOException e) {
                error(e);
            }
        } else {
            // this process wa launched from the user console.

            // verify the number of arguments
            if (args.length != 2 && args.length != 3) {
                System.err.println("args.length = " + args.length);
                usage();
            }

            // verify the correctness of my counterpart address
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(args[0]);
            }
            catch (UnknownHostException e) {
                error(e);
            }

            // verify the correctness of my counterpart port
            int port = 0;
            try {
                port = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                error(e);
            }
            if (port < 5000) {
                usage();
            }

            // check args[2] == "auto"
            if (args.length == 3 && args[2].equals("auto")) {
                // auto play
                OnlineTicTacToe game = new OnlineTicTacToe(args[0]);
            } else {
                // interactive play
                OnlineTicTacToe game = new OnlineTicTacToe(addr, port);
            }
        }
    }

    /**
     * Is the constructor that is remote invoked by JSCH. It behaves as a server. The constructor uses a Connection
     * object for communication with the client. It always assumes that the client plays first.
     */
    public OnlineTicTacToe() throws IOException {
        // receive a ssh2 connection from a user-local master server.
        Connection connection = new Connection();
        input = connection.in;
        output = connection.out;

        // for debugging, always good to write debugging messages to the local file
        // don't use System.out that is a connection back to the client.
        PrintWriter logs = new PrintWriter(new FileOutputStream("logs.txt"));
        logs.println("Autoplay: got started.");
        logs.flush();

        myMark = "X";   // auto player is always the 2nd.
        yourMark = "O";

        myTurn[0] = false; // auto player is always the 2nd.

        Set<Integer> set = new HashSet<>();
        while (true) {
            if (myTurn[0]) {
                int buttonID;
                do {
                    buttonID = (int) (Math.random() * NBUTTONS); // choose a random button
                } while (set.contains(buttonID));
                set.add(buttonID);
                output.writeObject(buttonID);
                output.flush();
                logs.println("Autoplay: I played " + buttonID);
                logs.flush();
            }

            if (!myTurn[0]) {
                try {
                    int youButtonID = (int) input.readObject();
                    set.add(youButtonID);
                    logs.println("Former user played " + youButtonID);
                    logs.flush();
                }
                catch (ClassNotFoundException e) {
                    error(e);
                }
            }
            myTurn[0] = !myTurn[0]; // my turn is over
        }
    }

    /**
     * Is the constructor that, upon receiving the "auto" option, launches a remote OnlineTicTacToe through JSCH. This
     * constructor always assumes that the local user should play first. The constructor uses a Connection object for
     * communicating with the remote process.
     *
     * @param hostname my auto counterpart's ip address
     */
    public OnlineTicTacToe(String hostname) {
        final int JschPort = 22;      // Jsch IP port

        // Read username, password, and a remote host from keyboard
        Scanner keyboard = new Scanner(System.in);
        String username = null;
        String password = null;

        // The JSCH establishment process is pretty much the same as Lab3.
        // IMPLEMENT BY YOURSELF

        try {
            // read the username from the console
            System.out.print("User: ");
            username = keyboard.nextLine();

            // read the password from the console
            Console console = System.console();
            password = new String(console.readPassword("Password: "));
        }
        catch (Exception e) {
            error(e);
        }

        // A command to launch remotely:
        //          java -cp ./jsch-0.1.54.jar:. JSpace.Server
        String cur_dir = System.getProperty("user.dir");
        String command = "java -cp " + cur_dir + "/jsch-0.1.54.jar:" + cur_dir + " OnlineTicTacToe";

        // establish a ssh2 connection to ip and run
        // Server there.
        Connection connection = new Connection(username, password, hostname, command);

        // the main body of the master server
        input = connection.in;
        output = connection.out;

        // set up a window
        makeWindow(true); // I'm a former

        // start my counterpart thread
        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    /**
     * Is the constructor that sets up a TCP connection with my counterpart, brings up a game window, and starts a slave
     * thread for listening to my counterpart.
     *
     * @param addr my counterpart's ip address
     * @param port my counterpart's port
     */
    public OnlineTicTacToe(InetAddress addr, int port) {
        System.out.println("Connecting to " + addr + " on port " + port);
        ServerSocket server = null;
        boolean isServer = false;
        boolean isOccupied = false;
        try {
            System.out.println("Trying to bind to port " + port + ", please wait.");
            server = new ServerSocket(port);
            server.setSoTimeout(INTERVAL);
        }
        catch (Exception e) {
            if (e instanceof BindException) {
                isOccupied = true; // port is already bound
            }
        }

        Socket client = null;
        System.out.println("Waiting for a connection request.");
        while (true) {
            if (addr.getHostName().equals("localhost")) {
                if (!isOccupied) {
                    try {
                        client = server.accept();
                    }
                    catch (SocketTimeoutException ste) {
                        // Couldn't receive a connection request within INTERVAL
                    }
                    catch (IOException ioe) {
                        error(ioe);
                    }
                    // Check if a connection was established. If so, leave the loop
                    if (client != null) {
                        isServer = true;
                        break;
                    }
                }

                if (isOccupied) {
                    try {
                        client = new Socket(addr, port);
                    }
                    catch (IOException ioe) {
                        // Connection refused
                    }
                    // Check if a connection was established, If so, leave the loop
                    if (client != null) {
                        break;
                    }
                }
            } else {
                if (!isOccupied) {
                    try {
                        client = server.accept();
                    }
                    catch (SocketTimeoutException ste) {
                        // Couldn't receive a connection request within INTERVAL
                    }
                    catch (IOException ioe) {
                        error(ioe);
                    }
                    // Check if a connection was established. If so, leave the loop
                    if (client != null) {
                        isServer = true;
                        break;
                    }
                }
                try {
                    client = new Socket(addr, port);
                }
                catch (IOException ioe) {
                    // Connection refused
                }
                // Check if a connection was established, If so, leave the loop
                if (client != null) {
                    break;
                }
            }
        }

        try {
            System.out.println("TCP connection established... [Server = " + !isServer + "]");
            makeWindow(!isServer);
            output = new ObjectOutputStream(client.getOutputStream());
            input = new ObjectInputStream(client.getInputStream());
        }
        catch (Exception e) {
            error(e);
        }

        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    /**
     * Creates a 3x3 window for the tic-tac-toe game
     *
     * @param amFormer true if this window is created by the former, (i.e., the person who starts first. Otherwise,
     *                 false.
     */
    private void makeWindow(boolean amFormer) {
        System.out.println("Creating a window..." + ((amFormer) ? "former" : "latter"));
        myTurn[0] = amFormer;
        myMark = (amFormer) ? "O" : "X";    // 1st person uses "O"
        yourMark = (amFormer) ? "X" : "O";  // 2nd person uses "X"

        // create a window
        window = new JFrame("OnlineTicTacToe(" + ((amFormer) ? "former)" : "latter)") + myMark);
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));

        // initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }

        // make it visible
        window.setVisible(true);
    }

    /**
     * Checks which button has been clicked
     *
     * @param event an event passed from AWT
     *
     * @return an integer (0 through to 8) that shows which button has been clicked. -1 upon an error.
     */
    private int whichButtonClicked(ActionEvent event) {
        for (int i = 0; i < NBUTTONS; i++) {
            if (event.getSource() == button[i]) return i;
        }
        return -1;
    }

    /**
     * Marks the i-th button with mark ("O" or "X")
     *
     * @param i    the  i-th button
     * @param mark a mark ( "O" or "X" )
     *
     * @return true if it has been marked in success
     */
    private boolean markButton(int i, String mark) {
        if (button[i].getText().equals("")) {
            button[i].setText(mark);
            button[i].setEnabled(false);
            return true;
        }
        return false;
    }

    /**
     * Checks if the i-th button has been marked with mark( "O" or "X" ).
     *
     * @param i    the i-th button
     * @param mark a mark ( "O" or "X" )
     *
     * @return true if the i-th button has been marked with mark.
     */
    private boolean buttonMarkedWith(int i, String mark) {
        return button[i].getText().equals(mark);
    }

    /**
     * Pops out another small window indicating that mark("O" or "X") won!
     *
     * @param mark a mark ( "O" or "X" )
     */
    private void showWon(String mark) {
        JOptionPane.showMessageDialog(null, "<html><h1 style=\"color:green;\"><b>" + mark + " won!</b></h1></html>",
                "Game Over", JOptionPane.PLAIN_MESSAGE);
        // Exit the game on clicking "OK"
        //System.exit(0);
        restart(); // restart the game
    }

    /**
     * Is called by AWT whenever any button has been clicked. You have to:
     * <ol>
     * <li> check if it is my turn,
     * <li> check which button was clicked with whichButtonClicked( event ),
     * <li> mark the corresponding button with markButton( buttonId, mark ),
     * <li> send this information to my counterpart,
     * <li> checks if the game was completed with
     *      buttonMarkedWith( buttonId, mark )
     * <li> shows a winning message with showWon( )
     */
    public void actionPerformed(ActionEvent event) {
        // IMPLEMENT BY YOURSELF
        System.out.println("actionPerformed"); // for debugging
        int buttonId = whichButtonClicked(event); // get the button id
        if (buttonId == -1) { // error
            System.out.println("Error: buttonId == -1"); // for debugging
            return;
        }
        // check if it is my turn
        if (myTurn[0]) {
            if (markButton(buttonId, myMark)) { // mark the button
                myTurn[0] = false; // it is not my turn anymore
                try {
                    output.writeObject(buttonId); // send the button id to the other side
                    output.flush(); // flush the output stream
                }
                catch (IOException ioe) {
                    error(ioe);
                }
            }
        }
        // check if the game was completed
        checkWin(buttonId, myMark);
    }

    /**
     * Checks if the game was completed.
     *
     * @param buttonId the id of the button that was clicked
     * @param myMark   my mark ( "O" or "X" )
     */
    private void checkWin(int buttonId, String myMark) {
        int[][] winningCombinations = {{0, 1, 2}, // horizontal
                {3, 4, 5}, // horizontal
                {6, 7, 8}, // horizontal
                {0, 3, 6}, // vertical
                {1, 4, 7}, // vertical
                {2, 5, 8}, // vertical
                {0, 4, 8}, // diagonal
                {2, 4, 6}   // diagonal
        };
        for (int[] combination : winningCombinations) {
            if (buttonMarkedWith(combination[0], myMark) && buttonMarkedWith(combination[1], myMark) && buttonMarkedWith(combination[2], myMark)) {
                showWon(myMark); // show the winning message
                break;
            }
        }
        checkDraw(); // check if the game was completed with a draw
    }

    /**
     * Checks if the game was completed with a draw.
     */
    private void checkDraw() {
        for (int i = 0; i < NBUTTONS; i++) { // check if all buttons are marked
            if (button[i].getText().equals("")) return; // if not, return
        }
        JOptionPane.showMessageDialog(null, "<html><h1 style=\"color:red;\"><b> Draw!</b></h1></html>", "Game Over",
                JOptionPane.PLAIN_MESSAGE);// show the draw message
        // System.exit(0); // exit the game on clicking "OK"
        restart(); // restart the game
    }

    /**
     * Pops out a dialog window asking the user if he/she wants to restart the game.
     */
    private void restart() {
        // Prompt for replay with custom dialog options and HTML formatting
        int input = JOptionPane.showOptionDialog(null, "<html><h2>Wanna restart?</h2></html>", "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        // Check user's choice
        if (input == JOptionPane.YES_OPTION) {
            // Reset game board
            for (int i = 0; i < NBUTTONS; i++) {
                button[i].setText("");
                button[i].setEnabled(true);
            }
        } else if (input == JOptionPane.NO_OPTION) {
            // Display custom message and exit game with HTML formatting
            JOptionPane.showMessageDialog(null, "<html><h2>Thanks for playing!</h2></html>", "Game Over",
                    JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * This is a reader thread that keeps reading from and behaving as my counterpart.
     */
    private class Counterpart extends Thread {

        /**
         * Is the body of the Counterpart thread.
         */
        @Override
        public void run() {
            // IMPLEMENT BY YOURSELF
            System.out.println("Counterpart.run()"); // for debugging
            if (input != null) {
                while (true) { // keep reading
                    try {
                        int buttonId = (Integer) input.readObject(); // read the button id from the other side
                        markButton(buttonId, yourMark); // mark the button
                        myTurn[0] = true; // it is my turn now
                        // check if the game was completed
                        checkWin(buttonId, yourMark); // check if the other side won
                    }
                    catch (IOException | ClassNotFoundException ioe) {
                        error(ioe);
                    }
                }
            }
        }
    }
}
