package routing;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.plaf.synth.Region;

//import org.hamcrest.Matcher;

//import DataHandler.*;
import report.MessageStatsReport;
import report.Report.*;
import routing.MaxPropRouter.combine;
import routing.MaxPropRouter.locs;
import routing.maxprop.MaxPropDijkstra;
import routing.maxprop.MeetingProbabilitySet;
import util.Tuple;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import core.*;
import core.ContactGraph.*;

public class CustomRouter extends ActiveRouter {

	public Set<String> ackedMessageIds;

	private static List<CustomRouter> allRouters;

	public static Map<combine, locs> updatedloc;

	public String loc1;
	public String loc2;
	
	/** The alpha parameter string*/
	public static final String ALPHA_S = "alpha";

	/** The alpha variable, default = 1;*/
	private double alpha;

	/** The default value for alpha */
	public static final double DEFAULT_ALPHA = 1.0;
	
//	private Map<Integer, Double> probs;
	/** the time when this MPS was last updated */
	private double lastUpdateTime;

    private int maxSetSize;

	private static StringBuilder getAll;

	private Map<DTNHost, Set<String>> sentMessages;

	public Map<DTNHost, List<Double>> trans;
	
	private static int probSetMaxSize;

//	public GenerateScenario scenario;
//	public Profile profile;
//	public BufferCustom buffer;
//	public HashMap<Integer, ConnectedNodeInfo> conNodesInfo;
	
	Map<DTNHost, List<DTNHost>> g = new HashMap<>();
	
	
	/** probabilities of meeting hosts */
	public MeetingProbabilitySet probs;
	/** meeting probabilities of all hosts from this host's point of view 
	 * mapped using host's network address */
	public Map<Integer, MeetingProbabilitySet> allProbs;
	/** the cost-to-node calculator */
	public MaxPropDijkstra dijkstra;	
	/** IDs of the messages that are known to have reached the final dst */
//	private Set<String> ackedMessageIds;
	/** mapping of the current costs for all messages. This should be set to
	 * null always when the costs should be updated (a host is met or a new
	 * message is received) */
	private Map<Integer, Double> costsForMessages;
	/** From host of the last cost calculation */
	private DTNHost lastCostFrom;
	
	
	public HashMap<Integer, List<locs>> RegionCoord= new HashMap<>();

	static {
		DTNSim.registerForReset(CustomRouter.class.getCanonicalName());
		reset();
	}

	public static void reset() {
		allRouters = new ArrayList<CustomRouter>();
	}

	public CustomRouter(Settings settings) 
	{
		super(settings);
		this.probSetMaxSize = 200;
//		Settings maxPropSettings = new Settings(MAXPROP_NS);		
//		if (maxPropSettings.contains(ALPHA_S)) {
//			alpha = maxPropSettings.getDouble(ALPHA_S);
//		} else {
//			alpha = DEFAULT_ALPHA;
//		}
//
//        Settings mpSettings = new Settings(MAXPROP_NS);
//        if (mpSettings.contains(PROB_SET_MAX_SIZE_S)) {
//            probSetMaxSize = mpSettings.getInt(PROB_SET_MAX_SIZE_S);
//        } else {
//            probSetMaxSize = DEFAULT_PROB_SET_MAX_SIZE;
//        }
	}

	@Override
	public CustomRouter replicate() {
		return new CustomRouter(this);
	}

	public CustomRouter(CustomRouter c) {
		super(c);
		this.ackedMessageIds = new HashSet<String>();
		this.sentMessages = new HashMap<DTNHost, Set<String>>();
		this.probs = new MeetingProbabilitySet(probSetMaxSize, this.alpha);
		this.allProbs = new HashMap<Integer, MeetingProbabilitySet>();
		this.dijkstra = new MaxPropDijkstra(this.allProbs);

	}

