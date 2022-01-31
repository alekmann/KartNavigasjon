import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Preprocessing {
    Graph graph;
    int[][] distances;
    int[] landmarks;

    public Preprocessing(boolean transposed) {
        this.graph = new Graph(filepath("noder.txt"), filepath("kanter.txt"), transposed);
        // Flateland, Drøbak, Örebro, Uppsala, Åbo
        this.landmarks = new int[]{5409887, 1791208, 481154, 353090, 5608434};
        this.distances = new int[landmarks.length][graph.nodes.length];
    }
    private static String filepath(String filename) {
        return System.getProperty("user.dir") + "//" + filename;
    }

    private void generateArray() {
        for(int i = 0; i < landmarks.length; i++){
            System.out.println("Processing landmark: " + landmarks[i]);
            graph.dijkstras(i);
            for(int j = 0; j < graph.nodes.length; j++){
                distances[i][j] = graph.getNode(j).getDistance();
            }
        }
    }

    public void writeArrayToFile(String filename) throws IOException {
        generateArray();
        StringBuilder sb = new StringBuilder();
        BufferedWriter br;
        sb.append(landmarks.length).append("   ").append(graph.nodes.length).append("\n");

        for(int i = 0; i < landmarks.length; i++){
            for(int j = 0; j < graph.nodes.length; j++){
                sb.append(distances[i][j]).append("    ");
            }
            sb.append("\n");
        }
        br = new BufferedWriter(new FileWriter(filepath(filename)));
        br.write(sb.toString());
        br.close();
    }
}
