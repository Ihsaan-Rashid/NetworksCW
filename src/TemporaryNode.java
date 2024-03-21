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
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            // Connect to the starting node's address
            socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //We send a START message to the starting node in order to initiate the communication.
            out.println("START 1 " + startingNodeName);

            // We wait for a response from the starting node. If the response matches the expected START message, then the connection is successful
            String response = in.readLine();
            if (response != null && response.equals("START 1 " + startingNodeName)) {
                System.out.println("Connected to the 2D#4 network");
                return true;
            } else {
                System.err.println("Failed to connect to the 2D#4 network");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error connecting to the 2D#4 network: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        try {
            // Send PUT? request to the server which will store the (key, value) pair
            out.println("PUT? 1 " + value.split("\n").length);
            out.println(key);
            out.println(value);

            // Receive response from the server and read it to determine if the store operation is successful.
            String response = in.readLine();
            if (response != null && response.equals("SUCCESS")) {
                System.out.println("Store successful!");
                return true;
            } else if (response != null && response.equals("FAILED")) {
                System.err.println("Store failed: Node refused to store the value.");
                return false;
            } else {
                System.err.println("Unexpected response from server: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error storing (key, value) pair: " + e.getMessage());
            return false;
        }
    }

    public String get(String key) {
        try {
            // Send GET? request to server to retrieve the value for the given key
            out.println("GET? 1");
            out.println(key);

            // Receive response from the server and read it to determine if the operation was successful.
            // If response starts with VALUE then the server found the value, and we parse the value from response and return it.
            //If response is NOPE it means server did not find the value and we print an error.
            String response = in.readLine();
            if (response != null && response.startsWith("VALUE")) {
                int numLines = Integer.parseInt(response.split(" ")[1]);
                StringBuilder valueBuilder = new StringBuilder();
                for (int i = 0; i < numLines; i++) {
                    valueBuilder.append(in.readLine()).append("\n");
                }
                return valueBuilder.toString();
            } else if (response != null && response.equals("NOPE")) {
                System.err.println("Value not found in the network.");
                return null;
            } else {
                System.err.println("Unexpected response from server: " + response);
                return null;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error retrieving value for key: " + e.getMessage());
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
}
