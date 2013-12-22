package zipf;
import zipf.controller.*;
import static zipf.mindview.Print.*;
import java.util.regex.*;
import java.io.*;
import java.util.*;

public class CLIControls extends Controller
{
	private Map<String, Integer> words = new HashMap<String, Integer>();
	private static List<String> stopWords = new ArrayList<String>();
	private Map<String, Map<String, Integer>> dictionary = new HashMap<String, Map<String, Integer>>();
	private String splitRegexp = "[ ,.!();:1234567890-]";
	public class DirFile extends Event
	{
		private String dirPath = ".";
		public DirFile(String[] args){super(args);}
		public void action()
		{
			if(args.length > 1)
				dirPath = args[1];
			File path = new File(dirPath);
			if(!path.isDirectory())
			{
				print("Path is not correct!");
				return;
			}
			String[] list;
			list = path.list(new FilenameFilter() 
			{
				private Pattern pattern = Pattern.compile(".*.txt");
				public boolean accept(File dir, String name) 
				{
					return pattern.matcher(name).matches();
				}
			});
			if(list.length == 0)
			{
				print("There's nothing here");
				return;
			}
			Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
			printTable(list);
		}
	}
	public class Documents extends Event 
	{
		private final double RANK_START = 0.3;
		private final double RANK_END = 0.7;
		public Documents(String[] args){super(args);}
		public class ValueComparator implements Comparator<String>
		{
			Map<String, Integer> base;
			public ValueComparator(Map<String, Integer> base) 
			{
				this.base = base;
			}    
			public int compare(String a, String b) 
			{
				if (base.get(a) >= base.get(b))
					return -1;
				else 
					return 1;
			}
		}
		public void readWords(String filename) throws IOException
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s;
			while((s = in.readLine()) != null)
			{
				String[] newWords = s.split(splitRegexp);
				for(String word : newWords)
				{
					word = word.trim();
					if(word.length() <= 3 || stopWords.contains(word))
						continue;
					word = word.toLowerCase();
					if(words.containsKey(word))
					{
						int value = words.get(word);
						words.put(word, ++value);
					}
					else
						words.put(word, 1);
				}
			}
			in.close();
		}
		private Map<Integer, List<String>> getRankWords()
		{
			ValueComparator comparator =  new ValueComparator(words);
			Map<String, Integer> sortedWords = new TreeMap<String, Integer>(comparator);
			sortedWords.putAll(words);
			int frequency = -1, rank = 0;
			Map<Integer, List<String>> rankWords = new HashMap<Integer, List<String>>();
			List<String> curentRankWords = new ArrayList<String>();
			for(Map.Entry<String, Integer> entry : sortedWords.entrySet())
			{
				int currentFrequency = entry.getValue();
				if(frequency == currentFrequency)
				{
					curentRankWords.add(entry.getKey());
					continue;
				}
				if(!curentRankWords.isEmpty())
				{
					rank++;
					rankWords.put(rank, curentRankWords);
					curentRankWords = new ArrayList<String>();
				}
				curentRankWords.add(entry.getKey());
				frequency = currentFrequency;
			}
			return rankWords;
		}
		private void addWord(String word, String filename)
		{
			Map<String, Integer> document = new HashMap<String, Integer>();
			if(dictionary.containsKey(word))
			{
				document = dictionary.get(word);
				if(document.containsKey(filename))
					return;
				document = new HashMap<String, Integer>();
				document.put(filename, words.get(word));
				dictionary.put(word, document);
			}
			else
			{
				document.put(filename, words.get(word));
				dictionary.put(word, document);
			}
		}
		private void getDictionary(Map<Integer, List<String>> rankWords, String filename)
		{
			List<String> curentRankWords = new ArrayList<String>();
			int rank, rankStart = new Double(rankWords.size() * RANK_START).intValue(), rankEnd = new Double(rankWords.size() * RANK_END).intValue();
			for(Map.Entry<Integer, List<String>> entry : rankWords.entrySet())
			{
				rank = entry.getKey();
				if(!(rank >= rankStart && rank <= rankEnd))
					continue;
				curentRankWords = entry.getValue();
				for(String word : curentRankWords)
					addWord(word, filename);
			}
		}
		public void getCollection(String filename)
		{
			try{readWords(filename);} catch(IOException e){
				print("I can not read the file");
				return;
			}
			if(words.isEmpty())
				return;
			Map<Integer, List<String>> rankWords = getRankWords();
			getDictionary(rankWords, filename);
		}
		public void action()
		{
			if(args.length <= 1)
			{
				print("Select the files");
				return;
			}
			int i = 0;
			for(String file : args)
			{
				i++;
				if(i == 1)
					continue;
				File f = new File(file);
				if(!f.isFile())
				{
					print("File " + file + " not exists");
					continue;
				}
				getCollection(file);
			}
		}
	}
	public class Dictionary extends Event 
	{
		public Dictionary(String[] args){super(args);}
		public void action()
		{
			if(dictionary.size() == 0)
			{
				print("Dictionary is empty");
				return;
			}
			List<String> dictWords = new ArrayList<String>();
			for(Map.Entry<String, Map<String, Integer>> entry : dictionary.entrySet())
				dictWords.add(entry.getKey());
			printTable(dictWords);
		}
	}
	public class TextOut extends Event 
	{
		public TextOut(String[] args){super(args);}
		public String read(String filename) throws IOException 
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s;
			StringBuilder sb = new StringBuilder();
			while((s = in.readLine())!= null)
				sb.append(s + "\n");
			in.close();
			return sb.toString();
		}
		public void action()
		{
			if(args.length < 2)
			{
				print("Select the file");
				return;
			}
			String filename = args[1];
			File f = new File(filename);
			if(!f.isFile())
			{
				print("File " + filename + " not exists");
				return;
			}
			try{print(read(filename));} catch(IOException e){print("I can not read the file");}
		}
	}
	public static class StopWords extends Event
	{
		private String file = "./texts/stopWords.txt";
		public StopWords(String[] args){super(args);}
		public void readWords() throws IOException 
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String s;
			while((s = in.readLine()) != null)
				stopWords.add(s);
			in.close();
		}
		public void action()
		{
			File f = new File(file);
			if(!f.isFile())
			{
				print("Stop words are not loaded");
				return;
			}
			try{readWords();} catch(IOException e){print("Stop words are not loaded");}
		}
	}
	public class SearchFiles extends Event 
	{
		private List<String> searchResult = new ArrayList<String>();
		public SearchFiles(String[] args){super(args);}
		private void find(String word)
		{
			word = word.toLowerCase();
			for(Map.Entry<String, Map<String, Integer>> entry : dictionary.entrySet())
			{
				String dictWord = entry.getKey();
				Pattern p1 = Pattern.compile("^("+Pattern.quote(dictWord)+")");
				Pattern p2 = Pattern.compile("^("+Pattern.quote(word)+")");
				if(!p1.matcher(word).find() && !p2.matcher(dictWord).find())
					continue;
				for(String filename : entry.getValue().keySet())
					if(!searchResult.contains(filename))
						searchResult.add(filename);
			}
		}
		public void action()
		{
			if(args.length < 2)
			{
				print("Enter the text");
				return;
			}
			Pattern p = Pattern.compile(splitRegexp);
			for(String word : args)
			{
				if(word.length() <= 3 || stopWords.contains(word) || p.matcher(word).matches())
					continue;
				find(word);
			}
			if(searchResult.isEmpty())
				print("Not find");
			else
				printTable(searchResult);
		}
	}
	public class Help extends Event 
	{
		public Help(String[] args){super(args);}
		public void action()
		{
			List<String[]> commands = new ArrayList<String[]>();
			commands.add(new String[] {"dir", "Dir files"});
			commands.add(new String[] {"addfiles", "Add files"});
			commands.add(new String[] {"dict", "Show dictionary"});
			commands.add(new String[] {"cat", "Output file"});
			commands.add(new String[] {"search", "Search"});
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
