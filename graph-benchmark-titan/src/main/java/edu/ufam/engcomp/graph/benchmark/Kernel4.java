package edu.ufam.engcomp.graph.benchmark;

import au.com.bytecode.opencsv.CSVReader;
import com.tinkerpop.blueprints.Direction;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import titan.Titan;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import edu.upc.dama.dex.utils.CmdLineArgs;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

public class Kernel4 implements Closeable {

    private static final String OPT_HELP = "Help";
    private static final String OPT_IMAGE = "Neo4J database";
    private static final String OPT_SAMPLE_FILE = "Sample File";
    private static final String OPT_SAMPLE_SIZE = "Sample Size";
    private static final String ATTR_WEIGHT = "WEIGHT";
    private static final String ATTR_ID_NODE = "ID_NODE";
    private IBechmarkGraph graphDb;
    private File sample;
    private long time;
    private int sampleSize;
    private final Logger log;

    void setSampleFile(File sample) {
        this.sample = sample;
    }

    private class VISITED {

        Vertex node;// nó visitado
        int next;// indice do proximo nó da lista de visitados
    }

    private class VERTEX {

        double BC = 0;
        double delta = 0;
        int sigma = 0;
        int d = -1;

        int first = -1;// primeiro
    };

    public Kernel4(IBechmarkGraph gdb, Logger log) {
        graphDb = gdb;
        time = 0;
        sampleSize = 0;
        this.log = log;
    }

    public static void main(String[] args) throws Exception {
        CmdLineArgs opt = new CmdLineArgs();
        opt.registerOption(OPT_HELP, 'h', false, ": print usage and exit",
                false);
        opt.registerString(OPT_IMAGE, 'i', true, ": Neo4J database folder",
                "neo-db/");
        opt.registerString(OPT_SAMPLE_FILE, 'k', true, ": sample file.",
                "./sample.txt");
        opt.registerInt(OPT_SAMPLE_SIZE, 's', true, ": sample size.",
                0);
        try {
            opt.parse(args);
        } catch (Exception e) {
            System.out.println("ERROR in arguments: " + e.getMessage());
            System.out.println("USAGE: Kernel4" + opt.usage());
            return;
        }

        if (opt.getOptionBool(OPT_HELP)) {
            System.out.println("USAGE: Kernel4" + opt.usage());
            return;
        }

        int sampleSize = opt.getOptionInt(OPT_SAMPLE_SIZE);
        if (sampleSize < 1) {
            System.out.println("La mida del sample ha de ser més gran que 0.");
        }

        File sample = new File(opt.getOptionString(OPT_SAMPLE_FILE));
        String fileLog = "kernel4_log_%u_%t.log";

        Handler handler = new FileHandler(fileLog);
        Logger log = Logger.getAnonymousLogger();
        log.addHandler(handler);

        Kernel4 k4 = new Kernel4(new Titan(log), log);
        k4.setSampleFile(sample);
        k4.setSampleSize(sampleSize);
        log.log(Level.INFO, "Starting Kernel4");
        k4.execute();
        log.log(Level.INFO, "Kernel4 executed in {0}ms", k4.getTime());
        k4.close();
    }

    void setSampleSize(int optionInt) {
        this.sampleSize = optionInt;
    }

    long getTime() {
        return time;
    }

    void execute() {
        time = System.currentTimeMillis();

        try {
            betweennessCentrality();
            graphDb.commit();

        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } finally {
            graphDb.rollback();
        }
        time = System.currentTimeMillis() - time;
    }

