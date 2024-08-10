package core;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import routing.CustomRouter.*;
import routing.maxprop.MeetingProbabilitySet;
import core.World;
import routing.MaxPropRouter.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ContactGraph 
{
//    private HashMap<String, HashSet<String>> graph;
	public HashMap<Contact, List<Map<Contact, Double>>> graph = new HashMap<>();

    public class Contact 
    {
       public DTNHost host1;
       public DTNHost host2;
       public String location;
       public String time;
       public double time_order;
       public double predict_prob;

       public Contact(DTNHost host1, DTNHost host2, String location, String time, double time_order, double predict_prob) 
       {
           this.host1 = host1;
           this.host2 = host2;
           this.location = location;
           this.time = time;
           this.time_order = time_order;
           this.predict_prob = predict_prob;
       }
    }
    
    class Path {
        Contact parent;
        double prob;

        public Path(Contact parent, double prob) {
            this.parent = parent;
            this.prob = prob;
        }
    }
    
    class Node implements Comparable<Node> {
        Contact contact;
        double dist;

        public Node(Contact contact, double dist) {
            this.contact = contact;
            this.dist = dist;
        }

        public int compareTo(Node other) {
            return Double.compare(dist, other.dist);
        }
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

//    public void addContact(String node1, String node2, String landmark) 
//    {
//        String contact = node1 + "-" + node2;
//        HashSet<String> landmarks = graph.getOrDefault(contact, new HashSet<>());
//        landmarks.add(landmark);
//        graph.put(contact, landmarks);
//    }


    
    public ArrayList<Contact> findCommonLandmarks(DTNHost host, Map<DTNHost, List<Map<combine, locs>>> others_updatedloc, List<Contact> contactFirst) 
    {
    	Map<combine, locs> m = host.updatedloc;
    	Set<combine> a = m.keySet();
    	
    	ArrayList<Contact> conc = new ArrayList<>();
    	conc.addAll(contactFirst);
    	
    	for(int i = 0;i<a.size();i++)
    	{
	    	combine c = a.iterator().next();
	    	locs l = host.updatedloc.get(c);
	    	
	    	String toLocation = l.loc2_s;
	    	String timeNow = c.time.time;
	    	List<DTNHost> keys = new ArrayList<>(others_updatedloc.keySet());
	    	
	    	if(keys == null || keys.isEmpty())
	    		continue;
	    	
	    	for(int k = 0;k<keys.size();k++)
	    	{
//	    		if(host1.equals(host))
	    		
	    		List al= others_updatedloc.get(keys.get(k));
	    	
		        for (int j = 0; j < al.size(); j++) 
		        {
		        	HashMap  mapVal= (HashMap) al.get(j);
		        	if(mapVal == null)
		        		continue;
		        	Set aVal = mapVal.keySet();
		        	
//		        	System.out.println("aVal is"+aVal);	
		        	if(aVal.isEmpty())
		        		continue;
		        	
		        	combine caVal = (combine) aVal.iterator().next();
		        	locs laVal = (locs) mapVal.get(caVal);
		        	
		        	if(caVal.time.time.equals(timeNow) && laVal.loc2_s.equals(toLocation))
		        	{
		        		System.out.println("Came here");
		        		Contact contact = new Contact(host, caVal.host, caVal.time.time, laVal.loc2_s, caVal.time.time_order, caVal.prob);
		        		conc.add(contact);
		        		Contact contact2 = new Contact(caVal.host, host, caVal.time.time, laVal.loc2_s, caVal.time.time_order, caVal.prob);
		        		conc.add(contact2);
		        	}
		        	
//		        	if()
		        	Contact contact = new Contact(host, getHost(laVal.loc2_s, host), caVal.time.time, laVal.loc2_s, caVal.time.time_order, caVal.prob);
		        	conc.add(contact);
		        	Contact contact2 = new Contact(getHost(laVal.loc2_s, host), host,caVal.time.time, laVal.loc2_s, caVal.time.time_order, caVal.prob);
		        	conc.add(contact2);
		        	
		        	
		        }
		        
		      }
	    	
	    	// This code creates a contact for all the information available till now for all the other DTN nodes and their probable locations.
	    	for(int k = 0;k<keys.size()-1;k++)
	    	{
//	    		get list of all 
	    		List al_f= others_updatedloc.get(keys.get(k));
	    		for (int j = k+1; j < keys.size(); j++) 
		        {
	    			
	    			List al_s = others_updatedloc.get(keys.get(j));
	    			
	    			for(int l_cnt = 0; l_cnt<al_f.size();l_cnt++)
	    			{
	    				HashMap<combine, locs> alfm = (HashMap) al_f.get(l_cnt);
	    				List<combine> calfm_lst = new ArrayList<>(alfm.keySet());
	    				if(calfm_lst.size() == 0)
	    					continue;
	    				combine calfm = (combine) calfm_lst.get(0);
	    				locs lalfm = alfm.get(calfm); 
	    				
	    				for(int m_cnt = 0;m_cnt<al_s.size();m_cnt++)
	    				{
	    					Map<combine, locs> alsm = (HashMap)al_s.get(m_cnt);
	    					List<combine> calfm_2_lst = new ArrayList<>(alsm.keySet());
	    					if(calfm_2_lst.size() == 0)
		    					continue;
	    					combine calfm_2 = (combine)calfm_2_lst.get(0);
		    				locs lalfm_2 = alsm.get(calfm_2); 
		    				
		    				//this adds the mobile nodes
	    		        	if(calfm.time.time.equals(calfm_2.time.time) && lalfm.loc2_s.equals(lalfm_2.loc2_s))
	    		        	{
	    		        		System.out.println("Came here");
	    		        		Contact contact = new Contact(calfm.host, calfm_2.host, calfm_2.time.time, lalfm_2.loc2_s, calfm_2.time.time_order, calfm.prob);
	    		        		conc.add(contact);
	    		        		Contact contact2 = new Contact(calfm_2.host, calfm.host, calfm_2.time.time, lalfm_2.loc2_s, calfm_2.time.time_order, calfm_2.prob);
	    		        		conc.add(contact2);
	    		        	}
	    		        	//this adds the pillars
	    		        	Contact contact = new Contact(calfm_2.host, getHost(lalfm_2.loc2_s, host), calfm_2.time.time, lalfm_2.loc2_s, calfm_2.time.time_order, calfm_2.prob);
	    		        	conc.add(contact);
	    		        	Contact contact2 = new Contact(getHost(lalfm_2.loc2_s, host), calfm_2.host, calfm_2.time.time, lalfm_2.loc2_s, calfm_2.time.time_order, calfm_2.prob);
	    		        	conc.add(contact2);
	    				}
	    				//this adds the pillars
	    				Contact contact = new Contact(calfm.host, getHost( lalfm.loc2_s, host), calfm.time.time, lalfm.loc2_s, calfm.time.time_order, calfm.prob);
    		        	conc.add(contact);
    		        	Contact contact2 = new Contact(getHost( lalfm.loc2_s, host), calfm.host, calfm.time.time, lalfm.loc2_s, calfm.time.time_order, calfm.prob);
    		        	conc.add(contact2);
	    			}
	    			
		        }
		        
		      }
		}
    	
    	Collections.sort(conc, timeComparator);
    	
//    	try 
//	    {
//	      FileWriter writer = new FileWriter("/usr/local/home/aghnw/Downloads/the-one-1.6.0/output1.txt");
//	     
////	      System.out.println("contacts are" + conc);
////	      if(conc.size() == 0)
//	    	  
//	      for(Contact c:conc)
//	      {
//	    	 String data = "";
//	    	 if(c == null)
//	    		 continue;
//	    	 System.out.println(c.host1.name + " " + c.host2.name + " " + c.location + " " + c.time + " "+ c.time_order + "\n");
//	    	 if(c.host1.name == null || c.host2.name== null || c.location== null || c.time== null)
//	    	 {
//	    		 System.out.println("out here");
//	    		 continue;
//	    	 }
//	    	data = c.host1.name + " " + c.host2.name + " " + c.location + " " + c.time + " "+ c.time_order + "\n";   
//	    	writer.append(data);
//	    	 
//	      }
////	      writer.append(data);
//	      writer.close();
//	      System.out.println("Contact Data written to file successfully.");
//	    } 
//	    catch (IOException e) 
//	    {
//	      System.out.println("An error occurred while writing data to file: " + e.getMessage());
//	    }
    	
    	
    	return conc;
    	
   }
    
    public static DTNHost getHost(String name, DTNHost host)
    {
    	DTNHost host_return = null;
    	
    	List<DTNHost> al = DTNHost.allHosts;
    	
//    	System.out.println(al);
    	
    	for(DTNHost h :al)
    	{
    		if(h.name.equals(name))
    			host_return = h;
    	}
    	
    	
    	return host_return; 
    }
    
   public double gettime_order()
   {
	   double order_number = 0;
	   double time = SimClock.getTime();
	   double value = time/3600;
	   order_number = Math.ceil(value/900);
	   
	   return order_number;
	   
   }
    

    public HashMap<Contact, List<Map<Contact, Double>>> getGraph() 
    {
        return graph;
    }
    
    
    //This method has been made to do the following things
    //1) Give the starting node (graph node) to find distance, how it has been done is detailed below
    //2) Find all the different contact possible
    //3) What should the destination node/s (graph node) be
    public List<Contact> getInformation(DTNHost host, Message m, DTNHost otherHost, List<DTNHost> pillars)
    {
    	ContactGraph g = new ContactGraph();
    	
//    	DTNHost pillar = getPillar(host, pillars);
    	List<Contact> cg = new ArrayList<>();
    	if(host.name.contains("p") || otherHost.name.contains("p"))
    	{
//    		System.out.println("Something went wrong");
    		Contact c_first = new Contact(host, otherHost, host.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 1.0);
    		Contact c_second = new Contact(otherHost, host, host.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 1.0);
    		cg.add(c_second);
    		cg.add(c_first);
    	}
    	else
    	{
    		DTNHost pillar = getPillar(host, pillars);
    		Contact c_first = new Contact(host, otherHost, pillar.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 1.0);
    		Contact c_second = new Contact(otherHost, host, pillar.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 1.0);
    		cg.add(c_second);
    		cg.add(c_first);
    	}
    	
    	
//    	graph.put(c_first, null);
    	ArrayList<Contact> conc = g.findCommonLandmarks(host, host.others_updatedloc, cg);
    	
    	
    	
    	Collections.sort(conc, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
            	
            	if(c1.predict_prob > c2.predict_prob)
            		return 1;
            	else if(c1.predict_prob < c2.predict_prob)
            		return -1;
            	else
            		return 0;
            	
//                return Integer.compare(c1.predict_prob - c2.predict_prob);
            }
        });
    	
    	List<Contact> first1000Values = new ArrayList<>();
    	if(conc.size() > 10000)
    		first1000Values = conc.subList(0, 10000);
    	else
    		first1000Values = conc;
    	
    	g.buildGraph(first1000Values);
    	
    	Contact c_last = g.getLastNode(m);
    	
    	
    	int s = 10000000;
    	
    	if(g.graph == null)
    	{
    		return null;
    	}
    	
//    	try 
//	    {
//	      FileWriter writer = new FileWriter("/usr/local/home/aghnw/Downloads/the-one-1.6.0/output.txt");
//	      String data = "";
//	      data += "we got\n";
//	      for(Map.Entry<Contact, List<Contact>> hm : g.graph.entrySet())
//	      {
//	    	   data +=  hm.getKey().host1.name + " " + hm.getKey().host2.name + " " + hm.getKey().location + " " + hm.getKey().time + " "+hm.getKey().time_order+"\n";
//	    	   for(int i =0;i< hm.getValue().size();i++)
//	    	   {
//	    		   data += hm.getValue().get(i).host1 + " " + hm.getValue().get(i).host2 + " " + hm.getValue().get(i).location + " " + 
//	    	   hm.getValue().get(i).time + " "+ hm.getValue().get(i).time_order+"\n";
//	    	   }
//	    		   
//	      }
//	      
//	      writer.write(data);
//	      writer.close();
//	      System.out.println("graph Data written to file successfully.");
//	    } 
//	    catch (IOException e) 
//	    {
//	      System.out.println("An error occurred while writing data to file: " + e.getMessage());
//	    }
    	
    	if(c_last == null)
    	{
    		System.out.println("we did not get a end node");
    		for(Contact cs : g.graph.keySet())
    		{
    			int s_find = g.graph.get(cs).size();
    			if(s_find < s)
    			{
    				System.out.println("came here for last node");
    				c_last = cs;
    				break;
    			}
    		}
//    		return;
    	}
    	
    	System.out.print("c last is"+c_last);
    	//if the last node is null, then the first contact which has the destination node as a host will become the destination node
    	
    	
    	List<String> times = Arrays.asList("9-10", "10-11", "11-12", "12-1", "1-2", "2-3", "3-4", "4-5");
    	//The case in which the last node is not found, we select all the different possible destination nodes and then find shortest path to all. 
    	//The one which has the shortest path is selected. In case of a tie, the first one is selected
    	if(c_last == null)
    	{

    		for(Map.Entry<Contact, List<Map<Contact, Double>>> hm : g.graph.entrySet())
    		{
    			if(hm.getKey().host1 == m.getTo() || hm.getKey().host2 == m.getTo())
    			{
    				c_last = hm.getKey();
    				break;
    			}
    				
    		}
    		List<Contact> res = g.shortestPath(cg.get(0), c_last);
    		
    	}
//    	else
    	System.out.println("clast is" + c_last + "c-first" + cg.get(0));
    	List<Contact> res = g.shortestPath(cg.get(0), c_last);
    	
    	if(res!=null)
    		System.out.println("we found a path");
    	
    	System.out.println("saving all graph information with c_last as" + c_last);
    	
    	try 
	    {
	      FileWriter writer = new FileWriter("/usr/local/home/aghnw/Downloads/the-one-master/output_results.txt");
	      String data = "";
	      data += "we got\n";
	      for(Contact c : res)
	      {
	    	  if(c == null)
	    		  continue;
	    	  data += c.host1.name + " " + c.host2.name + " " + c.location + " " + c.time + " " + c.time_order +"\n";
	      }
	    }
    	catch(Exception e)
    	{}
    	
    	
//    	try
//    	{
//    		TimeUnit.MINUTES.sleep(2);
//    	}
//    	catch(Exception e)
//    	{}
    	
    	return res;

    	
    }

    public List<Contact> getInformationMaxProp(DTNHost host, Message m, DTNHost otherHost, Map<Integer, MeetingProbabilitySet> meetingProbs, List<DTNHost> pillars, 
    		Map<Integer, MeetingProbabilitySet> allprobs)
    {

    	ContactGraph g = new ContactGraph();
    	
//    	DTNHost pillar = getPillar(host, pillars);
    	List<Contact> cg = new ArrayList<>();
    	
    	String filePath = "/outputPillarContact.txt";
		
		try {
            // Create a FileWriter to write to the file
            FileWriter fileWriter = new FileWriter(filePath, true);

            // Create a BufferedWriter for efficient writing
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Data to write to the file
//            String data = "Hello, world!\nThis is some data written to a file.";
            StringBuilder dataBuild = new StringBuilder();
            
            for(DTNHost h:pillars)
            {
//            	 if(host.name.contains("p"))
                 dataBuild.append(h.allProbs+"\n");
            }
//            if(host.name.contains("p"))
//            	dataBuild.append(host.allProbs+"\n");
//            
//            if(otherHost.name.contains("p"))
//            {
//            	dataBuild.append(otherHost.allProbs+"\n");
//            }
//            
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
    	
    	
    	if(host.name.contains("p") || otherHost.name.contains("p"))
    	{
// //    		System.out.println("Something went wrong");
    		
//     		System.out.println("host name is"+host.name);
//     		System.out.println("otherhost is"+otherHost.name);
//     		Integer add1 = host.getAddress();
//     		Map<Integer, MeetingProbabilitySet> mps1 = host.allProbs;
//     		Integer add2 = otherHost.getAddress();
//     		Map<Integer, MeetingProbabilitySet> mps2 = otherHost.allProbs;
    		
    		
//     		Double probVal1;
// //    		= mps1.get(add1).getProbFor(add2);
// //    		if(probVal1 == null)
// //    			probVal1 = 0.0;
    		
//     		if(mps1.isEmpty())
//     		{
//     			probVal1 = 0.0;
//     		}
//     		else
//     		{
//     			if(mps1.get(add1) == null)
//     				probVal1= 0.0;
//     			MeetingProbabilitySet result = mps1.get(add1);
//     			probVal1 = result.getProbFor(add2);
// 	    		System.out.println("probval is"+ probVal1);
//     		}
// //    		
//     		System.out.println("add1 is"+ add1);
//     		System.out.println("mps2 is" + mps2);
    		
//     		Double probVal2;
// //    		= mps2.get(add2).getProbFor(add1);
// //    		System.out.println("probval is"+ probVal2);
    		
// //    		Double probVal2 = mps2.get(add2).getProbFor(add1);
//     		if(mps2.isEmpty())
//     		{
//     			probVal2 = 0.0;
//     		}
//     		else
//     		{
// //    			if(mps2.get(add2).getProbFor(add1) == null)
// //    				probVal2 = 0.0;
//     			if(mps2.get(add2) == null)
//     				probVal2 = 0.0;
//     			MeetingProbabilitySet result = mps2.get(add2);
//     			probVal2 = result.getProbFor(add1);
    			
// 	    		System.out.println("probval is"+ probVal2);
//     		}
// //    		if (mps2.isEmpty() || e.getValue().getLastUpdateTime() > myMps.getLastUpdateTime() ) {
// //    				this.allProbs.put(e.getKey(), e.getValue().replicate());
    		
    		
//     		System.out.println("probval is"+ probVal2);
//     		if(probVal2==null)
//     			probVal2 = 0.0;
    		
    		Contact c_first = new Contact(host, otherHost, host.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 0.0);
    		Contact c_second = new Contact(otherHost, host, host.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 0.0);
    		cg.add(c_second);
    		cg.add(c_first);
    	}
    	else
    	{
    		DTNHost pillar = getPillar(host, pillars);
    		// Integer add1 = host.getAddress();
    		// Map<Integer, MeetingProbabilitySet> mps1 = host.allProbs;
    		// Integer add2 = otherHost.getAddress();
    		// Map<Integer, MeetingProbabilitySet> mps2 = otherHost.allProbs;
    		
    		// Double probVal1 = mps1.get(add1).getProbFor(add2);
    		// if(probVal1 == null)
    		// 	probVal1 = 0.0;
    		
    		// System.out.println("add1 is"+ add1);
    		// System.out.println("mps2 is" + mps2);
    		// Double probVal2 = mps2.get(add2).getProbFor(add1);
    		// System.out.println("probval is"+ probVal2);
    		// if(probVal2==null)
    		// 	probVal2 = 0.0;
    		
    		Contact c_first = new Contact(host, otherHost, pillar.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 0.0);
    		Contact c_second = new Contact(otherHost, host, pillar.name, getTime(SimClock.getTime()),Math.ceil((SimClock.getTime()/3600)/900), 0.0);
    		cg.add(c_second);
    		cg.add(c_first);
    	}
    	
    	
//    	graph.put(c_first, null);
    	ArrayList<Contact> conc = g.findCommonLandmarks(host, host.others_updatedloc, cg);
    	
    	
    	
    	Collections.sort(conc, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
            	
            	if(c1.predict_prob > c2.predict_prob)
            		return 1;
            	else if(c1.predict_prob < c2.predict_prob)
            		return -1;
            	else
            		return 0;
            	
//                return Integer.compare(c1.predict_prob - c2.predict_prob);
            }
        });
    	
    	List<Contact> first1000Values = new ArrayList<>();
    	if(conc.size() > 10000)
    		first1000Values = conc.subList(0, 10000);
    	else
    		first1000Values = conc;
    	
    	g.buildGraph(first1000Values);
    	
    	Contact c_last = g.getLastNode(m);
    	
    	
    	int s = 10000000;
    	
    	if(g.graph == null)
    	{
    		return null;
    	}
    	
