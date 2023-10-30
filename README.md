# Online-Tic-Tac-Toe-Game
A networked version of Tic Tac Toe allows for real-time gameplay between remote users.

This project exercises how to write a peer-to-peer communicating program using non-blocking accept( ), multiple threads (specifically, the main and slave threads), and JSCH (Java Secure Shell). This is an online Internet program that involves two users in the same tic-tac-toe game or allows a single user to play with an automated remote user.

In a two-user interactive play, each user starts the game locally and operates on a local 3-by-3 tic-tac-toe window that, however, interacts with his or her remote counterpart’s window through the Internet, so that the two users can view the same ongoing progress in their game. On the other hand, in an automated play, a single user gets a local window that interacts with an automated remote player. This remote player doesn’t have to pop out any windows but should write its ongoing status in a “log.txt” file. The game itself is simple needless to say. Therefore, the following specifications focus on only how to start the program and how to manage the game window:

In a two-user interactive play:
(a) Each of the two users starts a game with his or her counterpart’s IP address and port as follows: java OnlineTicTacToe IP_Address They do not care which of their programs would behave as a TCP client or a server. The users may be sitting on the same computer, which thus allows the IP address to be “localhost”. 
(b) After (at most) one TCP connection has been established, each program must decide which will play first with the mark “O” and second with the mark “X”. 
(c) A graphics window marked with “O” must play first, thus accepting its user’s choice of nine buttons. The selected button is marked with “O”, which should be reflected in the same button on the counterpart’s graphics window. Similarly, a graphics window marked with “X” must play second, mark its user’s choice of nine buttons with “X”, and reflect it to the counterpart’s graphics window.
(d) While the counterpart is playing, the local user cannot click any button in his or her game window. Such an action must be ignored or postponed until the local user gets a turn to play. Ignoring or postponing a too-early action is up to your design.
(e) Every time a user (regardless of local or remote) clicks a button, your program needs to check if the user has won the current game, in which case a winning message such as “O won!” or “X won!” should come out on the monitor.

In an automated single-user play:
(a) A user starts a game with his automated player’s IP address, specifying the “auto” parameter as follows: java OnlineTicTacToe IP_ Address auto An autoplay is initiated through JSCH (which uses port 22) and thus needs no port number. However, for an easier argument-parsing purpose, let OnlineTicTacToe still receive a port number as its second argument.
(b) Once a connection has been established through JSCH, this real user may assume that s/he will play first with the mark “O” and the automated player will play second with the mark “X”. 
(c) Only a graphics window marked with “O” will pop out for the real user, so let her or him play first. The automated player should print out its ongoing status in a file named “logs.txt” under your home directory. The auto-player is dumb enough to randomly choose an available button.
(d) The same as the two-user interactive play.
(e) The same as the two-user interactive play.

Graphics: This online tic-tac-toe game needs to display a 3-by-3 graphics window with which the local user can play. Since the main purpose of this programming project is peer-to-peer communication using non-blocking accept and multithreads, we do not have to spend too much time on graphics.

Main() verifies the arguments:

1. If no arguments are passed, this program has been invoked by JSCH remotely. It will instantiate an OnlineTicTacToe object without any arguments that behave as an automated counterpart player.
2. If two arguments are passed, this program starts a two-user interactive game. It will instantiate an OnlineTicTacToe object with the counterpart’s InetAddress and port.
3. If three arguments are passed and the third argument is “auto”, this program starts a single-user automated game. It will instantiate an OnlineTicTacToe object with a counterpart’s IP address (in String)

Documentation (Methods, Descriptions)

