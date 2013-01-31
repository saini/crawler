package ir.assignments.three;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;


public class RegexTest {
	private static Set<String> noCrawlSet = new HashSet<String>();
	public RegexTest(){
		System.out.println(isValidUrl("http://archive.ics.uci.edu/"));
	}
	private boolean isValidUrl(String input){
		String url = "archive.ics.uci";
		return "archive.ics.uci".matches("^(http://)?"+url+".*");
		/*String regexSubDomain = "^(http://)?(\\w+\\.)*ics.uci.edu";
		String regexDomain = "^(http://)?ics.uci.edu";
		//String testString = "http://ics.uci.edu"; // should match
		//String testString2 = "http://informatics.uci.edu"; // should not match
		Pattern pattern = Pattern.compile(regexSubDomain);
		Matcher matcher = pattern.matcher(input);
		return matcher.find();*/
	}
	
	
	public static void setTimer( ) {
        int delay = 1000; // millisec
        int interval = 5*1000*60; //x*1000*60 = x mins
        Timer timer = new Timer();

        timer.schedule( new TimerTask(){
           public void run() { 
               populateNoCrawlSet();
            }
         }, delay,interval);
   }
	
	
	
      private static void populateNoCrawlSet(){
      	BufferedReader br = null;
      	try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream("nocrawl.txt")));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while(null!=(line = br.readLine())){
					if(!noCrawlSet.contains(line.trim())){
						noCrawlSet.add(line.trim());
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
      }
	
	public static void main(String [] args){
		new RegexTest();
		//setTimer();
		/*Gson gson = new Gson();
		while(true){
			System.out.println(gson.toJson(noCrawlSet));
		}*/
	}
}
