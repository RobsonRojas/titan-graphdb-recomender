package edu.ufam.engcomp.graph.benchmark;

import com.tinkerpop.blueprints.Direction;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import titan.Titan;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import edu.upc.dama.dex.utils.CmdLineArgs;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

public class Kernel3 implements Closeable {
	

    private static final String OPT_HELP = "Help";
    private static final String OPT_IMAGE = "Neo database";
    private static final String OPT_PATH_LENGTH = "Subgraph path length";
    private static final String ATTR_WEIGHT = "WEIGHT";
    private static final String ATTR_ID_NODE = "ID_NODE";
    private Logger log;


	private IBechmarkGraph graphDb;
	private Object[] largeEdges;
	private long time;
	private int subGraphPathLenght;


    public Kernel3(IBechmarkGraph gDB, Logger log) {
        graphDb = gDB;
        this.log = log;
	}

    public void execute() {
	long start = System.currentTimeMillis();

	try{
            for (Object largeEdge : largeEdges) {
                bfsQueue(largeEdge);
            }
	graphDb.commit();
	}finally{
		graphDb.rollback();
	}
	
	time = (System.currentTimeMillis()-start);
    }

    private void bfsQueue(Object id) {
    	Edge edge = graphDb.getEdgeById(id);
        Set<Object> edges;
    	Set<Long> nodes = new HashSet<Long>();
    	nodes.add((Long) edge.getVertex(Direction.IN).getId());
    	nodes.add((Long) edge.getVertex(Direction.OUT).getId());
    	
        edges = new HashSet<Object>();
    	edges.add(edge.getId());
    	//System.out.println("tamany edges ha de ser 1: "+(edges.size()==1));
    	Deque<Pair> queue = new ArrayDeque<Pair>();
    	queue.addLast(new Pair(edge.getVertex(Direction.OUT),1));
        
    	while(!queue.isEmpty()){
    	    Pair current = queue.removeFirst();
    	    Iterable<Edge> edgesExplode = current.getVertex().getEdges(Direction.OUT);
                //System.out.println("tamany previ edges: "+ edges.size()+" amb node: "+current.getNode().getProperty(ATTR_ID_NODE));
                //int count = 0;
                for (Edge aux : edgesExplode) {
                    edges.add(aux.getId());
                    Vertex node = aux.getVertex(Direction.OUT);
                    if(nodes.add((Long) node.getId()) && current.getDepth()+1 < subGraphPathLenght){
                        queue.addLast(new Pair(node, current.getDepth()+1));
                    }
                    //	count++;
                } //System.out.println("tamany explode: "+count);
                //System.out.println("tamany post edges: "+ edges.size());
    	}
//    	System.out.println("Edge: "+
//    			edge.getStartNode().getProperty(ATTR_ID_NODE)+" "+
//    		    edge.getEndNode().getProperty(ATTR_ID_NODE)+" "+
//    		    edge.getProperty(ATTR_WEIGHT)+"");
//    	System.out.println("Nodes size: "+nodes.size());
//    	System.out.println("Edges size: "+edges.size());
//    	for(Iterator<Long> it = edges.iterator();it.hasNext();){
//    	    edge = neo.getRelationshipById(it.next());
//        	System.out.println(
//        			edge.getStartNode().getProperty(ATTR_ID_NODE)+" "+
//        		    edge.getEndNode().getProperty(ATTR_ID_NODE)+" "+
//        		    edge.getProperty(ATTR_WEIGHT)+"");
//    	}
//    	System.out.println();
	}

	public void setEdges(Object[] largeEdges) {
    	this.largeEdges = largeEdges;
    }

    public void setSubGraphPathLenght(int optionInt) {
	this.subGraphPathLenght	= optionInt;
    }
    
    public long getTime() {
        return time;
    }
    
	@Override
	public void close() throws IOException {
        
	}

	public static void main(String[] args) throws Exception {
		CmdLineArgs opt = new CmdLineArgs();
		opt.registerOption(OPT_HELP, 'h', false, ": print usage and exit",
				false);
		opt.registerString(OPT_IMAGE, 'i', true, ": neo database folder",
				"neo-db/");
		opt.registerInt(OPT_PATH_LENGTH, 'l', false, ": Subgraph path length",
				3);
		try {
			opt.parse(args);
		} catch (Exception e) {
			System.out.println("ERROR in arguments: " + e.getMessage());
			System.out.println("USAGE: Kernel3" + opt.usage());
			return;
		}

		if (opt.getOptionBool(OPT_HELP)) {
			System.out.println("USAGE: Kernel3" + opt.usage());
			return;
		}

		if (opt.getOptionInt(OPT_PATH_LENGTH) < 0) {
			System.out.println("INVALID " + OPT_PATH_LENGTH
					+ ". Must be greater than 0.");
			return;
		}

                String fileLog = "kernel23_log_%u_%t.log";

                Handler handler = new FileHandler(fileLog);
                Logger log =  Logger.getAnonymousLogger();
                log.addHandler(handler);

                Titan titanGraph = new Titan(log);
		Kernel2 k2 = new Kernel2(titanGraph, log);
		k2.execute();
		Object[] largeEdges = k2.getResult();
		k2.close();
                
		Kernel3 k3 = new Kernel3(titanGraph, log);
		k3.setEdges(largeEdges);
		k3.setSubGraphPathLenght(opt.getOptionInt(OPT_PATH_LENGTH));
		log.log(Level.INFO,"Starting Kernel3");
		k3.execute();
		log.log(Level.INFO,"Kernel3 executed in "+k3.getTime()+"ms");
		k3.close();
	}
	
    private class Pair{
    	Vertex node;
    	int depth;
    	public Pair(Vertex node, int depth){
    	    this.node = node;
    	    this.depth = depth;
    	}
    	public Vertex getVertex() {
    	    return node;
    	}
    	public int getDepth() {
    	    return depth;
    	}
        }
}
