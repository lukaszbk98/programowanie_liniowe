import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.jgrapht.alg.util.Pair;

public class Main {
  public static void main(String[] args) throws IOException {

    int numberOfDelivers = 2;
    int numberOfReceivers = 3;
    int nConnections = 10;
    Map<Integer, Integer> delivers = new HashMap<>();
    Map<Integer, Integer> receivers = new HashMap<>();
    Map<Integer, Integer> interPoints = new HashMap<>();

    delivers.put(1, 250);
    delivers.put(2, 300);
    receivers.put(3, 120);
    receivers.put(4, 250);
    receivers.put(5, 100);
    interPoints.put(6, 0);

    List<InterPoint> connections = new ArrayList<>();
    connections.add(new InterPoint(2, 1, 2));
    connections.add(new InterPoint(1, 3, 3));
    connections.add(new InterPoint(1, 6, 5));
    connections.add(new InterPoint(2, 6, 6));
    connections.add(new InterPoint(2, 5, 2));
    connections.add(new InterPoint(6, 3, 5));
    connections.add(new InterPoint(6, 4, 4));
    connections.add(new InterPoint(6, 5, 1));
    connections.add(new InterPoint(3, 4, 8));
    connections.add(new InterPoint(4, 5, 4));

    Model model = new Model("Zagadnienie pośrednika");

    //    DECLARE ROUTES TRANSPORTS VARIABLES
    IntVar[] routesTransports =
        IntStream.range(0, nConnections)
            .mapToObj(i -> model.intVar("route: " + i, 0, 300))
            .toArray(IntVar[]::new);
    //    DECLARE GOAL FUNCTION VARIABLE
    IntVar goalFunction = model.intVar("goalFunction", 0, IntVar.MAX_INT_BOUND);

    for (Map.Entry<Integer, Integer> entry : delivers.entrySet()) {
      List<Integer> deliversIndexes = getIndexesOfDelivers(entry.getKey(), connections);
      List<Integer> receiversIndexes = getIndexesOfReceivers(entry.getKey(), connections);
      List<IntVar> deliversVariables = new ArrayList<>();
      List<IntVar> receiversVariables = new ArrayList<>();
      for (Integer deliverIndex : deliversIndexes) {
        deliversVariables.add(routesTransports[deliverIndex]);
      }
      for (Integer receiverIndex : receiversIndexes) {
        receiversVariables.add(routesTransports[receiverIndex].neg().intVar());
      }
      List<IntVar> all = new ArrayList<>();
      all.addAll(deliversVariables);
      all.addAll(receiversVariables);
      model.sum(all.toArray(IntVar[]::new), "<=", entry.getValue()).post();
    }
    for (Map.Entry<Integer, Integer> entry : receivers.entrySet()) {
      List<Integer> deliversIndexes = getIndexesOfDelivers(entry.getKey(), connections);
      List<Integer> receiversIndexes = getIndexesOfReceivers(entry.getKey(), connections);
      List<IntVar> deliversVariables = new ArrayList<>();
      List<IntVar> receiversVariables = new ArrayList<>();
      for (Integer deliverIndex : deliversIndexes) {
        deliversVariables.add(routesTransports[deliverIndex]);
      }
      for (Integer receiverIndex : receiversIndexes) {
        receiversVariables.add(routesTransports[receiverIndex].neg().intVar());
      }
      List<IntVar> all = new ArrayList<>();
      all.addAll(deliversVariables);
      all.addAll(receiversVariables);
      model.sum(all.toArray(IntVar[]::new), ">=", entry.getValue()).post();
    }
    for (Map.Entry<Integer, Integer> entry : interPoints.entrySet()) {
      List<Integer> deliversIndexes = getIndexesOfDelivers(entry.getKey(), connections);
      List<Integer> receiversIndexes = getIndexesOfReceivers(entry.getKey(), connections);
      List<IntVar> deliversVariables = new ArrayList<>();
      List<IntVar> receiversVariables = new ArrayList<>();
      for (Integer deliverIndex : deliversIndexes) {
        deliversVariables.add(routesTransports[deliverIndex]);
      }
      for (Integer receiverIndex : receiversIndexes) {
        receiversVariables.add(routesTransports[receiverIndex].neg().intVar());
      }
      List<IntVar> all = new ArrayList<>();
      all.addAll(deliversVariables);
      all.addAll(receiversVariables);
      model.sum(all.toArray(IntVar[]::new), "=", entry.getValue()).post();
    }

    model
        .scalar(
            routesTransports,
            connections.stream().mapToInt(InterPoint::getCost).toArray(),
            "=",
            goalFunction)
        .post();
    //    SOLVE MODEL
    Solver solver = model.getSolver();
    Solution solution = solver.findOptimalSolution(goalFunction, false);
    System.out.println(solution.toString());
    //    WRITE SOLUTION TO FILE
    //    writeSolutionToFile(
    //        solution, numberOfDeliverers, numberOfReceivers, routesTransports, goalFunction);
  }


  private static void writeSolutionToFile(
      Solution solution,
      int numberOfDeliverers,
      int numberOfReceivers,
      IntVar[] routesTransports,
      IntVar goalFunction)
      throws IOException {
    List<String> lines = new ArrayList<>();
    int iterator = 0;
    for (int i = 0; i < numberOfDeliverers; i++) {
      for (int j = 0; j < numberOfReceivers; j++) {
        lines.add(
            String.format(
                "Dostawca[%d] - Odbiorca[%d]: %d",
                i, j, solution.getIntVal(routesTransports[iterator])));
        iterator++;
      }
    }
    lines.add(String.format("Zysk = %d", solution.getIntVal(goalFunction)));
    Path out = Paths.get("solution.txt");
    Files.write(out, lines, Charset.defaultCharset());
  }

  private static List<Integer> getIndexesOfDelivers(int index, List<InterPoint> connections) {
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < connections.size(); i++) {
      if (connections.get(i).getDeliver() == index) {
        indexes.add(i);
      }
    }
    return indexes;
  }

  private static List<Integer> getIndexesOfReceivers(int index, List<InterPoint> connections) {
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < connections.size(); i++) {
      if (connections.get(i).getReceiver() == index) {
        indexes.add(i);
      }
    }
    return indexes;
  }
}
