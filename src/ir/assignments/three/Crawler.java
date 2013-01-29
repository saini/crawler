package ir.assignments.three;

import ir.assignments.one.a.Frequency;
import ir.assignments.one.a.Utilities;
import ir.assignments.one.b.WordFrequencyCounter;
import ir.assignments.one.c.TwoGramFrequencyCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class Crawler extends WebCrawler {
		private static List stopwords;
		private static List stopwordsFreq;
		public static final String CRAWLER_JSON_FILE = "crawler.json.txt";
		public static final String pageDelimiter = "**-**-**-**-**-**-**";
		public static final String PAGES_FOLDER = "crawled_pages";
		static{
			try {
				stopwords = Utilities.tokenizeFile(new File("stopwords.rtf"));
				stopwordsFreq = WordFrequencyCounter.computeWordFrequencies(stopwords);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		public static AttributesPage maxLengthPage = new AttributesPage();
		public static int maxLength=0;
        private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
                        + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

        /**
         * You should implement this function to specify whether the given url
         * should be crawled or not (based on your crawling logic).
         */
        @Override
        public boolean shouldVisit(WebURL url) {
                String href = url.getURL().toLowerCase();
                return !FILTERS.matcher(href).matches() && href.contains("ics.uci.edu");
        }

        /**
         * This function is called when a page is fetched and ready to be processed
         * by your program.
         */
        @Override
        public void visit(Page page) {
                int docid = page.getWebURL().getDocid();
                String url = page.getWebURL().getURL();
                String domain = page.getWebURL().getDomain();
                String path = page.getWebURL().getPath();
                String subDomain = page.getWebURL().getSubDomain();
                String parentUrl = page.getWebURL().getParentUrl();
                System.out.println("new page");

                System.out.println("Docid: " + docid);
                System.out.println("URL: " + url);
                System.out.println("Domain: '" + domain + "'");
                System.out.println("Sub-domain: '" + subDomain + "'");
                System.out.println("Path: '" + path + "'");
                System.out.println("Parent page: " + parentUrl);
                String text = null;
                AttributesPage attributesPage = new AttributesPage();
                if (page.getParseData() instanceof HtmlParseData) {
                        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                        text = htmlParseData.getText();
                        String html = htmlParseData.getHtml();
                        List<WebURL> links = htmlParseData.getOutgoingUrls();
                        System.out.println("Text length: " + text.length());
                        System.out.println("Html length: " + html.length());
                        System.out.println("Number of outgoing links: " + links.size());
                        
                        List<Frequency>freqList = WordFrequencyCounter.computeWordFrequencies(Arrays.asList(text.split(" ")));
                        freqList.removeAll(stopwordsFreq);
                        int characterCount=0;
                        for(Frequency frequency: freqList){
                        	characterCount = characterCount + frequency.getFrequency();
                        }
                        attributesPage.setTop500Wrods(freqList);
                        List<Frequency> twoGramFreqList =  TwoGramFrequencyCounter.computeTwoGramFrequencies(new ArrayList<String>(Arrays.asList(text.split(" "))));
                        attributesPage.setTop20TwoGrams(twoGramFreqList);
                        attributesPage.setLength(characterCount);
                        if(maxLength<characterCount){
                        	maxLength=characterCount;
                        	maxLengthPage = attributesPage;
                        }
                        //attributesPage.setText(text);
                }
                // create object of AttributesPage
                attributesPage.setDomain(domain);
                attributesPage.setSubDomain(subDomain);
                attributesPage.setUrl(url);
                // get top 500 words of this page.
                printToFile(attributesPage);
                Gson gson = new Gson();
                appendStringToFile(gson.toJson(page),PAGES_FOLDER+System.getProperty("file.separator")+url+".txt");
                System.out.println("=============");
        }
        
        private void printToFile(AttributesPage attributesPage){
        	Gson gson = new Gson();
        	String json = gson.toJson(attributesPage);
        	appendStringToFile(json,CRAWLER_JSON_FILE);
        	appendStringToFile("\n"+pageDelimiter+"\n",CRAWLER_JSON_FILE);
        }
        
        public static void appendStringToFile(String text, String filename){
        	FileWriter fileWriter = null;
        	try{
        		File file = new File(filename);
            	fileWriter = new FileWriter(file, true);
            	fileWriter.write(text);
        	} catch (IOException e) {
				e.printStackTrace();
			}finally{
        		try {
					fileWriter.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        	}
        }
        
        /**
    	 * This method is for testing purposes only. It does not need to be used
    	 * to answer any of the questions in the assignment. However, it must
    	 * function as specified so that your crawler can be verified programatically.
    	 * 
    	 * This methods performs a crawl starting at the specified seed URL. Returns a
    	 * collection containing all URLs visited during the crawl.
    	 */
    	public static Collection<String> crawl(String seedURL) {
    		// TODO implement me
    		return null;
    	}
}
