/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package titan;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import edu.ufam.engcomp.graph.benchmark.IBechmarkGraph;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;

/**
 *
 * @author wesllen
 */
public class Titan  implements IBechmarkGraph {
    
	private static final Logger log = Logger.getAnonymousLogger();
//	private static final String ATTR_WEIGHT = "WEIGHT";
//        private static final String ATTR_LABEL = "ARC";
//	private static final String ATTR_ID_NODE = "ID_NODE";

	private int countNodes = 0;
        TitanGraph graph;

    public Titan(Logger fileLog) {
     
    }

        
    public boolean conectarRemote(String ip) {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("storage.backend", "cassandra");
        config.setProperty("storage.hostname", ip);
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
    
    public void InitKeys(){
        if (graph!=null) {
            Set<String> keys =  graph.getIndexedKeys(Vertex.class);
            if(!keys.contains(ATTR_ID_NODE))
                graph.createKeyIndex(ATTR_ID_NODE, Vertex.class);
            
            keys = graph.getIndexedKeys(Edge.class);
            if(!keys.contains(ATTR_WEIGHT))
                graph.createKeyIndex(ATTR_WEIGHT, Edge.class);
        }
    }
    
    
    public boolean createEdge(long src, long tar, long weight) {
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
