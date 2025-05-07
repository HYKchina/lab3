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
    
    private final List<Tuple> tupleSpace = new ArrayList<>();
   
    private int clientCount = 0;
    private int operationCount = 0;
    private int readCount = 0;
    private int getCount = 0;
    private int putCount = 0;
    private int errorCount = 0;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    

}
