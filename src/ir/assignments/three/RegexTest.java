package ir.assignments.three;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;


public class RegexTest {
	private static Set<String> noCrawlSet = new HashSet<String>();
	public RegexTest(){
		System.out.println(isValidUrl("http://ftp.ics.uci.edu/dsfsdf"));
		//System.out.println(getCleanWord("father?"));
	}
	private boolean isValidUrl(String input){
		String url = "archive.ics.uci";
		if("http://ftp.ics.uci.edu/pub/adaptive/".indexOf("public_ftp")!=-1){
			return false;
		}
		return true;
		//return "archive.ics.uci".matches("^(http://)?"+url+".*");
		/*String regexSubDomain = "^(http://ftp).ics.uci.edu";
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
	public static List<String> getCleanWord(String str){
		ArrayList<String> result = null;
        try
        {
            result = new ArrayList<String>();
            str = str.trim().toLowerCase();
            StringBuffer strbuf = new StringBuffer();
            for(int index = 0 ; index< str.length() ; index++)
            {
                char c = str.charAt(index);
                if( ((c-'a')>=0 && (c-'a')<26)  || ((c-'0')>=0 && (c-'0')<=9 ) )
                {
                    strbuf.append(c);
                }
                else if(strbuf.length()>0)
                {
                    result.add(strbuf.toString());
                     strbuf.setLength(0);
                 }
            }
            if(strbuf.length()!=0){
            	result.add(strbuf.toString());
            }
        }catch(Exception e)
        {
            System.out.println(e.toString());
        }
        return result;
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