//    	try 
//	    {
//	      FileWriter writer = new FileWriter("/usr/local/home/aghnw/Downloads/the-one-1.6.0/output.txt");
//	      String data = "";
//	      data += "we got\n";
//	      for(Map.Entry<Contact, List<Contact>> hm : g.graph.entrySet())
//	      {
//	    	   data +=  hm.getKey().host1.name + " " + hm.getKey().host2.name + " " + hm.getKey().location + " " + hm.getKey().time + " "+hm.getKey().time_order+"\n";
//	    	   for(int i =0;i< hm.getValue().size();i++)
//	    	   {
//	    		   data += hm.getValue().get(i).host1 + " " + hm.getValue().get(i).host2 + " " + hm.getValue().get(i).location + " " + 
//	    	   hm.getValue().get(i).time + " "+ hm.getValue().get(i).time_order+"\n";
//	    	   }
//	    		   
//	      }
//	      
//	      writer.write(data);
//	      writer.close();
//	      System.out.println("graph Data written to file successfully.");
//	    } 
//	    catch (IOException e) 
//	    {
//	      System.out.println("An error occurred while writing data to file: " + e.getMessage());
//	    }
    	
    	if(c_last == null)
    	{
    		System.out.println("we did not get a end node");
    		for(Contact cs : g.graph.keySet())
    		{
    			int s_find = g.graph.get(cs).size();
    			if(s_find < s)
    			{
    				System.out.println("came here for last node");
    				c_last = cs;
    				break;
    			}
    		}
//    		return;
    	}
    	
    	System.out.print("c last is"+c_last);
    	//if the last node is null, then the first contact which has the destination node as a host will become the destination node
    	
    	
    	List<String> times = Arrays.asList("9-10", "10-11", "11-12", "12-1", "1-2", "2-3", "3-4", "4-5");
    	//The case in which the last node is not found, we select all the different possible destination nodes and then find shortest path to all. 
    	//The one which has the shortest path is selected. In case of a tie, the first one is selected
    	if(c_last == null)
    	{

    		for(Map.Entry<Contact, List<Map<Contact, Double>>> hm : g.graph.entrySet())
    		{
    			if(hm.getKey().host1 == m.getTo() || hm.getKey().host2 == m.getTo())
    			{
    				c_last = hm.getKey();
    				break;
    			}
    				
    		}
    		List<Contact> res = g.shortestPath(cg.get(0), c_last);
    		
    	}
