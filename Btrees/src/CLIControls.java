package btrees;
import btrees.controller.*;
import static btrees.mindview.Print.*;
import java.io.*;
import java.util.*;

public class CLIControls extends Controller
{
	private static BTree<String, String> btree = new BTree<String, String>();
	private static RandomAccessFile bTreeFile;
	public static class BTreeLoad extends Event
	{
		public BTreeLoad(String[] args){super(args);}
		public void read() throws IOException 
		{
			bTreeFile = new RandomAccessFile("btree.bt", "rwd");
			String s;
			while((s = bTreeFile.readLine()) != null)
			{
				String[] line = s.split(" ");
				if(line.length <= 1)
				{
					bTreeFile.close();
					print("Can't initialize");
					System.exit(0);
				}
				btree.put(line[0], line[1]);
			}
		}
		public void action()
		{
			try{read();} 
			catch(IOException e){
				print("Can't load tree");
				System.exit(0);
			}
		}
	}
	public class Push extends Event 
	{
		public Push(String[] args){super(args);}
		public void action()
		{
			if(args.length < 3)
			{
				print("Enter the key and value");
				return;
			}
			try{
				bTreeFile.writeBytes(args[1] + " " + args[2] + "\n");
				btree.put(args[1], args[2]);
			} 
			catch(IOException e){
				print("Can't push in tree");
				System.exit(0);
			}
		}
	}
	public class Get extends Event 
	{
		public Get(String[] args){super(args);}
		public void action()
		{
			if(args.length < 2)
			{
				print("Enter the key");
				return;
			}
			print(args[1] + " : " + btree.get(args[1]));
		}
	}
	public class Info extends Event 
	{
		public Info(String[] args){super(args);}
		public void action()
		{
			System.out.println("size:    " + btree.size());
			System.out.println("height:  " + btree.height());
			System.out.print(btree);
		}
	}
	public class Help extends Event 
	{
		public Help(String[] args){super(args);}
		public void action()
		{
			List<String[]> commands = new ArrayList<String[]>();
			commands.add(new String[] {"push", "Push line"});
			commands.add(new String[] {"search", "Search info"});
			commands.add(new String[] {"showinfo", "Show all info"});			
			commands.add(new String[] {"help", "Help"});
			commands.add(new String[] {"exit", "Exit"});
			printTable(commands, true);
		}
	}
	public class Exit extends Event 
	{
		public Exit(String[] args){super(args);}
		public void action()
		{
			print("Bye!");
			System.exit(0);
		}
	}
	public class CommandNotExist extends Event
	{
		public CommandNotExist(String[] args){super(args);}
		public void action()
		{
			print("The command " + args[0] + " does not exist!");
		}
	}
}