    private void betweennessCentrality() {
        Iterator<Vertex> nodes = graphDb.getAllVertices();
        ArrayDeque<Vertex> S = new ArrayDeque<Vertex>();
        ArrayDeque<Vertex> Q = new ArrayDeque<Vertex>();

        ArrayList<VISITED> m_visited = new ArrayList<VISITED>();
        HashMap<Vertex, VERTEX> vertices = new HashMap<Vertex, VERTEX>();
//
//	// ignore ID=0
        for (Iterator<Vertex> it = nodes; it.hasNext();) {
            Vertex node = it.next();
            VERTEX v = new VERTEX();
            v.BC = 0;
            v.first = -1;
            v.d = -1;
            vertices.put(node, v);
        }

        //Scanner samplesScanner;
        CSVReader reader;
        try {
            FileReader rd = new FileReader(sample);
            reader = new CSVReader(rd);
            //	samplesScanner = new Scanner(sample);
        } catch (FileNotFoundException e1) {
            //System.out.println("Sample file doesn't exists!");
            log.log(Level.SEVERE, "Sample file doesn't exists!");
            return;
        }

        List<Vertex> sampleNodes = new ArrayList<Vertex>();
        int i = 0;
        // criando lista de nós de amostra
        while (i < sampleSize) {
            String[] row = null;
            //countEdges = 0;
            long idNode = 0;
            try {
                row = reader.readNext();
                if (row != null) {
                    log.log(Level.INFO, "VALORES STRING: nodeid {0}. ", row[0]);
                    idNode = Long.parseLong(row[0], 10);
                    Vertex idGraphNode = graphDb.getSingleNode(ATTR_ID_NODE, idNode);
                    sampleNodes.add(idGraphNode);
                    log.log(Level.INFO, "VALORES PARSEADO: nodeid {0}. ", idNode);

                } else {
                    log.log(Level.WARNING, "VALORES STRING: nodeid null. ");
                }
            } catch (IOException nfe) {
                log.log(Level.SEVERE, "Exception com os valores: linha: {0},  Mensagem: {2}.", new Object[]{195, nfe.getMessage()});
            } catch (NumberFormatException nfe) {
                log.log(Level.SEVERE, "Exception com os valores: linha: {0},  Mensagem: {2}.", new Object[]{195, nfe.getMessage()});
            } catch (Exception nfe) {
                log.log(Level.SEVERE, "ERRO!!!!: idNode: {0} nao encontrado. mensagem: {1}", new Object[]{idNode, nfe.getMessage()});
            }
            i++;
        }
        //samplesScanner.close();
        try {
            reader.close();
        } catch (IOException ex) {
            //Logger.getLogger(Kernel4.class.getName()).log(Level.SEVERE, null, ex);
            log.log(Level.SEVERE, null, ex);
        }

        int count = 0;
	//System.out.println("Sample size= "+temp.size());
        //TODO: should be random. escollir els vertex llavor?
        long step = System.currentTimeMillis();
        Iterator<Vertex> itn = sampleNodes.iterator();
        while (itn.hasNext()) {
            Vertex s = itn.next();
            count++;
            log.log(Level.INFO, "AMOSTRA NRO:  {0}, vertex id_node: {1} ", new Object[]{count, s.getProperty(ATTR_ID_NODE)});
            
//	    // Per cada vertex llavor
            m_visited.clear();
//            for (Iterator<Vertex> it = nodes; it.hasNext();) {
//                Vertex node = it.next();
//                VERTEX vertex = vertices.get(node);
//                vertex.sigma = 0;
//                vertex.d = -1;
//                vertex.delta = 0;
//                vertex.first = -1;
//                log.log(Level.INFO, "AMOSTRA NRO:  {0}, no resetado: {1} ", new Object[]{count, node.getProperty(ATTR_ID_NODE)});
//                
//            }
            
            for (Map.Entry<Vertex, VERTEX> entrySet : vertices.entrySet()) {
                Vertex node = entrySet.getKey();
                VERTEX vertex = entrySet.getValue();
                vertex.sigma = 0;
                vertex.d = -1;
                vertex.delta = 0;
                vertex.first = -1;
                //log.log(Level.INFO, "AMOSTRA NRO:  {0}, no resetado: {1} ", new Object[]{count, node.getProperty(ATTR_ID_NODE)});
            }

            vertices.get(s).sigma = 1;
            vertices.get(s).d = 0;
            Q.add(s);

            while (!Q.isEmpty()) {
                Vertex v = Q.poll();
                S.push(v);
                Iterable<Edge> nb = v.getEdges(Direction.OUT);
//		//System.out.println("Edges out v:"+dbg.getAttribute(v,nodeIDAttr)+" degree: "+nb.size());
                Iterator<Edge> itnb = nb.iterator();
                while (itnb.hasNext()) {
                    Edge e = itnb.next();
                    long weight = (Long) e.getProperty(ATTR_WEIGHT);
                    Vertex head = e.getVertex(Direction.IN);
                    if (((weight + 1) & 7) != 0) {
                        Vertex w = head;
                        if (w.getId() != v.getId()) {
                            VERTEX verticeW = vertices.get(w);
                            VERTEX verticeV = vertices.get(v);
                            if (verticeW.d < 0) {
                                Q.add(w);
                                verticeW.d = verticeV.d + 1;
                            }
                            if (verticeW.d == verticeV.d + 1) {
                                verticeW.sigma += verticeV.sigma;
                                VISITED vs = new VISITED();
                                vs.node = v;
                                vs.next = verticeW.first;
                                m_visited.add(vs);
                                verticeW.first = m_visited.size() - 1;//m_visited.indexOf(vs);
                                //log.log(Level.INFO, "VIZITADO: {0} ", new Object[]{v.getProperty(ATTR_ID_NODE)});
                            }
                        }
                    }
                }
            }

            //log.log(Level.INFO, "AMOSTRA NRO:  {0}, vizitados: {1} ", new Object[]{count, m_visited.size()});

            while (!S.isEmpty()) {
                Vertex w = S.pop();
                VERTEX pw = vertices.get(w);

                int id = pw.first;
                while (id != -1 && 0 < m_visited.size()) {
                    VISITED vi = m_visited.get(id);

                    if (vi != null) {
                        //Long v = (Long) vi.node.getId();
                        VERTEX vtx = vertices.get(vi.node);

                        vtx.delta += (double) vtx.sigma * (1 + pw.delta)
                                / (double) pw.sigma;
                        id = m_visited.get(id).next;
                    }
                }
                if (w.getId() != s.getId()) {
                    pw.BC += pw.delta;
                }
            }

//            Logger.getAnonymousLogger().log(Level.INFO, count+" llavors tractades en "+(System.currentTimeMillis()-time)+" step: "+(System.currentTimeMillis()-step));
//	    	step = System.currentTimeMillis();
            log.log(Level.INFO, "AMOSTRA NRO:  {0}, vizitados: {1} ", new Object[]{count, m_visited.size()});
        }
    }

//    private void betweennessCentrality() {
//	Iterator<Vertex> allDbVertices = graphDb.getAllVertices();
//
//	ArrayDeque<Long> bfsS = new ArrayDeque<Long>();
//	ArrayDeque<Long> bfsQ = new ArrayDeque<Long>();
//
//	ArrayList<VISITED> m_visited = new ArrayList<VISITED>();
//	HashMap<Long, VERTEX> allNodeIdToVERTEXMap = new HashMap<Long, VERTEX>();
////
////	// ignore ID=0
//        //System.out.println("class node id: "+ allDbVertices.hasNext());
//	for (Iterator<Vertex> it = allDbVertices;it.hasNext();) {
//	    Vertex node = it.next();
//	    VERTEX v = new VERTEX();
//	    v.BC = 0;
//            v.first = -1;
//            v.d = -1;
//            //System.out.println("class node id: "+ node.getId().getClass());
//            //log.log(Level.INFO, "class node id: {0}", node.getId().getClass());
//	    allNodeIdToVERTEXMap.put((Long)node.getId(), v);
//            // removendo da lista
//            //it.remove();
//	}
//        // limpando a lista
//        //allDbVertices.remove();
//        
//	//Scanner samplesScanner;
//        CSVReader reader;
//	try {
//                FileReader rd = new FileReader(sample);
//                reader = new CSVReader(rd);
//	//	samplesScanner = new Scanner(sample);
//	} catch (FileNotFoundException e1) {
//		//System.out.println("Sample file doesn't exists!");
//                log.log(Level.SEVERE, "Sample file doesn't exists!");
//		return;
//	}
//
//        // criando lista de nós de amostra
//	List<Long> sampleNodes = new ArrayList<Long>();
//	int i = 1;
//	while(i<= sampleSize){
//            
//            String[] row;
//            //countEdges = 0;
//            long idNode = 0;
//            try {
//                
//                row = reader.readNext();
//                //log.log(Level.INFO, "VALORES STRING: nodeid {0}. ", row[0]);
//                idNode = Long.parseLong(row[0], 10);
//                //log.log(Level.INFO, "VALORES PARSEADO: nodeid {0}. ", idNode);
//            } catch (IOException nfe) {
//                log.log(Level.SEVERE, "Exception com os valores: linha: {0},  Mensagem: {2}.", new Object[]{195, nfe.getMessage()});
//            } catch (NumberFormatException nfe) {
//                log.log(Level.SEVERE, "Exception com os valores: linha: {0},  Mensagem: {2}.", new Object[]{195, nfe.getMessage()});
//            }catch (Exception nfe) {
//                log.log(Level.SEVERE, "Exception com os valores: linha: {0},  Mensagem: {2}.", new Object[]{195, nfe.getMessage()});
//            }
//            
//            
//            
//            //int idNode = samplesScanner.nextBigDecimal().intValue();
//	    try {
//                Long idGraphNode = (Long)graphDb.getSingleNode(ATTR_ID_NODE, idNode).getId();
//                sampleNodes.add(idGraphNode);
//                //log.log(Level.INFO, " INSERINDO id do nó na lista de amostras -- ID_NODE: {0} -> id do nó no grafo: {1}.", new Object[]{idNode, idGraphNode});
//            } catch (Exception e) {
//                //System.out.println("ERRO!!!!: idNode: "+idNode+" nao encontrado. mensagem: "+ e.getMessage());
//                log.log(Level.SEVERE, "ERRO!!!!: idNode: "+idNode+" nao encontrado. mensagem: "+ e.getMessage());
//            }
//            
//	    i++;
//	}
//        //samplesScanner.close();
//        try {
//            reader.close();
//        } catch (IOException ex) {
//            //Logger.getLogger(Kernel4.class.getName()).log(Level.SEVERE, null, ex);
//            log.log(Level.SEVERE, null, ex);
//        }
//        
//        
//        
//        //log.log(Level.INFO, " sampleNodes,count: {0}", sampleNodes.size());
//	int count = 1;
//	//System.out.println("Sample size= "+temp.size());
//	//TODO: should be random. escollir els vertex llavor?
//	long step = System.currentTimeMillis();
//        
//        // o algoritmo faz loop para cada nó exemplo
//	Iterator<Long> iteratorSampleNodes = sampleNodes.iterator();
//	while (iteratorSampleNodes.hasNext()) {
//	    Long sampleNodeId = iteratorSampleNodes.next();
////            log.log(Level.INFO, "AMOSTRA NRO:  {0} ", count);
//	    count++;
//	    
////	    // Per cada vertex do grafo
//            // inicializar todos os vertices do grapho
//	    m_visited.clear();
//            for (Map.Entry<Long, VERTEX> entrySet : allNodeIdToVERTEXMap.entrySet()) {
//                VERTEX vertex = entrySet.getValue();
//                vertex.sigma = 0;
//		vertex.d = -1;
//		vertex.delta = 0;
//		vertex.first = -1;
//            }
//
//            // TODO: e se o sample não existir no mapa?
//	    allNodeIdToVERTEXMap.get(sampleNodeId).sigma = 1;
//	    allNodeIdToVERTEXMap.get(sampleNodeId).d = 0;
//            
//            // adiciona no fim da fila o nó para processamento
//	    bfsQ.add(sampleNodeId);
//
//            // bread first search para cada vertice do bfsQ
//            // primeiro computar n arvores de caminhos mais curtos (shortest path)
//            // uma para cada s em V.
//            // Durante estas computações, também mantém manteém o conjunto de predecessores
//            // pred(s,v). 
//	    while (!bfsQ.isEmpty()) {
//                // pega o nó source ou tail
//		Long idVerticeSourceQ = bfsQ.poll();
//                //log.log(Level.INFO, "Id do nó retirado do bfsQ:  {0} ", idVerticeSourceQ);
//                // empurra o vertice source para a pilha bfsS
//		bfsS.push(idVerticeSourceQ);
//                //log.log(Level.INFO, " vertice id add em bfsS: {0} ", new Object[]{verticeSourceQ.getProperty(ATTR_ID_NODE)});
//                // pega arestas de saida do vertice
//		//Iterable<Edge> edgesSaidaVertice = verticeSourceQ.getEdges(Direction.OUT);
//		
//		Iterator<Edge> iteratorEdgesSaidaVertice = null;
//                Vertex nodeSource = null;
//                //iteratorEdgesSaidaVertice = graphDb.getSingleNode(ATTR_ID_NODE, idVerticeSourceQ).getEdges(Direction.OUT).iterator();// verticeSourceQ.getEdges(Direction.OUT).iterator();
//                try {
//                    nodeSource = graphDb.getNodeById(idVerticeSourceQ);
//                    iteratorEdgesSaidaVertice = nodeSource.getEdges(Direction.OUT).iterator();// verticeSourceQ.getEdges(Direction.OUT).iterator();
//                } catch (Exception e) {
//                    log.log(Level.SEVERE, "ERRO!!!!!!:  {0} causa: {1} linha: {2}. id do nó: {3}. id do nó encontrado: {4}. mensagem: {5}", new Object[]{e.getMessage(), e.getCause(), 269, idVerticeSourceQ.toString(), nodeSource == null?0: nodeSource.getId(), e});
//                    continue;
//                }
//                
//                // para cada aresta de verticeQ
//		while (iteratorEdgesSaidaVertice.hasNext()) {
//                    // pega aresta de saida
//		    Edge edgSaidaVtx = iteratorEdgesSaidaVertice.next();
//                    // pega peso da aresta de saida
//		    long weight = (Long) edgSaidaVtx.getProperty(ATTR_WEIGHT);
//                    // pegar vertice na saída da aresta
//		    Long vertexDest = (Long)edgSaidaVtx.getVertex(Direction.IN).getId();
//                    log.log(Level.INFO, "edge: origem: {0}, peso: {1}, destino: {2}", new Object[]{nodeSource.getProperty(ATTR_ID_NODE), weight, vertexDest});
//
//                    // somente arestas com peso não multiplo de oito devem ser consideradas
//                    // peso não é multiplo de 8?
//		    if (((weight + 1) & 7) != 0) {
//			Long idVertexDest = vertexDest;
//                        // não é o mesmo nó (evita loop)
//			if (!idVertexDest.equals(idVerticeSourceQ)) {
//                            VERTEX vertexDestW = allNodeIdToVERTEXMap.get(idVertexDest);
//			    VERTEX vertexSourceV = allNodeIdToVERTEXMap.get(idVerticeSourceQ);
//			    // distancia nao calculada?
//			    if (vertexDestW.d < 0) {
//				bfsQ.add(idVertexDest);
//				vertexDestW.d = vertexSourceV.d + 1;
//			    }
//                            // adicionando predecessor
//			    if (vertexDestW.d == vertexSourceV.d + 1) {
//				vertexDestW.sigma += vertexSourceV.sigma;
//				VISITED vs = new VISITED();
//				vs.nodeId = idVerticeSourceQ;
//				vs.next = vertexDestW.first;
//				m_visited.add(vs);
//                                log.log(Level.INFO, " vertice id add em m_visited: {0} ", new Object[]{nodeSource.getProperty(ATTR_ID_NODE)});
//				vertexDestW.first = m_visited.indexOf(vs);
//                                //vertexDestW.first = m_predecessor.size()-1;
//			    }
//			}
//		    }
//		}
//	    }
//
//            //log.log(Level.INFO, " bfsS.size: {0} m_visited.size: {1}", new Object[]{bfsS.size(), m_visited.size()});
//            //log.log(Level.INFO, " bfsQ.size: {0}", bfsQ.size());
//            //log.log(Level.INFO, " m_visited.size: {0}", m_visited.size());
//            // As dependencias pdoem ser computadas pelo caminhamento
//            // do vertice em ordem nao incremental de sua distancia de s 
//            //( indo da folha até a origem s) 
//	    while (!bfsS.isEmpty()) {
//		Long wId = bfsS.pop();
//		VERTEX pw = allNodeIdToVERTEXMap.get(wId);
//
//		int id = pw.first;
//                //log.log(Level.INFO, "\nfirst id: {0} node id: {2} ", new Object[]{id, w.getProperty(ATTR_ID_NODE)});
//
//                while (id != -1 && id < m_visited.size()) {
//                    //log.log(Level.INFO, "next: {0} m_predecessor.get(id).idnode: {1} id {2} \n", new Object[]{m_visited.get(id).next, m_visited.get(id).node.getProperty(ATTR_ID_NODE), id});
//		    VISITED vi = m_visited.get(id);
//                    
//                    if (vi != null) {
//                        Long v = (Long) vi.nodeId;
//                        VERTEX vtx = allNodeIdToVERTEXMap.get(v);
//
//                        vtx.delta += (double) vtx.sigma * (1 + pw.delta)
//			    / (double) pw.sigma;
//                        id = m_visited.get(id).next;
//                    }
//		}
//		if (!wId.equals(sampleNodeId)) {
//                // o valor da centralidade de v, é encontrado somando todas as dependencias (n)
//                // computados durante as n diferentes computacoes sssp
//		    pw.BC += pw.delta;
//		}
//                //log.log(Level.INFO, " bfsS.size: {0}", bfsS.size());
//	    }
//            //log.log(Level.INFO, count+" llavors tractades en "+(System.currentTimeMillis()-time)+" step: "+(System.currentTimeMillis()-step));
//            step = System.currentTimeMillis();
//            log.log(Level.INFO, "AMOSTRA NRO:  {0}, vizitados: {1} ", new Object[]{ count, m_visited.size()});
//	}
//    }
    @Override
    public void close() throws IOException {

    }
}
