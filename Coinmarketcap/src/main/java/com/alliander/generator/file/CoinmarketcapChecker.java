package com.alliander.generator.file;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CoinmarketcapChecker {
	/** URL of page */
	private static final String URI = "https://coinmarketcap.com";

	public static void main(String[] args) throws Exception {
		Document doc = Jsoup.connect(URI).get();
        Elements cryptoHTMLClasses = doc.select("tbody tr");
        for(Element cryptoHTMLElement : cryptoHTMLClasses) {
            String indexNumber = cryptoHTMLElement.select(".text-center").text();
            String cryptoName = cryptoHTMLElement.select(".currency-name-container").text();
            String cryptoPrice = cryptoHTMLElement.select(".price").text();
            System.out.println(indexNumber + ". Name: " + cryptoName + " | Price: " + cryptoPrice);
        }
	}
}