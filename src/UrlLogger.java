import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlLogger {
    private List<String> visitedUrls = new ArrayList<>();
    private CountDownLatch browserThreadLatch = new CountDownLatch(1);

    public void startMonitoring() {
        // Launch the default web browser with a URL in a separate thread
        Thread browserThread = new Thread(() -> {
            try {
                String urlToOpen = "http://example.com"; // Replace with your desired URL
                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", urlToOpen);
                Process process = processBuilder.start();

                // Read the process output to capture URLs
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    captureUrls(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Notify the main thread that the browser thread has completed
                browserThreadLatch.countDown();
            }
        });

        // Start the browser monitoring thread
        browserThread.start();

        try {
            // Wait for the browser thread to complete before continuing
            browserThreadLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void captureUrls(String text) {
        // Define a regular expression to match URLs
        String regex = "(https?://[^\s]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String url = matcher.group();
            visitedUrls.add(url);
            System.out.println("Visited URL: " + url);
        }
    }

    public List<String> getVisitedUrls() {
        return visitedUrls;
    }

    public static void main(String[] args) {
        UrlLogger urlLogger = new UrlLogger();
        urlLogger.startMonitoring();

        // Add your main application logic here
        System.out.println("Main application is running...");
    }
}
