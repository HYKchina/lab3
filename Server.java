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

            // Schedule the SummaryTask to run every TIMEOUT_SECONDS seconds, starting immediately.
            timer.schedule(new SummaryTask(), 0, TIMEOUT_SECONDS * 1000);
            
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
            // Use try-with-resources to auto-close input/output streams
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                // Variable to store each line of client input
                String inputLine;
                
                // Continuously read requests from client until connection closes
                while ((inputLine = in.readLine()) != null) {
                    // Increment total operation counter (thread safety needed in multi-threaded environment)
                    operationCount++;
                    
                    // Split input line into command components using whitespace
                    String[] parts = inputLine.split(" ");
                    // Extract command type (first token)
                    String command = parts[0];
                    // Extract key (second token)
                    String key = parts[1];
                    String response;
        
                    // Process PUT command
                    if ("PUT".equals(command)) {
                        // Validate required parameters (key + value)
                        if (parts.length < 3) {
                            response = "ERR invalid input";
                            errorCount++;  // Track invalid input errors
                        } else {
                            // Extract value (third token onward)
                            String value = parts[2];
                            // Delegate to PUT handler method
                            response = handlePut(key, value);
                        }
                    }
                    // Process READ command
                    else if ("READ".equals(command)) {
                        response = handleRead(key);
                    }
                    // Process GET command
                    else if ("GET".equals(command)) {
                        response = handleGet(key);
                    }
                    // Handle unknown commands
                    else {
                        response = "ERR invalid command";
                        errorCount++;  // Track invalid command errors
                    }
                    
                    // Send response back to client
                    out.println(response);
                }
            } catch (IOException e) {
                // Log I/O errors (e.g., connection reset, stream closed)
                e.printStackTrace();
            } finally {
                try {
                    // Ensure client socket is closed even if exceptions occur
                    clientSocket.close();
                } catch (IOException e) {
                    // Silent handling of socket close failure (common during normal disconnects)
                }
            }
        }
        private String handlePut(String key, String value) {
            // Increment the counter tracking total PUT operations
            putCount++;

            // Iterate through all existing tuples to check for duplicate keys
            for (Tuple tuple : tupleSpace) {
                // If a tuple with the same key is found
                if (tuple.getKey().equals(key)) {
                    // Return error message indicating key already exists (protocol-compliant format)
                    return "ERR " + key + " already exists";
                }
            }
                 
    // If the key does not exist, add a new tuple to the tuple space.
            tupleSpace.add(new Tuple(key, value));
            // Return success message with the added key-value pair (protocol-compliant format)
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
    private class SummaryTask extends TimerTask {
        // The main method that is executed when the timer fires.

        @Override
        public void run() {
            // Get the current number of tuples in the tuple space
            int tupleCount = tupleSpace.size();
            
            // Initialize accumulators for total size calculations
            int totalTupleSize = 0;       // Total combined size of all keys and values
            int totalKeySize = 0;         // Total size of all keys (sum of key lengths)
            int totalValueSize = 0;       // Total size of all values (sum of value lengths)
            
            // Iterate through each tuple in the tuple space
            for (Tuple tuple : tupleSpace) {
                // Calculate combined length of key and value for the current tuple
                totalTupleSize += tuple.getKey().length() + tuple.getValue().length();
                
                // Accumulate key length to total key size
                totalKeySize += tuple.getKey().length();
                
                // Accumulate value length to total value size
                totalValueSize += tuple.getValue().length();
            }

            // Calculate averages (avoid division by zero)
            double averageTupleSize = tupleCount > 0 ? (double) totalTupleSize / tupleCount : 0;
            double averageKeySize = tupleCount > 0 ? (double) totalKeySize / tupleCount : 0;
            double averageValueSize = tupleCount > 0 ? (double) totalValueSize / tupleCount : 0;

            // Print server summary statistics
            System.out.println("--------------------- Server Summary ---------------------");
            System.out.println("Tuples number: " + tupleCount);                   // 元组数量
            System.out.println("The average of tuple size: " + averageTupleSize);           // 平均元组大小（字符数）
            System.out.println("The average of key size: " + averageKeySize);               // 平均键长度
            System.out.println("The average of value size: " + averageValueSize);           // 平均值长度
            System.out.println("Total clients number: " + clientCount);           // 总客户端连接数
            System.out.println("Total  operations number: " + operationCount);     // 总操作次数
            System.out.println("Total READs: " + readCount);                         // READ 操作次数
            System.out.println("Total GETs: " + getCount);                           // GET 操作次数
            System.out.println("Total PUTs: " + putCount);                           // PUT 操作次数
            System.out.println("Total errors: " + errorCount);                       // 总错误次数
        }
    }
    // Main entry point for the server
    public static void main(String[] args) {
        // Create a server instance
        Server server = new Server();
        // Start the server
        server.start();
    }
}
