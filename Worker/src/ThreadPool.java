package myworker;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.nio.channels.*;
import java.util.concurrent.*;
import org.json.JSONArray;
import org.json.JSONObject;
import static myworker.Task.*;
import static java.util.concurrent.TimeUnit.*;

public class ThreadPool
{
	private static final int COUNT_ON_PAGE=30;
	private static final int COUNT_WORKERS=8;
	private static final int PAUSE_BEFORE_FAKE_TASK=RateLimit.ONESECOND*20;
	private static int page=0;
	private static int getCountAndSave()
	{
		HashMap<String,String> pathParams=new HashMap<String,String>();
		HashMap<String,String> getParams=new HashMap<String,String>();
		pathParams.put("path","vendor");
		pathParams.put("format","json");
		getParams.put("page",Integer.toString(++page));
		getParams.put("count",Integer.toString(COUNT_ON_PAGE));
		String link=getUrl(pathParams,getParams);
		System.out.println(link);
		String content=getContentFromYandexAPI(link);
		saveInfo(content);
		JSONObject jsonObject=new JSONObject(content);
		return (int)(jsonObject.getJSONObject("vendorList").getInt("total")/COUNT_ON_PAGE);
	}
	private static void logException(Exception e)
	{
		Logger logger=Logger.getLogger("ThreadPool");
		StringWriter trace=new StringWriter();
		e.printStackTrace(new PrintWriter(trace));
		logger.severe(trace.toString());
		System.exit(0);
	}
	private static void importData()
	{
		int countPages=0;
		try{countPages=getCountAndSave();}catch(Exception e){logException(e);}
		TaskQueue tq=new TaskQueue(COUNT_WORKERS);
		for(int i=page+1;i<countPages;i++)
			try{tq.addTask(new Task(i));}catch(Exception e){logException(e);}
		while(!tq.isEmpty())
			try{TimeUnit.MILLISECONDS.sleep(RateLimit.ONESECOND);}catch(Exception e){logException(e);}
		try{TimeUnit.MILLISECONDS.sleep(PAUSE_BEFORE_FAKE_TASK);}catch(Exception e){logException(e);}
		tq.addTask(new Task(Task.FAKE));
	}
	private static void unlock(FileLock lock)
	{
		try
		{
			if(lock!=null)
				lock.release();
		}
		catch(Exception e)
		{
			logException(e);
		}
	}
	public static void main(String[] args)
	{
		FileLock lock=null;
		try
		{
			FileChannel channel=new RandomAccessFile(new File("./classes/fakeFile"),"rw").getChannel();
			lock=channel.tryLock();
			if(lock==null)
			{
				System.out.println("Sorry, import runs...");
				System.exit(0);
			}
			importData();
		}
		catch(Exception e)
		{
			logException(e);
		}
		finally
		{
			unlock(lock);
		}
	}
}