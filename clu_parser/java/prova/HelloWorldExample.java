package prova;

/*import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;*/

/*import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.exception.NoHostAvailableException;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Geoshape;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.apache.tinkerpop.gremlin.util.Gremlin;
*/
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
//import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser.TraversalPredicate_notStartingWithContext;
//import org.antlr.v4.runtime.RuleContext;
//import org.antlr.v4.runtime.ParserRuleContext;

import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.apache.tinkerpop.gremlin.driver.ser.GraphBinaryMessageSerializerV1;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.groovy.jsr223.dsl.credential.__;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.baidu.hugegraph.driver.GraphManager;
import com.baidu.hugegraph.driver.GremlinManager;
import com.baidu.hugegraph.driver.HugeClient;
import com.baidu.hugegraph.driver.SchemaManager;
import com.baidu.hugegraph.driver.TraverserManager;
//import com.baidu.hugegraph.io.HugeGraphIoRegistry;
import com.baidu.hugegraph.structure.constant.T;
import com.baidu.hugegraph.structure.graph.Edge;
import com.baidu.hugegraph.structure.graph.Path;
import com.baidu.hugegraph.structure.graph.Vertex;
import com.baidu.hugegraph.structure.gremlin.Result;
import com.baidu.hugegraph.structure.gremlin.ResultSet;


/*import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;*/

/*
import com.baidu.hugegraph.driver.GraphManager;
import com.baidu.hugegraph.driver.GremlinManager;
import com.baidu.hugegraph.driver.HugeClient;
import com.baidu.hugegraph.driver.SchemaManager;
import com.baidu.hugegraph.structure.constant.T;
import com.baidu.hugegraph.structure.graph.Edge;
import com.baidu.hugegraph.structure.graph.Path;
import com.baidu.hugegraph.structure.graph.Edge;
import com.baidu.hugegraph.structure.graph.Vertex;*/
// Reuse 'g' across the application
// and close it on shut-down to close open connections with g.close()

