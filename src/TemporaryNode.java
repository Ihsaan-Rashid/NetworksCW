// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Ihsaan Ishaaq Rashid
// 220009476
// Ihsaan.Rashid@city.ac.uk

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {

    private String ipAddress;
    private int port;
    private String hashID; // Added hashID as a class member

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            // Find the closest full node
            String closestNodeAddress = findClosestFullNode(hashID);

            if (closestNodeAddress != null) {
                // Split the closest node address into IP address and port
                String[] parts = closestNodeAddress.split(":");
                ipAddress = parts[0];
                port = Integer.parseInt(parts[1]);

                // Connect to the closest full node
                Socket socket = new Socket(ipAddress, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send START message
                out.println("START 1 " + startingNodeName);

                // Receive response
                String response = in.readLine();

                // Close connection
                socket.close();

                // Check if connection was successful
                return response != null && response.equals("START 1");
            } else {
                // Closest full node not found
                System.err.println("Error: Closest full node not found");
                return false;
            }
        } catch (Exception e) {
            // Handle connection error
            System.err.println("Error connecting to the closest full node: " + e.getMessage());
            return false;
        }
    }


    public boolean store(String key, String value) {
        try {
            // Connect to the 2D#4 network
            Socket socket = new Socket(ipAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send PUT? request
            out.println("PUT? 1 1"); // Assuming key and value each occupy one line
            out.println(key);
            out.println(value);

            // Receive response
            String response = in.readLine();

            // Close connection
            socket.close();

            // Check if store was successful
            return response != null && response.equals("SUCCESS");

        } catch (Exception e) {
            // Handle store error
            System.err.println("Error storing (key, value) pair: " + e.getMessage());
            return false;
        }
    }


    public String get(String key) {
        try {
            // Connect to the 2D#4 network
            Socket socket = new Socket(ipAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send GET? request
            out.println("GET? " + key); // Assuming key is sent as part of the GET? request

            // Receive response
            String response = in.readLine();

            // Close connection
            socket.close();

            // Check if response contains the value
            if (response != null && response.startsWith("VALUE ")) {
                // Extract the value from the response
                String value = response.substring(6); // Assuming "VALUE " prefix is removed
                return value;
            } else {
                // Value not found or error response
                return null;
            }

        } catch (Exception e) {
            // Handle error
            System.err.println("Error retrieving value from the network: " + e.getMessage());
            return null;
        }
    }

    // New method to find closest full node
    public String findClosestFullNode(String hashID) {
        try {
            // Connect to the 2D#4 network
            Socket socket = new Socket(ipAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send FIND? request
            out.println("FIND? " + hashID); // Assuming hashID is sent as part of the FIND? request

            // Receive response
            String response = in.readLine();

            // Close connection
            socket.close();

            // Check if response contains the closest full node's address
            if (response != null && response.startsWith("CLOSEST ")) {
                // Extract the closest full node's address from the response
                String closestNodeAddress = response.substring(8); // Assuming "CLOSEST " prefix is removed
                return closestNodeAddress;
            } else {
                // No closest full node found or error response
                return null;
            }

        } catch (Exception e) {
            // Handle error
            System.err.println("Error finding closest full node: " + e.getMessage());
            return null;
        }
    }
}
