import java.io.IOException;

class Main {
    public static void main(String[] args) throws IOException {
        //preprocess();
        dikjstrasVSALT("Oslo", "Trondheim");
        dikjstrasVSALT("Trondheim", "Tampere");
        dikjstrasVSALT("Kårvåg", "Gjemnes");
        printClosestGasStations();
    }

    private static void printClosestGasStations() throws IOException {
        Graph graph = new Graph(filepath("noder.txt"), filepath("kanter.txt"), false);
        graph.initPOI("interessepkt.txt");
        graph.generateCSV("værnes-bensinstasjoner.csv", graph.tenClosest(2, "Trondheim lufthavn, Værnes"));
    }

    private static void dikjstrasVSALT(String from, String to) throws IOException {
        // Dijkstras
        Graph graph = new Graph(filepath("noder.txt"), filepath("kanter.txt"), false);
        graph.initPOI("interessepkt.txt");
        int startIndex = graph.findIndexOf(from), goalIndex = graph.findIndexOf(to);

        long[] dijkResults = graph.dijkstras(startIndex, goalIndex);
        String dijkPath = graph.shortestPath(graph.getNode(goalIndex));

        System.out.println("Dijkstras stats:");
        System.out.println("Nodes traversed: " + dijkResults[2]);
        System.out.println("Time used: " + dijkResults[0] / 1000000 + " ms");
        System.out.println("Number of nodes in path: " + graph.getNodePathArr(graph.getNode(goalIndex)).length);
        graph.printPathTime(goalIndex);

        System.out.println("-------------");

        // ALT
        graph.initDistances("distancesFrom.csv", "distancesTo.csv");
        long[] altResults = graph.ALT(startIndex, goalIndex);
        String altPath = graph.shortestPath(graph.getNode(goalIndex));

        System.out.println("ALT stats:");
        System.out.println("Nodes traversed: " + altResults[2]);
        System.out.println("Time used: " + altResults[0] / 1000000 + " ms");
        System.out.println("Number of nodes in path: " + graph.getNodePathArr(graph.getNode(goalIndex)).length);
        graph.printPathTime(goalIndex);

        System.out.println("\nEqual paths: " + dijkPath.equals(altPath));

        graph.generateCSV(from + "-" + to + ".csv", graph.getNodePathArr(graph.getNode(goalIndex)));
    }

    private static void preprocess() throws IOException {
        Preprocessing pre = new Preprocessing(false);
        pre.writeArrayToFile("distancesFrom.csv");

        pre = new Preprocessing(true);
        pre.writeArrayToFile("distancesTo.csv");
    }

    private static String filepath(String filename) {
        return System.getProperty("user.dir") + "//" + filename;
    }
}