//    	else
    	System.out.println("clast is" + c_last + "c-first" + cg.get(0));
    	List<Contact> res = g.shortestPath(cg.get(0), c_last);
    	
    	if(res!=null)
    		System.out.println("we found a path");
    	
    	System.out.println("saving all graph information with c_last as" + c_last);
    	
    	try 
	    {
	      FileWriter writer = new FileWriter("./the-one-master/output_results.txt");
	      String data = "";
	      data += "we got\n";
	      for(Contact c : res)
	      {
	    	  if(c == null)
	    		  continue;
	    	  data += c.host1.name + " " + c.host2.name + " " + c.location + " " + c.time + " " + c.time_order +"\n";
	      }
	    }
    	catch(Exception e)
    	{}
    	
    	
//    	try
//    	{
//    		TimeUnit.MINUTES.sleep(2);
//    	}
//    	catch(Exception e)
//    	{}
    	
    	return res;

    	
    
    }
    
    
    
    public DTNHost getPillar(DTNHost h, List<DTNHost> pillars)
    {
    	DTNHost pillar = h;
    	double least = 100000.0;
    	if(h == null)
    	{
    		System.out.println("this was an issue");
    		return h;
    	}
//    	System.out.println("host is" + h.world.hosts);
    	for(int i = 0;i<DTNHost.allPillars.size();i++)
    	{
    		DTNHost hw = pillars.get(i);
    		if(hw.name.contains("p"))
    		{
    			double dx = h.getLocation().getX() - hw.getLocation().getX();
    		    double dy = h.getLocation().getX() - hw.getLocation().getX();
    		    double less =  Math.sqrt(dx * dx + dy * dy);
    		    if(less < least)
    		    {
    		    	least = less;
    		    	pillar = hw;
    		    }
    		    
    		}
    	}
    	
    	
    	return pillar;
    }
    
    //Finds the last destination graph node to which the shortest path needs to be found.
    public Contact getLastNode(Message m) 
    {
    	Set<Contact> allNodes = new HashSet<>();
    	Set<Contact> outgoingNodes = new HashSet<>();
        List<DTNHost> allPillars = DTNHost.pillar_region_connection.get(m.getTo().actualRegion);

        // Get all nodes in the graph and all nodes with outgoing edges

        if(graph.values() == null)
        	System.out.println("graph is null");
        if(graph.get(m.getFrom()) == null)
        	return null;
        for (Map<Contact, Double> edges : graph.get(m.getFrom())) 
        {
            allNodes.addAll(edges.keySet());
            outgoingNodes.addAll(edges.keySet());
        }

        // Return the node that is not in the set of nodes with outgoing edges
        for (Contact node : allNodes) 
        {
            if (!outgoingNodes.contains(node)) 
            {
                return node;
            }
//            List<Contact> c = (List)node.keySet();
            if(allPillars.contains(node) || allPillars.contains(node))
            	return node;
            
        }
        
        
        
        return null; // No last node found
    }
    
    private void buildGraph(List<Contact> contacts) 
    {
    	if(contacts == null)
    		return;
    	System.out.println("contacts size is" + contacts.size());
        for (int i = 0; i < contacts.size(); i++) 
        {
            Contact contact1 = contacts.get(i);
            for (int j = i + 1; j < contacts.size(); j++) 
            {
                Contact contact2 = contacts.get(j);
                if (isCommonNode(contact1, contact2) && isSequential(contact1, contact2)) 
                {
                	double probability = calculateProbability(contact1, contact2);
                    addEdge(contact1, contact2, probability);
                }
            }
        }
        System.out.println("graph size is"+ graph.size());
    }
    
    double calculateProbability(Contact c1, Contact c2)
    {
    	double res = c1.predict_prob+c2.predict_prob;
    	if(res !=0)
    		return res;
    	return 0.0;
    }
    
    /**
     * Check if the given time is in sequential order with the current contact time
     * @param time the time to compare
     * @return true if the time is in sequential order, false otherwise
     */
    private static boolean isSequential(Contact c1, Contact c2) 
    {
    	if(c1 == null)
    		return false;
    	if(c2 == null)
    		return false;
    	String time1; String time2;
    	time1 = c1.time;
    	time2 = c2.time;
        List<String> times = Arrays.asList("9-10", "10-11", "11-12", "12-1", "1-2", "2-3", "3-4", "4-5");
        int index1 = times.indexOf(time1);
        int index2 = times.indexOf(time2);
        return Math.abs(index1 - index2) == 1;
    }
    
    /**
     * Check if the given contact has a DTN node in common with the current contact
     * @param other the contact to compare
     * @return true if the contacts have a node in common, false otherwise
     */
    public boolean isCommonNode(Contact c1, Contact c2) 
    {
    	if(c1 == null)
    		return false;
    	if(c2 == null)
    		return false;
    	if(c1.host1 == null || c1.host2 == null || c2.host1 == null || c2.host2 == null)
    		return false;
        return (c1.host1.equals(c2.host1) || c1.host1.equals(c2.host2) || c1.host2.equals(c2.host1) || c1.host2.equals(c2.host2));
    }
    
    public void addEdge(Contact from, Contact to, double probability) {
        List<Map<Contact, Double>> edgeList = graph.getOrDefault(from, new ArrayList<>());
        Map<Contact, Double> edgeMap = new HashMap<>();
        edgeMap.put(to, probability);
//        edgeMap.put("probability", probability);
        edgeList.add(edgeMap);
        graph.put(from, edgeList);
    }
    