public class HelloWorldExample
{
	public static void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	            System.out.println(fileEntry.getName());
	        }
	    }
	}
	
	public static ArrayList<List<String>> getSplittedList(List<String> labels){
		ArrayList<List<String>> res = new ArrayList<List<String>>();
		if(labels.size() <= 20000) {
			res.add(labels);
			return res;
		}
		
		boolean check = true;
		int pos = 0;
		while(true) {
			
			List<String> list = new ArrayList<String>();
			for(int i = pos; i < labels.size(); ++i){
				
				if(list.size() < 20000) {
					list.add(labels.get(i));
				}
				
				if(list.size() == 20000) {
					pos = i+1;
					res.add(list);
					break;
				}
				
				if(i == labels.size()-1) {
					check = false;
				}
				
			}

			if(!check) {
				break;
			}
		}
		
		//System.out.println("UUUU: "+res.get(0).size());
		return res;
	}
	
    public static void main( String... args ) throws Exception
    {
    	//final File folder = new File("/Users/Alessandro/Desktop/TESI/primoset");
    	//listFilesForFolder(folder);
    	 
    	
    	// If connect failed will throw a exception.
        HugeClient hugeClient = HugeClient.builder("http://localhost:8080",
                                                   "hugegraph")
                                          .build();

        //SchemaManager schema = hugeClient.schema();
        
        /*int inizio = 0;
    	int fine = 0;
    	String cypher = "MATCH (b:pos)-[:insertion]-(a:subg) where NOT a.clu_id STARTS WITH 00000000000000005565_ and  b.searchlabel IN [ale, matti, DiGio ]  return a.clu_id, a.num_aln";
    	for (int i= 0; i < cypher.length(); i++) {
    		if (cypher.charAt(i)=='[')
    			inizio = i+1;
    		
    		if(cypher.charAt(i)==']')
    			fine = i;
    		
    	}
    	
    	String sub = cypher.substring(inizio,fine-1);
    	List<String> labels = new ArrayList<String>(Arrays.asList(sub.split(", ")));*/
        /*
        schema.propertyKey("name").asText().ifNotExist().create();
        schema.propertyKey("age").asInt().ifNotExist().create();
        schema.propertyKey("city").asText().ifNotExist().create();
        schema.propertyKey("weight").asDouble().ifNotExist().create();
        schema.propertyKey("lang").asText().ifNotExist().create();
        schema.propertyKey("date").asDate().ifNotExist().create();
        schema.propertyKey("price").asInt().ifNotExist().create();

        schema.vertexLabel("person")
              .properties("name", "age", "city")
              .primaryKeys("name")
              .ifNotExist()
              .create();

        schema.vertexLabel("software")
              .properties("name", "lang", "price")
              .primaryKeys("name")
              .ifNotExist()
              .create();

        schema.indexLabel("personByCity")
              .onV("person")
              .by("city")
              .secondary()
              .ifNotExist()
              .create();

        schema.indexLabel("personByAgeAndCity")
              .onV("person")
              .by("age", "city")
              .secondary()
              .ifNotExist()
              .create();

        schema.indexLabel("softwareByPrice")
              .onV("software")
              .by("price")
              .range()
              .ifNotExist()
              .create();

        schema.edgeLabel("knows")
              .sourceLabel("person")
              .targetLabel("person")
              .properties("date", "weight")
              .ifNotExist()
              .create();

        schema.edgeLabel("created")
              .sourceLabel("person").targetLabel("software")
              .properties("date", "weight")
              .ifNotExist()
              .create();

        schema.indexLabel("createdByDate")
              .onE("created")
              .by("date")
              .secondary()
              .ifNotExist()
              .create();

        schema.indexLabel("createdByWeight")
              .onE("created")
              .by("weight")
              .range()
              .ifNotExist()
              .create();

        schema.indexLabel("knowsByWeight")
              .onE("knows")
              .by("weight")
              .range()
              .ifNotExist()
              .create();

        GraphManager graph = hugeClient.graph();
        Vertex marko = graph.addVertex(T.label, "person", "name", "marko",
                                       "age", 29, "city", "Beijing");
        Vertex vadas = graph.addVertex(T.label, "person", "name", "vadas",
                                       "age", 27, "city", "Hongkong");
        Vertex lop = graph.addVertex(T.label, "software", "name", "lop",
                                     "lang", "java", "price", 328);
        Vertex josh = graph.addVertex(T.label, "person", "name", "josh",
                                      "age", 32, "city", "Beijing");
        Vertex ripple = graph.addVertex(T.label, "software", "name", "ripple",
                                        "lang", "java", "price", 199);
        Vertex peter = graph.addVertex(T.label, "person", "name", "peter",
                                       "age", 35, "city", "Shanghai");

        marko.addEdge("knows", vadas, "date", "2016-01-10", "weight", 0.5);
        marko.addEdge("knows", josh, "date", "2013-02-20", "weight", 1.0);
        marko.addEdge("created", lop, "date", "2017-12-10", "weight", 0.4);
        josh.addEdge("created", lop, "date", "2009-11-11", "weight", 0.4);
        josh.addEdge("created", ripple, "date", "2017-12-10", "weight", 1.0);
        peter.addEdge("created", lop, "date", "2017-03-24", "weight", 0.2);
        
        schema.propertyKey("name").asText().ifNotExist().create();
        schema.propertyKey("prefix").asText().ifNotExist().create();
        schema.propertyKey("age").asInt().ifNotExist().create();
        schema.propertyKey("city").asText().ifNotExist().create();
        
        schema.vertexLabel("person")
        .properties("name", "prefix")
        .primaryKeys("name")
        .ifNotExist()
        .create();
        
        schema.edgeLabel("fratello")
        .sourceLabel("person")
        .targetLabel("person")
        .ifNotExist()
        .create();*/
                                          
        /*schema.vertexLabel("alien")
        .properties("name", "age")
        .primaryKeys("name")
        .ifNotExist()
        .create();*/
        
        //String prefix = "Ti";
        
        GremlinManager gremlin = hugeClient.gremlin();
        //ResultSet res = gremlin.gremlin("g.V().count()").execute();
        ResultSet res = gremlin.gremlin("g.E().hasLabel(\"gtris\").has(\"case\", \"1\").count()").execute();
        System.out.println(res.iterator().next().getObject());
        //ResultSet v1 = gremlin.gremlin("g.addV(\"person\").property(\"name\",\"Gio\").property(\"prefix\", \"Gi\")").execute();
        //Vertex v1_gio = v1.iterator().next().getVertex();
        
        //ResultSet v2 = gremlin.gremlin("g.addV(\"person\").property(\"name\",\"Ale\").property(\"prefix\", \"Al\")").execute();
        //Vertex v2_ale = v2.iterator().next().getVertex();
        
        //ResultSet v3 = gremlin.gremlin("g.addV(\"person\").property(\"name\",\"Tim\").property(\"prefix\", \"Ti\")").execute();
        //Vertex v3_tim = v3.iterator().next().getVertex();
        
        //v1_gio.addEdge("fratello", v2_ale);
        //v3_tim.addEdge("fratello", v2_ale);
        
        
        /*ResultSet qq = gremlin.gremlin("g.V().hasLabel(\"person\").has(\"name\",\"Ale\").as(\"a\").in(\"fratello\").not(has(\"prefix\", eq(\""+prefix+"\"))).as(\"b\")"
        								+ ".as(\"c\").select(\"c\", \"b\")"
        							   	  +"by(values(\"name\").fold())."
        							   	  +"by(values(\"prefix\").fold())").execute();*/
        
        //System.out.println(qq.iterator().next().getObject());
                                         
        /*Iterator<Result> q_res = qq.iterator();
        LinkedHashMap res = (LinkedHashMap) q_res.next().getObject();
    	List<String> r_cluid = (List<String>) res.get("a");
    	List<Integer> r_num_aln = (List<Integer>) res.get("b");
    	System.out.println(r_cluid);
    	System.out.println(r_num_aln);*/
        
        //hugeClient.close();
        /*System.out.println("==== Path ====");
        ResultSet resultSet = gremlin.gremlin("g.V().outE().path()").execute();
        Iterator<Result> results = resultSet.iterator();
        results.forEachRemaining(result -> {
            System.out.println(result.getObject().getClass());
            Object object = result.getObject();
            if (object instanceof Vertex) {
                System.out.println(((Vertex) object).id());
            } else if (object instanceof Edge) {
                System.out.println(((Edge) object).id());
            } else if (object instanceof Path) {
                List<Object> elements = ((Path) object).objects();
                elements.forEach(element -> {
                    System.out.println(element.getClass());
                    System.out.println(element);
                });
            } else {
                System.out.println(object);
            }
        });*/
        //"g.V().count()"
        //"g.V().hasLabel(\"persona\").has(\"name\", \"DiGio\")"
        //"g.addV(\"pp\").property(\"name\", \"DiGio\").property(\"age\", 12)"
        //"g.V().hasLabel(\"pp\").has(\"name\", within("+labels+")"
        //"g.V().hasLabel(\"pp\").has(\"name\", TextP.eq(\"DiGio\"))"
        
        //String lab = labels.stream().collect(Collectors.joining("\", \"", "\"", "\""));
        //List<String> labelss = new ArrayList<String>(Arrays.asList(lab.split(", ")));
        //System.out.println(labelss);
        //System.out.println("g.V().hasLabel(\"pp\").has(\"name\", within("+labels+"))");
        //ResultSet resultSet2 = gremlin.gremlin("g.V().hasLabel(\"pp\").not(__.has(\"name\", eq(\"ale\")))").execute();
        //ResultSet resultSet2 = gremlin.gremlin("g.E().count()").execute();
        //Iterator<Result> results2 = resultSet2.iterator();
        //System.out.println(results2.next().getObject());
        
        /*ResultSet query = gremlin.gremlin("g.V().drop().iterate()").execute();
        
        ResultSet qq = gremlin.gremlin("g.V().count()").execute();
        Iterator<Result> q_res = qq.iterator();
        System.out.println(q_res.next().getObject());
        
        ResultSet q2 = gremlin.gremlin("g.E().count()").execute();
        Iterator<Result> q_res2 = q2.iterator();
        System.out.println(q_res2.next().getObject());*/
        //Iterator<Result> q_res = query.iterator();
        //System.out.println(q_res.next().getObject());
        
        /*Vertex v1 =  results2.next().getVertex();
        
        ResultSet res1 = gremlin.gremlin("g.V().hasLabel(\"person\").has(\"name\", \"DiGio\")").execute();
        Iterator<Result> results1 = res1.iterator();
        Vertex v2 = results1.next().getVertex();
        
        schema.propertyKey("weight").asText().ifNotExist().create();
        schema.propertyKey("date").asText().ifNotExist().create();
        
        schema.edgeLabel("fratello")
        .sourceLabel("person")
        .targetLabel("person")
        .properties("date", "weight")
        .ifNotExist()
        .create();
        
        v1.addEdge("fratello", v2, "date", "20-03-2020", "weight", "5");
        v2.addEdge("fratello", v1, "date", "20-03-2020", "weight", "5");
        
        ResultSet query = gremlin.gremlin("g.E().count()").execute();
        Iterator<Result> q_res = query.iterator();
        System.out.println(q_res.next().getObject());*/
        //hugeClient.close();
        
 

        ////////////////// PARTE HUGEGRAPH PROVA /////////////////
    	/*HugeClient hugeClient = HugeClient.builder("http://localhost:8080",
                "hugegraph")
       .build();
    	
		SchemaManager schema = hugeClient.schema();
		
		schema.propertyKey("name").asText().ifNotExist().create();
		schema.propertyKey("age").asInt().ifNotExist().create();
		schema.propertyKey("lang").asText().ifNotExist().create();
		schema.propertyKey("date").asDate().ifNotExist().create();
		schema.propertyKey("price").asInt().ifNotExist().create();
		
		schema.vertexLabel("person")
		.properties("name", "age")
		.primaryKeys("name")
		.ifNotExist()
		.create();
		
		schema.vertexLabel("person")
		.properties("price")
		.nullableKeys("price")
		.append();
		
		schema.vertexLabel("software")
		.properties("name", "lang", "price")
		.primaryKeys("name")
		.ifNotExist()
		.create();
		
		schema.indexLabel("softwareByPrice")
		.onV("software").by("price")
		.range()
		.ifNotExist()
		.create();
		
		schema.edgeLabel("knows")
		.link("person", "person")
		.properties("date")
		.ifNotExist()
		.create();
		
		schema.edgeLabel("created")
		.link("person", "software")
		.properties("date")
		.ifNotExist()
		.create();
		
		schema.indexLabel("createdByDate")
		.onE("created").by("date")
		.secondary()
		.ifNotExist()
		.create();
		
		// get schema object by name
		System.out.println(schema.getPropertyKey("name"));
		System.out.println(schema.getVertexLabel("person"));
		System.out.println(schema.getEdgeLabel("knows"));
		System.out.println(schema.getIndexLabel("createdByDate"));
		
		// list all schema objects
		System.out.println(schema.getPropertyKeys());
		System.out.println(schema.getVertexLabels());
		System.out.println(schema.getEdgeLabels());
		System.out.println(schema.getIndexLabels());
		
		GraphManager graph = hugeClient.graph();
		
		Vertex marko = new Vertex("person").property("name", "marko")
		        .property("age", 29);
		Vertex vadas = new Vertex("person").property("name", "vadas")
		        .property("age", 27);
		Vertex lop = new Vertex("software").property("name", "lop")
		        .property("lang", "java")
		        .property("price", 328);
		Vertex josh = new Vertex("person").property("name", "josh")
		       .property("age", 32);
		Vertex ripple = new Vertex("software").property("name", "ripple")
		           .property("lang", "java")
		           .property("price", 199);
		Vertex peter = new Vertex("person").property("name", "peter")
		        .property("age", 35);
		
		Edge markoKnowsVadas = new Edge("knows").source(marko).target(vadas)
		             .property("date", "2016-01-10");
		Edge markoKnowsJosh = new Edge("knows").source(marko).target(josh)
		            .property("date", "2013-02-20");
		Edge markoCreateLop = new Edge("created").source(marko).target(lop)
		              .property("date",
		                        "2017-12-10");
		Edge joshCreateRipple = new Edge("created").source(josh).target(ripple)
		                .property("date",
		                          "2017-12-10");
		Edge joshCreateLop = new Edge("created").source(josh).target(lop)
		             .property("date", "2009-11-11");
		Edge peterCreateLop = new Edge("created").source(peter).target(lop)
		              .property("date",
		                        "2017-03-24");
		
		List<Vertex> vertices = new ArrayList<>();
		vertices.add(marko);
		vertices.add(vadas);
		vertices.add(lop);
		vertices.add(josh);
		vertices.add(ripple);
		vertices.add(peter);
		
		List<Edge> edges = new ArrayList<>();
		edges.add(markoKnowsVadas);
		edges.add(markoKnowsJosh);
		edges.add(markoCreateLop);
		edges.add(joshCreateRipple);
		edges.add(joshCreateLop);
		edges.add(peterCreateLop);
		
		vertices = graph.addVertices(vertices);
		vertices.forEach(vertex -> System.out.println(vertex));
		
		edges = graph.addEdges(edges, false);
		edges.forEach(edge -> System.out.println(edge));
		
		hugeClient.close();*/
    	
    	/////////////////// PARTE JANUSGRAPH ///////////////////
    	
    	//GraphTraversalSource g = traversal().withRemote("conf/remote-graph.properties");
    	//Object herculesAge = g.V().has("name", "mirko").values("age").next();
    	//System.out.println("Hercules is " + herculesAge + " years old.");
        //Object vertex_zeus = g.V().hasLabel("god").has("name", "zeus").next();
    	//Object vertex_pos = g.V().hasLabel("god").has("name", "poseidone").next();
    	//g.V(vertex_zeus).as("zeus").V(vertex_pos).as("poseidone").addE("fratello").from("zeus").to("poseidone").next();
    	
    	/*Object s = g.V().has("person","name","Gio").
	      fold().
	      coalesce((Traversal)__.unfold(),__.addV("person").property("name","Gio")).next();
    	
    	Object s2 = g.V().hasLabel("person").has("name", "Ale").
    		      fold().
    		      coalesce((Traversal)__.unfold(),__.addV("person").property("name","Ale")).next();
    	
    	
    	g.V(s).as("gio").V(s2).as("ale").
        coalesce(__.inE("fratello").where(__.outV().as("gio")),
        __.addE("fratello").property("carattere", "bravo").from("gio").to("ale")).next();*/
    	
    	/*Object q = g.V().hasLabel("person").has("name", TextP.startingWith("A")).as("a").in("fratello")
     		   .hasLabel("person").has("name", TextP.startingWith("G")).as("b").
     		   select("a", "b").
     		   	by(__.values("name").fold()).
     		   	by(__.values("name").fold()).next();
    	
    	System.out.println(q);
    	g.close();*/
    	/*Object s = g.V().hasLabel("person").has("name", TextP.startingWith("G")).next();
    	System.out.println(s);*/
    	
    	
    	/*int inizio = 0;
    	int fine = 0;
    	String cypher = "MATCH (b:pos)-[:insertion]-(a:subg) where NOT a.clu_id STARTS WITH 00000000000000005565_ and  b.searchlabel IN [ale, matti, Gio ]  return a.clu_id, a.num_aln";
    	for (int i= 0; i < cypher.length(); i++) {
    		if (cypher.charAt(i)=='[')
    			inizio = i+1;
    		
    		if(cypher.charAt(i)==']')
    			fine = i;
    		
    	}
    	
    	String sub = cypher.substring(inizio,fine-1);
    	List<String> labels = new ArrayList<String>(Arrays.asList(sub.split(", ")));
    	
    	//trovare un modo per salvare entrambi in una lista al momento 
    	// salva solo l'ultimo specificato
    	//System.out.println(labels);
        Object q = g.V().hasLabel("person").has("name", TextP.within(labels)).as("a").outE("fratello").as("l").inV()
        		   .hasLabel("person").has("name", TextP.startingWith("T")).as("b").
        		   select("a", "l").
        		   	by(__.values("name").fold()).
        		   	by(__.values("carattere").fold()).next();*/
    	//Object q = g.V().hasLabel("person").has("name", "Gio").outE("fratello").valueMap().next();
        		   
        //System.out.println(q);
        /*List<String> clu_id = (ArrayList<String>) ((LinkedHashMap) q).get("a");
        List<String> num_aln = (ArrayList<String>) ((LinkedHashMap) q).get("b");
        
        for (int i = 0; i < clu_id.size(); i++) {
        	System.out.println(clu_id.get(i));
        	System.out.println(num_aln.get(i));
        }*/
    	
    	/*List<String> names = new ArrayList<String>();
    	names.add("ale");
    	names.add("matti");
    	names.add("gio");
    	names.add("franco");
    	names.add("franco");
    	names.add("ale");
    	names.add("giovanni");
    	
    	for(String elem: names) {
    		Object q = g.V().hasLabel("person").has("name", elem).
		      fold().
		      coalesce((Traversal)__.unfold(),__.addV("person").property("name", elem)).next();
    	}
    	
    	GraphTraversal res = g.V().hasLabel("person").has("name");
    	
    	
    	while(res.hasNext()) {
    		System.out.println(res.next());
    	}
    	
        g.close();*/
    	/*try {
    		Object s = g.V().hasLabel("sample").has("UniqueID", "00000").next();
    	} catch (Exception ex) {
    		g.addV("sample").property("UniqueID", "00000").next();
    	}*/
    	
    	//System.out.println();
    	/*
    	Transaction tx = g.tx();

    	// spawn a GraphTraversalSource from the Transaction. Traversals spawned
    	// from gtx will be essentially be bound to tx
    	GraphTraversalSource gtx = tx.begin();
    	try {
    	    gtx.addE("fratello").
    	    gtx.close();
    	} catch (Exception ex) {
    	    tx.rollback();
    	}*/
    	
    	//Scanner s = new Scanner(new File());
    	/*BufferedReader br = new BufferedReader(new FileReader("/Users/Alessandro/Desktop/prova.txt"));
    	String line;
    	String elem;
    	int inizio;
    	int fine;
    	List<String> list = new ArrayList<String>();
    	line = br.readLine();
    	for(int i = 0; i < line.length(); i++){
    		if(line.charAt(i) == '"') {
    			inizio = i;
    			for (int j = i+1; j < line.length(); j++) {
    				if(line.charAt(j) == '"') {
    					fine=j;
    					elem = line.substring(inizio, fine);
    					i = j+1;
    					list.add(elem+"\"");
    					break;
    				}
    			}
    			
    		}
    		
    	}
    	    //list.add(elem);
    	//System.out.println("QUA: "+list.size());
    	ArrayList<List<String>> pp = getSplittedList(list);
    	for (List<String> l : pp) {
    		System.out.println(l);
    	}*/
    	//s.close();
    	/*List<String> list2 = new ArrayList<String>();
    	int cont = 0;
    	for (String e:list) {
    		list2.add(e);
    		cont+=1;
    		if(cont == 20000)
    			break;
    	}
    	String lista = list2.toString();
    	lista = lista.substring(1, lista.length()-1);
    	System.out.println(lista);*/
    	
    }
 }
