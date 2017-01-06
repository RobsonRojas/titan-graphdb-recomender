package edu.ufam.engcomp.graph.benchmark;

import au.com.bytecode.opencsv.CSVReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.FileReader;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import titan.Titan;


public class Kernel1 implements Closeable {

	private String db_path = "neo-db-scale";
	private Logger log;
       	private static final String OPT_HELP = "Help";
	private static final String OPT_INPUT = "Input file";
	private static final String OPT_SCALE = "Scale";
	private static final String ATTR_WEIGHT = "WEIGHT";
	private static final String ATTR_ID_NODE = "ID_NODE";

	private CSVReader reader;
        private IBechmarkGraph graphDb;
    private long time;
    private int countEdges;

	public Kernel1(String inputFile, IBechmarkGraph gdb, Logger log) throws Exception {
            //this.optimize = (1 << scale) / 10;
            //optimize = (optimize > 1000000) ? 1000000
            //		: (optimize < 100000) ? 100000 : optimize;

            this.log = log;
            File input = new File(inputFile);

            FileReader rd = new FileReader(input);
            reader = new CSVReader(rd);

            graphDb = gdb;
	}
        
        public boolean execute(){
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger(Kernel1.class.getName()).log(Level.SEVERE, null, ex);
                log.log(Level.SEVERE, "KERNEL1 exception"+ ex.getMessage());
                return false;
            }
        
            return true;
        }
        
        
	public void load() throws Exception {
		long start = System.currentTimeMillis();
		String[] row = reader.readNext();
                
                countEdges = 0;
                long src = 0;
                long tar = 0;
                long weight = 0;
		while (row != null) {
                    try {
                        //log.log(Level.INFO, "VALORES STRING: src {0},  tar {1}, weight {2}. ", new Object[]{row[0], row[1], row[2]});
                        src = Long.parseLong(row[0], 10);
                        tar = Long.parseLong(row[1], 10);
                        weight = Long.parseLong(row[2], 10);
                        //log.log(Level.INFO, "VALORES LONG : src {0},  tar {1}, weight {2}. ", new Object[]{src, tar, weight});
                    } catch (NumberFormatException nfe) {
                        log.log(Level.INFO, "Exception com os valores: src {0},  tar {1}, weight {2}. Mensagem: {3}.", new Object[]{row[0], row[1], row[2], nfe.getMessage()});
                    }
                    catch (Exception ex)
                    {
                        log.log(Level.INFO, "Exception com os valores: src {0},  tar {1}, weight {2}. Mensagem: {3}.", new Object[]{row[0], row[1], row[2], ex.getMessage()});
                    }

                    graphDb.createEdge(src, tar, weight);
                    //graphDb.commit();
                    row = reader.readNext();
                    countEdges++;
		}
                
                time = System.currentTimeMillis()-start;
//
//		log.log(Level.INFO, "{0} edges loaded in {1}ms", new Object[]{countEdges, time});
//		log.log(Level.INFO, "{0} nodes.", graphDb.getNodesCreated());
	}

	public void close() throws IOException {
		
	}
        

        public long getTime() {
            return time;
        }
        
        public long getNodesCreated(){
            return graphDb.getNodesCreated();
        }
        
        public long getEdgesCreated(){
            return countEdges;
        }
    
    
    	public static void main(String args[]) throws Exception {
/*		CmdLineArgs opt = new CmdLineArgs();

		opt.registerOption(OPT_HELP, 'h', false, ": print usage and exit",
				false);
		opt.registerString(OPT_INPUT, 'i', true, ": input file", null);
		opt.registerInt(OPT_SCALE, 's', true,
				": Integer between 0 and 30. Number of nodes = 2^Scale.", 0);

		try {
			opt.parse(args);
		} catch (Exception e) {
			log.log(Level.SEVERE, "ERROR in arguments: " + e.getMessage());
			System.out.println("USAGE: Kernel1" + opt.usage());
			return;
		}

		if (opt.getOptionBool(OPT_HELP)) {
			System.out.println("USAGE: Kernel1" + opt.usage());
			return;
		}

		int scale = opt.getOptionInt(OPT_SCALE);
		try {
			if (scale > 30 || scale < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			log
					.log(Level.SEVERE,
							"\"Scale\" must an integer between 0 and 30.");
			return;
		}*/


                String input = "HpcData15.csv";
                String fileLog = "kernel1_log_%u_%t.log";
            
                Handler handler = new FileHandler(fileLog);
                Logger log =  Logger.getAnonymousLogger();
                log.addHandler(handler);

		Kernel1 k1 = new Kernel1(input, new Titan(log), log);
		k1.execute();
		k1.close();
	}

    void cleanVertexAndEdges() {
        graphDb.clean();
        graphDb.commit();
    }


}