1. OnlineTicTacToe( )
The OnlineTicTacToe constructor serves as a server for communication with the client through a Connection object. It sets up an automated player instance, creates an SSH2 connection, initializes input/output streams, sets player marks, and begins a loop for automated play with random button choices. The process is logged for debugging purposes, and synchronized access ensures a shared myTurn flag. The loop continues until game termination.
2. OnlineTicTacToe(String hostname)
When given the "auto" option, the OnlineTicTacToe constructor launches a remote game with JSCH. It prompts the user for their username and password, establishes an SSH2 connection to the specified hostname with the provided credentials, and runs the OnlineTicTacToe program remotely. It sets up input/output streams for communication, creates the game window, and starts the Counterpart thread for remote communication. It assumes that the local user goes first and utilizes the Connection object for communication with the remote process.
3. OnlineTicTacToe(InetAddress addr, int port)
This OnlineTicTacToe constructor sets up a TCP connection with a counterpart at a specified IP address and port. It acts as either the server or client depending on whether the port is bound. If the port is not bound, it creates a ServerSocket and waits for a connection request using accept(). If a connection request is received, it sets the isServer flag to true. If the port is bound, it tries to establish a connection as a client using Socket. Once the connection is established, it creates ObjectOutputStream and ObjectInputStream for communication, sets up the game window using makeWindow(), and starts the Counterpart thread for listening to the counterpart's messages.
4. actionPerformed(ActionEvent event)
The actionPerformed() function handles the user's turn in the Tic-Tac-Toe game. It checks if it is the user's turn, marks the clicked button, sends information to the counterpart, checks for game completion, and shows the winning message.
5. Counterpart.run( )
The Counterpart.run() function is the body of the Counterpart thread. It acts as a reader thread that keeps reading from the counterpart and behaves accordingly in the game. It waits for the counterpart's turn, reads the button ID from the input stream, marks the corresponding button, updates the turn status, notifies waiting threads, and checks for game completion.
6. checkWin(int buttonId, String myMark)
The checkWin() function checks if the current player (indicated by myMark) has won the game by checking all possible winning combinations on the Tic-Tac-Toe board. If a winning combination is found, it calls showWon(myMark) to display the winning message. It calls checkDraw() to check for a draw and invokes the restart() method.
7. checkDraw()
The checkDraw() function checks if all buttons on the Tic-Tac-Toe board are marked, indicating a draw. If an unmarked button is found, it returns without taking action. If all buttons are marked, it displays a "Draw!" message using JOptionPane.showMessageDialog() and exits the game when the "OK" button is clicked. It invokes the restart() method.
8. restart()
The restart() function displays a custom dialog window that asks the user if they want to restart the game after it has ended. It uses a custom dialog with HTML formatting to display the prompt "Wanna restart?" as the title in a larger font. The dialog has two options: "Yes" and "No". If the user selects "Yes," the game board is reset by clearing the text on all buttons and enabling them. If the user selects "No," it displays a custom message "Thanks for playing!" in the dialog with HTML formatting as the title, and exits the game using System.exit(0).

Discussions 

Additional Features:
The game now includes a restart feature that allows users to start a new game with a fresh window. This provides a more advanced feel to the game and allows users to continue playing without having to restart the entire program. For example, after a game ends, the user can choose to restart the game by clicking on a "Restart" button, which resets the game board and allows them to start a new game without closing the program.
1. Aesthetic Enhancements - The game now visually indicates the game result to the users by changing the color of the winning message. When a user wins the game, the winning message is displayed in bright green color, making it visually appealing and indicating a positive outcome. Similarly, when the game ends in a draw, the winning message is displayed in red color, indicating a different outcome. For example, after a user wins the game, a message like "You won!" can be displayed in green color, providing a visual cue to the user about the game result.
2. Acknowledging Game Result - The game now smoothly terminates when the user clicks "OK" to acknowledge the game result. This provides a better user experience as it allows the user to easily close the game window after viewing the game result. For example, after a game ends, a dialog box can be displayed with a message like "Game Over! Click OK to continue", and when the user clicks "OK", the game window closes gracefully, without any abrupt termination.

Limitations:
Currently, the game only supports two players playing against each other. To make the game more versatile and accommodating for larger groups, support for multiple players could be added. For example, the game could be modified to allow more than two players to join the game and play against each other, either in teams or individually.
1. Technology Improvement - The game currently uses AWT for its graphical user interface (GUI), which is platform-dependent and considered heavyweight. To make the game more modern and platform-independent, the GUI could be upgraded to use a newer framework like Swing, which is lightweight and widely used in Java applications. For example, by migrating from AWT to Swing, the game could have a more modern and responsive user interface.
2. Exception Handling - The program's exception handling could be improved to cover edge cases and ensure smooth execution. For example, proper exception handling could be added to handle scenarios such as invalid user input, unexpected network issues, or file I/O errors. This would help in providing better error messages to the users and prevent the program from crashing due to unexpected errors.

Possible Improvements:
The program could be further improved by adding a modern GUI to make it more user-friendly and visually appealing. For example, a sleek and intuitive GUI with attractive graphics and animations could enhance the overall user experience and make the game more engaging.
1. Smart/Auto Player Logic - The auto player logic could be enhanced by making the Autoplayer choose the best possible move to increase the chances of winning. For example, the auto player could be programmed to analyze the game board and make intelligent decisions based on the current state of the game, such as choosing the optimal move to block the opponent or create a winning strategy.
2. Save and Resume Feature - Adding a feature that allows users to save and resume their games later could be a valuable addition to the game. For example, users could be given the option to save their game progress and resume it later, allowing them to take breaks or continue playing from where they left off.
3. AI Opponent - Adding a feature that allows users to play against an AI opponent could make the game more challenging and enjoyable. For example, users could choose to play against a computer-generated opponent with different difficulty levels, providing a dynamic and engaging gaming experience.
