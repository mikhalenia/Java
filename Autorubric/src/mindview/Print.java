package autorubric.mindview;
import java.io.*;
import java.util.*;

public class Print 
{
	// Print with a newline:
	public static void print(Object obj) 
	{
		System.out.println(obj);
	}
	// Print a newline by itself:
	public static void print() 
	{
		System.out.println();
	}
	// Print with no line break:
	public static void printnb(Object obj) 
	{
		System.out.print(obj);
	}
	// Print table
	private static int interval = 20;
	public static void printTable(List<String[]> list, boolean isArray)
	{
		if(list.isEmpty())
			return;
		print("---");
		for(String[] args : list)
		{
			for(String arg : args)
			{
				System.out.print(arg);
				printf("%" + Integer.toString(interval - arg.length()) + "s", " ");
			}
			System.out.println();
		}
		print("---");
	}
	public static void printTable(List<String> list)
	{
		if(list.isEmpty())
			return;
		print("---");
		for(String arg : list)
		{
			print(arg);
		}
		print("---");
	}
	public static void printTable(Map<String, List<String>> map)
	{
		if(map.isEmpty())
			return;
		print("---");
		String sInterval = Integer.toString(interval);
		for(String key : map.keySet())
		{ 
			System.out.print(key);
			printf("%" + Integer.toString(interval - key.length()) + "s", " ");
			List<String> list = map.get(key);
			int i = 0;
			for(String keyList: list)
			{
				if(i != 0)
				{
					printf("%" + sInterval + "s", " ");
					print(keyList);
				}
				else
				{
					print(keyList);
					i++;
				}
			}
		}
		print("---");
	}
	public static void printTable(String[] list)
	{
		if(list.length == 0)
			return;
		print("---");
		for(String arg : list)
			print(arg);
		print("---");
	}
	// The new Java SE5 printf() (from C):
	public static PrintStream
	printf(String format, Object... args) 
	{
		return System.out.printf(format, args);
	}
}