//    public Path shortestPath(Contact source, Contact target) 
//    {
//    	if(source == null)
//    		return null;
//    	if(target == null)
//    		return null;
//        HashMap<Contact, Path> paths = new HashMap<>();
//        PriorityQueue<Contact> queue = new PriorityQueue<>();
//        HashSet<Contact> visited = new HashSet<>();
//        
//        queue.offer(source);
//        visited.add(source);
//        
//        while (!queue.isEmpty()) 
//        {
//            Contact current = queue.poll();
//            if (current.equals(target)) 
//            {
//                return reconstructPath(paths, source, target);
//            }
//            if (graph != null && graph.containsKey(current)) 
//            {
//	            for (Contact neighbor : graph.get(current)) 
//	            {
//	                if (!visited.contains(neighbor)) 
//	                {
//	                	double weight = getWeight(current, neighbor);
//	                    double newDist = current.predict_prob + weight;
//	                    double newProb = paths.get(current).prob * getProb(current, neighbor);
//	                    Node newNode = new Node(neighbor, newDist);
//	                    Path newPath = new Path(current, newProb);
//	      
//	                    paths.put(neighbor, newPath);
//	                    queue.offer(neighbor);
//	                }
//	            }
//            }
//        }
//        return null;
//    }
    
    
    public List<Contact> shortestPath(Contact source, Contact target) {
        if (source == null || target == null) {
            return new ArrayList<>();
        }

        // Initialize distance and probability arrays
        Map<Contact, Double> distance = new HashMap<>();
        Map<Contact, Double> probability = new HashMap<>();
        for (Contact contact : graph.keySet()) {
            distance.put(contact, Double.POSITIVE_INFINITY);
            probability.put(contact, 0.0);
        }
        distance.put(source, 0.0);
        probability.put(source, 1.0);

        // Initialize priority queue
        PriorityQueue<Contact> queue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));
        queue.offer(source);

        // Initialize parent map and visited set
        Map<Contact, Contact> parentMap = new HashMap<>();
        Set<Contact> visited = new HashSet<>();

        // Perform Dijkstra's algorithm
        while (!queue.isEmpty()) 
        {
            Contact current = queue.poll();
            visited.add(current);

            if (current.equals(target)) {
                return reconstructPath(parentMap, source, target);
            }
            if(!graph.containsKey(current))
            	continue;
            for (Map<Contact, Double> edgeMap : graph.get(current)) 
            {
                for (Map.Entry<Contact, Double> entry : edgeMap.entrySet()) {
                    Contact neighbor = entry.getKey();
                    double weight = entry.getValue();
                    double prob = edgeMap.getOrDefault("probability", 1.0);

                    if (visited.contains(neighbor)) {
                        continue;
                    }

                    double newDistance = distance.get(current) + weight;
                    double newProbability = probability.get(current) * prob;
                    
                    if(distance.get(neighbor) == null)
                    	continue;
                    
                    if (newDistance < distance.get(neighbor)) 
                    {
                        distance.put(neighbor, newDistance);
                        probability.put(neighbor, newProbability);
                        parentMap.put(neighbor, current);
                        queue.offer(neighbor);
                    } else if (newDistance == distance.get(neighbor) && newProbability > probability.get(neighbor)) {
                        probability.put(neighbor, newProbability);
                        parentMap.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    
//    public double getWeight(Contact from, Contact to) 
//    {
//    	List<Contact> lst = graph.get(from);
//        if (!graph.containsKey(from) || !graph.get(from).contains(to)) {
//            return Double.POSITIVE_INFINITY;
//        }
//        return graph.get(from).get(to);
//    }
//
//    public double getProb(Contact from, Contact to) {
//        if (!graph.containsKey(from) || !graph.get(from).contains(to)) {
//            return 0.0;
//        }
//        return graph.get(from).get(to);
//    }
    
    private List<Contact> reconstructPath(Map<Contact, Contact> parentMap, Contact source, Contact target)
    {
    	if(source == null)
    		return new ArrayList<>();
    	if(target == null)
    		return new ArrayList<>();
        LinkedList<Contact> path = new LinkedList<>();
        Contact current = target;
        while (current != null && !current.equals(source)) {
            path.addFirst(current);
            current = parentMap.get(current);
        }
        path.addFirst(source);
        return path;
    }

 
     
     
     List<String> timeOrder = Arrays.asList("9-10", "10-11", "11-12", "12-1", "1-2", "2-3", "3-4", "4-5");

  // Define a custom comparator for sorting contacts by time
  Comparator<Contact> timeComparator = new Comparator<Contact>() {
      @Override
      public int compare(Contact c1, Contact c2) 
      {
    	  if((timeOrder.indexOf(c1.time) == timeOrder.indexOf(c2.time)) && c1.time_order == c2.time_order)
    		  return 0;
    	  else if((timeOrder.indexOf(c1.time) < timeOrder.indexOf(c2.time)))
    		  return -1;
    	  else if((timeOrder.indexOf(c1.time) == timeOrder.indexOf(c2.time)) && c1.time_order < c2.time_order)
    		  return -1;
    	  else if((timeOrder.indexOf(c1.time) == timeOrder.indexOf(c2.time)) && c1.time_order > c2.time_order)
    		  return 1;
    	  else
    		  return 1;
    	  
//          return Integer.compare(index1, index2);
      }
  };

  // Sort the contacts by time using the custom comparator
  

}
