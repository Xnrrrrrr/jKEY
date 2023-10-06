import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LogExporter {
    // Method to export logs to a TXT file
    public static void exportLogs(List<String> logs, String filename) {
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
}