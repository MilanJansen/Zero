package com.alliander.generator.file;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.sound.sampled.Line;
import java.io.*;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class CoinmarketcapChecker {
    /**
     * URL of page
     */
    private static final String URI = "https://coinmarketcap.com";

    public static void main(String[] args) throws Exception {
        BufferedReader buff_old = new BufferedReader(new FileReader("currency.txt"));
        String line_old;
        Map<String, String> map_old = new LinkedHashMap<String, String>();
        while ((line_old = buff_old.readLine()) != null) {
            String[] keyval = line_old.split(",");
            map_old.put(keyval[1],keyval[2]);
        }
        System.out.println(map_old);




        Document doc = Jsoup.connect(URI).get();
        Elements cryptoHTMLClasses = doc.select("tbody tr");
        FileWriter writer = new FileWriter("currency_new.txt");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        //writer.write(dateFormat.format(date) + "\r\n"); //2016/11/16 12:08:43
        for (Element cryptoHTMLElement : cryptoHTMLClasses) {
            String indexNumber = cryptoHTMLElement.select(".text-center").text();
            String cryptoName = cryptoHTMLElement.select(".currency-name-container").text();
            String cryptoPrice = cryptoHTMLElement.select(".price").text();
            //System.out.println(indexNumber + ". Name: " + cryptoName + " | Price: " + cryptoPrice);
            //System.out.println(cryptoPrice);
            writer.write(indexNumber + "," + cryptoName + "," + cryptoPrice.replace("$","") + "\r\n");
        }
        writer.close();



        BufferedReader buff_new = new BufferedReader(new FileReader("currency_new.txt"));
        String line_new;
        Map<String, String> map_new = new LinkedHashMap<String, String>();
        while ((line_new = buff_new.readLine()) != null) {
            String[] keyval = line_new.split(",");
            map_new.put(keyval[1],keyval[2]);
            }
        System.out.println(map_new);


        for (String key : map_new.keySet()){
            if (map_old.get(key) == null){
                break;
            }
            //String old_str = map_old.get(key);
            //System.out.println(old_str);
            //System.out.println(value_old);
            float value_old = Float.valueOf(map_old.get(key));
            float value_new = Float.valueOf(map_new.get(key));
            float change = value_old - value_new;
            //System.out.println(change);
            float change_percent = change/value_old;
            System.out.print(String.format("%1$20s",key)+"\t");
            System.out.printf(String.format("%1$10s",(value_new)));
            System.out.printf(String.format("%1$15s", (change)));
            System.out.print("\t");
            System.out.printf("%.2f", change_percent);
            System.out.print("%\n");
            //System.out.println(change_percent);
            //System.out.println(map_old.get(key));

        }






    }
}




