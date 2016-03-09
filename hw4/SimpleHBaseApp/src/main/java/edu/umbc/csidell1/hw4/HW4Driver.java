package edu.umbc.csidell1.hw4;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

public class HW4Driver {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		//So we're not waiting forever...
		int readAmt = 20;
		String hardCodeGet = "433103646511751168";
		
		BufferedReader TSVFile = new BufferedReader(new FileReader("tweets.tsv"));
		String dataRow = TSVFile.readLine();
		String[] title = {"tweet_id","user_id","date","source","text"}; 
		Configuration config = HBaseConfiguration.create();
		
		HTable table = new HTable(config, "tweets");
		
		int num = 0;
		while(dataRow != null && num++ < readAmt)
		{
			String[] arr = dataRow.toString().split("\\t");
			//tweet_id user_id date source text
			for(int i = 0; i < 5; i++)
			{
				byte[] cf,attr;
				
				switch(title[i])
				{
				case "text":
					cf = "messages".getBytes();
					attr = title[i].getBytes();
					break;
				default:
					cf = "metadata".getBytes();
					attr = title[i].getBytes();
				}
				Put put = new Put(Bytes.toBytes(arr[0]));
				put.add(cf,attr,Bytes.toBytes(arr[i]));
				
				table.put(put);
				//System.out.println(title[i]+": "+arr[i]);
			}		
			dataRow = TSVFile.readLine();
		}
		
		TSVFile.close();
		
		Get get = new Get(Bytes.toBytes(hardCodeGet));
		
		Result result = table.get(get);
		
		String[] values = new String[title.length];
		for(int i = 0; i < title.length; i++)
		{
			if(title[i].equals("text")) continue;
			byte[] value = result.getValue(Bytes.toBytes("metadata"),Bytes.toBytes(title[i]));
			values[i] = Bytes.toString(value);
			
			System.out.println(hardCodeGet+"\t metadata:"+title[i]+"\t"+values[i]);
		}
		
		//System.out.println("user_id: "+values[Arrays.asList(title).indexOf("user_id")]);
		
		Filter filter = new PageFilter(10);
		
		Scan scan = new Scan();
		scan.addColumn(Bytes.toBytes("messages"), Bytes.toBytes("text"));
		scan.setFilter(filter);
		
		ResultScanner scanner = table.getScanner(scan);
		
		int i =0;
		for (Result res = scanner.next(); res != null && i++ < 10; res = scanner.next())
		{
			byte[] value =res.getValue(Bytes.toBytes("messages"),Bytes.toBytes("text"));
			System.out.println(Bytes.toString(value)+"\n");
		}
	      //closing the scanner
	      scanner.close();
		
	}

}