	public void init(DTNHost host, List<MessageListener> mListeners) 
	{
		super.init(host, mListeners);
		
		
		String filePath = "/usr/local/home/aghnw/Downloads/the-one-master/outputPillarInit.txt";
		
		try {
            // Create a FileWriter to write to the file
            FileWriter fileWriter = new FileWriter(filePath, true);

            // Create a BufferedWriter for efficient writing
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Data to write to the file
//            String data = "Hello, world!\nThis is some data written to a file.";
            StringBuilder dataBuild = new StringBuilder();
            
            if(host.name.contains("p"))
    			dataBuild.append("Pillar "+ host.name +"was initialized\n");
            
            String data = dataBuild.toString();

            // Write the data to the file
            bufferedWriter.write(data);

            // Close the BufferedWriter and FileWriter to release resources
            bufferedWriter.close();
            fileWriter.close();

            System.out.println("Data has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		

		if (this.getHost().getAddress() == 0) {
//			GenerateScenario.Reset();
		}

//		scenario = GenerateScenario.GetInstance();
//		profile = new Profile(scenario, this);
//		buffer = new BufferCustom(this);
//		conNodesInfo = new HashMap<Integer, ConnectedNodeInfo>();
		System.out.println("number is"+host.noofgroups);
		
		
		this.probs = new MeetingProbabilitySet(probSetMaxSize, this.alpha);
		this.allProbs = new HashMap<Integer, MeetingProbabilitySet>();
		host.probs = this.probs;
		host.allProbs = this.allProbs;
		
		for(int i=1;i<=host.noofgroups;i++)
		{
			ArrayList<locs> al = new ArrayList<>();
//			locs l1 = new locs(754*(i-1), 700*(i*1));
//			locs l1 = new locs(754*(i-1), 700*(i*1));
//			locs l2 = new locs(754*i, 700*i);
//			al.add(l1);
//			al.add(l2);
			System.out.println("region is"+RegionCoord);
			RegionCoord.put(i, al);
		}
		
		
		
//		if(host.noofgroups
	}

//	@Override
//	public MessageRouter replicate() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
	 
	@Override
	public void update() 
	{
//		super.update();
		
//		get the data and then form a dataframe by which the information can be sent to the model.
		super.update();
		if (SimClock.getTime() % 5 == 0) 
		{
			createInformation(this.getHost());
		}
		
		DTNHost host = this.getHost();
			
//		this.buffer.deleteExpired();
//		current region of the host
		
		
		
//		if (host.name.indexOf('p') != -1) 
//		{
//			int region = getRegion(host);
//			host.regionP = region;
//
//			return;
//		}
//		
		updateRegionVals(host.getLocation(), host);

		// if (SimClock.getTime() % 900 == 0) 
		// {


// 			HttpURLConnection conn = null;
// 			DataOutputStream os = null;
// 			try {
// 				URL url = new URL("http://127.0.0.1:8000/add/"); // important to add the trailing slash after add

// 				Object inputData[] = host.p.toArray();
// 				StringBuilder sendi = new StringBuilder();
// 				sendi.append("{\"x\":\"");
// 				for (Object e : inputData) {
					
// 					sendi.append(e.toString());
// 					sendi.append("b");

// 				}
// //	            
// 				sendi.append("\",\"y\":\"" + host.name + "\"}");
// //	            sendi.append("\"}");

// 				host.p.removeAll(host.p);

// //	            System.out.println("sendi is"+sendi);

// //	            for(Object input1: inputData)
// //	            {
// //            	o input = inputData.toString();
// 				byte[] postData = sendi.toString().getBytes(StandardCharsets.UTF_8);
// 				conn = (HttpURLConnection) url.openConnection();
// 				conn.setDoOutput(true);
// 				conn.setRequestMethod("POST");
// 				conn.setRequestProperty("Content-Type", "application/json");
// 				conn.setRequestProperty("charset", "utf-8");
// //                conn.setRequestProperty("Content-Length", Integer.toString(input.length()));
// 				os = new DataOutputStream(conn.getOutputStream());
// 				os.write(postData);
// 				os.flush();

// 				if (conn.getResponseCode() != 200) 
// 				{
// 					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
// 				}

// 				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

// 				String output;
// //                StringBuilder getAll = new StringBuilder();
// //                System.out.println("Output from Server .... \n");

// ////                while()
// //                output = br.readLine();
// //                System.out.println(output);
// 				StringBuilder res = new StringBuilder();
// 				Map<String, String> makeprob = new HashMap<>();
// 				while ((output = br.readLine()) != null) {
// //                	getAll.append(output);
// 					res.append(output);
// //                	System.out.println()
// //					System.out.println(output);
// 				}
// //                System.out.println("changed"+ res.toString());

// 				String res1 = res.toString();
// //				System.out.println("res is" + res1);
// 				res1 = res1.replace("\\", "");
// //				System.out.println("res is" + res1);

		        
// 				Pattern p1 = Pattern.compile("(\\\"\\d+-\\d+\\\",\\d+)");
// 				Pattern p2 = Pattern.compile("(\\d+\\.\\d+)");
// 				Pattern p3 = Pattern.compile("([a-z]\\d+)");
// //				Pattern p4 = Pattern.compile("([\\d+-\\d+,\\d+])");
				
				
// 				Matcher m1 = p1.matcher(res1);
// 				Matcher m2 = p2.matcher(res1);
// 				Matcher m3 = p3.matcher(res1);
// //				Matcher m4 = p4.matcher(res1);
				
// 				List<timeVals> all_times = new ArrayList<>();
// 				List<Double> all_probs = new ArrayList<>();
// 				List<String> locs_from = new ArrayList<>();
// 				List<String> locs_to = new ArrayList<>();
				
				
// 				while(m1.find())
// 				{
// //					System.out.println("came here");
// 					String st = m1.group(0);
// //					st.replace("\"", "");
// 					String s1[] = st.split(",");
// //					System.out.println(s1[0].replace("\"", ""));
// 					timeVals v = new timeVals(s1[0].replace("\"", ""), s1[1], (gettime_order()+1));
// //					System.out.println("came here" + v.time);
// 					all_times.add(v);
					
// 				}
				
// 				while(m2.find())
// 				{
// //					System.out.println("came here");
// //					System.out.println("came here" + m2.group(0));
// 					all_probs.add(Double.parseDouble(m2.group(0)));
// 				}
// 				int idx = 0;
// 				while(m3.find())
// 				{
// //					System.out.println("came here");
// //					System.out.println("group count is" + m3.groupCount());
// //					System.out.println("came here" + m3.group());
// //					System.out.println("came here" + m3.group(0));
// 					if(idx%2 == 0)
// 					{
// 						locs_from.add(m3.group(0));
// 						idx+=1;
// 					}
// 					else
// 					{
// 						locs_to.add(m3.group(0));
// 						idx+=1;
// 					}
// 				}
				
		
			
// //				System.exit(0);
// 				String time_s = "time";
// 				String probs = "probs";
// 				List<Integer> all_time_idx = new ArrayList<>();
				
				
// 				List<Integer> all_probs_idx = new ArrayList<>();
// 				int indexp = res1.indexOf(probs);
// 				int indext = res1.indexOf(time_s);
			
				
// //				System.out.println(indext + " " + indexp);
				
// 				String getSplitSemi[] = res1.split(",");
				
// //				"{"time":{"0":"9-10"},"probs":{"0":[0.5,["p29","p10"]]}}"

// 				for (int i = 0; i < all_times.size(); i++) 
// //				{
// //					
// //					if(i > getSplitSemi.length)
// //						break;
// //					
// //					String locationsPart = getSplitSemi[i];
// //					
// //					
// ////					while (indext >= 0) 
// ////					{
// ////						all_time_idx.add(indext);
// ////						System.out.println(indext);
// ////					    indext = res1.indexOf(time_s, indext + 1);
// ////					    
// ////					}
// ////					
// ////					while (indexp >= 0) 
// ////					{
// ////						all_probs_idx.add(indexp);
// ////						System.out.println(indexp);
// ////					    indexp = res1.indexOf(probs, indexp + 1);
// ////					    
// ////					}
// //					
// ////					System.out.println("locations part is"+locationsPart);
// ////					
// ////					int first_idx = all_time_idx.get(0);
// ////					first_idx = first_idx+11;
// ////					String time_res = getSplitSemi[i].substring(first_idx, first_idx+6);
// ////					System.out.println(time_this.ackedMessageIds = new HashSet<String>();res);
// //					
// ////					System.exit(0);
// //					
// ////					String splitByComma[] = new String[10];
// //
// ////					String probString = splitByComma[2];
// //					
// ////					String fromString = getSplitSemi[i+1];
// ////					String toString = "";
// //					
// //
// ////					int index1 = fromString.lastIndexOf("[");
// ////					int index2 = toString.indexOf("]");
// //
// //					String res_csv1 = locs_from.get(i);
// //					String res_csv2 = locs_to.get(i);
// //
// //					System.out.println("loc1 is" + res_csv1);
// //
// //					System.out.println("loc2 is" + res_csv2);
// //
// //					locs l1 = new locs(res_csv1, res_csv2);
// //
// ////					if (i + 1 > getSplitSemi.length)
// ////						break;
// //
// ////					String timingPart = getSplitSemi[i + 1];
// //
// ////					int first = timingPart.indexOf("\"");
// ////					int second = timingPart.indexOf("\"", first + 1);
// //
// ////					String time = timingPart.substring(first + 1, second);
// //					timeVals time = all_times.get(i);
// //					
// //					System.out.println("all time is"+ all_times.size());
// //					System.out.println("all probs size is" + all_probs.size());
// //					System.out.println("all probs size is" + locs_from.size());
// //					System.out.println("all probs size is" + locs_to.size());
// //
// ////					combine c1 = new combine(host, time, all_probs.get(i));
// //					
// ////					System.out.println(c1.host +" " + c1.time);
// //
// ////					System.out.println("I got the time right" + time);
// //
// //					if (!host.updatedloc.containsKey(c1)) 
// //					{
// //						int updated = 0;
// //						for(Map.Entry<combine, locs> ent: host.updatedloc.entrySet())
// //						{
// //							combine c = ent.getKey();
// //							if(c.host.equals(c1.host) && c.time.time.equals(c1.time.time) && c.time.val.equals(c1.time.val))
// //							{
// //								host.updatedloc.remove(ent.getKey(), ent.getValue());
// //								host.updatedloc.put(c1, l1);
// //								updated = 1;
// //								break;
// //							}
// //						}
// //						
// //						if(updated == 0)
// //							host.updatedloc.put(c1, l1);
// //					} 
// //					else 
// //					{
// //						host.updatedloc.remove(c1);
// //						host.updatedloc.put(c1, l1);
// //					}
// //
// //				}

// //                int index1 = res1.indexOf("[");
// //                int index2 = res1.indexOf("]");
// //                
// //                int comma_index = res1.lastIndexOf(",");
// //                String afterComma = res1.substring(comma_index);
// //                String vals_time[] = afterComma.split(":");
// //                System.out.println("vals time is" + vals_time[1]);
// //                
// //                String s1 = res1.substring(index1, index2); 
// //                String csv[] = s1.split(",");
// //                int index_csv1 = csv[1].indexOf("\"");
// //                int index_csv2 = csv[1].lastIndexOf("\"");
// //                
// //                int index_csv3 = csv[2].indexOf("\"");
// //                int index_csv4 = csv[2].lastIndexOf("\"");
// //                
// //                String res_csv1 = csv[1].substring(index_csv1+1, index_csv2);
// //                
// //                String res_csv2 = csv[2].substring(index_csv3+1, index_csv4);
// //                
// //                System.out.println(index_csv1+ " "+ index_csv2);
// //                
// //                System.out.println(index_csv3+ " "+ index_csv4);
// //                
// //                
// //                System.out.println("res csv is " + res_csv1);
// //                System.out.println("res csv is " + res_csv2);
// //                
// ////                List<combine> lst = new ArrayList<>();
// //                combine c1 = new combine(host, vals_time[1]);
// ////                lst.add(c1);
// //                
// //                locs l1 = new locs(res_csv1, res_csv2);
// //                
// //                if(!host.updatedloc.containsKey(c1))
// //                {
// //                	host.updatedloc.put(c1, l1);
// //                }
// //                else
// //                {
// //                	host.updatedloc.remove(c1);
// //                	host.updatedloc.put(c1, l1);
// //                }

// //                
// //                if(this.updatedloc.containsKey(host))
// //                {
// //                	this.updatedloc.put(host, res_csv1);
// //                }
// //                

// //                System.out.println("val is "+ csv[1]);

// //                con.world
// //				DTNHost h = this.getHost();
// //				World w = h.world;
// //				
// //				System.out.println("Working for "+ h.name);

// //				Map<String, Coord> mp = new HashMap<>();

// //                for(DTNHost dtn:w.getHosts())
// //                {
// //                	String dtn_name = dtn.name;
// //                	if(dtn_name.contains("p"))
// //					{
// //						mp.put(dtn_name, dtn.getLocation());
// //					}
// //                }
				
// //				getRegion();
// //                

// //                System.out.println("res is" + res.get ('0'));
// 				conn.disconnect();
// //                res is{  "0": {    "probs": [      0.5,      [        "K",        "H"      ]    ],    "time": "9-10"  }}

// //			send information to the model and predict for the next hour

// 			} catch (MalformedURLException e) {
// 				e.printStackTrace();
// 			} catch (IOException io) {

// 			}

// 			finally {
// 				if (conn != null) {
// 					conn.disconnect();
// 				}
// 			}
// 		}

		
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		// Try first the messages that can be delivered to final recipient
//		synchronized (this) {
			
		
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}
//		}

		// then try any/all message to any/all connection
		
		///////////////////////////////
//		tryOtherPlaces(host);
		///////////////////////////////
		
		tryOtherMessages();	
		
//		this.tryAllMessagesToAllConnections();

	}
	
	private Tuple<Message, Connection> tryOtherMessages() 
	{
		List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>(); 
	
		Collection<Message> msgCollection = getMessageCollection();
		
//		List<Message> lstm = this.getHost().buffer.allList;
		
//		if(lstm!=null)
//			msgCollection.addAll(lstm);
		
		/* for all connected hosts that are not transferring at the moment,
		 * collect all the messages that could be sent */
		for (Connection con : getConnections()) 
		{
			DTNHost other = con.getOtherNode(getHost());
			
//			List<Message> lstm_other = other.buffer.allList;
			
			CustomRouter othRouter = (CustomRouter)other.getRouter();
			Set<String> sentMsgIds = this.sentMessages.get(other);
			
			if (othRouter.isTransferring()) {
				continue; // skip hosts that are transferring
			}
			
//			HashMap<Message, Double> probMap = new HashMap<>();
//		    HashMap<Message, Integer> hopMap = new HashMap<>();
//		    HashMap<Message, Integer> ttlMap = new HashMap<>();
//		    HashMap<Message, Double> graphProbMap = new HashMap<>();
			
			for (Message m : msgCollection) 
			{
				/* skip messages that the other host has or that have
				 * passed the other host */
				if (othRouter.hasMessage(m.getId()) ||
						m.getHops().contains(other)) {
					continue; 
				}
				/* skip message if this host has already sent it to the other
				   host (regardless of if the other host still has it) */
				if (sentMsgIds != null && sentMsgIds.contains(m.getId())) {
					continue;
				}
				
				con.hopMap.put(m, m.getHopCount());
				con.ttlMap.put(m, m.getTtl());
				HashMap<Message, List<Contact>> hm = (HashMap)con.getOtherNode(other).res;
				List<Contact> lst = hm.get(m);
				double sum_prob = 0.0;
				double otherProb = 0.0;
				
				if(lst!=null)
				{	for(Contact each:lst)
					{
						sum_prob+= each.predict_prob;
						if(each.host1.equals(getHost()) && each.host2.equals(other))
							otherProb = each.predict_prob;
						if(each.host1.equals(other) && each.host2.equals(getHost()))
							otherProb = each.predict_prob;
					}
				}
				
				else
				{
					sum_prob = 1.0;
					otherProb = 1.0;
				}
				
				con.probMap.put(m, otherProb);
				con.graphProbMap.put(m, sum_prob);
//				if(messages.size() > 30)
//					break;
				
//				if(m.getTtl() < 15)
//					continue;
				
						    
//				if(m.path_Contact)
				
				/* message was a good candidate for sending */
				messages.add(new Tuple<Message, Connection>(m,con));
				
				
			}	
			
		}
		
		if (messages.size() == 0) {
			return null;
		}
		
		
		
		/* sort the message-connection tuples according to the criteria
		 * defined in MaxPropTupleComparator */ 

		System.out.println("messages size is"+messages.size());
//		System.exit(0);
//		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
//		if(messages.size() > 2)
//			Collections.sort(messages, new CustomMetricComparator());
		
		return tryMessagesForConnected(messages);	
	}
	

	
	public class CustomMetricComparator implements Comparator<Tuple<Message, Connection>> 
	{
		

	    public CustomMetricComparator() {}

	    @Override
	    public int compare(Tuple<Message, Connection> tuple1, Tuple<Message, Connection> tuple2) 
	    {
	    	if(tuple1.getValue().hopMap.get(tuple1.getKey()) == 0 || (tuple1.getValue().ttlMap.get(tuple1.getKey()) == 0))
	    			return 0;
	    	
	    	if(tuple1.getValue().probMap.get(tuple1.getKey()) == 0)
	    		return 0;
	    	
	    	if(tuple2.getValue().probMap.get(tuple2.getKey()) == 0)
	    		return 0;
	    	
	    	if(tuple2.getValue().graphProbMap.get(tuple2.getKey()) == 0)
	    		return 0;
	    	
	    	if(tuple1.getValue().graphProbMap.get(tuple1.getKey()) == 0)
	    		return 0;
	    	
	    	if(tuple2.getValue().hopMap.get(tuple2.getKey()) == 0 || (tuple2.getValue().ttlMap.get(tuple2.getKey()) == 0))
    			return 0;
	    	
	    	double metric1 = tuple1.getValue().probMap.get(tuple1.getKey()) * tuple1.getValue().graphProbMap.get(tuple1.getKey()) 
	    			* (tuple1.getValue().hopMap.get(tuple1.getKey()) / (tuple1.getValue().ttlMap.get(tuple1.getKey())));
	    	
	    	
	        double metric2 = tuple2.getValue().probMap.get(tuple2.getKey()) * tuple2.getValue().graphProbMap.get(tuple2.getKey()) 
	        		* (tuple2.getValue().hopMap.get(tuple2.getKey()) / (tuple2.getValue().ttlMap.get(tuple2.getKey())));
	        
	        System.out.print("metric 1 is " + metric1);
	        System.out.print("metric 2 is " + metric2);

	        
	        // Sort messages based on the custom metric, in descending order
	        if (metric1 > metric2) {
	            return 1;
	        } else if (metric1 < metric2) {
	            return -1;
	        } else {
	            return 0;
	        }
	        
//	        for(Contact c: m.path_Contact)
//			{
//				if(c.host1 == other)
//					messages.add(new Tuple<Message, Connection>(m,con));
//			}
	        // First, compare based on the number of hops
//	        int hopDiff = hopMap.get(m1) - hopMap.get(m2);
//	        if (hopDiff != 0) {
//	            return hopDiff;
//	        }
//
//	        // If the number of hops is the same, compare based on the TTL
//	        int ttlDiff = ttlMap.get(m1) - ttlMap.get(m2);
//	        if (ttlDiff != 0) {
//	            return ttlDiff;
//	        }
//
//	        // If the TTL is the same, compare based on the predicted probability
//	        double probDiff = probMap.get(m1) - probMap.get(m2);
//	        if (probDiff != 0) {
//	            return Double.compare(probDiff, 0);
//	        }
//
//	        // If the predicted probability is also the same, compare based on the sum of probabilities of the contact graph
//	        double graphProbDiff = graphProbMap.get(m1) - graphProbMap.get(m2);
//	        return Double.compare(graphProbDiff, 0);
	    }
	}
	
	protected Tuple<Message, Connection> tryMessagesForConnected(List<Tuple<Message, Connection>> tuples) 
	{
		List<Message> msgs = new ArrayList<>();
		if (tuples.size() == 0) {
			return null;
		}
		
		

		for(Tuple<Message, Connection> t: tuples)
		{
			msgs.add(t.getKey());
		}
		
		Comparator<Message> transferComparator = new Comparator<Message>() 
		{
			public int compare(Message m1, Message m2) {
				double pred1 = 0.0;
				
				System.out.println(m1.path_Contact);
//				if(m1.path_Contact == null)
//					c.pre = 0.0;
				
				if(m1.path_Contact == null)
					return 0;
				else
				{
					for(Contact c:m1.path_Contact)
					{
						if(m1.path_Contact == null)
							continue;
						pred1+=c.predict_prob;
					}
				}
				
				double pred2 = 0.0;
				if(m2.path_Contact == null)
					return 0;
				else
				{
					for(Contact c:m2.path_Contact)
					{
						if(m2.path_Contact == null)
							continue;
						pred2+=c.predict_prob;
					}
				}
				
				double val1 = m1.getTtl() + pred1;
				double val2 = m2.getTtl() + pred2; 
				return (int)(val1 - val2);
			}
		};
		
		
//		Collections.sort(msgs, transferComparator);
		
		
		
		for (Message m : msgs) 
		{
			m = m.replicate();
			m.replicationFactor--;
			if(m.getHopCount() > 15)
				continue;

			for(Tuple<Message, Connection> t : tuples)
			{
				Message mK = t.getKey();
				Connection con = t.getValue();
				if(mK == m)
				{
					if (startTransfer(m, con) == RCV_OK) {
						return t;
					}
					
				}
			}
		}
		
		
		return null;
	}

	
	 @Override
	protected Message getNextMessageToRemove(boolean excludeMsgBeingSent) {
		Collection<Message> messages = this.getMessageCollection();
		List<Message> validMessages = new ArrayList<Message>();

		for (Message m : messages) {	
			if (excludeMsgBeingSent && isSending(m.getId())) {
				continue; // skip the message(s) that router is sending
			}
			validMessages.add(m);
		}
		
		Collections.sort(validMessages, new CustomRouterComparator(this.calcThreshold())); 
		
		
		return validMessages.get(validMessages.size()-1); // return last message
	}
	 
		public int calcThreshold() 
		{
			/* b, x and p refer to respective variables in the paper's equations */
			int b = (int)this.getBufferSize();
			int x = 0;
			int p;

			if (x == 0) {
				/* can't calc the threshold because there's no transfer data */
				return 0;
			}
			
			/* calculates the portion (bytes) of the buffer selected for priority */
			if (x < b/2) {
				p = x;
			}
			else if (b/2 <= x && x < b) {
				p = Math.min(x, b-x);
			}
			else {
				return 0; // no need for the threshold 
			}
			
			/* creates a copy of the messages list, sorted by hop count */
			ArrayList<Message> msgs = new ArrayList<Message>();
			msgs.addAll(getMessageCollection());
			if (msgs.size() == 0) {
				return 0; // no messages -> no need for threshold
			}
			/* anonymous comparator class for hop count comparison */
			Comparator<Message> hopCountComparator = new Comparator<Message>() {
				public int compare(Message m1, Message m2) {
					return m1.getHopCount() - m2.getHopCount();
				}
			};
			
			Collections.sort(msgs, hopCountComparator);

			/* finds the first message that is beyond the calculated portion */
			int i=0;
			for (int n=msgs.size(); i<n && p>0; i++) {
				p -= msgs.get(i).getSize();
			}
			
			i--; // the last round moved i one index too far 
			if (i < 0) {
				return 0;
			}
			
			/* now i points to the first packet that exceeds portion p; 
			 * the threshold is that packet's hop count + 1 (so that packet and
			 * perhaps some more are included in the priority part) */
			return msgs.get(i).getHopCount() + 1;
		}
	
	 private class CustomRouterComparator implements Comparator<Message> {
			private int threshold;
			private DTNHost from1;
			private DTNHost from2;
			
			/**
			 * Constructor. Assumes that the host where all the costs are calculated
			 * from is this router's host.
			 * @param treshold Messages with the hop count smaller than this
			 * value are transferred first (and ordered by the hop count)
			 */
			public CustomRouterComparator(int treshold) {
				this.threshold = treshold;
				this.from1 = this.from2 = getHost();
			}

			/**
			 * Constructor. 
			 * @param treshold Messages with the hop count smaller than this
			 * value are transferred first (and ordered by the hop count)
			 * @param from1 The host where the cost of msg1 is calculated from
			 * @param from2 The host where the cost of msg2 is calculated from 
			 */
			public CustomRouterComparator(int treshold, DTNHost from1, DTNHost from2) {
				this.threshold = treshold;
				this.from1 = from1;
				this.from2 = from2;
			}
	 
	
	
	private class CustomRouteTupleComparator implements Comparator<Message> 
	{
		private int threshold;
		private DTNHost from1;
		private DTNHost from2;
		
		/**
		 * Constructor. Assumes that the host where all the costs are calculated
		 * from is this router's host.
		 * @param treshold Messages with the hop count smaller than this
		 * value are transferred first (and ordered by the hop count)
		 */
		public CustomRouteTupleComparator() {
			this.threshold = threshold;
			this.from1 = this.from2 = getHost();
		}

		@Override
		public int compare(Message arg0, Message arg1) {
			// TODO Auto-generated method stub
			return 0;
		}
	}



	@Override
	public int compare(Message arg0, Message arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

		/**
		 * Constructor. 
		 * @param treshold Messages with the hop count smaller than this
		 * value are transferred first (and ordered by the hop count)
		 * @param from1 The host where the cost of msg1 is calculated from
		 * @param from2 The host where the cost of msg2 is calculated from 
		 */
//		public MaxPropComparator(int treshold, DTNHost from1, DTNHost from2) {
//			this.threshold = treshold;
//			this.from1 = from1;
//			this.from2 = from2;
//		}
	}
	
//	void tryOtherPlaces(DTNHost host)
//	{
//		//Check if any of the connected DTN nodes, which are not transferring can send the messages to the other nodes. If possible, send them the messages.
//		
//		for(Connection c:getConnections())
//		{
//			if(host.name.contains("p") && c.getOtherNode(host).name.contains("p"))
//			{
//				host.pillars.add(c.getOtherNode(host));
//				c.getOtherNode(host).pillars.add(host);
//				
//				if(host.connectedPillars.containsKey(c.getOtherNode(host)))
//				{
//					List<DTNHost> devices = host.connectedPillars.get(host);
//					devices.add(c.getOtherNode(host));
//					host.connectedPillars.put(host, devices);
//				}
//				else
//				{
//					List<DTNHost> devices = new ArrayList<>();
//					devices.add(c.getOtherNode(host));
//					host.connectedPillars.put(host, devices);
//				}
//			}
//			
//		}
//	}
	
	public double gettime_order()
	{
	   double order_number = 0;
	   double time = SimClock.getTime();
	   double value = time/3600;
	   order_number = Math.floor(value/900);
	   
	   return order_number;
	   
	}
	
	public class timeVals
	{
		public String time;
		public String val;
		public double time_order;
		public timeVals(String time, String val, double d) 
		{
			// TODO Auto-generated constructor stub
			this.time = time;
			this.val = val;
			this.time_order = d;
		}
	}
	
	public void updateRegionVals(Coord currlocation, DTNHost host)
	{


		
		if ((currlocation.getX() > 0 && currlocation.getX() <= 1508) && (currlocation.getY() > 0 && currlocation.getY() <= 1400))
		{
			double val = host.regionProb.get(1);
			val+=0.25;
			host.regionProb.put(1, val);
//			host.region1 += 0.125;
		}
			
		if ((currlocation.getX() > 1508 && currlocation.getX() <= 3016) && (currlocation.getY() > 1400 && currlocation.getY() <= 2800))
		{
			double val = host.regionProb.get(2);
			val+=0.25;
			host.regionProb.put(2, val);
//			host.region2 += 0.25;
		}
			
		if ((currlocation.getX() > 3016 && currlocation.getX() <= 4524) && (currlocation.getY() > 2800 && currlocation.getY() <= 4200))
		{
			double val = host.regionProb.get(3);
			val+=0.25;
			host.regionProb.put(3, val);
//			host.region3 += 0.25;
		}
			
		if ((currlocation.getX() > 4524) && (currlocation.getY() > 4200))
		{
			double val = host.regionProb.get(4);
			val+=0.25;
			host.regionProb.put(4, val);
//			host.region4 += 0.25;
		}

	}

//	public class combine {
//		public DTNHost host;
//		public timeVals time;
//		public Double prob;
//
//		combine(DTNHost host, timeVals time, Double prob) {
//			this.host = host;
//			this.time = time;
//			this.prob = prob;
//		}
//
//	}
//
//	public class locs {
//		double loc1;
//		double loc2;
//		
//		public String loc1_s;
//		public String loc2_s;
////		public String locs_2;
//
//		locs(double loc1, double loc2) 
//		{
//			this.loc1 = loc1;
//			this.loc2 = loc2;
//		}
//		
//		locs(String loc1_s, String locs2_s)
//		{
//			this.loc1_s = loc1_s;
//			this.loc2_s = locs2_s;
//		}
//	}

//	@Override
//	public Message messageTransferred(String id, DTNHost from) {
//		Message m = super.messageTransferred(id, from);
//
//		HashMap updated_loc = (HashMap) from.updatedloc;
//
//		System.out.println("message received");
//
//		Set s = updated_loc.keySet();
//
////		from.
//
//		return m.getRequest();
//	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
//		this.costsForMessages = null; // new message -> invalidate costs
		Message m = super.messageTransferred(id, from);
		/* was this node the final recipient of the message? */
		if (isDeliveredMessage(m)) 
		{
			String filePath = "/usr/local/home/aghnw/Downloads/the-one-1.6.0/outputInterStats.txt";
			
			try {
	            // Create a FileWriter to write to the file
	            FileWriter fileWriter = new FileWriter(filePath, true);

	            // Create a BufferedWriter for efficient writing
	            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

	            // Data to write to the file
//	            String data = "Hello, world!\nThis is some data written to a file.";
	            StringBuilder dataBuild = new StringBuilder();
	            
	            dataBuild.append("The message orginated from "+m.getFrom() +" \n");
	            dataBuild.append("The message was destined to"+m.getTo()+" \n");
	            dataBuild.append("Id of message is "+m.getId() + " \n");
	            dataBuild.append("HopCount of the message is" + m.getHopCount() +" \n");
	            dataBuild.append("Message was delivered at" + m.getReceiveTime()+" \n");
	            MessageStatsReport msr = new MessageStatsReport();
//	            dataBuild.append("Delivered till now is" + msr.nrofDelivered +"created till now is " + msr.nrofCreated +" \n");
	            
	            String data = dataBuild.toString();

	            // Write the data to the file
	            bufferedWriter.write(data);

	            // Close the BufferedWriter and FileWriter to release resources
	            bufferedWriter.close();
	            fileWriter.close();

	            System.out.println("Data has been written to the file.");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
			this.ackedMessageIds.add(id);
//			this.buffer.allList.remove(m);
		}
		
		DTNHost host = getHost();
//		if(this.buffer.remainingStorage < m.getSize())
//		{
//			this.buffer.updateTheList(m);
//		}
//		this.buffer.allList.add(m);
//		this.buffer.remainingStorage -= m.getSize();
		
		return m;
	}	

	
//	@Override
//	protected void transferDone(Connection con) {
//		System.out.println("information is being sent");
//		Message m = con.getMessage();
//		String id = m.getId();
//		DTNHost recipient = con.getOtherNode(getHost());
//
//		DTNHost other = m.getTo();
//
////		other.region1 = 
//	}
	
	@Override
	protected void transferDone(Connection con) 
	{
		Message m = con.getMessage();
		String id = m.getId();
		DTNHost recipient = con.getOtherNode(getHost());
		Set<String> sentMsgIds = this.sentMessages.get(recipient);
		
		/* was the message delivered to the final recipient? */
		if (m.getTo() == recipient) { 
			this.ackedMessageIds.add(m.getId()); // yes, add to ACKed messages
			this.deleteMessage(m.getId(), false); // delete from buffer
			
////////////////////////////////////////////////////////////////
//			this.buffer.allList.remove(m);
		}
		
		/* update the map of where each message is already sent */
		if (sentMsgIds == null) {
			sentMsgIds = new HashSet<String>();
			this.sentMessages.put(recipient, sentMsgIds);
		}		
		sentMsgIds.add(id);
	}


	public void createInformation(DTNHost dtnHost) 
	{

//		msg.
//		DTNHost currHost = dtnHost 
		double currSpeed = dtnHost.getSpeed();
		Coord currlocation = dtnHost.getLocation();
		Coord toLocation = dtnHost.getDestination();
		double currTime = SimClock.getTime();
		String time = "";

		if (toLocation == null)
			toLocation = currlocation;

		System.out.println(currSpeed + " " + currlocation + " " + toLocation + " " + currTime);
//		
//		if(exchangeDeliverableMessages() != null) 
//		{
//			return;
//		}
		

		time = getTime((int) currTime);
//		if(currTime >=0.0 && currTime <= 3600.0)
//			time = "9-10";
//		else if(currTime > 3600 && currTime <= 7200)
//			time = "10-11";
//		else if(currTime > 7200 && currTime <= 10800)
//			time = "11-12";
//		else if(currTime > 10800 && currTime <= 14400)
//			time = "12-1";
//		else if(currTime > 14400 && currTime <= 18000)
//			time = "1-2";
//		else if(currTime > 18000 && currTime <= 21600)
//			time = "2-3";
//		else if(currTime > 21600 && currTime <= 24200)
//			time = "3-4";
//		else if(currTime > 24200 && currTime <= 28800)
//			time = "4-5";

//		System.out.println(Double.toString(currSpeed) + " " + currTime + " " + currlocation + " " + toLocation);

		String adding = Double.toString(currSpeed) + " " + currTime + " " + currlocation + " " + toLocation;

		dtnHost.p.add(adding);


		Double xLoc = currlocation.getX();
		Double yLoc = currlocation.getY();

//		if (n.minxLoc > xLoc)
//			n.minxLoc = xLoc;
//		if (n.minyLoc > yLoc)
//			n.minyLoc = yLoc;
//		if (n.maxxLoc < xLoc)
//			n.maxxLoc = xLoc;
//		if (n.maxyLoc < yLoc)
//			n.maxyLoc = yLoc;

//		if ((currlocation.getX() > 0 && currlocation.getX() <= 754)
//				&& (currlocation.getY() > 0 && currlocation.getY() <= 700))
//		if(dtnHost.name.contains("v1"))
//			dtnHost.region1 += 1;
//		if(dtnHost.name.contains("v2"))
//			dtnHost.region2 += 1;
//		if(dtnHost.name.contains("v3"))
//			dtnHost.region3 += 1;
//		if(dtnHost.name.contains("v4"))
//			dtnHost.region4 += 1;
//		if ((currlocation.getX() > 755 && currlocation.getX() <= 1508)
//				&& (currlocation.getY() > 700 && currlocation.getY() <= 1400))
//			dtnHost.region2 += 1;
//		if ((currlocation.getX() > 1508 && currlocation.getX() <= 2262)
//				&& (currlocation.getY() > 1400 && currlocation.getY() <= 2100))
//			dtnHost.region3 += 1;
//		if ((currlocation.getX() > 2262 && currlocation.getX() <= 3016)
//				&& (currlocation.getY() > 2100 && currlocation.getY() <= 2800))
//			dtnHost.region4 += 1;
//		if ((currlocation.getX() > 3016 && currlocation.getX() <= 3720)
//				&& (currlocation.getY() > 2800 && currlocation.getY() <= 3500))
//			dtnHost.region5 += 1;
//		if ((currlocation.getX() > 3720 && currlocation.getX() <= 4524)
//				&& (currlocation.getY() > 3500 && currlocation.getY() <= 4200))
//			dtnHost.region6 += 1;
//		if ((currlocation.getX() > 4524 && currlocation.getX() <= 5078)
//				&& (currlocation.getY() > 4200 && currlocation.getY() <= 4800))
//			dtnHost.region7 += 1;
//		if ((currlocation.getX() > 5078) && (currlocation.getY() > 4800))
//			dtnHost.region8 += 1;

//		System.out.println("values are" + " " + n.minxLoc + " " + n.maxxLoc + " " + n.minyLoc + " " + n.maxyLoc);

//		profile.allMessages.add(Double.toString(currSpeed) + " " + currTime + " "+currlocation + " " + toLocation);

//		create message about the current location, time, speed and angle of movement
//		if(dtnHost.getId().startsWith("m")) 
//		{
//			
//			
//			Metadata metadata = new Metadata(this);Contact
//			metadata.GenerateReceiverContactInfo(dtnHost.getTo().getAddress());
//			dtnHost.addProperty(Constant.METADATA, metadata);
//		}
//		//if this is the content message
//		else if(dtnHost.getId().startsWith("c")) {
//			
//		}
//		//if this is the reward message
//		else if(dtnHost.getId().startsWith("r")) {
//			
//		}		
//		
//		makeRoomForNewMessage(dtnHost.getSize());
//		dtnHost.setTtl(this.msgTtl);
//		addToMessages(dtnHost, true);
//		return true;

	}

//	public List<String> getAllData()
//	{
//		return con.allMessages;
//	}
	
	public void changedConnectionWork(Connection con)
	{

//		get all the data from the neighbouring DTN node
		DTNHost otherHost = con.getOtherNode(getHost());
		CustomRouter otherRouter = (CustomRouter)otherHost.getRouter();
			
		
		int t = (int) SimClock.getTime();

		String time_t = getTime(t);

		double lambda = 0.7;
		
		DTNHost host = getHost();
		
		
		this.ackedMessageIds.addAll(otherRouter.ackedMessageIds);
		otherRouter.ackedMessageIds.addAll(this.ackedMessageIds);
		deleteAckedMessages();
		otherRouter.deleteAckedMessages();
//		this.buffer.deleteExpired();
		
		
		if (exchangeDeliverableMessages() != null) 
		{
			return;
		}

	
		
		if(host.name.contains("p") && otherHost.name.contains("p"))
		{
			host.pillars.add(otherHost);
			otherHost.pillars.add(host);
			
			if(host.connectedPillars.containsKey(otherHost))
			{
				List<DTNHost> devices = host.connectedPillars.get(host);
				devices.add(otherHost);
				host.connectedPillars.put(host, devices);
			}
			else
			{
				List<DTNHost> devices = new ArrayList<>();
				devices.add(otherHost);
				host.connectedPillars.put(host, devices);
			}
		}
		

		
//		for(Map.Entry<combine, locs> e: otherHost.updatedloc.entrySet())
//		{
		if(host.others_updatedloc.containsKey(otherHost))
		{
			List<Map<combine, locs>> al = host.others_updatedloc.get(otherHost);
			al.add(otherHost.updatedloc);
			host.others_updatedloc.put(otherHost, al);
		}
		else
		{
			List<Map<combine, locs>> al = new ArrayList<>();
			al.add(otherHost.updatedloc);
			host.others_updatedloc.put(otherHost, al);
		}

		
		List<DTNHost> othernode_connectedPillars = new ArrayList<>();
		

//		for (Connection c : getConnections()) 
//		{
			DTNHost other = con.getOtherNode(getHost());
			int currregion = getRegion(other);
			
			Collection<Message> msgCollection = getMessageCollection();

//			System.out.println("information from" + getHost() + "host is" + other.updatedloc);

			Set s = other.updatedloc.keySet();
					
			
			for (Message m : msgCollection) 
			{
				
				//see if the pillar that we currently connected to, has any other pillar which could be the destination of the current message we have
				//if so, we send the message
				if(otherHost.name.contains("p"))
				{
					
					if(otherHost.pillars.contains(m.getTo()))
						startTransfer(m.replicate(), con);
					if(otherHost.connectedPillars.containsKey(m.getTo()))
						startTransfer(m.replicate(), con);
					
					List<DTNHost> otherHost_pillars = host.connectedPillars.get(otherHost);
					
					System.out.println("otherHost pillars are" + otherHost_pillars);
					
//					List<Message> mList = this.buffer.msgForPillar(host.connectedPillars, otherHost);
					
//					for(Message each : mList)
//					{
//						startTransfer(m, con);
//					}
					
					if(otherHost_pillars != null)
					{	
						for(int i = 0;i<otherHost_pillars.size();i++)
						{
							if(otherHost_pillars.get(i).pillars.contains(m.getTo())) 
							{
								startTransfer(m.replicate(), con);
								continue;
							}
						}
					}
					
				}
				
//				Check about the message contact graph from before
				if(m.path_Contact !=null )
				{
					for(Contact c: m.path_Contact)
					{
						if(c.host1 == otherHost || c.host2==otherHost || c.host1==host || c.host2==host)
							startTransfer(m.replicate(), con);
							continue;
					}
				}
//				System.out.println(DTNHost.pillar_region_connection.get(m.getTo().actualRegion));
				
				List<DTNHost> allPillars = DTNHost.pillar_region_connection.get(m.getTo().actualRegion);
//				
				

				if (m.getTo() == other) 
				{
					startTransfer(m.replicate(), con);
					continue;
				}

				else 
				{
					//this part check for 2 things
					//1 . If the region of the destination node is the same as that of the current region 
					//2. If the region we are in at is different than the actual region of the destination node
					//In Case 1, we would then transfer the message to the miners who have a high inside region (current region) probability 
					//In Case 2. We would want to send the message to all the DTN nodes who have higher outside region probability.
					List<Integer> regProbs = calculateRegionProbs(other, currregion);
					DTNHost toHost = m.getTo();
					Map<Integer, Double> regionDest = getHighestProbability(other);

					Set toHostlocs = toHost.updatedloc.keySet();

					if (toHost.actualRegion == currregion)
					{
						startTransfer(m.replicate(), con);
						continue;
					}
					//if we are outside the region where the information needs to be sent.
					else
					{
						//This is Case 2. In this case, we have to first check if the message has to be sent to which DTN nodes. 
						// When we get the DTN node to which this message has to be sent, we would then find out the probable next location of the current connection
						// The message is sent in the following cases
						// 1. If the current connection is also going to the same region/pillar, we send the message
						// 2. If the current connection has a high probability of going to the region, we send the message
						// 3. If it is going to meet someone, who is going to go to the destination, we can send the message
						// 3. Use DikstraImplement to form the CGR.  
						
						
						//for the current connected DTNHost, if probability of going to destination DTN node region >0.1
						String currTime = getTime(SimClock.getTime());
						String nextTime = nextTimeMethod(currTime);
						
						Map<combine, locs> maxim = new HashMap<>();
						double maxProb = -1.0;
						String maxTime = "0-0";
						locs maxLoc;
//						List<DTNHost> allPillars = DTNHost.pillar_region_connection.get(toHost.actualRegion);
						
						
						if(other.actualRegion == toHost.actualRegion)
						{
							startTransfer(m.replicate(), con);
							continue;
						}
						//1) if the current connected DTN node is going to be at/near a pillar which is in the region of the destination's actual region, we send the message
						for(Map.Entry<combine, locs> ent : other.updatedloc.entrySet())
						{
							combine c1 = ent.getKey();
							locs l1 = ent.getValue();
							
							Set<Integer> st = getKeysByValue(DTNHost.pillar_region_connection, l1.loc2_s);
//							System.out.println("region is"+ host.pillar_region_connection.keySet());
							
							if(st.contains(toHost.actualRegion))
								startTransfer(m.replicate(), con);
							
//				
							
//							System.out.println("actual region right now is"+toHost.actualRegion);
							if(toHost.actualRegion == 0)
								continue;
							// if the destination DTN node is a pillar to which this connected node is going to be at, send the message
							for(DTNHost sP: allPillars)
							{
//								System.out.println("pillars is" + sP);
								if(l1.loc2_s.equals(sP.name))
								{
									startTransfer(m.replicate(), con);
//									break;
								}
							}
	
						}
						List<DTNHost> keys = new ArrayList<>(otherHost.others_updatedloc.keySet());
						
						//The following code is now used to find if the desination node exists in any of the following cases
						//1) If the current connected DTN node is going to meet some other DTN node who going to the same region and same time
						//2) If the current connected DTN node is going to meet some other DTN node who going to the same pillar and same time
						
						for(int k = 0;k<keys.size()-1;k++)
				    	{
//				    		get list of all 
				    		List al_f= otherHost.others_updatedloc.get(keys.get(k));
				    		for (int j = k+1; j < keys.size(); j++) 
					        {
				    			
				    			List al_s = otherHost.others_updatedloc.get(keys.get(j));
				    			
				    			for(int l_cnt = 0; l_cnt<al_f.size();l_cnt++)
				    			{
				    				HashMap<combine, locs> alfm = (HashMap) al_f.get(l_cnt);
				    				List<combine> calfm_lst = new ArrayList<>(alfm.keySet());
				    				if(calfm_lst.size() == 0)
				    					continue;
				    				combine calfm = (combine) calfm_lst.get(0);
				    				locs lalfm = alfm.get(calfm); 
				    				
				    				if(al_s.size() == 0)
				    					continue;
				    				
				    				for(int m_cnt = 0;m_cnt<al_s.size();m_cnt++)
				    				{
				    					Map<combine, locs> alsm = (HashMap)al_s.get(m_cnt);
				    					List<combine> calfm_2_lst = new ArrayList<>(alsm.keySet());
				    					
				    					if(calfm_2_lst.size() == 0)
					    					continue;

				    					combine calfm_2 = (combine)calfm_2_lst.get(0);
					    				locs lalfm_2 = alsm.get(calfm_2); 
					    				
					    				System.out.println("time is " + calfm_2.time.time);
					    				System.out.println("time is " + lalfm_2.loc2_s);
				    		        	
				    		        	if(calfm.time.time.equals(calfm_2.time.time) && lalfm.loc2_s.equals(lalfm_2.loc2_s))
				    		        	{
				    		        		System.out.println("Came here");

				    		        		if(calfm.host == m.getTo() || calfm_2.host == m.getTo())
				    		        			startTransfer(m.replicate(), con);
				    		        	}

				    				}
				    				

				    			}
				    			
					        }
					        
					      }
							
						}
						
						//get all the other miners information as well. If the other miner also has a possibility of going to the same region then send message
//					build a contact graph
						ContactGraph g = new ContactGraph();
						System.out.print("size of pillars is" + DTNHost.allPillars.size());
						
//						List<Contact> res= g.getInformation(host, m, otherHost, DTNHost.allPillars);
						
						host.allProbs = allProbs;
						
						otherHost.allProbs = otherHost.allProbs;
						List<Contact> res_MaxProp = g.getInformationMaxProp(host, m, otherHost, allProbs, DTNHost.allPillars, allProbs);
						
						List<Contact> res = res_MaxProp;
						
						m.path_Contact  =res;
						if(!host.res.containsKey(m))
							host.res.put(m, res);
						else
						{
							host.res.remove(m);
							host.res.put(m, res);
						}
						int counter = 0;
//						for(Contact c_res: res)
//						{
						System.out.println("res is"+ res.size());
						if(res.size() == 0)
							continue;
						else
//						if(res.size()!=0 || res!=null)
						{
							System.out.print("Path from "+res.get(0).host1.name+" "+res.get(0).host2.name);
							System.out.println(" -> ");
						}

						if(counter == 1)
							startTransfer(m.replicate(), con);
						
						
					}

					}
//		}

				
	}
	
	private void updateTransitiveProbs(Map<Integer, MeetingProbabilitySet> p) {
		for (Map.Entry<Integer, MeetingProbabilitySet> e : p.entrySet()) {
			MeetingProbabilitySet myMps = this.allProbs.get(e.getKey()); 
			if (myMps == null || 
				e.getValue().getLastUpdateTime() > myMps.getLastUpdateTime() ) {
				this.allProbs.put(e.getKey(), e.getValue().replicate());
			}
		}
	}
	
	
//	public void updateMeetingProbFor(Integer index) {
//        Map.Entry<Integer, Double> smallestEntry = null;
//        double smallestValue = Double.MAX_VALUE;
//
//		this.lastUpdateTime = SimClock.getTime();
//		
//		if (probs.size() == 0) { // first entry
//			probs.put(index, 1.0);
//			return;
//		}
//		
//		double newValue = getProbFor(index) + alpha;
//		probs.put(index, newValue);
//
//		/* now the sum of all entries is 1+alpha;
//		 * normalize to one by dividing all the entries by 1+alpha */ 
//		for (Map.Entry<Integer, Double> entry : probs.entrySet()) {
//			entry.setValue(entry.getValue() / (1+alpha));
//            if (entry.getValue() < smallestValue) {
//                smallestEntry = entry;
//                smallestValue = entry.getValue();
//            }
//
//		}
//
//        if (probs.size() >= maxSetSize) {
//            core.Debug.p("Probsize: " + probs.size() + " dropping " + 
//                    probs.remove(smallestEntry.getKey()));
//        }
//	}
//	

	@Override
	public void changedConnection(Connection con) 
	{
		
//		super.changedConnection(con);
		if (con.isUp()) 
		{
//			Thread t1=new Thread() {
//				   public void run() {
		   changedConnectionWork(con);
		   
		   DTNHost otherHost = con.getOtherNode(getHost());
		   MessageRouter mRouter = otherHost.getRouter();
		   
		   CustomRouter otherRouter = (CustomRouter)mRouter;
		   
		   probs.updateMeetingProbFor(otherHost.getAddress());
			otherRouter.probs.updateMeetingProbFor(getHost().getAddress());
			
			/* exchange the transitive probabilities */
			this.updateTransitiveProbs(otherRouter.allProbs);
			otherRouter.updateTransitiveProbs(this.allProbs);
			
			this.allProbs.put(otherHost.getAddress(),otherRouter.probs.replicate());
			
			otherRouter.allProbs.put(getHost().getAddress(),this.probs.replicate());
//				   }
//				};
//				t1.start();
			
		}
	}

//	            }

	public Set<Integer> getKeysByValue(Map<Integer, List<DTNHost>> pillar_region_connection, String loc2_s) 
	{
	    Set<Integer> keys = new HashSet<Integer>();
	    for (Entry<Integer, List<DTNHost>> entry : pillar_region_connection.entrySet()) 
	    {
	    	List<DTNHost> al = entry.getValue();
	    	for(DTNHost eachHost : al)
	    	{
		    	if(eachHost.name.equals(loc2_s))
		    		keys.add(entry.getKey());
	    	}
//	        if (Objects.equals(loc2_s, entry.getValue())) 
//	        {
//	            keys.add(entry.getKey());
//	        }
	    }
	    return keys;
	}


	String getTime(double d) {
		String time = " ";
		if (d >= 0.0 && d <= 3600.0)
			time = "9-10";
		else if (d > 3600 && d <= 7200)
			time = "10-11";
		else if (d > 7200 && d <= 10800)
			time = "11-12";
		else if (d > 10800 && d <= 14400)
			time = "12-1";
		else if (d > 14400 && d <= 18000)
			time = "1-2";
		else if (d > 18000 && d <= 21600)
			time = "2-3";
		else if (d > 21600 && d <= 24200)
			time = "3-4";
		else if (d > 24200 && d <= 28800)
			time = "4-5";

		return time;
	}
	
	String nextTimeMethod(String s)
	{
		String res = "";
		if(s.equals("9-10"))
			res = "10-11";
		else if(s.equals("10-11"))
			res = "11-12";
		else if(s.equals("11-12"))
			res = "1-2";
		else if(s.equals("1-2"))
			res = "2-3";
		else if(s.equals("2-3"))
			res = "3-4";
		else
			res = "4-5";
		return res;
	}

	Map<Integer, Double> getHighestProbability(DTNHost host) 
	{
		int region = -1;
//		ArrayList<Integer> al = new ArrayList<>(Arrays.asList(host.region1, host.region2, host.region3, host.region4,
//				host.region5, host.region6, host.region7, host.region8));
		
		ArrayList<Integer> res = new ArrayList<>();
		
//		Map<Integer,Double> curr = new HashMap<>();
		
		double prob = -1.0;
		
		Map<Integer, Double> retu = new HashMap<>();
		
		for(Map.Entry<Integer, Double> curr: host.regionProb.entrySet())
		{
			if(curr.getValue() > prob)
			{
				prob = curr.getValue();
				region = curr.getKey();
				retu.put(region, prob);
			}
		}

//		int maxVal = Collections.max(al);

		return retu;
	}

	List<Integer> calculateRegionProbs(DTNHost host, int region) {
		ArrayList<Integer> regs = new ArrayList<>();

		int inregion = 0;
//		List<Integer> outregion = new ArrayList<>();
		int outregion = 0;

		if (region == 1) 
		{
//			inregion = host.region1;
//			if(host.region2 > 0.5)
//				outregion.add(2);
//			else if(host.region3 > 0.5)
//				outregion.add(3);
//			else if(host.region4 > 0.5)
//				outregion.add(4);
//			else if(host.region5 > 0.5)
//				outregion.add(5);
		}
				
//			outregion = host.region2 + host.region3 + host.region4 + host.region5 + host.region6 + host.region7
//					+ host.region8;
//		} else if (region == 2) {
//			inregion = host.region2;
//			outregion = host.region1 + host.region3 + host.region4 + host.region5 + host.region6 + host.region7
//					+ host.region8;
//		} else if (region == 3) {
//			inregion = host.region3;
//			outregion = host.region2 + host.region1 + host.region4 + host.region5 + host.region6 + host.region7
//					+ host.region8;
//		} else if (region == 4) {
//			inregion = host.region4;
//			outregion = host.region2 + host.region3 + host.region1 + host.region5 + host.region6 + host.region7
//					+ host.region8;
//		} else if (region == 5) {
//			inregion = host.region5;
//			outregion = host.region2 + host.region3 + host.region4 + host.region1 + host.region6 + host.region7
//					+ host.region8;
//		} else if (region == 6) {
//			inregion = host.region6;
//			outregion = host.region2 + host.region3 + host.region4 + host.region5 + host.region1 + host.region7
//					+ host.region8;
//		} else if (region == 7) {
//			inregion = host.region7;
//			outregion = host.region2 + host.region3 + host.region4 + host.region5 + host.region6 + host.region1
//					+ host.region8;
//		}
//
//		else {
//			inregion = host.region8;
//			outregion = host.region2 + host.region3 + host.region4 + host.region5 + host.region6 + host.region7
//					+ host.region1;
//		}

		regs.add(inregion);
		regs.add(outregion);

		return regs;
	}

	int getRegion(DTNHost host) 
	{
		Coord currlocation = host.getLocation();
		int currRegion = -1;
		
		if ((currlocation.getX() > 0 && currlocation.getX() <= 1508) && (currlocation.getY() > 0 && currlocation.getY() <= 1400))
			currRegion = 1;
		if ((currlocation.getX() > 1508 && currlocation.getX() <= 3016) && (currlocation.getY() > 1400 && currlocation.getY() <= 2800))
			currRegion = 2;
		if ((currlocation.getX() > 3016 && currlocation.getX() <= 4524) && (currlocation.getY() > 2800 && currlocation.getY() <= 4200))
			currRegion = 3;
		if ((currlocation.getX() > 4524) && (currlocation.getY() > 4200))
			currRegion = 4;
		
		
//		if ((currlocation.getX() > 0 && currlocation.getX() <= 754)
//				&& (currlocation.getY() > 0 && currlocation.getY() <= 700))
//			currRegion = 1;
//		if ((currlocation.getX() > 755 && currlocation.getX() <= 1508)
//				&& (currlocation.getY() > 700 && currlocation.getY() <= 1400))
//			currRegion = 2;
//		if ((currlocation.getX() > 1508 && currlocation.getX() <= 2262)
//				&& (currlocation.getY() > 1400 && currlocation.getY() <= 2100))
//			currRegion = 3;
//		if ((currlocation.getX() > 2262 && currlocation.getX() <= 3016)
//				&& (currlocation.getY() > 2100 && currlocation.getY() <= 2800))
//			currRegion = 4;
//		if ((currlocation.getX() > 3016 && currlocation.getX() <= 3720)
//				&& (currlocation.getY() > 2800 && currlocation.getY() <= 3500))
//			currRegion = 5;
//		if ((currlocation.getX() > 3720 && currlocation.getX() <= 4524)
//				&& (currlocation.getY() > 3500 && currlocation.getY() <= 4200))
//			currRegion = 6;
//		if ((currlocation.getX() > 4524 && currlocation.getX() <= 5078)
//				&& (currlocation.getY() > 4200 && currlocation.getY() <= 4800))
//			currRegion = 7;
//		if ((currlocation.getX() > 5078) && (currlocation.getY() > 4800))
//			currRegion = 8;

		return currRegion;
	}

//	@Override
//	public Message messageTransferred1(String id, DTNHost from)
//	{
//		Message m = super.messageTransferred(id, from);
//		
//		return m;
//		
//	}

	private void deleteAckedMessages() 
	{
		
		if(this.ackedMessageIds.size() == 0)
			return;
		
//		for(String id: this.ackedMessageIds)
//		{
////			for(Message msg : this.buffer.allList)
////			{
//				if(msg.getId() == null)
//					continue;
//				if(id == null)
//					continue;
//				if(msg.getId().equals(id))
//				{
//					
////					this.buffer.allList.remove(msg);
//				}
////			}
//		}
		
		for (String id : this.ackedMessageIds) 
		{
			if(id == null)
				continue;
			if (this.hasMessage(id) && !isSending(id)) {
				this.deleteMessage(id, false);
			}
		}
	}


}
