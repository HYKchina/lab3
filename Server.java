import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server {
    //The time interval (in seconds) at which the server will print a summary of its operations.
    private static final int PORT = 9090;
     //The server listens for the port number of the incoming client connection.
    private static final int TIMEOUT_SECONDS = 10;
    //A list that stores tuples in the tuple space. Each tuple consists of a key-value pair.
    private final List<Tuple> tupleSpace = new ArrayList<>();
    //The total number of clients that have connected to the server.
    private int clientCount = 0;
    private int operationCount = 0;
    private int readCount = 0;
    private int getCount = 0;
    private int putCount = 0;
    private int errorCount = 0;
    // A thread pool with a fixed size of 10 threads for processing multiple client connections at the same time.

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // Start the server, listen for incoming client connections, and schedule rollup tasks
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //Prints a message that the server is running and ready to accept clients        
            System.out.println("Server is running and ready to accept multiple clients...");
            //Create a timer to schedule summary tasks
            Timer timer = new Timer();
            
        } 
        catch (IOException e) {
            // Print the stack trace if an I/O error occurs while starting the server.
            e.printStackTrace();
        }
    }
    //andle a single client session
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

    // Constructor to initialize the client socket.
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    operationCount++;
                    
                    String[] parts = inputLine.split(" ");
                    String command = parts[0];
                    String key = parts[1];
                    String response;
                    if ("PUT".equals(command)) {
                        if (parts.length < 3) {
                            response = "ERR invalid input";
                            errorCount++;
                        } else {
                            String value = parts[2];
                            response = handlePut(key, value);
                        }
                    }else if ("READ".equals(command)) {
                        response = handleRead(key);
                    } else if ("GET".equals(command)) {
                        response = handleGet(key);
                    }
                     else {
                        response = "ERR invalid command";
                        errorCount++;
                    }
                     
                    out.println(response);
                }
            } catch (IOException e) {
                
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                      
                }
            }
        }
        private String handlePut(String key, String value) {
            putCount++;
            for (Tuple tuple : tupleSpace) {
                if (tuple.getKey().equals(key)) {
                    return "ERR " + key + " already exists";
                }
            }
                 
    // If the key does not exist, add a new tuple to the tuple space.
            tupleSpace.add(new Tuple(key, value));
            return "OK (" + key + ", " + value + ") added";
        }
        // Handles the READ operation by retrieving the value associated with the key from the tuple space.
        private String handleRead(String key) {
            // Increment the counter tracking total READ operations
            readCount++;

            // Iterate through all tuples in the tuple space to find a matching key
            for (Tuple tuple : tupleSpace) {
                // Check if the current tuple's key matches the requested key
                if (tuple.getKey().equals(key)) {
                    // Return a success response with the key-value pair
                    return "OK (" + key + ", " + tuple.getValue() + ") read";
                }
            }
            // If no matching key is found, increment the error counter
            errorCount++;
            return "ERR " + key + " does not exist";
        }

        // Handles the GET operation by removing the tuple associated with the key from the tuple space.
        private String handleGet(String key) {
            // Increment the counter tracking total GET operations
            getCount++;
            // Use an iterator to safely traverse and modify the tuple space
            for (Iterator<Tuple> it = tupleSpace.iterator(); it.hasNext(); ) {
                // Get the next tuple in the iteration
                Tuple tuple = it.next();// Check if the current tuple's key matches the requested key
                if (tuple.getKey().equals(key)) {
                    it.remove();// Remove the tuple from the tuple space using the iterator
                    return "OK (" + key + ", " + tuple.getValue() + ") removed";// Return a success response with the removed key-value pair
                }
            }
            // If no matching key is found, increment the error counter
            errorCount++;
            // Return an error response indicating the key does not exist
            return "ERR " + key + " does not exist";
        }
    }
}
