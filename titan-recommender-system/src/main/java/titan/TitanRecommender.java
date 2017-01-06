/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package titan;

import au.com.bytecode.opencsv.CSVReader;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import edu.ufam.engcomp.graph.benchmark.IBechmarkGraph;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;

/**
 *
 * @author wesllen
 */
public class TitanRecommender {
    
	private static final Logger log = Logger.getAnonymousLogger();
	private static final String ATTR_WEIGHT = "WEIGHT"; // TODO: remove
        private static final String ATTR_LABEL = "ARC"; // TODO: remove
	private static final String ATTR_ID_NODE = "ID_NODE"; // TODO: remove
        
        private static final String ATTR_HIT_EDGE_LABEL = "genera";
        private static final String ATTR_MOVIE_NAME = "MOVIE_NAME";
        private static final String ATTR_MOVIE_ID = "MOVIE_ID";
        private static final String ATTR_HIT_NAME = "HIT_NAME";
        
        private static final String ATTR_USER_ID = "USER_ID";
        private static final String ATTR_USER_GENDER = "USER_GENDER";
        private static final String ATTR_USER_AGE = "USER_AGE";
        
        private static final String ATTR_OCCUPATION_ID = "OCUPATION_ID";
        private static final String ATTR_OCCUP_EDGE_LABEL = "hasOccupation";
        
        private Map occupations = new HashMap();
	
        
	private int countNodes = 0;
        TitanGraph graph;

    public TitanRecommender(Logger fileLog) {
        // ocupations map
        occupations.put(0, "other");
        occupations.put(1, "academic/educator");
        occupations.put(2, "artist");
        occupations.put(3, "clerical/admin");
        occupations.put(4, "customer service");
        occupations.put(5, "doctor/health care");
        occupations.put(6, "executive/managerial");
        occupations.put(7, "farmer");
        occupations.put(8, "homemaker");
        occupations.put(9, "homemaker");
        occupations.put(10,"K-12 student");
        occupations.put(11, "lawyer");
        occupations.put(12, "programmer");
        occupations.put(13, "retired");
        occupations.put(14, "sales/marketing");
        occupations.put(15, "scientist");
        occupations.put(16, "self-employed");
        occupations.put(17, "technician/engineer");
        occupations.put(18, "tradesman/craftsman");
        occupations.put(19, "unemployed");
        occupations.put(20, "writer");
    }

        
    public boolean conectarRemote(String ip) {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("storage.backend", "cassandra");
        config.setProperty("storage.hostname", ip);
        config.setProperty("storage.keyspace","recomm");
        
        graph = TitanFactory.open(config);
        
        return graph !=null;
    }
    
    public boolean conectarLocal(String localConnectionString) {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("storage.backend", "embeddedcassandra");
        config.setProperty("storage.cassandra-config-dir", localConnectionString);

        graph = TitanFactory.open(config);

        return graph !=null;
    }
    
    public void InitIndex(){
        if (graph!=null) {
            Set<String> keys =  graph.getIndexedKeys(Vertex.class);
            if(!keys.contains(ATTR_ID_NODE))
                graph.createKeyIndex(ATTR_ID_NODE, Vertex.class);
            
            keys = graph.getIndexedKeys(Edge.class);
            if(!keys.contains(ATTR_WEIGHT))
                graph.createKeyIndex(ATTR_WEIGHT, Edge.class);
            
            
            keys = graph.getIndexedKeys(Vertex.class);
            if(!keys.contains(ATTR_MOVIE_ID))
                graph.createKeyIndex(ATTR_MOVIE_ID, Vertex.class);
        }
    }
    
    public void loadMovies(String moviesFile) throws FileNotFoundException, IOException{
    
        File input = new File(moviesFile);

        FileReader rd = new FileReader(input);
        CSVReader reader = new CSVReader(rd);
        
        String[] components = reader.readNext();
            
        while (components != null) {
            // create movies vertexes
            if(components.length >=2){
                String movieId = components[0];
                String movieName = components[1];
                
                // create movie vertex
                Vertex movieVertex = graph.addVertex(null);
                movieVertex.setProperty(ATTR_MOVIE_ID, movieId);
                movieVertex.setProperty(ATTR_MOVIE_NAME, movieName);
                graph.commit();
            
                // create hits vertexes and edges
                for (int hitId = 2; hitId < components.length; hitId++) {
                    
                    // add hit vertex
                    String hit = components[hitId];
                    
                    Vertex hitVertex = graph.addVertex(null);
                    hitVertex.setProperty(ATTR_HIT_NAME, hit);
                    graph.commit();
                    
                    // add edge
                    Edge e = movieVertex.addEdge(ATTR_HIT_EDGE_LABEL, hitVertex);
                    graph.commit();
                }
            }
            
            // get next line
            // TODO: check this
            components = reader.readNext();
        }
    }
    
