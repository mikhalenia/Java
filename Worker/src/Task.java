package myworker;
import java.util.concurrent.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import static java.util.concurrent.TimeUnit.*;
import static myworker.Config.*;
import static myworker.JDBC.*;

public class Task implements Runnable
{
	public static final boolean FAKE=true;
	private static int count=0;
	private final int id=count++;
	private boolean isFakeTask=false;
	private String curUrl;
	private int workerId;
	private int page=0;
	private static final int COUNT_ON_PAGE=30;
	private static HashMap<String,String> yandexConfig=getYandexConfig();
	public Task(int page)
	{
		this.page=page;
	}
	public Task(boolean isFakeTask)
	{
		this.isFakeTask=isFakeTask;
	}
	public static String getUrl(HashMap<String,String> pathParams,HashMap<String,String> getParams)
	{
		if(pathParams.isEmpty())
			return null;
		String link=yandexConfig.get("url")+"/"+yandexConfig.get("version")+"/";
		if(pathParams.containsKey("path"))
			link+=pathParams.get("path");
		if(pathParams.containsKey("format"))
			link+="."+pathParams.get("format");
		if(getParams.isEmpty())
			return link;
		link+="?";
		String[] get=new String[getParams.size()];
		int i=0;
		for(String key:getParams.keySet())
			get[i++]=key+"="+getParams.get(key);
		link+=implodeArray(get,"&");
		return link;
	}
	public static String getUrl(HashMap<String,String> pathParams)
	{
		return getUrl(pathParams,null);
	}
	public static String getContentFromYandexAPI(String link)
	{
		if(link=="")
			return null;
		try
		{
			URL url= new URL(link);
			URLConnection connect=url.openConnection();
			connect.setRequestProperty("Authorization",yandexConfig.get("key"));
			BufferedReader in=new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String inputLine,result="";
			while((inputLine=in.readLine())!=null)
				result+=inputLine;
			in.close();
			return result;
		}
		catch(IOException e)
		{
			System.out.println("Sorry, cant load content...");
			e.printStackTrace(System.out);
			return null;
		}
	}
	public static String implodeArray(String[] inputArray,String glueString)
	{
		String output="";
		if(inputArray.length==0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(inputArray[0]);
		for (int i=1; i<inputArray.length; i++)
		{
			sb.append(glueString);
			sb.append(inputArray[i]);
		}
		output=sb.toString();
		return output;
	}
	public static void saveInfo(String content)
	{
		JSONObject jsonObject=new JSONObject(content);
		JSONObject innerObject=jsonObject.getJSONObject("vendorList");
		JSONArray jsonArray=innerObject.getJSONArray("vendor");
		int size=jsonArray.length();
		String[] makerValues=new String[size];
		for(int i=0;i<size;i++)
		{
			JSONObject objectInArray=jsonArray.getJSONObject(i);
			String categories=objectInArray.getJSONArray("categories").length()==0?"0":"1";
			String site=objectInArray.has("site")?objectInArray.getString("site"):"";
			String picture=objectInArray.has("picture")?objectInArray.getString("picture"):"";
			makerValues[i]="("+
				Integer.toString(objectInArray.getInt("id"))+","+
				"'"+StringEscapeUtils.escapeSql(objectInArray.getString("name"))+"',"+
				"'"+StringEscapeUtils.escapeSql(site)+"',"+
				"'"+StringEscapeUtils.escapeSql(picture)+"',"+
				"'"+StringEscapeUtils.escapeSql(categories)+"',"+
				"1"+
			")";
		}
		try
		{
			Connection dbConnect=getDBConnection();
			dbConnect.prepareStatement(
				"INSERT DELAYED INTO makers(id,name,site,picture,categories,status)"+
				"VALUES "+implodeArray(makerValues,",")+
				"ON DUPLICATE KEY UPDATE "+
					"picture=VALUES(picture),"+
					"categories=VALUES(categories),"+
					"status=VALUES(status)"
			).execute();
		}
		catch(SQLException e)
		{
			System.out.println("Sorry, can't save the content...");
			e.printStackTrace(System.out);
		}
	}
	public void setWorkerId(int workerId)
	{
		this.workerId=workerId;
	}
	private void runFakeTask()
	{
		try
		{
			Connection dbConnect=getDBConnection();
			dbConnect.prepareStatement("UPDATE makers SET status=1-status").execute();
		}
		catch(SQLException e)
		{
			System.out.println("Sorry, can't change status...");
			e.printStackTrace(System.out);
		}
		Thread.currentThread().interrupt();
	}
	public void run()
	{
		if(isFakeTask)
		{
			runFakeTask();
			return;
		}
		RateLimit.check();
		HashMap<String,String> pathParams=new HashMap<String,String>();
		HashMap<String,String> getParams=new HashMap<String,String>();
		pathParams.put("path","vendor");
		pathParams.put("format","json");
		getParams.put("page",Integer.toString(page));
		getParams.put("count",Integer.toString(COUNT_ON_PAGE));
		this.curUrl=getUrl(pathParams,getParams);
		String content=getContentFromYandexAPI(this.curUrl);
		saveInfo(content);
		System.out.println(this);
	}
	public String toString()
	{
		return "Worker#"+this.workerId+"Thread#"+Thread.currentThread().getId()+"Task#"+id+"link#"+this.curUrl;
	}
}