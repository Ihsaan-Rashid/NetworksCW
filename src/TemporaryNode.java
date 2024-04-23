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


    public boolean start(String startingNodeName, String startingNodeAddress) {

        try {
            // Splitting starting node address in order to receive the IP address and port
            String[] parts = startingNodeAddress.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Establishing a TCP connection to the starting node
            socket = new Socket(ipAddress, port);

            // Attempt to send START message
            PrintWriter pwout = new PrintWriter(socket.getOutputStream(), true);
            pwout.println("START 1 " + startingNodeName);

            BufferedReader brin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = brin.readLine();

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
            // Constructing the PUT request
            String putRequest = "PUT? " + key.split("\n").length + " " + value.split("\n").length + "\n" + key + value;

            // Attempting to send PUT message
            PrintWriter pwout = new PrintWriter(socket.getOutputStream(), true);
            pwout.println(putRequest);

            BufferedReader brin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = brin.readLine();

            // This will check to see if the response indicates success or failure and outputs a message regarding the situation.
            if (response != null && response.equals("SUCCESS")) {
                return true;
            } else if (response != null && response.equals("FAILED")) {
                System.err.println("Store command failed: Network Directory refused request");
                return false;
            } else {
                System.err.println("Invalid response has been received");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error storing the key-value pair: " + e.getMessage());
            return false;
        }
    }


    public String get(String key) {

        try {
            // Constructing the GET request
            String getRequest = "GET? " + key.split("\n").length + "\n" + key;

            // This will attempt to send GET message
            PrintWriter pwout = new PrintWriter(socket.getOutputStream(), true);
            pwout.println(getRequest);

            BufferedReader brin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String sResponse = brin.readLine();


            // This will check if the response a indicates success or failure and displays a message regarding the situation.
            if (sResponse != null && sResponse.startsWith("VALUE")) {
                // Extracting value
                int valueLines = Integer.parseInt(sResponse.split(" ")[1]);
                StringBuilder valueBuilder = new StringBuilder();
                for (int i = 0; i < valueLines; i++) {
                    valueBuilder.append(brin.readLine());
                    if (i < valueLines - 1)
                        valueBuilder.append("\n");
                }
                return valueBuilder.toString();
            } else if (sResponse != null && sResponse.equals("NOPE")) {
                System.err.println("No value has been found for key: " + key);
                return null;
            } else {
                System.err.println("Invalid response has been received");
                return null;
            }
        } catch (IOException e) {
            System.err.println("There has been an error retrieving the value for key: " + e.getMessage());
            return null;
        }
    }



}