    public void loadUsers(String usersFile) throws FileNotFoundException, IOException{
        
        File input = new File(usersFile);

        FileReader rd = new FileReader(input);
        CSVReader reader = new CSVReader(rd);
        
        String[] components = reader.readNext();
            
        while (components != null) {
            // create user vertexes
            if(components.length >=3){ // id, gender, age, occupation
                String userId = components[0];
                String userGender = components[1];
                String userAge = components[2];
                
                // create user vertex
                Vertex userVertex = graph.addVertex(null);
                userVertex.setProperty(ATTR_USER_ID, userId);
                userVertex.setProperty(ATTR_USER_GENDER, userGender);
                userVertex.setProperty(ATTR_USER_AGE, userAge);
                graph.commit();
                
                if(components.length >= 4) {
                    // create occupation vertex
                    String userOccupation = components[3];                    
                    
                    Vertex occupVertex = graph.addVertex(null);
                    occupVertex.setProperty(ATTR_OCCUPATION_ID, userOccupation);
                    graph.commit();
                    
                    // add edge to ocupation vertex
                    Edge e = userVertex.addEdge(ATTR_OCCUP_EDGE_LABEL, occupVertex);
                    graph.commit();
                }
            }
            
            // get next line
            components = reader.readNext();
        }     
    }
    
    public void loadRatings(String ratingsFile) throws FileNotFoundException, IOException{
//          
//        File input = new File(ratingsFile);
//
//        FileReader rd = new FileReader(input);
//        CSVReader reader = new CSVReader(rd);
//        
//        String[] components = reader.readNext();
//            
//        while (components != null) {
//            // get user vertex
//            if(components.length >=3){ // userId, movieId, stars
//                
//                // find user vertex
//                // TODO: aqui
//                Vertex userVertex = null;
//                Iterable<Vertex> it = graph.getVertices(ATTR_USER_ID, components[0]);
//
//                if (it.iterator().hasNext()) {
//                    userVertex = it.iterator().next();
//                    
//
//                    // find movie vertex
//
//
//                    // create rating edge
//                }
//
//
//            }
//        } 
    }
    
    public boolean createEdge(Object src, Object tar, long weight) {
        // tail
        Vertex tail = null;
        Iterable<Vertex> it = graph.getVertices(ATTR_ID_NODE, src);
        
        if (it.iterator().hasNext()) {
            tail = it.iterator().next();
        }
        
        if (tail == null) {
                tail = graph.addVertex(null);
                tail.setProperty(ATTR_ID_NODE, src);
                graph.commit();
                countNodes++;
        }
        
        
        
        // head
        Vertex head = null;
        it =  graph.getVertices(ATTR_ID_NODE, tar);
        if (it.iterator().hasNext()) {
            head = it.iterator().next();
        }
                
        
        if (head == null) {
                head = graph.addVertex(null);
                head.setProperty(ATTR_ID_NODE, tar);
                graph.commit();
                countNodes++;
        }

        
        // edge
        Edge e = head.addEdge(ATTR_LABEL, tail);
        //e.setProperty(ATTR_WEIGHT, weight);
        e.setProperty(ATTR_WEIGHT, weight);
        graph.commit();
        return true;
    }
    
    public void commit(){
        graph.commit();
    }

    public void desconectar() {
        
        this.graph.shutdown();

    }

    public int getNodesCreated() {
        return countNodes;
    }
    
    public Iterator<Vertex> getAllVertices(){
        return graph.getVertices().iterator();
    }
    
//    public Iterator<Vertex> getAllVerticesId(){
//        return graph.getVertices().iterator();
//    }

    public void rollback() {
        
        graph.rollback();
    }

    public Edge getEdgeById(Object id) {
        return graph.getEdge(id);
    }
    
    public Vertex getNodeById(Object id){
        return graph.getVertex(id);
    }
//
//    public Vertex getSingleNode(String ID_NODE, int nextInt) {
//        return graph.getVertices(ID_NODE, nextInt).iterator().next();
//    }
//    
    public Vertex getSingleNode(String ID_NODE, long nextInt) {
        return graph.getVertices(ID_NODE, nextInt).iterator().next();
    }

    public void clean() {

        for ( Iterator<Edge> edges = graph.getEdges().iterator(); edges.hasNext();) {
            graph.removeEdge(edges.next());
            graph.commit();
        }
        
        for (Iterator<Vertex> vertices = graph.getVertices().iterator(); vertices.hasNext();) {
            graph.removeVertex(vertices.next());
            graph.commit();
        }
//        
//        
//        if (graph.getType(ATTR_ID_NODE) != null) {
//            graph.dropKeyIndex(ATTR_ID_NODE, Vertex.class);    
//        }
//        
//        if (graph.getType(ATTR_WEIGHT) != null) {
//            graph.dropKeyIndex(ATTR_WEIGHT, Edge.class);
//        }
        
    }
    
    public boolean checkKeys(){
    
        
        return true;
    }
        
}
