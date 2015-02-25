package IllegalDetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

public class Basic {
	
	public static HashMap<Long, location> nodemap = new HashMap<Long, location>();
	public static TreeSet<Long> fact = new TreeSet<Long>();
	public static TreeSet<Long> guilty = new TreeSet<Long>();
	public static ArrayList<record> recordlist = new ArrayList<record>();
	public static ArrayList<record> candidatelist = new ArrayList<record>();

	public static BufferedReader br2;
	
	//File
	public static String input_file = "a";
	public static String ground_truth = "a";
	
	//Parameters
	public static int RecordWindow = 12;
	public static double DistanceWindow = 3;
	public static int CountLimit = 1;
	public static double SpeedLimit = 0.1;
	public static double DistanceLimit = 25;
	public static final double EARTH_RADIUS = 6371.0;

	public static class record implements Comparable<Object> {
		
		long id;
		Calendar time;
		long camera;
		double prob;
		String orginal;
		
		public record() {
			id = -1;
		}
		
		@Override
		public int compareTo(Object o) {
			if(!(o instanceof record)){  
	            throw new ClassCastException();  
	        }
			record rx = (record)o;
			if(id < rx.id) return -1;
			if(id > rx.id) return 1;
			return time.compareTo(rx.time);
		}

	}
	
	public static class location {
		
		double x;
		double y;
	}
	
	public static void main(String args[]) throws IOException {
		
		Scanner sc = new Scanner(new FileReader(ground_truth));
		while(sc.hasNext()) {
			long id = sc.nextLong();
			fact.add(id);
		}
		sc.close();
		
		InputStreamReader isr1 = new InputStreamReader(new FileInputStream("location.txt"));
		BufferedReader br1 = new BufferedReader(isr1);
		br1.readLine();
		while(br1.ready()) {
			String line[] = br1.readLine().split(",");
			long nodeid = Long.parseLong(line[0]);
			location l = new location();
			l.x = Double.parseDouble(line[1]);
			l.y = Double.parseDouble(line[2]);
			nodemap.put(nodeid, l);
		}
		
		br1.close();
		
		run(0);
	}
	
	public static void run(double para) throws NumberFormatException, IOException {
		InputStreamReader isr2 = new InputStreamReader(new FileInputStream(input_file));
		br2 = new BufferedReader(isr2);
		
		process();
		
		br2.close();
		
		PrintWriter pwt2 = new PrintWriter(new FileWriter("guilty.txt"));
		
		double right = 0;
		Iterator<Long> ix = guilty.iterator();
		while(ix.hasNext()) {
			long id = ix.next();
			if(fact.contains(id)) right++;
		}
		
		double recall = right / 100;
		double precision = right / guilty.size();
		double F1score = 2 * recall * precision / (recall + precision);
		System.out.println(para + "\t" + recall + "\t" + precision + "\t" + F1score);
		
		recordlist.clear();
		guilty.clear();
		
		Iterator<record> iy = candidatelist.iterator();
		while(iy.hasNext()) {
			record r = iy.next();
			pwt2.println(r.orginal + " " + r.prob);
		}
		
		pwt2.close();
	}
	
	public static void process() throws NumberFormatException, IOException {
		record last = new record();
		
		while(br2.ready()) {
			record r = new record();
			r.orginal = br2.readLine();
			
			String line[] = r.orginal.split(" +");
			long id = Long.parseLong(line[0]);
			String dline[] = line[1].split("-");
			String tline[] = line[2].split(":");
			Calendar time = Calendar.getInstance();
			time.set(Integer.parseInt(dline[0]), Integer.parseInt(dline[1]) - 1, Integer.parseInt(dline[2]),
					Integer.parseInt(tline[0]), Integer.parseInt(tline[1]), Integer.parseInt(tline[2]));
			long camera = Long.parseLong(line[3]);
			
			r.id = id;
			r.time = time;
			r.camera = camera;
			recordlist.add(r);
			
			if(last.id != -1) {
				double flag = NaiveMethod(r, last);
				if(flag > 0) {
					int ctr = recordlist.size() - 2;
					int icount = 1;
					
					int rctr = 0;
					while(ctr >= 0) {
						rctr++;
						if(rctr > RecordWindow) break;
						record rc = recordlist.get(ctr);
						if(rc.id != r.id) break;
						location l1 = nodemap.get(r.camera);
						location l2 = nodemap.get(rc.camera);
						double dist = calc(l1.x, l1.y, l2.x, l2.y);
						if(dist < DistanceWindow) {
							icount++;
						}
						ctr--;
					}
					if(icount > CountLimit) {
						guilty.add(r.id);
						
						int overcount = CountLimit - icount;
						double xs = Math.pow(overcount, 2) * flag; 
						r.prob = 1 - Math.log(xs) / xs;
						candidatelist.add(r);
					}
				}
			}

			last.id = id;
			last.camera = camera;
			last.time = time;
		}
	}
	
	public static double NaiveMethod(record r1, record r2) {
		if(r1.id != r2.id) return -1;
		else {
			location l1 = nodemap.get(r1.camera);
			location l2 = nodemap.get(r2.camera);
			if(l1 == null) System.out.println(r1.id + " " + r1.camera);
			if(l2 == null) System.out.println(r1.id + " " + r1.camera);
			double dist = calc(l1.x, l1.y, l2.x, l2.y);
			long time1 = r1.time.getTimeInMillis();
			long time2 = r2.time.getTimeInMillis();
			double second = Math.abs((time1 - time2) / 1000);
			
			//smooth
			second = second + 1;
			
			if(dist / second > SpeedLimit) {
				return dist / second / SpeedLimit;
			}
			if(dist > DistanceLimit) {
				return dist / DistanceLimit;
			}
			return -1;
		}
	}
	
    public static double getDistance(double x1, double y1, double x2, double y2){
    	double x,y,distance;
    	x = Math.abs(x1 - x2);
    	y = Math.abs(y1 - y2);
    	distance = Math.sqrt((y * y * 111.2 * 111.2) + (x * x * 96.3 * 96.3));
    	return distance;
    }
    
	public static double calc(double latt1, double lonn1, double latt2, double lonn2) {
    	double R = EARTH_RADIUS; // earth radius in km
    	double lat1 = Math.toRadians(latt1), lon1 = Math.toRadians(lonn1);  
    	double lat2 = Math.toRadians(latt2), lon2 = Math.toRadians(lonn2);  
    	double dLat = lat2 - lat1;  
    	double dLon = lon2 - lon1;  
    	double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon/2) * Math.sin(dLon/2);  
    	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));  
    	return R * c;
	}
    
}
