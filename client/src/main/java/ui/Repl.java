package ui;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    private State state;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println(CROWN_EMOJI + "Welcome to chess! Try one of the following commands to get started." + CROWN_EMOJI);
        String startPrompt = client.help();
        System.out.print(startPrompt);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            state = client.state;
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(RESET_TEXT_BLINKING + RESET_TEXT_COLOR + result);
            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(RESET_TEXT_BLINKING + RESET_TEXT_COLOR + SET_TEXT_COLOR_RED + SET_TEXT_BOLD + msg
                        + RESET_TEXT_BOLD_FAINT);
            }
        }
        System.out.println();
    }

    public void notify(ServerMessage serverMessage) {
        if (serverMessage instanceof NotificationMessage notification) {
            System.out.println(notification.getMessage());
            printPrompt();
        } else if (serverMessage instanceof ErrorMessage error) {
            System.out.println(error.getErrorMessage());
            printPrompt();
        } else {
            LoadGameMessage loadGame = (LoadGameMessage) serverMessage;
            BoardDisplay.displayBoard(loadGame.game.game().getBoard());
            printPrompt();
        }
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_BLINKING + RESET_TEXT_COLOR +
                        SET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN +
                "[" + state + "] " + ">>> ");
    }
}
