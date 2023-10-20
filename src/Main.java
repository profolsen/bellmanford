import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//absolutely no guarantee this code is correct.
//But, feel free to use it or whatever AT YOUR OWN RISK.
public class Main {

    public static void main(String[] args) {
        ArrayList<Vertex> graph = createGraph();
        for(Vertex v : graph) v.initializePathInformation();
        boolean updated = true;
        int i = 0;
        while(updated) {  //In practice, this algorithm never needs to terminate...
                    //a vertex would only send its distance vector to its neighbors
                    //when its own distance vector changes.  For simplicity,
                    //this demonstration forces convergence by disallowing a vertex
                    //to send multiple update messages until all other vertices have
                    //updated their neighbors (i.e., it uses iterations in which each
                    //vertex sends its current distance vector to each of its
                    //neighbors.
            System.out.println("After " + i + " iterations: ");
            for(Vertex v : graph) {  //print out current distance vectors and routing information.
                System.out.println("\t" + v + ": " + v.distances);
                System.out.println("\t" + v + ": " + v.next);
                System.out.println();
            }
            updated = false;
            Collections.shuffle(graph);  //demonstrating the decentralized nature of the algorithm:
                    //vertices do not need to coordinate WHEN they do their updates...
            for(Vertex v : graph) {
                for(Vertex u : v.neighbors.keySet()) {  //update ALL of v's neighbors.
                    updated = u.update(v) || updated;  //u is receiving a message from v and updating
                            // its distance vector accordingly.
                }
            }
            i++;
        }
        System.out.println("Testing routing...");
        Vertex z = get(graph, "z");
        z.route("test message", get(graph, "t"), 0.0);
    }

    //This method creates and returns the graph from the quiz 5+6
    //I.e., a graph with 7 vertices and 12 edges and some weights.
    public static ArrayList<Vertex> createGraph() {
        ArrayList<Vertex> answer = new ArrayList<Vertex>();
        Vertex z = new Vertex("z");
        Vertex x = new Vertex("x");
        Vertex y = new Vertex("y");
        Vertex v = new Vertex("v");
        Vertex u = new Vertex("u");
        Vertex t = new Vertex("t");
        Vertex w = new Vertex("w");
        Vertex.connect(z, y, 12);
        Vertex.connect(z, x, 8);
        Vertex.connect(y, x, 6);
        Vertex.connect(y, v, 8);
        Vertex.connect(y, t, 7);
        Vertex.connect(x, w, 6);
        Vertex.connect(x, v, 3);
        Vertex.connect(v, t, 4);
        Vertex.connect(v, u, 3);
        Vertex.connect(v, w, 4);
        Vertex.connect(w, u, 3);
        Vertex.connect(u, t, 2);
        answer.add(t);
        answer.add(u);
        answer.add(v);
        answer.add(w);
        answer.add(x);
        answer.add(y);
        answer.add(z);
        return answer;
    }

    //gets a vertex by ID from the graph.
    public static Vertex get(ArrayList<Vertex> graph, String vertexID) {
        for(Vertex v : graph) if(v.id.equals(vertexID)) return v;
        return null;
    }
}

class Vertex {
    String id;  //the vertex id.
    HashMap<Vertex, Double> neighbors = new HashMap<Vertex, Double>();  //the neighbors of this vertex (undirected edges)
    HashMap<Vertex, Double> distances = new HashMap<Vertex, Double>(); //distance vector for this vertex.
    HashMap<Vertex, Vertex> next = new HashMap<Vertex, Vertex>(); //routing information (where to send a message after this vertex when following the shortest path)

    //creates a vertex.
    public Vertex(String id) {
        this.id = id;
    }

    //adds a bidirectional (undirected) edge (link) between two vertices of the given weight.
    public static void connect(Vertex x, Vertex y, double weight) {
        x.neighbors.put(y, weight);
        y.neighbors.put(x, weight);
    }

    //initializes distance vector and routing information to start a version of Bellman-Ford.
    public void initializePathInformation() {
        distances = new HashMap<Vertex, Double>();
        distances.put(this, 0.0);
        next = new HashMap<Vertex, Vertex>();
        next.put(this, this);
    }

    //returns the distance to v according to the current distance vector or infinity if, according to the current distance vector, v is unreachable.
    public double distanceTo(Vertex v) {
        if(distances.containsKey(v)) return distances.get(v);
        return Double.POSITIVE_INFINITY;
    }

    //updates the current distance vector based on the Bellman-Ford equation and other's distance vector.
    public boolean update(Vertex other) {
        boolean answer = false;
        double linkCost = neighbors.get(other);
        for(Vertex r : other.distances.keySet()) {
            if(linkCost + other.distanceTo(r) < distanceTo(r)) {  //Bellman-Ford equation, reimagined as...
                distances.put(r, linkCost + other.distanceTo(r)); //an if statement (and for a single neighbor...
                next.put(r, other);                               //at a time.
                answer = true;
            }
        }
        return answer;
    }

    //To show how the "next" map is used.
    //Equivalently, how routing is accomplished from a router's point of view.
    public void route(String message, Vertex dest, double time) {
        if(! distances.containsKey(dest)) {
            System.out.println("Error: No path to " + dest + " from " + this + " exists.");
            return;
        }
        if(this.equals(dest)) {
            System.out.println("(" + id + ") received \"" + message + "\" at time: " + time);
            return;
        }
        System.out.println("(" + id + ") received \"" + message + "\" at time: " + time);
        Vertex n = this.next.get(dest);
        n.route(message, dest, time + neighbors.get(n));
    }

    //the following are Java methods to get hashmaps to work...
    @Override public boolean equals(Object other) {
        if(other instanceof Vertex) {
            return ((Vertex) other).id.equals(id);
        }
        return false;
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    //and for convenience in reporting results.
    @Override public String toString() {
        return id;
    }
}