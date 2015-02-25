package IllegalDetector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import IllegalDetector.Basic.record;

public class sort {
	
	public static TreeSet<record> rlist = new TreeSet<record>();
	public static String toBeSorted = "a";
	public static String sort_output = "a";
	
	public static void main(String args[]) throws IOException {
		
		InputStreamReader isr2 = new InputStreamReader(new FileInputStream(toBeSorted));
		BufferedReader br2 = new BufferedReader(isr2);
		
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
			
			rlist.add(r);
			
		}
		br2.close();
		
		
		Iterator<record> iter = rlist.iterator();
		PrintWriter pwt = new PrintWriter(new FileWriter(sort_output));
		while(iter.hasNext()) {
			pwt.println(iter.next().orginal);
		}
		pwt.close();
	}
	
	
}
