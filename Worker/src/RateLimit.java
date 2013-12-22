package myworker;
import java.util.concurrent.*;
import java.util.*;
import static java.util.concurrent.TimeUnit.*;

public class RateLimit
{
	private static final int RATELIMIT=8;
	public static final int ONESECOND=1000;
	private static Long[] timeBuffer=new Long[RATELIMIT];
	private static Boolean isInit=false;
	private static synchronized long getDelay()
	{
		if(!isInit)
		{
			isInit=initBuffer();
			return 0;
		}
		long tm=System.currentTimeMillis();
		long delta=tm-timeBuffer[0];
		offSet(tm);
		if(delta>=ONESECOND)
			return 0;
		return ONESECOND-delta;
	}
	private static Boolean initBuffer()
	{
		for(int i=0;i<timeBuffer.length;i++)
		{
			if(timeBuffer[i]!=null)
				continue;
			timeBuffer[i]=System.currentTimeMillis();
			if(i==timeBuffer.length-1)
				return true;
			return false;
		}
		return true;
	}
	private static void offSet(long tm)
	{
		for(int i=0;i<timeBuffer.length;i++)
			if(i==timeBuffer.length-1)
				timeBuffer[i]=tm;
			else
				timeBuffer[i]=timeBuffer[i+1];
	}
	private static void pause(long delay) 
	{
		try
		{
			TimeUnit.MILLISECONDS.sleep(delay);
		} 
		catch(InterruptedException e) 
		{
			e.printStackTrace(System.out);
		}
	}
	public static void check()
	{
		long delay=getDelay();
		if(delay!=0)
			pause(delay);
	}
}