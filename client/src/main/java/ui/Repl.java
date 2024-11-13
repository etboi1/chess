package ui;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(CROWN_EMOJI + "Welcome to chess! Type \"Help\" to get started." + CROWN_EMOJI);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(RESET_TEXT_BLINKING + RESET_TEXT_COLOR + SET_TEXT_COLOR_RED + msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_BLINKING + RESET_TEXT_COLOR +
                        SET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN +
                ">>> " + RESET_TEXT_COLOR + RESET_TEXT_BLINKING +
                SET_TEXT_COLOR_DARK_GREY);
    }
}
