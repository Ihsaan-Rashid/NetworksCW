// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Ihsaan Ishaaq Rashid
// 220009476
// Ihsaan.Rashid@City.ac.uk


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted

        try {
            // Splitting starting node address to get IP address and port
            String[] parts = startingNodeAddress.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Establishing TCP connection to starting node
            socket = new Socket(ipAddress, port);

            // Sending START message
            PrintWriter pwout = new PrintWriter(socket.getOutputStream(), true);
            pwout.println("START 1 " + startingNodeName);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();

            // Checking if START message received successfully
            if (response != null && response.startsWith("START")) {
                System.out.println("Connected to the 2D#4 network");
                return true;
            } else {
                System.err.println("Failed to commence communication with starting node");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to starting node: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        try {
            // Check if the socket is not connected or if it is empty
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                System.err.println("You are not connected to the network");
                return false;
            }

            // Create PrintWriter for socket communication
            PrintWriter writerOut = new PrintWriter(socket.getOutputStream(), true);

            // Create the PUT request
            String request = "PUT? " + key.lines().count() + " " + value.lines().count() + "\n" + key + value;
            // This sends the PUT request
            writerOut.println(request);

            // Flush the writer to ensure the request is sent immediately
            writerOut.flush();

            // Returns if the operation is successful
            return true;
        } catch (IOException e) {
            System.err.println("Issues with storing keys and values" + e.getMessage());
            return false;
        }
    }


    public String get(String key) {
        try {
            // Check if the socket is not connected or if it is empty
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                System.err.println("You are not connected to the network");
                return null;
            }

            // Create PrintWriter and BufferedReader for socket communication
            PrintWriter writerOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader readerIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Create the GET request
            String request = "GET? " + key.lines().count() + "\n" + key;
            // This sends the GET request
            writerOut.println(request);

            // Flush the writer to ensure the request is sent immediately
            writerOut.flush();

            // This would get the request
            String response = readerIn.readLine();

            if (response != null && response.startsWith("VALUE")) {
                // Return the string if the get worked
                return response.substring(6).trim();
            } else {
                // Return null if the value could not be found which means NOPE
                return null;
            }
        } catch (IOException e) {
            System.err.println("Issues with getting value for key " + e.getMessage());
            // Return null if it did not get the value for key
            return null;
        }
    }

    public String echoRequest() {
        try {
            // Send ECHO? request to check if the connection is active
            out.println("ECHO?");

            // Receive response from the server and read it to determine if the connection is active or not.
            String response = in.readLine();
            if (response != null && response.equals("OHCE")) {
                return "Connection is active.";
            } else {
                return "Connection is not active.";
            }
        } catch (IOException e) {
            System.err.println("Error sending ECHO? request: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    public boolean notifyRequest(String nodeName, String nodeAddress) {
        try {
            // Send NOTIFY request to inform the server of a full node's address
            //Therefore we provide the node name and address as parameters to the method.
            out.println("NOTIFY?");
            out.println(nodeName);
            out.println(nodeAddress);

            // Receive response from the server. If response is NOTIFIED then we return true.
            //If response is unexpected then we handle it accordingly and return false.
            String response = in.readLine();
            return response != null && response.equals("NOTIFIED");
        } catch (IOException e) {
            System.err.println("Error sending NOTIFY? request: " + e.getMessage());
            return false;
        }
    }
    public String[] nearestRequest(String hashID) {
        try {
            // Send NEAREST? request to find the nearest nodes to the given hashID
            out.println("NEAREST? " + hashID);

            // Receive response from the server. If response starts with NODES, it means nearest nodes were found,and we parse and return them.
            //If response is unexpected we return an empty array.
            String response = in.readLine();
            if (response != null && response.startsWith("NODES")) {
                int numNodes = Integer.parseInt(response.split(" ")[1]);
                String[] nodes = new String[numNodes];
                for (int i = 0; i < numNodes; i++) {
                    nodes[i] = in.readLine();
                }
                return nodes;
            } else {
                return new String[0];
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error sending NEAREST? request: " + e.getMessage());
            return new String[0];
        }
    }
    public void end() {
        try {
            // Send END message to terminate the communication
            out.println("END");

            // Close the input and output streams and the socket
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error ending communication: " + e.getMessage());
        }
    }
}
