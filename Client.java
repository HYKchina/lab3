import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    // The host address of the server 
    private static final String SERVER_HOST = "localhost";
    // The port number of the server 
    private static final int SERVER_PORT = 9090;

    // Main method: Entry point of the client program
    public static void main(String[] args) {
        // Define the list of client request files to be sent
        String[] filePaths = {
            "client_1.txt",
            "client_2.txt",
            "client_3.txt",
            "client_4.txt",
            "client_5.txt",
            "client_6.txt",
            "client_7.txt",
            "client_8.txt",
            "client_9.txt",
            "client_10.txt"
        };
        // Iterate through each file path and process client requests sequentially
        for (String filePath : filePaths) {
            // Use try-with-resources to auto-close resources (file reader, socket, streams)
            try (
                // Create a file reader to read the current client request file
                BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
                // Create a socket to connect to the server
                Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                // Create an input stream to read server responses
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Create an output stream to send requests to the server
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Temporary variable: Store each line of request read from the file
                String inputLine;
                // Read each line from the file until EOF
                while ((inputLine = fileReader.readLine()) != null) {
                    // Send the request to the server
                    out.println(inputLine);
                    // Read the response from the server
                    String response = in.readLine();
                    // Print the request and its corresponding response
                    System.out.println(inputLine + ": " + response);
                }
            } catch (IOException e) {
                // Catch and handle I/O exceptions (e.g., file not found, network error)
                e.printStackTrace();
            }
        }
    
    }
}
