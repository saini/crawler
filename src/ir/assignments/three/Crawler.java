package ir.assignments.three;

import ir.assignments.one.a.Frequency;
import ir.assignments.one.a.Utilities;
import ir.assignments.one.b.WordFrequencyCounter;
import ir.assignments.one.c.TwoGramFrequencyCounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

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
	private static Logger logger = Logger.getLogger(Crawler.class);
	private static List<String> stopwords;
	private static List<Frequency> stopwordsFreq;
	public static final String CRAWLER_JSON_FILE = "crawler.json.txt";
	public static final String pageDelimiter = "**-**-**-**-**-**-**";
	public static final String PAGES_FOLDER = "crawled_pages";
	public static String regexSubDomain;
	public static Pattern pattern;
	private static Set<String> noCrawlSet = new HashSet<String>();
	static {
		try {
			stopwords = Utilities.tokenizeFile(new File("stopwords.rtf"));
			stopwordsFreq = WordFrequencyCounter
					.computeWordFrequencies(stopwords);
			new File(PAGES_FOLDER).mkdirs();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		regexSubDomain = "^(http://)?(\\w+\\.)*ics.uci.edu";
		// String testString = "http://ics.uci.edu"; // should match
		// String testString2 = "http://informatics.uci.edu"; // should not
		// match
		pattern = Pattern.compile(regexSubDomain);
		setTimer();
	}
	private static Set<String> hrefSet = new HashSet<String>();
	private static Set<String> subDomainSet = new HashSet<String>();
	
	public static AttributesPage maxLengthPage = new AttributesPage();
	public static int maxLength = 0;
	private final static Pattern FILTERS = Pattern
			.compile(".*(\\.(css|js|bmp|gif|jpe?g"
					+ "|png|tiff?|mid|mp2|mp3|mp4"
					+ "|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz|icsgz|tar" +
					"|exe|doc|ppt|mpg|tif|psd|xls|ps|tgz|7z|iso))$");

	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		if (href.indexOf("?") != -1) {
			href = href.split("[?]")[0];
		}
		if (isValidUrl(href) && !hrefSet.contains(href)) {
			hrefSet.add(href);
			return !FILTERS.matcher(href).matches();
		}
		return false;

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
		// add new sub domain to file. bookkeeping only
		if (!subDomainSet.contains(subDomain)) {
			subDomainSet.add(subDomain);
			appendStringToFile(subDomain + "\n", "subdomainList.txt");
		}
		String parentUrl = page.getWebURL().getParentUrl();
		logger.info("new page");

		logger.info("Docid: " + docid);
		logger.info("URL: " + url);
		logger.info("Domain: '" + domain + "'");
		logger.info("Sub-domain: '" + subDomain + "'");
		logger.info("Path: '" + path + "'");
		logger.info("Parent page: " + parentUrl);
		String text = null;
		AttributesPage attributesPage = new AttributesPage();
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			
			text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			List<WebURL> links = htmlParseData.getOutgoingUrls();
			logger.info("Text length: " + text.length());
			logger.info("Html length: " + html.length());
			logger.info("Number of outgoing links: " + links.size());

			List<Frequency> freqList = WordFrequencyCounter
					.computeWordFrequencies(Arrays.asList(text.split(" ")));
			freqList.removeAll(stopwordsFreq);
			int characterCount = 0;
			for (Frequency frequency : freqList) {
				characterCount = characterCount + frequency.getFrequency();
			}
			attributesPage.setTop500Wrods(freqList);
			List<Frequency> twoGramFreqList = TwoGramFrequencyCounter
					.computeTwoGramFrequencies(new ArrayList<String>(Arrays
							.asList(text.split(" "))));
			attributesPage.setTop20TwoGrams(twoGramFreqList);
			attributesPage.setLength(characterCount);
			if (maxLength < characterCount) {
				maxLength = characterCount;
				maxLengthPage = attributesPage;
			}
			htmlParseData.setHtml("");
			// attributesPage.setText(text);
		}
		// create object of AttributesPage
		attributesPage.setDomain(domain);
		attributesPage.setSubDomain(subDomain);
		attributesPage.setUrl(url);
		// get top 500 words of this page.
		printToFile(attributesPage);
		Gson gson = new Gson();
		page.setContentData(null);

		try {
			appendStringToFile(gson.toJson(page),
					PAGES_FOLDER + System.getProperty("file.separator")
							+ URLEncoder.encode(url, "ISO-8859-1") + ".txt");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("=============");
	}

	public static void setTimer() {
		populateNoCrawlSet();
		int delay = 1000; // millisec
		int interval = 5 * 1000 * 60; // x*1000*60 = x mins
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				populateNoCrawlSet();
			}
		}, delay, interval);
	}

	private static void populateNoCrawlSet() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					"nocrawl.txt")));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while (null != (line = br.readLine())) {
				if (!noCrawlSet.contains(line.trim())) {
					noCrawlSet.add(line.trim());
					logger.info(line +" added to noCrawlset");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * mreturn true if url is a domain or subdomain of ics.uci.edu
	 * 
	 * @param input
	 * @return
	 */
	private boolean isValidUrl(String input) {
		Iterator<String> it = noCrawlSet.iterator();
		while(it.hasNext()){
			String url = it.next();
			if(input.matches("^(http://)?"+url+".*")){
				logger.info("\n\n***********************************************************ignoring url********************"+ input);
				return false;
			}
		}
		// subdomain or main domain should match
		Matcher matcher = pattern.matcher(input);
		return matcher.find();
	}

	private void printToFile(AttributesPage attributesPage) {
		Gson gson = new Gson();
		String json = gson.toJson(attributesPage);
		appendStringToFile(json, CRAWLER_JSON_FILE);
		appendStringToFile("\n" + pageDelimiter + "\n", CRAWLER_JSON_FILE);
	}

	public static void appendStringToFile(String text, String filename) {
		FileWriter fileWriter = null;
		try {
			File file = new File(filename);
			fileWriter = new FileWriter(file, true);
			fileWriter.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method is for testing purposes only. It does not need to be used to
	 * answer any of the questions in the assignment. However, it must function
	 * as specified so that your crawler can be verified programatically.
	 * 
	 * This methods performs a crawl starting at the specified seed URL. Returns
	 * a collection containing all URLs visited during the crawl.
	 */
	public static Collection<String> crawl(String seedURL) {
		// TODO implement me
		return null;
	}
}
