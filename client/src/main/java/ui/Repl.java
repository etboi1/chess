package ui;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(CROWN_EMOJI + "Welcome to chess! Try one of the following commands to get started." + CROWN_EMOJI);
        String startPrompt = client.help();
        System.out.print(startPrompt);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
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

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_BLINKING + RESET_TEXT_COLOR +
                        SET_TEXT_BLINKING + SET_TEXT_COLOR_GREEN +
                ">>> ");
    }
}
