package ir.assignments.three;

import ir.assignments.one.a.Frequency;

import java.util.ArrayList;
import java.util.List;

public class AttributesPage {
	private String url;
	private String domain;
	private String subDomain;
	private int length;
	private String text;
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	private List<Frequency> top500Wrods = new ArrayList<Frequency>();
	private List<Frequency> top20TwoGrams = new ArrayList<Frequency>();
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the subDomain
	 */
	public String getSubDomain() {
		return subDomain;
	}
	/**
	 * @param subDomain the subDomain to set
	 */
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	/**
	 * @return the top500Wrods
	 */
	public List<Frequency> getTop500Wrods() {
		return top500Wrods;
	}
	/**
	 * @param top500Wrods the top500Wrods to set
	 */
	public void setTop500Wrods(List<Frequency> top500Wrods) {
		this.top500Wrods = top500Wrods;
	}
	/**
	 * @return the top20TwoGrams
	 */
	public List<Frequency> getTop20TwoGrams() {
		return top20TwoGrams;
	}
	/**
	 * @param top20TwoGrams the top20TwoGrams to set
	 */
	public void setTop20TwoGrams(List<Frequency> top20TwoGrams) {
		this.top20TwoGrams = top20TwoGrams;
	}

}
