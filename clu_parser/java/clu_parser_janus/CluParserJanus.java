package clu_parser_janus;

//import org.neo4j.driver.v1.*;
//import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
/*
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;*/
//import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*
import java.nio.file.*;

import java.io.IOException;
import java.util.*;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.groovy.jsr223.dsl.credential.__;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.*;

class Target implements Comparable<Target>{
String target_id = null;
int centroid = 0;
int offset = 0;
int aln_score = 0;

public Target(String tid, int cent, int off, int as) {
    target_id = tid;
    centroid = cent;
    offset = off;
    aln_score = as;
}

public void PrintTarget() {
    System.out.println(target_id+"\t"+centroid+"\t"+offset+"\t"+aln_score);
}

public int compareTo(Target otherT) {
    return this.aln_score - otherT.aln_score;
}
}


/******************************************************/
/******************************************************/
/******************************************************/



class Insertion {
String clu_id = null;
String cons_seq = null;
Vector<Target> targetlist = new Vector<Target>(1);
Integer num_aln = null;
Integer max_aln_score = null;
Integer seq_len = null;
Integer weight = null;
Vector<String> masked = new Vector<String>(1);


public void PrintInsertion(String id) {
    Enumeration en = targetlist.elements();

    System.out.println("*** "+id+"_"+clu_id+ " T: "+targetlist.size() + " L: "+masked.size()+" ***");
    while(en.hasMoreElements()) {
        Target t = (Target)en.nextElement();
        t.PrintTarget();

    }
    if(!masked.isEmpty()) {
        //https://www.javacodeexamples.com/remove-duplicate-elements-from-vector-in-java-example/3271

        LinkedHashSet<String> lhSet = new LinkedHashSet<String>(masked);
        masked.clear();
        masked.addAll(lhSet);


        en = masked.elements();
        while(en.hasMoreElements()) {
            String s = (String) en.nextElement();
            System.out.println("--- "+s);
        }
    }

}

public static ArrayList<List<String>> getSplittedList(List<String> l){
	ArrayList<List<String>> res = new ArrayList<List<String>>();
	if(l.size() <= 100) {
		res.add(l);
		return res;
	}
	
	boolean check = true;
	int pos = 0;
	while(true) {
		
		List<String> list = new ArrayList<String>();
		for(int i = pos; i < l.size(); ++i){
			
			if(list.size() < 100) {
				list.add(l.get(i));
			}
			
			if(list.size() == 100) {
				pos = i+1;
				res.add(list);
				break;
			}
			
			if(i == l.size()-1) {
				check = false;
			}
			
		}

		if(!check) {
			break;
		}
	}
	
	return res;
}


public void AddInsertion(String id, GraphTraversalSource g, BufferedWriter logfile, Hashtable<String, Integer> hsample) {
    String cluid = id + "_" + clu_id;
    String prefixclu = id + "_";

    Enumeration en;
    String command1, command2, command3;
    ResultSet result;
    String subgres;

    String candidate;
    int degree1, degree2;
    Integer count;

    long startTime, stopTime;
    Float g4val = Float.valueOf(0);

    Hashtable<String, Integer> hmpos = new Hashtable<String, Integer>();    //To count how many genomic positions are shared
    Hashtable<String, Integer> hmlabel = new Hashtable<String, Integer>();  //To check the label sharing
    Hashtable<String, Integer> hmdegree = new Hashtable<String, Integer>(); //To get the degree of the considered subg
    Hashtable<String, Float> hmmain = new Hashtable<String, Float>();

    System.out.println("Starting " + id + "_" + clu_id + " T : " + num_aln + " ***");

    //Collegamento tra sample e subg
    /*command1 = "MATCH (ss:sample {UniqueID: \"" + id + "\" }) \n";
    command1 = command1.concat("MERGE (s:subg {clu_id: \"" + cluid + "\", " +
            //"cons_seq: \"" + cons_seq + "\", "+
            "num_aln: " + num_aln + ", " +
            "cons_seq: \"" + cons_seq + "\", " +
            "max_aln_score: " + max_aln_score + ", " +
            "seq_len: " + seq_len + ", " +
            "weight: " + weight + "})\n");
    command1 = command1.concat("MERGE (ss)-[:sample2subg]->(s)");*/
    
    //command1: testato funziona correttamente
	Object sample = g.V().hasLabel("sample").has("UniqueID", "\"" + id + "\"").next();
	
	Object subg = g.V().hasLabel("subg").has("clu_id", "\"" + cluid + "\"").has("num_aln", num_aln)
			.has("cons_seq", "\"" + cons_seq + "\"").has("max_aln_score", max_aln_score)
			.has("seq_len", seq_len).has("weight", weight).
      	fold().
      	coalesce((Traversal)__.unfold(),__.addV("subg").property("clu_id", "\"" + cluid + "\"").property("num_aln", num_aln)
    			.property("cons_seq", "\"" + cons_seq + "\"").property("max_aln_score", max_aln_score)
    			.property("seq_len", seq_len).property("weight", weight)).next();	

	g.V(sample).as("sample").V(subg).as("subg").
    coalesce(__.inE("sample2subg").where(__.outV().as("sample")),
    		__.addE("sample2subg").from("sample").to("subg")).next();
		
    /*try (Session session = d.session()) {
  	  System.out.println("QUERY: "+ command1);
        session.run(command1);
    }*/

    command1 = "MATCH (s:subg {clu_id: \"" + cluid + "\"})\n";
    command3 = null;

    /***********************************  POS ***********************************/
    //(n:subg)-[l:insertion]-(m:pos)
    int i = 0;
    en = targetlist.elements();

    startTime = System.currentTimeMillis(); ////////////////////////////

    /*command2 = "MATCH (s:subg {clu_id: $clid}) " +
            "MERGE (n:pos {target_id: $tid, centroid: $ci, searchlabel: $sl}) " +
            "MERGE (s)-[:insertion {offset: $os, aln_score: $as}]-(n)";*/

    //try (Transaction tx = d.session().beginTransaction()) {
    while (en.hasMoreElements()) {
    	Target t = (Target) en.nextElement();


        //Costruisco la ricerca di altre subg di sample diversi che condividono l'inserzione con intorno [+2,-2]
        if (command3 != null) command3 = command3.concat(", \"" + t.target_id + "_" + t.centroid + "\", \"" +
                t.target_id + "_" + Integer.toString(t.centroid - 2) + "\", \"" + t.target_id + "_" + Integer.toString(t.centroid - 1) + "\", \"" +
                t.target_id + "_" + Integer.toString(t.centroid + 1) + "\", \"" + t.target_id + "_" + Integer.toString(t.centroid + 2)) + "\" ";
        else
            command3 = "MATCH (b:pos)-[:insertion]-(a:subg) where NOT a.clu_id STARTS WITH \"" + prefixclu + "\" and  " +
                    "b.searchlabel IN [\"" + t.target_id + "_" + t.centroid + "\", \"" +
                    t.target_id + "_" + Integer.toString(t.centroid - 2) + "\", \"" + t.target_id + "_" + Integer.toString(t.centroid - 1) + "\", \"" +
                    t.target_id + "_" + Integer.toString(t.centroid + 1) + "\", \"" + t.target_id + "_" + Integer.toString(t.centroid + 2) + "\" ";
        
        i++;
        
        /* 
         clu_id stringa
         tid stringa
         ci intero
         sl stringa t.target_id + "_" + t.centroid
         os intero 
         as intero
         */
        
        String label = "\""+ t.target_id + "_" + t.centroid + "\"";
        
        //ERRATA da fare con il while
        GraphTraversal _subg = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
        while(_subg.hasNext()) {
        	Object s_subg = _subg.next();
	        Object n_pos = g.V().hasLabel("pos").has("target_id", "\""+t.target_id+"\"").has("centroid", t.centroid).
	        		has("searchlabel", label).
	        		fold().
	    	      	coalesce((Traversal)__.unfold(),__.addV("pos").property("target_id", "\""+t.target_id+"\"").property("centroid", t.centroid).
	                		property("searchlabel", label)).next();
	        
	        //direzione da s_subg -> n_pos
	        g.V(s_subg).as("s").V(n_pos).as("n").
	        coalesce(__.inE("insertion").where(__.outV().as("s")),
	        __.addE("insertion").property("offset", t.offset).property("aln_score", t.aln_score).
	        from("s").to("n")).next();
	        
        }
            //tx.run(command2, Values.parameters("clid", cluid, "tid", t.target_id, "ci", t.centroid, "sl", t.target_id + "_" + t.centroid, "os", t.offset, "as", t.aln_score));
    }
        //tx.commit();
    //}
    
    stopTime = System.currentTimeMillis(); ////////////////////////////
    System.out.println("Position took " + (stopTime - startTime) + " milliseconds");

    command3 = command3.concat("]  return a.clu_id, a.num_aln ");
    //System.out.println(command3);
    
    //mi converto la command3 completa
    int inizio = 0;
	int fine = 0;
	
	for (int j= 0; j < command3.length(); j++) {
		if (command3.charAt(j) =='[')
			inizio = j+1;
		
		if(command3.charAt(j)==']')
			fine = j;
	}

	String sub = command3.substring(inizio,fine-1);
	List<String> tot_labels = new ArrayList<String>(Arrays.asList(sub.split(", ")));
	
    startTime = System.currentTimeMillis(); ////////////////////////////

    ArrayList<List<String>> labels = getSplittedList(tot_labels);
    
    for (List<String> list: labels) {
	    GraphTraversal res_com3 = g.V().hasLabel("pos").as("b").has("searchlabel", TextP.within(list)).in("insertion")
			      .has("clu_id", TextP.notStartingWith(prefixclu)).as("a.clu_id").as("a.num_aln")
			      .select("a.clu_id", "a.num_aln").
			   		by(__.values("clu_id").fold()).
			   		by(__.values("num_aln").fold());
	    
	    while(res_com3.hasNext()) {
	    	LinkedHashMap res = (LinkedHashMap) res_com3.next();
	    	List<String> r_cluid = (List<String>) res.get("a.clu_id");
	    	List<Integer> r_num_aln = (List<Integer>) res.get("a.num_aln");
	    	subgres = r_cluid.get(0);
	    	count = hmpos.get(subgres);
	        hmpos.put(subgres, count == null ? 1 : ++count);
	        hmdegree.put(subgres, r_num_aln.get(0));
	    	//System.out.println(((LinkedHashMap) res_com3.next()).get("a.clu_id"));
	    }
    }
    
    stopTime = System.currentTimeMillis(); ////////////////////////////
    System.out.println("Query took " + (stopTime - startTime) + " milliseconds");

    degree1 = i;
    if (degree1 != num_aln) {
        System.out.println("*** *** *** Problema sul numero di allineamenti "+degree1+ " e "+ num_aln+" *** *** ***");
        try {
            logfile.write("*** *** *** Problema sul numero di allineamenti "+degree1+ " e "+ num_aln+" *** *** ***\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /* Search for MAIN INSERTION for case 4 */
    if (degree1 > 1) { // The two topmost insertions differs for at least 30%
        startTime = System.currentTimeMillis(); ////////////////////////////
        Collections.sort(targetlist, Collections.reverseOrder());
        stopTime = System.currentTimeMillis(); ////////////////////////////

        try {
            logfile.write("SORT took " + (stopTime - startTime) + " milliseconds\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        float aa = (float) targetlist.get(0).aln_score;
        float bb = (float) targetlist.get(1).aln_score;

        if ( (bb/aa) < 0.7 ) {
            g4val = bb/aa;
            String mtg = targetlist.get(0).target_id;
            int mco = targetlist.get(0).centroid;
            int mof = targetlist.get(0).offset;

            startTime = System.currentTimeMillis(); ////////////////////////////

            /*String commanda = "MATCH (s:subg {clu_id: $clid}) " +
                    "MERGE (n:pos {target_id: $tid, centroid: $ci}) " +
                    "MERGE (s)-[:main_insertion {offset: $os, aln_score: $as, val: $v}]-(n)";*/

            //SEARCH for other MAIN INSERTIONS on the same genomic position
            String commandb = "MATCH (b:pos)-[l:main_insertion]-(a:subg) where NOT a.clu_id STARTS WITH \"" + prefixclu +
                    "\" and b.searchlabel IN [\"" + mtg + "_" + mco + "\", \"" +
                    mtg + "_" + Integer.toString(mco - 1) + "\", \"" + mtg + "_" + Integer.toString(mco +1) + "\", \"" +
                    mtg + "_" + Integer.toString(mco - 2) + "\", \"" + mtg + "_" + Integer.toString(mco +2) +
                    "\" ]  return a.clu_id, l.val ";
           
            //try (Session session = d.session()) {
          	  //System.out.println("QUERY: "+commanda);
                //session.run(commanda, Values.parameters("clid", cluid, "tid", mtg, "ci", mco, "os", mof, "as", aa, "v", g4val));
            
            //aggiungo searchlabel mancante
            String s_label = "\""+mtg+"_"+mco+"\"";
            
            GraphTraversal _subg = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
            
            while(_subg.hasNext()) {
            	Object s_subg = _subg.next();
	            Object n_pos = g.V().hasLabel("pos").has("target_id", "\""+mtg+"\"").has("centroid", mco).has("searchlabel", s_label).
	            				fold().
	            				coalesce((Traversal)__.unfold(),__.addV("pos").property("target_id", "\""+mtg+"\"").property("centroid", mco).property("searchlabel", s_label)).next();
	            
	            // da subg -> pos
	            g.V(s_subg).as("s").V(n_pos).as("n").
	            coalesce(__.inE("main_insertion").where(__.outV().as("s")),
	            __.addE("main_insertion").property("offset", mof).property("aln_score", aa).property("val", g4val).
	            from("s").to("n")).next();
	            
            }
            //System.out.println("QUERY: "+ commandb);
            //result = session.run(commandb);
            int in = 0;
        	int fi = 0;
        	
        	for (int j= 0; j < commandb.length(); j++) {
        		if (commandb.charAt(j) =='[')
        			in = j+1;
        		
        		if(commandb.charAt(j)==']')
        			fi = j;
        	}
            
        	String sottostringa = commandb.substring(in,fi-1);
        	List<String> tot_labels_2 = new ArrayList<String>(Arrays.asList(sottostringa.split(", ")));
        	
        	ArrayList<List<String>> labels_2 = getSplittedList(tot_labels_2);
            
            for (List<String> list: labels_2) {
	        	GraphTraversal res_comdb = g.V().hasLabel("pos").as("b").has("searchlabel", TextP.within(list)).inE("main_insertion").as("l.val")
	      		      .hasLabel("subg").has("clu_id", TextP.notStartingWith(prefixclu)).as("a.clu_id")
	      		      .select("a.clu_id", "l.val").
	      		   		by(__.values("clu_id").fold()).
	      		   		by(__.values("val").fold());
	        	
	        	while(res_comdb.hasNext()) {
	        		LinkedHashMap res = (LinkedHashMap) res_comdb.next();
	            	List<String> r_cluid = (List<String>) res.get("a.clu_id");
	            	List<Float> r_val = (List<Float>) res.get("l.val");
	        		hmmain.put(r_cluid.get(0), r_val.get(0));
	        		
	                try {
	                    logfile.write("1 Found "+r_cluid.get(0) +"\n");
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
		            
	        	}
            }

            //SEARCH for single INSERTION on the same genomic position
            commandb = "MATCH (b:pos)-[l:insertion]-(a:subg) where NOT a.clu_id STARTS WITH \"" + prefixclu +
                    "\" and a.num_aln = 1 and b.searchlabel IN [\"" + mtg + "_" + mco + "\", \"" +
                    mtg + "_" + Integer.toString(mco - 1) + "\", \"" + mtg + "_" + Integer.toString(mco +1) + "\", \"" +
                    mtg + "_" + Integer.toString(mco - 2) + "\", \"" + mtg + "_" + Integer.toString(mco +2) +"\" ]  return a.clu_id ";
            
            int ini = 0;
        	int fin = 0;
        	
        	for (int j= 0; j < commandb.length(); j++) {
        		if (commandb.charAt(j) =='[')
        			ini = j+1;
        		
        		if(commandb.charAt(j)==']')
        			fin = j;
        	}
            
        	String sottostrin = commandb.substring(ini,fin-1);
        	List<String> tot_labels_3 = new ArrayList<String>(Arrays.asList(sottostrin.split(", ")));
        	
        	ArrayList<List<String>> labels_3 = getSplittedList(tot_labels_3);
            
            for (List<String> list: labels_3) {
	        	GraphTraversal res_commdb = g.V().hasLabel("pos").as("b").has("searchlabel", TextP.within(list)).in("insertion")
	      		      .hasLabel("subg").has("clu_id", TextP.notStartingWith(prefixclu)).as("a.clu_id").has("num_aln", 1)
	      		      .select("a.clu_id").
	      		   		by(__.values("clu_id").fold());
	        	
	        	while(res_commdb.hasNext()) {
	
		        	List<String> r_cluid = (List<String>) res_commdb.next();
		            
		        	hmmain.put(r_cluid.get(0), Float.valueOf(-1));
		        	try {
		                logfile.write("2 Found "+r_cluid.get(0) +"\n");
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
	            }
            }

            stopTime = System.currentTimeMillis(); ////////////////////////////


            try {
                logfile.write("*** MAIN_INSERTION "+mtg+ "_"+ mco+" : "+g4val+"\n");
                //logfile.write(commandb+"\n");
                logfile.write("It tooks " + (stopTime - startTime) + " milliseconds\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    } else { //A subg with a single assignemnt search for MAIN INSERTIONS
    	
        String mtg = targetlist.get(0).target_id; //Only 1 pos
        int mco = targetlist.get(0).centroid;

        String commandb = "MATCH (b:pos)-[l:main_insertion]-(a:subg) where NOT a.clu_id STARTS WITH \"" + prefixclu +
                "\" and b.searchlabel IN [\"" + mtg + "_" + mco + "\", \"" +
                mtg + "_" + Integer.toString(mco - 1) + "\", \"" + mtg + "_" + Integer.toString(mco +1) + "\", \"" +
                mtg + "_" + Integer.toString(mco - 2) + "\", \"" + mtg + "_" + Integer.toString(mco +2) +"\" ]  return a.clu_id, l.val ";

        int in = 0;
    	int fi = 0;
    	
    	for (int j= 0; j < commandb.length(); j++) {
    		if (commandb.charAt(j) =='[')
    			in = j+1;
    		
    		if(commandb.charAt(j)==']')
    			fi = j;
    	}
        
    	String sottostringa = commandb.substring(in,fi-1);
    	List<String> tot_labels_4 = new ArrayList<String>(Arrays.asList(sottostringa.split(", ")));
    	
    	ArrayList<List<String>> labels_4 = getSplittedList(tot_labels_4);
        
        for (List<String> list: labels_4) {
	    	GraphTraversal res_comdb = g.V().hasLabel("pos").as("b").has("searchlabel", TextP.within(list)).inE("main_insertion").as("l.val")
	  		      .hasLabel("subg").has("clu_id", TextP.notStartingWith(prefixclu)).as("a.clu_id")
	  		      .select("a.clu_id", "l.val").
	  		   		by(__.values("clu_id").fold()).
	  		   		by(__.values("val").fold());
	    	
	    	while(res_comdb.hasNext()) {
	    		LinkedHashMap res = (LinkedHashMap) res_comdb.next();
		    	List<String> res_cluidd = (List<String>) res.get("a.clu_id");
		        List<Float> res_val = (List<Float>) res.get("l.val");
		        
	        	hmmain.put(res_cluidd.get(0),res_val.get(0));
	        	
	            try {
	            	logfile.write("3 Found "+res_cluidd.get(0) +"\n");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	    	}
        }
    }


    /***********************************  LABEL ***********************************/
    //(n:subg)-[l:repeat]-(m:label)

    command3 = null;
    i = 0;

    //SE ho etichette le inserisco
    if(!masked.isEmpty()) {

        startTime  = System.currentTimeMillis(); ////////////////////////////

        //https://www.javacodeexamples.com/remove-duplicate-elements-from-vector-in-java-example/3271

        LinkedHashSet<String> lhSet = new LinkedHashSet<String>(masked);
        masked.clear();
        masked.addAll(lhSet);

        en = masked.elements();
        while(en.hasMoreElements()) {
            String stringa = (String) en.nextElement();
            
            //Collegamento tra subg e label
            command2 = command1.concat("MERGE (m"+i+":label {name: \"" +stringa+ "\"})\n");
            command2 = command2.concat("MERGE (s)-[:repeat]->(m"+i+")\n");
            i++;

            //Altre subg che condividono la label
            if(command3 != null) command3 = command3.concat(", \""+stringa+"\"");
            else command3 = "MATCH (a:subg)-[:repeat]-(b:label) where NOT a.clu_id STARTS WITH \"" +prefixclu+ "\" and b.name IN  [\"" +stringa+ "\"";

            //System.out.println(command3);

            /*try (Session session = d.session()) {
          	  System.out.println("QUERY: "+command2);
                session.run(command2);
            }*/
            
            GraphTraversal _com1 = g.V().hasLabel("subg").has("clu_id", "\"" + cluid + "\"");
        	
            while(_com1.hasNext()) {
            	Object com1 = _com1.next();
	        	Object com1_m = g.V().hasLabel("labell").as("m"+i).has("name", "\"" + stringa + "\"").
	              	fold().
	              	coalesce((Traversal)__.unfold(),__.addV("labell").as("m"+i).property("name", "\"" + stringa + "\"")).next();	
	        	
	        	//da s->m+i
	        	g.V(com1).as("s").V(com1_m).as("m"+i).
	            coalesce(__.inE("repeat").where(__.outV().as("s")),
	            		__.addE("repeat").from("s").to("m"+i)).next();
            }
        }


        command3 = command3.concat("] return a.clu_id ");
        
        //try (Session session = d.session()) {
      	  //System.out.println("QUERY: "+command3);
          //  result = session.run(command3);
        int ini = 0;
    	int fin = 0;
    	
    	for (int j= 0; j < command3.length(); j++) {
    		if (command3.charAt(j) =='[')
    			ini = j+1;
    		
    		if(command3.charAt(j)==']')
    			fin = j;
    	}
        
    	String sottostrin = command3.substring(ini,fin-1);
    	List<String> tot_labels_5 = new ArrayList<String>(Arrays.asList(sottostrin.split(", ")));
    	
    	tot_labels_5.set(tot_labels_5.size()-1, tot_labels_5.get(tot_labels_5.size()-1)+"\"");
    	
    	//da provare
    	//labb.set(labb.size()-1, labb.get(labb.size()-1)+"\"");
    	
    	ArrayList<List<String>> labels_5 = getSplittedList(tot_labels_5);
        
        for (List<String> list: labels_5) {
	    	GraphTraversal res_commdb = g.V().hasLabel("subg").as("a").has("clu_id", TextP.notStartingWith(prefixclu)).out("repeat")
	  		      .hasLabel("labell").has("name", TextP.within(list))
	  		      .select("a").
	  		   		by(__.values("clu_id").fold());
	    	
	    	while(res_commdb.hasNext()) {
	
	    		List<String> r_cluid = (List<String>) res_commdb.next();
	                //Qui non mi interessa il degree
	    		subgres = r_cluid.get(0);
	            count = hmlabel.get(subgres);
	            hmlabel.put(subgres, count == null ? 1 : ++count);
	        }
        }

        stopTime = System.currentTimeMillis(); ////////////////////////////
        System.out.println("Label took " + (stopTime - startTime) + " milliseconds");
    }



    ////(n:subg)-[l:gtris]-(m:subg) m.prefixclu != n.prefixclu

    /* REGOLE per il collegamento
    1) se ha un'inserzione unica e l'altra pure a posizione +-3 OK (A2-B2-C2) NO LABEL
    2) se le etichette di allineamento (target, centroid) sono condivise almeno per il 50%  (e non ci sono etichette di repeat)
    3) condividono almeno una etichetta della repeat (almeno una) ed ALMENO UN punto di inserzione (etichetta/annotazione genomica) (sempre distanza +-3) (A3-C3, A1-B1)
    4) se ho inserzione unica ed una main_insertion o due main_insertion
    */



    startTime  = System.currentTimeMillis(); ////////////////////////////

    Set<String> keys;
    Iterator<String> itr;


    //Regole 1 e 2
    if (!hmpos.isEmpty()) {
        keys = hmpos.keySet();
        itr = keys.iterator();
        while (itr.hasNext()) {

            candidate = itr.next();
            degree2 = hmdegree.get(candidate);

            if(degree1 == 1 && degree2 == 1) { //REGOLA 1
                command1 =  "MATCH (a:subg {clu_id: \"" +cluid+ "\"})\n"+
                            "MATCH (b:subg {clu_id: \"" +candidate+ "\"})\n"+
                            "MERGE (a)-[:gtris {case: \"1\"}]-(b)";
                //command1 = ("MERGE (a:subg {clu_id: \"" +cluid+ "\"})-[:gtris {case: \"1\"}]-(b:subg {clu_id: \"" +candidate+ "\"})\n");

                //Conto quanto link tra i due sample delle due subg
                String[] candidateid = candidate.split("_");
                count = hsample.get(candidateid[0]);
                hsample.put(candidateid[0], count == null ? 1 : ++count);

                hmlabel.remove(candidate); //Spesso 1 implica 3, per evitare di avere due link gtris
                hmmain.remove(candidate); //NON dovrebbe capitare
                
                //try (Session session = d.session()) {
              	  //System.out.println("QUERY: "+command1);
                  //  session.run(command1);
                String temp_cluid = "\""+cluid+"\"";
                if(!temp_cluid.equals(candidate)) {
                
	                GraphTraversal subg_a = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
	                GraphTraversal subg_b = g.V().hasLabel("subg").has("clu_id", candidate);
	                
	                
	                while(subg_a.hasNext() && subg_b.hasNext()) {
		                Object a = subg_a.next();
		                Object b = subg_b.next();
		                System.out.println("First");
		                System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", \""+cluid+"\")");
		                System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", "+candidate+")");
		                g.V(a).as("a").V(b).as("b").
		                coalesce(__.inE("gtris").where(__.outV().as("a")),
		                		__.addE("gtris").from("a").to("b").property("case", "1")).next();
	                
		                System.out.println("*** GTRIS 1 between "+cluid+ " and "+ candidate);
		                try {
		                	logfile.write("*** GTRIS 1 between "+cluid+ " and "+ candidate);
		                    logfile.write(" and between "+id+" and "+candidateid[0]+"\n");
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
	                }
                }
            } else { //REGOLA 2
                count = hmpos.get(candidate);

                //SE OR avrei un GTRIS 2 monodirezionale
                //Se un nodo ha solo 2 inserzioni considero 1 come il 50%+1, altrimenti dovrei avere esattamente 2

                boolean left, right;
                if (degree1 == 2 ) left = (count >= 1); //dovrebbe essere sempre vera
                else left = (count > (degree1/2));

                if (degree2 == 2 ) right = (count >= 1); //idem
                else right = (count > (degree2/2));

                //if (count > (degree1/2) && count > (degree2/2) ) {
                if( left && right ) {
                    command1 =  "MATCH (a:subg {clu_id: \"" +cluid+ "\"})\n"+
                                "MATCH (b:subg {clu_id: \"" +candidate+ "\"})\n"+
                                "MERGE (a)-[:gtris {case: \"2\"}]-(b)";

                    String[] candidateid = candidate.split("_");
                    count = hsample.get(candidateid[0]);
                    hsample.put(candidateid[0], count == null ? 1 : ++count);

                    hmlabel.remove(candidate); //A volte
                    hmmain.remove(candidate);

                    //try (Session session = d.session()) {
                  	  //System.out.println("QUERY: "+command1);
                        //session.run(command1);
                    String temp_cluid = "\""+cluid+"\"";
                    if(!temp_cluid.equals(candidate)) {
                    
	                    GraphTraversal subg_a = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
	                    GraphTraversal subg_b = g.V().hasLabel("subg").has("clu_id", candidate);
	                    
	                    while(subg_a.hasNext() && subg_b.hasNext()) {
	    	                Object a = subg_a.next();
	    	                Object b = subg_b.next();
	    	                System.out.println("Second");
	                        System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", \""+cluid+"\")");
	                        System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", "+candidate+")");
	    	                g.V(a).as("a").V(b).as("b").
	    	                coalesce(__.inE("gtris").where(__.outV().as("a")),
	    	                		__.addE("gtris").from("a").to("b").property("case", "2")).next();
	                    
		                    System.out.println("*** GTRIS 2 between "+cluid+ " and "+ candidate);
		                    try {
		                        logfile.write("*** GTRIS 2 between "+cluid+ " and "+ candidate);
		                        logfile.write(" and between "+id+" and "+candidateid[0]+"\n");
		                    } catch (IOException e) {
		                        e.printStackTrace();
		                    }
	                    }
                    }
                }

            }
        }
    }

    //Regola 3: due subg condividono almeno 1 label ed almeno 1 insertion
    if (!hmlabel.isEmpty()) {
        keys = hmlabel.keySet();
        itr = keys.iterator();

        while (itr.hasNext()) {  //Scorro tutte le subg che hanno in comune almeno una label
            candidate = itr.next();
            if (hmpos.get(candidate) != null) { //E' presente almeno una inserzione in comune
                command1 =  "MATCH (a:subg {clu_id: \"" +cluid+ "\"})\n"+
                        "MATCH (b:subg {clu_id: \"" +candidate+ "\"})\n"+
                        "MERGE (a)-[:gtris {case: \"3\"}]-(b)";

                //command1 = ("MERGE (a:subg {clu_id: \"" +cluid+ "\"})-[:gtris {case: \"3\"}]-(b:subg {clu_id: \"" +candidate+ "\"})\n");

                //Conto quanti link tra i due sample delle due subg
                String[] candidateid = candidate.split("_");
                count = hsample.get(candidateid[0]);
                hsample.put(candidateid[0], count == null ? 1 : ++count);

                hmmain.remove(candidate); //potrebbe capitare

               // try (Session session = d.session()) {
              	//  System.out.println("QUERY: "+command1);
                //    session.run(command1);
                    //session.run(command2);
                String temp_cluid = "\""+cluid+"\"";
                if(!temp_cluid.equals(candidate)) {
                
	                GraphTraversal subg_a = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
	                GraphTraversal subg_b = g.V().hasLabel("subg").has("clu_id", candidate);
	                
	                while(subg_a.hasNext() && subg_b.hasNext()) {
		                Object a = subg_a.next();
		                Object b = subg_b.next();
		                System.out.println("Terzo");
		                System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", \""+cluid+"\")");
		                System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", "+candidate+")");
		                g.V(a).as("a").V(b).as("b").
		                coalesce(__.inE("gtris").where(__.outV().as("a")),
		                		__.addE("gtris").from("a").to("b").property("case", "3")).next();
		                
		                System.out.println("*** GTRIS 3 between "+cluid+ " and "+ candidate);
		                try {
		                    logfile.write("*** GTRIS 3 between "+cluid+ " and "+ candidate );
		                    logfile.write(" and between "+id+" and "+candidateid[0]+"\n");
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
	                }
                }
            }
        }
    }

    //Regola 4:  inserzione unica ed una main_insertion o due main_insertion
    if (!hmmain.isEmpty()) {
        keys = hmmain.keySet();
        itr = keys.iterator();
        Float maxv;

        while (itr.hasNext()) {  //Scorro tutte le subg che hanno in comune almeno una label
            candidate = itr.next();

            if (g4val > hmmain.get(candidate)) maxv = g4val;
            else maxv = hmmain.get(candidate);

            command1 =  "MATCH (a:subg {clu_id: \"" +cluid+ "\"})\n"+
                    "MATCH (b:subg {clu_id: \"" +candidate+ "\"})\n"+
                    "MERGE (a)-[:gtris {case: \"4\", val: "+maxv+"}]-(b)";

            String[] candidateid = candidate.split("_");
            count = hsample.get(candidateid[0]);
            hsample.put(candidateid[0], count == null ? 1 : ++count);


            //try (Session session = d.session()) {
          	//  System.out.println("QUERY: "+command1);
            //    session.run(command1);
                //session.run(command2);
            String temp_cluid = "\""+cluid+"\"";
            if(!temp_cluid.equals(candidate)) {
            
	            GraphTraversal subg_a = g.V().hasLabel("subg").has("clu_id", "\""+cluid+"\"");
	            GraphTraversal subg_b = g.V().hasLabel("subg").has("clu_id", candidate);
	            
	            while(subg_a.hasNext() && subg_b.hasNext()) {
	            	System.out.println("Qui");
	            	
	            	System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", \""+cluid+"\")");
	                System.out.println("g.V().hasLabel(\"subg\").has(\"clu_id\", "+candidate+")");
	                
	                Object a = subg_a.next();
	                Object b = subg_b.next();
	                g.V(a).as("a").V(b).as("b").
	                coalesce(__.inE("gtris").where(__.outV().as("a")),
	                		__.addE("gtris").from("a").to("b").property("case", "4").property("val", "\""+maxv+"\"")).next();
	                
		            System.out.println("*** GTRIS 4 between "+cluid+ " and "+ candidate);
		            try {
		                logfile.write("*** GTRIS 4 between "+cluid+ " and "+ candidate );
		                logfile.write(" and between "+id+" and "+candidateid[0]+"\n");
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
	            }
            }
        }
    }


    stopTime = System.currentTimeMillis(); ////////////////////////////
    System.out.println("gtris took " + (stopTime - startTime) + " milliseconds");

    //System.out.println(command);
    System.out.println("Added "+id+"_"+clu_id+ " T : "+targetlist.size() + " L : "+masked.size()+" ***");
    try {
        logfile.write("Added "+id+"_"+clu_id+ " T : "+targetlist.size() + " L : "+masked.size()+" ***\n");
    } catch (IOException e) {
        e.printStackTrace();
    }

}
}



/******************************************************/
/******************************************************/
/******************************************************/




public class CluParserJanus {


    public static void main(String[] args) {
	    //Path file = Paths.get("/Users/dago/Desktop/ivan/BED/unique_r1_ID00000000000000016511.filt.out.all.noprobl.clu");
	    //Path file = null;
    	long start_program = System.currentTimeMillis();
    	final File folder = new File("/Users/Alessandro/Desktop/TESI/testset");
    	
    	for (final File fileEntry : folder.listFiles()) {
			Path file = Paths.get(folder+"/"+fileEntry.getName());
		    String ID ="Error";
		    
		    Hashtable<String, Integer> hsample = new Hashtable<String, Integer>();
		
		    //Driver driver;
		
		    if (file == null)
		        if (args.length == 0) {
		            System.out.println("Indicare il file da leggere! \n Uso: java CluParser filename [ID]");
		            System.exit(-1);
		        } else file = Paths.get(args[0]);
		
		    //GREMLIN
		    System.out.println(file);
		    GraphTraversalSource g = null;
			try {
				g = traversal().withRemote("conf/remote-graph.properties");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    //BASTION
		    //driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "ciaociao"));
		
		    //LOCAL
		    //driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4j123Stella"));
		
		    if(args.length > 1) ID = args[1];
		    else if (file.getFileName().toString().contains("unique_r1_ID")) {
		        System.out.println("Il nome del file continene un ID: "); //E me lo cerco del posto opportuno
		
		        int i1 = file.getFileName().toString().indexOf("_ID");
		        int i2 = file.getFileName().toString().indexOf(".");
		
		        ID = file.getFileName().toString().substring(i1+1,i2);
		    } else if (file.getFileName().toString().contains("unique_r1_LMv") || file.getFileName().toString().contains("unique_r1_LAM")) {
		        int i1 = file.getFileName().toString().indexOf("_r1");
		        int i2 = file.getFileName().toString().indexOf("_LC");
		        int i3 = file.getFileName().toString().indexOf(".filt");
		
		        //DA LAM-LTR_Block_L_11_LTR21_LC66_BR2_TR1
		        //A  LAM-LTR_Block_L_11_LTR21.LC66_BR2_TR1
		        
		        String CompleteAmplificationID = file.getFileName().toString().substring(i1+4,i2);
		        CompleteAmplificationID = CompleteAmplificationID.concat("."+file.getFileName().toString().substring(i2+1,i3));
		
		        //String command1 = "MATCH (ss:sample {CompleteAmplificationID: \"" + CompleteAmplificationID  + "\" }) return ss.UniqueID \n";
		        //System.out.println(command1);
		        
		        //non ci entro mai, avendo file di nome ID...
		        /*try (Session session = driver.session()) {
		        	
		            Result result = session.run(command1);
		            ID = result.single().get(0).asString();
		
		            System.out.println(CompleteAmplificationID+" -> "+ID);
		        }*/
		      
		    } else if(file.getFileName().toString().contains("ID")) { //aggiunto
		  	  int i1 = file.getFileName().toString().indexOf("ID");
		  	  int i2 = file.getFileName().toString().indexOf(".");
		  	  
		  	  ID = file.getFileName().toString().substring(i1+2,i2);
		  	  
		    } else{
		        System.out.println("++++ ++++ I cannot determine the ID of "+file.getFileName().toString()+" ++++ ++++");
		        System.exit(-1);
		    }
		
		    BufferedWriter logfile = null;
		    try {
		        logfile = new BufferedWriter(new FileWriter(ID+"_log.txt"));
		        logfile.write("FILE "+file.getFileName().toString()+" "+ID+"\n\n");
		    }  catch (IOException x) {
		        System.err.println(x);
		    }
		
		
		    try {
		        BufferedReader br = new BufferedReader(new FileReader(file.toString()));
		        String line, lineaux;
		        int num_insertion_file = 0;
		        int numbrackets = 0;
		        boolean isfirst = true;
		        
		        
		        while ((line = br.readLine()) != null) {
		            if (line.contains("ISCluster")) {
		                Insertion iscluster = new Insertion();
		                num_insertion_file++;
		                if (line.contains("{")) numbrackets++; //Dovrebbe essere sempre vero
		
		                while ((line = br.readLine()) != null) {
		                    //Definisco il blocco
		                    if (line.contains("{")) numbrackets++;
		                    if (line.contains("}")) {
		                        numbrackets--;
		                        if (numbrackets == 0) {
		                            //iscluster.PrintInsertion();
		                            break;
		                        }
		                    }
		
		                    if (line.contains("clu_id")) {
		                        lineaux = line.substring(line.indexOf("\"") + 1);
		                        lineaux = lineaux.substring(0, lineaux.indexOf("\""));
		                        iscluster.clu_id = lineaux;
		                    }
		
		                    if (line.contains("cons_seq")) {
		                        lineaux = line.substring(line.indexOf("\"") + 1);
		                        if(lineaux.indexOf("\"") != -1) {
		                            lineaux = lineaux.substring(0, lineaux.indexOf("\""));
		                            iscluster.cons_seq = lineaux;
		                        } else {
		                            iscluster.cons_seq = lineaux;
		                            do {
		                                lineaux = br.readLine();
		                                if(lineaux.indexOf("\"") != -1) iscluster.cons_seq = iscluster.cons_seq.concat(lineaux.substring(0, lineaux.indexOf("\"")));
		                                else iscluster.cons_seq = iscluster.cons_seq.concat(lineaux);
		                            } while (lineaux.indexOf("\"") == -1);
		
		                        }
		                        //La fine è indicata da ". Potrebbe essere una linea sola o piu' linee
		                        // di cui solo l'ultima contiene "
		                        //System.out.println(num_insertion_file+" : "+iscluster.cons_seq);
		                    }
		
		                    if (line.contains("num_aln")) {
		                        lineaux = line.substring(line.indexOf("l") + 3);
		                        lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                        iscluster.num_aln = Integer.parseInt(lineaux);
		                        //System.out.println(num_insertion_file+" : "+iscluster.num_aln);
		                    }
		
		                    if (line.contains("max_aln_score")) {
		                        lineaux = line.substring(line.indexOf("e") + 2);
		                        lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                        iscluster.max_aln_score = Integer.parseInt(lineaux);
		                        //System.out.println(num_insertion_file+" : "+iscluster.max_aln_score);
		                    }
		
		                    if (line.contains("seq_len")) {
		                        lineaux = line.substring(line.indexOf("n") + 2);
		                        lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                        iscluster.seq_len = Integer.parseInt(lineaux);
		                        //System.out.println(num_insertion_file+" : "+iscluster.seq_len);
		                    }
		
		                    if (line.contains("weight")) {
		                        lineaux = line.substring(line.indexOf("t") + 2);
		                        lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                        iscluster.weight = Integer.parseInt(lineaux);
		                        //System.out.println(num_insertion_file+" : "+iscluster.weight);
		                    }
		
		                    if (line.contains("masked")) {
		                        while (! (line = br.readLine()).contains("}") ) {
		                            if (line.contains("\"")) {
		                                lineaux = line.substring(line.indexOf("\"") + 1);
		                                lineaux = lineaux.substring(0, lineaux.indexOf("\""));
		                                //System.out.println(num_insertion_file + " : " + lineaux);
		                                iscluster.masked.addElement(lineaux);
		                            }
		                            else {
		                                System.out.println("*** STRANA riga in masked : "+line);
		                                logfile.write("*** STRANA riga in masked : "+line+"\n");
		                                System.exit(-1);
		                            }
		                        }
		                        numbrackets--;
		                    }
		
		                    if (line.contains("lab")) {
		                        boolean stop = false;
		                        String tid = null;
		                        int c = 0, e = 0, o = 0, as = 0;
		                        double cd = 0.0;
		                        boolean isfloat = false;
		
		                        while (!stop) {
		                            line = br.readLine();
		
		                            while (!line.contains("},") || line.contains("centroid")) {
		                                if (line.contains("target_id")) {
		                                    lineaux = line.substring(line.indexOf("\"") + 1);
		                                    lineaux = lineaux.substring(0, lineaux.indexOf("\""));
		                                    tid = lineaux;
		                                }
		
		                                if (line.contains("centroid")) {
		                                    lineaux = line.substring(line.indexOf("{") + 2);
		                                    lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                                    try {
		                                        c = Integer.parseInt(lineaux);
		                                    } catch (NumberFormatException nfe) {
		                                        cd = Double.parseDouble(lineaux);
		                                        isfloat = true;
		                                        System.out.println("*** ***\n\n"+line+" : "+cd);
		                                    }
		                                    //10^exp
		                                    lineaux = line.substring(line.indexOf(",")+1);
		                                    lineaux = lineaux.substring(lineaux.indexOf(",")+2);
		                                    lineaux = lineaux.substring(0, lineaux.indexOf(" }"));
		                                    e = Integer.parseInt(lineaux);
		                                    if(isfloat) {
		                                        cd *= Math.pow(10, e);
		                                        c = Math.toIntExact(Math.round(cd));
		                                        System.out.println(iscluster.clu_id+" : "+cd+" ^ "+e+" : "+c+" \n\n*** ***");
		                                        isfloat = false;
		                                    }
		                                    else if (e > 0) {
		                                        c *= Math.pow(10, e);
		                                    }
		                                    else if (e < 0) {
		                                        cd = c * Math.pow(10, e);
		                                        c = Math.toIntExact(Math.round(cd));
		                                        System.out.println(iscluster.clu_id+" : "+cd+" ^ "+e+" : "+c+" \n\n*** ***");
		
		                                    }
		                                }
		
		                                if (line.contains("offset")) {
		                                    lineaux = line.substring(line.indexOf("t ") + 2);
		                                    lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                                    o = Integer.parseInt(lineaux);
		                                }
		
		                                if (line.contains("aln_score")) {
		                                    lineaux = line.substring(line.indexOf("e ") + 2);
		                                    lineaux = lineaux.substring(0, lineaux.indexOf(","));
		                                    as = Integer.parseInt(lineaux);
		                                }
		
		                                line = br.readLine();
		                                //System.out.println(line);
		                            }
		                            iscluster.targetlist.addElement(new Target(tid,c,o,as));
		                            if(e < 0) System.out.println(iscluster.clu_id+" : "+tid+" "+c+" "+o+" "+as+" \n\n*** ***");
		
		                            if ( (line = br.readLine()).contains("merged")) stop = true;
		                            else if( !line.contains("{")) {
		                                System.out.println("*** STRANEZZE IN LAB "+ num_insertion_file + " : " + line);
		                                System.exit(-1);
		                            }
		                        }
		                    }
		
		
		                    //PROBLEMI
		                    if (line.contains("ISCluster")) {
		                        System.out.println("*** BLOCCO non chiuso");
		                        logfile.write("*** BLOCCO non chiuso\n");
		                        System.exit(-1);
		                    }
		                }
		
		                //iscluster.PrintInsertion(ID);
		                if(isfirst) {
		                    /*String command1 = "MERGE (ss:sample {UniqueID: \"" + ID + "\" })\n";
		                    
		                    //System.out.println("MATCH (ss:sample {UniqueID: \"" + ID + "\" }) return ss");
		                    logfile.write("MATCH (ss:sample {UniqueID: \"" + ID + "\" }) return ss");
		                    
		                    System.out.println("QUERY:"+command1);
		                    //try (Session session = driver.session()) {
		                    //    session.run(command1);
		                    //}*/
		                	
		                	
		                	//MERGE in Gremlin
		                	g.V().hasLabel("sample").has("UniqueID", "\"" + ID + "\"").
		          	      	fold().
		          	      	coalesce((Traversal)__.unfold(),__.addV("sample").property("UniqueID", "\"" + ID + "\"")).next();
		                	
		                    isfirst = false;
		                    
		                    
		                }
		
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                iscluster.AddInsertion(ID,g,logfile,hsample);
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                /***************************************************/
		                
		                try {
		                    logfile.flush();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		
		            } // Iscluster
		        }
		
		        if(!hsample.isEmpty()) {
		
		            Set<String> keys = hsample.keySet();
		            Iterator<String> itr = keys.iterator();
		
		            while (itr.hasNext()) {
		                String samplename = itr.next();
		                String command2 =  "MATCH (ss:sample {UniqueID: \"" +ID+ "\"})\n"+
		                        "MATCH (ll:sample {UniqueID: \"" +samplename+ "\"})\n"+
		                        "MERGE (ss)-[:gtris_sample {num: \""+hsample.get(samplename) +"\"}]-(ll)";
		
		                //try (Session session = driver.session()) {
		              	  //System.out.println("QUERY: "+command2);
		                  //  session.run(command2);
		                String temp_id = "\""+ID+"\"";
		                String temp_samplename = samplename + "\"";
		                if(!temp_id.equals(temp_samplename)) {
			                GraphTraversal sample_1 = g.V().hasLabel("sample").has("UniqueID", "\""+ID+"\"");
			                GraphTraversal sample_2 = g.V().hasLabel("sample").has("UniqueID", samplename+"\"");
			                
			                while(sample_1.hasNext() && sample_2.hasNext()) {
				                Object ss = sample_1.next();
				                Object ll = sample_2.next();
				                
				                g.V(ss).as("ss").V(ll).as("ll").
				                coalesce(__.inE("gtris_sample").where(__.outV().as("ss")),
				                		__.addE("gtris_sample").from("ss").to("ll").property("num", "\""+hsample.get(samplename)+"\"")).next();
				                
				                
				                System.out.println("*** *** *** GTRIS between "+ID+ " and "+ samplename +"  ("+hsample.get(samplename)+" links)");
				                try {
				                    logfile.write("*** *** *** GTRIS between "+ID+ " and "+ samplename+"  ("+hsample.get(samplename)+" links)\n");
				                } catch (IOException e) {
				                    e.printStackTrace();
				                }
			                }
		                }
		            }
		
		        }
		
		        br.close();
		    }
		    catch (IOException x) {
		        System.err.println(x);
		    }
		
		    //driver.close();
		    try {
				g.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		    try {
		        logfile.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		
		    System.out.println("DONE");
		    //System.exit(0);
    	}
    	long end_program = System.currentTimeMillis();
  	  	System.out.println("Elapsed Time in milli seconds: "+ (end_program-start_program));
	}
}

/* MANUAL

https://neo4j.com/docs/driver-manual/1.7/
https://github.com/neo4j/neo4j-java-driver/wiki
https://neo4j.com/docs/api/java-driver/1.7/

*/

/* Per debug
Check a subg
match (pos:subg {clu_id: "ID00000000000000003866_subg_1627"})-[]-(n) return *;

DELETE all nodes and links related to a file
match ()-[n:repeat]-() delete n; match (n:label) delete n;
match ()-[n:insertion]-() delete n; match (n:pos) delete n; match (n:subg) delete n;

CHECK
call db.labels();

*/


/* TO DO per efficienza
Collegamento tra subg e inserzione
CREATE INDEX ON :pos(searchlabel);
CREATE INDEX ON :subg(clu_id);
CREATE INDEX on :sample(UniqueID);
CALL db.indexes;

*/

/*
match (n:sample {UniqueID: "ID00000000000000024148"})-[:gtris_sample]-(m) return *;


(:subg {clu_id: "ID00000000000000024148_subg_1415"}) | [:gtris {case: "3"}] | (:subg {clu_id: "ID00000000000000024132_subg_2491"}) |

match (n:sample {UniqueID: "ID00000000000000024148"})-[:sample2subg]-(n1:subg)-[r1:gtris]-(n2:subg)-[:sample2subg]-(m:sample {UniqueID: "ID00000000000000024140"}) return n1,r1,n2;

match (n:sample {UniqueID: "ID00000000000000024148"})-[:sample2subg]-(n1:subg   {clu_id: "ID00000000000000024148_subg_1415"})-[r:gtris]-(m1:subg) return *;

match (n1:subg {clu_id: "ID00000000000000024148_subg_2093"})-[i:gtris]-(p:subg) return *;


match (n:sample {UniqueID: "ID00000000000000024148"})-[:sample2subg]-(n1:subg)-[r1:gtris]-(m1:subg) return n1,r1,m1;

GOOD
match (n:sample {UniqueID: "ID00000000000000024148"})-[:gtris_sample]-(m) return *;
match (n1:subg)-[i:gtris]-(p:subg) where n1.clu_id STARTS WITH "ID00000000000000024148"return *;

match (n1:subg {clu_id: "ID00000000000000024148_subg_4"}) return *
match (n1:subg {clu_id: ""ID00000000000000024132_subg_2624"}) return *

match (n:sample {UniqueID: "ID00000000000000024148"})-[:sample2subg]-(n1:subg) return count(n1);

between ID00000000000000024162_subg_440 and ID00000000000000024161_subg_717
*/

/*


FILE

unique_r1_LAM-LTR_Splink_UT_94_LTR91_LC48_BR3_TR3.filt.out.all.noprobl.clu
Spezzo e cambio un _ in .

LAM-LTR_Splink_UT_94_LTR91.LC48_BR3_TR3

e lo trovo in

CompleteAmplificationID


unique_r1_LAM-LTR_Splink_UT_52_LTR7_LC8_BR2_TR2.filt.out.all.noprobl.clu

LAM-LTR_Splink_UT_52_LTR7.LC8_BR2_TR2

https://neo4j.com/developer/kb/understanding-memory-consumption/

MATCH(sg:subg) WITH collect(sg) as subregion
call apoc.cypher.mapParallel('MATCH (_)-[:insertion]-(b:pos) where NOT _.clu_id STARTS WITH "ID00000000000000024161_" and b.target_id =  "chr1" and  b.centroid IN ["91573669", "91573671", "91573670", "91573667", "91573668"] return _.clu_id, _.num_aln',{},subregion) YIELD value return value.clu_id, value.num_aln;



DUMP e LOAD
https://neo4j.com/docs/operations-manual/3.5/tools/dump-load/

$neo4j-home> bin/neo4j-admin dump --database=graph.db --to=/backups/graph.db/2016-10-02.dump

$neo4j-home> ls /backups/graph.db

$neo4j-home> 2016-10-02.dump

$neo4j-home> bin/neo4j stop

Stopping Neo4j.. stopped

$neo4j-home> bin/neo4j-admin load --from=/backups/graph.db/2016-10-02.dump --database=graph.db --force



match (n:pos)-[ii:insertion]-(a:subg)-[:sample2subg]-(m:sample) return n.searchlabel, ii.offset, ii.aln_score, m.UniqueID order by n.searchlabel;


TOLGO SAMPLE INUTILI

match (n:sample) where not  (n)-[]-(:subg) delete n;
*/

