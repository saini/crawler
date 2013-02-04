package ir.assignments.three;
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


import ir.assignments.one.a.Frequency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class BasicCrawlController {
		private static Logger logger = Logger.getLogger(BasicCrawlController.class);
		private static Map<String, Set<String>> subdoaminToPageCountMap = new HashMap<String,Set<String>>();
		//private static List<Frequency> top500WordsList = new LinkedList<Frequency>();
		private static Set<String> uniqueURLSet = new HashSet<String>();
		private static Map<String,Frequency> top500WordsMap = new HashMap<String,Frequency>();
		private static Map<String,Frequency> top20_2Grams = new HashMap<String,Frequency>();
		
		public static void main(String[] args) throws Exception {
        	logger.info("starting the main");
                if (args.length != 2) {
                        System.out.println("Needed parameters: ");
                        System.out.println("\t rootFolder (it will contain intermediate crawl data)");
                        System.out.println("\t numberOfCralwers (number of concurrent threads)");
                        return;
                }

                /*
                 * crawlStorageFolder is a folder where intermediate crawl data is
                 * stored.
                 */
                String crawlStorageFolder = args[0];

                /*
                 * numberOfCrawlers shows the number of concurrent threads that should
                 * be initiated for crawling.
                 */
                int numberOfCrawlers = Integer.parseInt(args[1]);

                CrawlConfig config = new CrawlConfig();

                config.setCrawlStorageFolder(crawlStorageFolder);

                /*
                 * Be polite: Make sure that we don't send more than 1 request per
                 * second (1000 milliseconds between requests).
                 */
                config.setPolitenessDelay(300);

                /*
                 * You can set the maximum crawl depth here. The default value is -1 for
                 * unlimited depth
                 */
                config.setMaxDepthOfCrawling(-1);

                /*
                 * You can set the maximum number of pages to crawl. The default value
                 * is -1 for unlimited number of pages
                 */
                config.setMaxPagesToFetch(-1);

                /*
                 * Do you need to set a proxy? If so, you can use:
                 * config.setProxyHost("proxyserver.example.com");
                 * config.setProxyPort(8080);
                 * 
                 * If your proxy also needs authentication:
                 * config.setProxyUsername(username); config.getProxyPassword(password);
                 */

                /*
                 * This config parameter can be used to set your crawl to be resumable
                 * (meaning that you can resume the crawl from a previously
                 * interrupted/crashed crawl). Note: if you enable resuming feature and
                 * want to start a fresh crawl, you need to delete the contents of
                 * rootFolder manually.
                 */
                config.setResumableCrawling(true);
                
                // set useragent string.
                config.setUserAgentString("UCI IR crawler 12087590, 19162716");
                config.setConnectionTimeout(3000); // 3000 millisec, 3sec.
                config.setSocketTimeout(3000);
                /*
                 * Instantiate the controller for this crawl.
                 */
                PageFetcher pageFetcher = new PageFetcher(config);
                RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
                RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
                CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

                /*
                 * For each crawl, you need to add some seed urls. These are the first
                 * URLs that are fetched and then the crawler starts following links
                 * which are found in these pages
                 */

                controller.addSeed("http://ftp.ics.uci.edu/pub");
              
                /*
                 * Start the crawl. This is a blocking operation, meaning that your code
                 * will reach the line after this only when crawling is finished.
                 */
                long t1 = System.currentTimeMillis();
                controller.start(Crawler.class, numberOfCrawlers);
                long t2 = System.currentTimeMillis();
                logger.info("crawling ends, total time taken "+ (t2-t1));
                Crawler.appendStringToFile("1) Time taken to crawl the entire domain"+ (t2-t1) +" milliseconds.", "answers.txt");
                Gson gson = new Gson();
                System.out.println(gson.toJson(Crawler.maxLengthPage));
                //analyse();
                logger.info("exiting main");
        }
		
		public static void main2 (String[] args){
			readFile(new File(Crawler.CRAWLER_JSON_FILE));
			Gson gson = new Gson();
			
			Crawler.appendStringToFile(gson.toJson(subdoaminToPageCountMap), "subdomain-info.txt");
			logger.info("subdomain-info.txt populated");
			
			//Answers
			Crawler.appendStringToFile("2) Unique pages in ics.uci.edu : "+ uniqueURLSet.size(), "answers.txt");
			logger.info("answer to 2 is printed");
			printSubDomains(); // 3rd Answer
			logger.info("answer to 3 is printed");
			Crawler.appendStringToFile("4) Longest page URL: "+Crawler.maxLengthPage.getUrl(), "answers.txt");
			logger.info("answer to 4 is printed");
			printTop500Words(); // 5th Answer
			logger.info("answer to 5 is printed");
            printTop20_2Grams(); // 6th Answer
            logger.info("answer to 6 is printed");
		}

		private static void readFile(File file){
			logger.info("reading file: "+ file.getName());
        	BufferedReader br = null;
        	try {
				br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(file)));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while(null!=(line = br.readLine())){
					if(line.equals(Crawler.pageDelimiter)){
						String json = sb.toString();
						process(json);
						sb.setLength(0);
					}else{
						sb.append(line);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
        
        private static void process(String json){
        	Gson gson = new Gson();
        	AttributesPage attributesPage = gson.fromJson(json, AttributesPage.class);
        	populateSubdomainMap(attributesPage);
        	getTop500Words(attributesPage);
        	getTop20_2Grams(attributesPage);
        	populateUniqueUrlSet(attributesPage);
        }
        
        private static void populateSubdomainMap(AttributesPage attributesPage){
        	if(subdoaminToPageCountMap.containsKey(attributesPage.getSubDomain())){
        		Set<String> urls = subdoaminToPageCountMap.get(attributesPage.getSubDomain());
        		urls.add(attributesPage.getUrl());
        	}else{
        		Set<String> urls = new HashSet<String>();
        		urls.add(attributesPage.getUrl());
        		subdoaminToPageCountMap.put(attributesPage.getSubDomain(),urls);
        	}
        }
        
        
        
        private static void getTop500Words(AttributesPage attributesPage){
        	for(Frequency freq : attributesPage.getTop500Wrods()){
        		String word = freq.getText();
        		if(top500WordsMap.containsKey(word)){
        			Frequency frequency = top500WordsMap.get(word);
        			frequency.setFrequency(frequency.getFrequency()+freq.getFrequency());
        		}else{
        			Frequency frequency = new Frequency(word,freq.getFrequency());
        			top500WordsMap.put(word,frequency);
        		}
        	}
        }
        
        private static void getTop20_2Grams(AttributesPage attributesPage){
        	for(Frequency freq : attributesPage.getTop20TwoGrams()){
        		String word = freq.getText();
        		if(top20_2Grams.containsKey(word)){
        			Frequency frequency = top20_2Grams.get(word);
        			frequency.setFrequency(frequency.getFrequency()+freq.getFrequency());
        		}else{
        			Frequency frequency = new Frequency(word,freq.getFrequency());
        			top20_2Grams.put(word,frequency);
        		}
        	}
        }
        
        /**
         * populate the unique urls into the private set.
         * @param attributesPage
         */
        private static void populateUniqueUrlSet(AttributesPage attributesPage){
        	uniqueURLSet.add(attributesPage.getUrl());
        }
        
        private static void printSubDomains()
        {
        	Set<Entry<String,Set<String> >> subDomainPageCountset = subdoaminToPageCountMap.entrySet();
        	for(Entry<String,Set<String>> entry: subDomainPageCountset)
        	{
        		Crawler.appendStringToFile(entry.getKey()+"ics.uci.edu,"+entry.getValue().size(), "Subdomains.txt");
        	}
        }
        
        private static void printTop500Words()
		{
			List<Frequency> values = new ArrayList<Frequency>( top500WordsMap.values());
			Collections.sort(values);
			Collections.reverse(values);
			for(Frequency freq:values.subList(0, Math.min(values.size(),500)))
			{
				Crawler.appendStringToFile(freq.toString(), "CommonWords.txt");
			}
//			Crawler.appendStringToFile(gson.toJson(values.subList(0, 500)), "top500wordsMap-json.txt");
		}
        
        private static void printTop20_2Grams()
		{
			List<Frequency> values = new ArrayList<Frequency>( top20_2Grams.values());
			Collections.sort(values);
			Collections.reverse(values);
			for(Frequency freq:values.subList(0, Math.min(values.size(),20)))
			{
				Crawler.appendStringToFile(freq.toString(), "Common2Grams .txt");
			}
//			Crawler.appendStringToFile(gson.toJson(values.subList(0, 29)), "top20_2Grams-json.txt");
		}
        
}
