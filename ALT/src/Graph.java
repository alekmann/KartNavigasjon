import java.io.*;
import java.util.*;

public class Graph {
    static int N, K;
    public static final int INFINITY = 1000000000;
    Node[] nodes;
    int[][] distanceFromLM;
    int[][] distanceToLM;

    public Graph(String nodePath, String edgePath, boolean transposed) {
        try {
            initNodes(nodePath);
            initEdges(edgePath, transposed);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void initNodes(String nodePath) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(nodePath));
        StringTokenizer st = new StringTokenizer(bf.readLine());
        N = Integer.parseInt(st.nextToken());
        nodes = new Node[N];

        String line;
        while ((line = bf.readLine()) != null) {
            st = new StringTokenizer(line);
            int index = Integer.parseInt(st.nextToken());
            float latitude = Float.parseFloat(st.nextToken());
            float longitude = Float.parseFloat(st.nextToken());
            nodes[index] = new Node(index, latitude, longitude);
        }
    }

    public void initPOI(String placePath) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(placePath));
        StringTokenizer st = new StringTokenizer(bf.readLine());
        int P = Integer.parseInt(st.nextToken());

        String pLine;

        while((pLine = bf.readLine()) != null){
            st = new StringTokenizer(pLine);
            int pIndex = Integer.parseInt(st.nextToken());
            int pCode = Integer.parseInt(st.nextToken());
            StringBuilder pName = new StringBuilder(st.nextToken());

            while (st.hasMoreTokens()) {
                pName.append(" ").append(st.nextToken());
            }

            nodes[pIndex].code = pCode;
            nodes[pIndex].name = pName.substring(1, pName.length()-1);
        }

        bf.close();
    }

    private void initEdges(String edgePath, boolean transposed) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(edgePath));
        StringTokenizer st = new StringTokenizer(bf.readLine());
        K = Integer.parseInt(st.nextToken());

        String line;
        while ((line = bf.readLine()) != null) {
            st = new StringTokenizer(line);
            int from = Integer.parseInt(st.nextToken());
            int to = Integer.parseInt(st.nextToken());
            int driveLength = Integer.parseInt(st.nextToken());

            Edge edge;
            if (transposed) {
                edge = new Edge(nodes[from], driveLength);
                nodes[to].edges.add(edge);
                continue;
            }
            edge = new Edge(nodes[to], driveLength);
            nodes[from].edges.add(edge);
        }
        bf.close();
    }

    public void initDistances(String distFromPath, String distToPath) throws IOException {
        distanceFromLM = readDistance(distFromPath);
        distanceToLM = readDistance(distToPath);
    }

    private int[][] readDistance(String distancePath) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(distancePath));
        StringTokenizer st = new StringTokenizer(bf.readLine());
        int[][] distArr = new int[Integer.parseInt(st.nextToken())][Integer.parseInt(st.nextToken())];
        String line;
        for (int rowNum = 0; (line = bf.readLine()) != null; rowNum++) {
            st = new StringTokenizer(line);

            for (int colNum = 0; st.hasMoreTokens(); colNum++) {
                distArr[rowNum][colNum] = Integer.parseInt(st.nextToken());
            }
        }
        return distArr;
    }

    public void dijkstras(int startIndex) {
        resetNodes();
        Node startNode = nodes[startIndex];

        startNode.distance = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        priorityQueue.add(startNode);

        Node currentNode;
        while ((currentNode = priorityQueue.poll()) != null) {
            for (Edge edge : currentNode.edges) {
                optimizeDistToStart(currentNode, edge, priorityQueue);
            }
        }
    }

    public long[] dijkstras(int startIndex, int goalIndex) {
        resetNodes();
        // Index 0: timeUsed, index 1: distance, index 2: nodes traversed
        long[] values = new long[3];
        long timeStarted = System.nanoTime();

        Node startNode = nodes[startIndex];
        Node goalNode = nodes[goalIndex];

        startNode.distance = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        priorityQueue.add(startNode);

        Node currentNode;
        while ((currentNode = priorityQueue.poll()) != null) {
            // Quit if goal is reached
            if (currentNode.equals(goalNode)) {
                values[1] = goalNode.distance;
                values[0] = System.nanoTime() - timeStarted;
                return values;
            }

            // Redirect path if a shorter path is found
            for (Edge edge : currentNode.edges) {
                optimizeDistToStart(currentNode, edge, priorityQueue);
            }
            values[2]++;
        }
        throw new RuntimeException("Could not find a path between these two points.");
    }

    public long[] ALT(int startIndex, int goalIndex) {
        resetNodes();
        // Index 0: timeUsed, index 1: distance, index 2: nodes traversed
        long[] values = new long[3];
        long timeStarted = System.nanoTime();

        Node startNode = nodes[startIndex];
        Node goalNode = nodes[goalIndex];

        startNode.distance = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance + n.heuristic));
        priorityQueue.add(startNode);

        Node currentNode;
        while ((currentNode = priorityQueue.poll()) != null) {
            if (currentNode.equals(goalNode)) {
                values[1] = goalNode.distance;
                values[0] = System.nanoTime() - timeStarted;
                return values;
            }

            for (Edge edge : currentNode.edges) {
                if (!edge.nextNode.found) {
                    calcHeuristicValue(edge.nextNode, goalNode);
                    edge.nextNode.found = true;
                    values[2]++;
                }
                optimizeDistToStart(currentNode, edge, priorityQueue);
            }
        }
        System.out.println("Traversed: " + values[2]);
        throw new RuntimeException("Could not find a path between these two points.");
    }

    private void optimizeDistToStart(Node currentNode, Edge edge, PriorityQueue<Node> priorityQueue) {
        Node nextNode = edge.nextNode;
        if (nextNode.distance > currentNode.distance + edge.driveLength) {
            nextNode.predecessor = currentNode;
            nextNode.distance = currentNode.distance + edge.driveLength;
            if (nextNode.found) {
                priorityQueue.remove(nextNode);
            }
            priorityQueue.add(nextNode);
            nextNode.found = true;
        }
    }

    private void calcHeuristicValue(Node currentNode, Node goalNode) {
        ArrayList<Integer> estimates = new ArrayList<>();

        for (int[] distance : distanceFromLM) {
            estimates.add(Math.abs(distance[currentNode.nodeID] - distance[goalNode.nodeID]));
        }

        currentNode.heuristic = Collections.min(estimates);
    }

    public int findIndexOf(String locationName) {
        for (Node node : nodes) {
            if (node.name != null && node.name.equals(locationName)) {
                return node.nodeID;
            }
        }
        throw new RuntimeException("Could not find node with name: " + locationName);
    }

    public void printPathTime(int goalIndex) {
        int totalSeconds = getNode(goalIndex).distance / 100;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds / 60) - (hours * 60);
        int seconds = totalSeconds % 60;

        System.out.println("Route travel time: " + hours + ":" + minutes + ":" + seconds);
    }

    public Node[] tenClosest(int type, String location) {
        int locationIndex = findIndexOf(location);
        dijkstras(locationIndex);
        Arrays.sort(nodes, Comparator.comparingInt(n -> n.distance));
        Node[] tenClosest = new Node[10];

        int num = 0;

        for (Node node : nodes) {
            if (node.code == type) {
                tenClosest[num] = node;
                num++;
                System.out.println(num + ". " + node.name);
            }
            if (num == 10)
                break;
        }
        return tenClosest;
    }

    private void resetNodes() {
        for (Node node : nodes) {
            node.heuristic = 0;
            node.distance = INFINITY;
            node.predecessor = null;
            node.found = false;
        }
    }

    public String shortestPath(Node goalNode) {
        Node currentNode = goalNode;
        String path = "";
        while (currentNode != null) {
            path = " --> " + currentNode + path;
            currentNode = currentNode.predecessor;
        }
        return path.substring(5);
    }

    public Node getNode(int index) {
        return nodes[index];
    }

    public Node[] getNodePathArr(Node goalNode){
        ArrayList<Node> nodePath = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null){
            nodePath.add(currentNode);
            currentNode = currentNode.predecessor;
        }
        Collections.reverse(nodePath);
        return nodePath.toArray(new Node[0]);
    }

    public void generateCSV(String filename, Node[] points) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "//" + filename));
        StringBuilder sb = new StringBuilder();
        sb.append("lat").append(",").append("lng").append("\n");
        for(Node node : points){
            sb.append(node.latitude).append(",").append(node.longitude).append("\n");
        }
        bw.write(sb.toString());
        bw.close();
    }

    // Inner classes

    static class Node {
        private final int nodeID;
        private boolean found;
        private int heuristic;
        private int distance;
        private Node predecessor;
        private final double latitude;
        private final double longitude;
        private final ArrayList<Edge> edges;
        private int code;
        private String name;

        public Node(int nodeID, double latitude, double longitude) {
            edges = new ArrayList<>();
            this.found = false;
            this.nodeID = nodeID;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distance = INFINITY;
            this.predecessor = null;
        }

        public int getDistance() {
            return distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return nodeID == node.nodeID;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeID);
        }

        @Override
        public String toString() {
            return String.valueOf(nodeID);
        }
    }

    static class Edge {
        private Node nextNode;
        private int driveLength;

        public Edge(Node nextNode, int driveLength) {
            this.nextNode = nextNode;
            this.driveLength = driveLength;
        }
    }
}
