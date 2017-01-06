/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ufam.engcomp.graph.benchmark;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import java.util.Iterator;



/**
 *
 * @author ncr
 */
public interface IBechmarkGraph {
   
        static final String ATTR_WEIGHT = "WEIGHT";
        static final String ATTR_LABEL = "ARC";
        static final String ATTR_ID_NODE = "ID_NODE";
        //int countNodes = 0;
    
        public boolean conectarRemote(String ip);
        public boolean conectarLocal(String localConnectionString);
         public boolean createEdge(long src, long tar, long weight) ;
         public void commit();
         public void desconectar();
         public int getNodesCreated();
         public Iterator<Vertex> getAllVertices();
         public void rollback();
         public Edge getEdgeById(Object id);
         public Vertex getNodeById(Object id);
         public Vertex getSingleNode(String ID_NODE, long nextInt) ;
         public void clean() ;
        public boolean checkKeys();
        public void InitKeys();
        
}
