// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Ihsaan Ishaaq Rashid
// 220009476
// Ihsaan.Rashid@City.ac.uk

import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    ServerSocket serverSocket;
    Socket clientSocket;
    private Map<String, String> storage;
    private Map<String, String> networkMap;

    public FullNode() {
        this.storage = new HashMap<>();
        this.networkMap = new HashMap<>();
    }

    public boolean listen(String ipAddress, int portNumber) {
        //This method returns true if the node can accept incoming connections and returns false otherwise.
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("FullNode listens for incoming connections on " + ipAddress + ":" + portNumber);
            return true;
        } catch (IOException e) {
            System.err.println("There is an error when listening for incoming connections: " + e.getMessage());
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try {
            while (true) {
                // This accepts the incoming connection
                clientSocket = serverSocket.accept();
                System.out.println("Incoming connection has been accepted from " + clientSocket.getInetAddress().getHostAddress());

                BufferedReader brin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter pwout = new PrintWriter(clientSocket.getOutputStream(), true);

                String response;
                while ((response = brin.readLine()) != null) {
                    if (response.startsWith("ECHO?")) {
                        pwout.println("OHCE");

                    } else if (response.startsWith("PUT?")) {
                        responseToPutRequest(brin, pwout, Integer.parseInt(response.split(" ")[1]), Integer.parseInt(response.split(" ")[2]));

                    } else if (response.startsWith("GET?")) {
                        responseToGetRequest(brin, pwout, Integer.parseInt(response.split(" ")[1]));

                    } else if (response.startsWith("NEAREST?")) {
                        responseToNearestRequest(brin, pwout);

                    } else if (response.startsWith("START")) {
                        // This will store the starting node in the current network map
                        networkMap.put(startingNodeName, startingNodeAddress);
                        System.out.println("Connected to the network with node name: " + response.split(" ")[2]);
                        pwout.println("START 1 " + startingNodeName);

                    } else if (response.startsWith("NOTIFY?")) {
                        // This extracts the node name and address from the NOTIFY? request. It will store the new node in the network map and respond with NOTIFIED.
                        String nodeName = brin.readLine();
                        String nodeAddress = brin.readLine();

                        networkMap.put(nodeName, nodeAddress);

                        pwout.println("NOTIFIED");

                    //This exits the loop and closes the connection.

                    } else if (response.startsWith("END")) {
                        break;
                    }
                }

                // This will close the connection and associated resources
                clientSocket.close();
                brin.close();
                pwout.close();
            }
        } catch (IOException e) {
            System.err.println("There is an error when handling incoming connections: " + e.getMessage());
        }
    }

    // This will respond to PUT? request correctly
    private void responseToPutRequest(BufferedReader brin, PrintWriter pwout, int keyLines, int valueLines) throws IOException {

        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < keyLines; i++) {
            keyBuilder.append(brin.readLine()).append("\n");
        }
        String key = keyBuilder.toString();

        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 0; i < valueLines; i++) {
            valueBuilder.append(brin.readLine()).append("\n");
        }
        String value = valueBuilder.toString();

        // This will store key value pair.
        storage.put(key, value);

        // Responds with SUCCESS
        pwout.println("SUCCESS");
    }

    // This will respond to the GET? request correctly
    private void responseToGetRequest(BufferedReader brin, PrintWriter pwout, int keyLines) throws IOException {

        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < keyLines; i++) {
            keyBuilder.append(brin.readLine()).append("\n");
        }
        String key = keyBuilder.toString();

        // This will check if the value of key exists in storage
        String v = storage.get(key);

        if (v != null) {
            // Respond with the VALUE
            pwout.println("VALUE " + v.split("\n").length + "\n" + v);
        } else {
            // Respond with NOPE command
            pwout.println("NOPE");
        }
    }

    // This will respond to the NEAREST? request correctly. It will find the three nodes with the closest hashID
    private void responseToNearestRequest(BufferedReader brin, PrintWriter pwout) throws IOException {
        String hashId = brin.readLine().split(" ")[1];

        List<String> nearestNodes = SearchNearestNodes(hashId);
        if (nearestNodes.isEmpty()) {
            // Respond with NOPE message
            pwout.println("NOPE");
        } else {
            // Respond with NEAREST message
            StringBuilder responseBuilder = new StringBuilder("NODES" + nearestNodes.size() + "\n");
            for (String node : nearestNodes) {
                responseBuilder.append(node).append("\n");
            }
            pwout.println(responseBuilder.toString());
        }
    }

    // This method is used perform the calculation of the distance between two hashIDs
    private int calculationOfDistance(String hID1, String hID2) {
        int distance = 0;
        for (int i = 0; i < hID1.length(); i++) {
            if (hID1.charAt(i) != hID2.charAt(i)) {
                distance = 256 - i;
                break;
            }
        }
        return distance;
    }

    private List<String> SearchNearestNodes(String hashId) {
        List<String> nearestNodes = new ArrayList<>();

        // This calculates the difference between the provided hashID and each node's hashID
        Map<String, Integer> hashIdDifferences = new HashMap<>();
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            String nodeName = entry.getKey();
            byte[] nodeHashId;
            String nodeHash = "";
            try {
                nodeHashId = HashID.computeHashID(nodeName);
                nodeHash = new String(nodeHashId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("Unsupported encoding: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error converting node hash to byte array: " + e.getMessage());
                return nearestNodes;
            }

            int differenceBetween = calculationOfDistance(nodeHash, hashId);
            hashIdDifferences.put(nodeName, differenceBetween);
        }

        // Next it will sort the nodes based on the difference of distance.
        List<Map.Entry<String, Integer>> sortedNodes = new ArrayList<>(hashIdDifferences.entrySet());
        sortedNodes.sort(Map.Entry.comparingByValue());

        // Then it will add the closest three nodes to the nearestNodes list
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedNodes) {
            if (count >= 3) {
                break;
            }
            nearestNodes.add(entry.getKey());
            count++;
        }
        return nearestNodes;
    }
}
