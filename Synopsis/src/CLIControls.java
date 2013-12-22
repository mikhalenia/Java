package synopsis;
import synopsis.controller.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import static synopsis.mindview.Print.*;

public class CLIControls extends Controller
{
	private static List<String> stopWords = new ArrayList<String>();
	private Map<String, Map<String, Double>> dictionary = new HashMap<String, Map<String, Double>>();
	private Map<String, String> documents = new HashMap<String, String>();
	public class Abstract extends Event
	{
		private Map<String, Double> words = new HashMap<String, Double>();
		private Zipf zipf;
		private List<String> sentences = new ArrayList<String>();
		private void getSenteces(String text)
		{
			Pattern p = Pattern.compile("(^|(?<=[.!?]\\s))(\\d+\\.\\s?)*[À-ßA-Z][^!?]*?[.!?](?=\\s*(\\d+\\.\\s)*[À-ßA-Z]|$)", Pattern.MULTILINE);
			Matcher m = p.matcher(text);
			while (m.find())
				sentences.add(m.group().toString());
		}
		public Abstract(String[] args)
		{
			super(args);
			zipf = new Zipf();
		}
		private void getDictionary(String filename)
		{
			try{				
				words = zipf.readFile(filename, stopWords);
			}
			catch(IOException e){
				print("I cann't read the file");
				return;
			}
			if(words.isEmpty())
			{
				print("File's empty");
				return;
			}
			zipf.getFrequency();
			Map<Integer, List<String>> rankWords = zipf.getRankWords();
			dictionary = zipf.filterZipfs(rankWords, filename, dictionary);
			documents.put(filename, zipf.getText());
		}
		private void abstracted(String filename, int percentage)
		{
			if(!dictionary.containsKey(filename))
				getDictionary(filename);
			if(!documents.containsKey(filename) || percentage == 0)
				return;
			if(percentage >= 100)
			{
				print(documents.get(filename));
				return;
			}
			getSenteces(documents.get(filename));
			int textLength = documents.get(filename).length();
			Map<String, Double> wordsList = dictionary.get(filename);
			StringBuilder resultText = new StringBuilder();
			for(String word: wordsList.keySet())
			{
				Pattern p = Pattern.compile(Pattern.quote(word));
				List<Integer> keys = new ArrayList<Integer>();
				for(String sentence: sentences)
				{
					if(p.matcher(sentence).find())
					{
						keys.add(sentences.indexOf(sentence));
						resultText.append(sentence + " ");
					}
				}
				for(Integer key: keys)
					sentences.remove(key);
				keys = new ArrayList<Integer>();
				if(((double)resultText.length() * 100 / (double)textLength) >= percentage)
					break;
			}
			for(String sentence: sentences)
			{
				resultText.append(sentence + " ");
				if(((double)resultText.length() * 100 / (double)textLength) >= percentage)
					break;
			}
			print(resultText);
		}
		public void action()
		{
			if(args.length <= 2)
			{
				print("Enter the file name and the percentage of abstracting");
				return;
			}
			String fileName = args[1];
			String percentage = args[2];
			File f = new File(fileName);
			if(!f.isFile())
			{
				print("File " + fileName + " not exists");
				return;
			}
			Pattern p = Pattern.compile("^(\\d+)\\%$");
			Matcher m = p.matcher(percentage);
			if(!m.find())
			{
				print("Enter a percentage in the format: integer+%");
				return;
			}
			abstracted(fileName, Integer.parseInt(m.group(1)));
		}
	}
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
				print("Path isn't correct!");
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
	public class TextOutput extends Event 
	{
		public TextOutput(String[] args){super(args);}
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
	public class Help extends Event 
	{
		public Help(String[] args){super(args);}
		public void action()
		{
			List<String[]> commands = new ArrayList<String[]>();
			commands.add(new String[] {"dir", "Dir files"});
			commands.add(new String[] {"abstracted", "Abstracted"});
			commands.add(new String[] {"cat", "Output file"});
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
	public static void sendCommand(String[] command, CLIControls cli)
	{
		String prototype = command[0];
		switch(prototype)
		{
			case "dir":
				cli.addEvent(cli.new DirFile(command));
				break;
			case "abstracted":
				cli.addEvent(cli.new Abstract(command));
				break;
			case "help":
				cli.addEvent(cli.new Help(command));
				break;
			case "exit":
				cli.addEvent(cli.new Exit(command));
				break;
			case "cat":
				cli.addEvent(cli.new TextOutput(command));
				break;
			default:
				cli.addEvent(cli.new CommandNotExist(command));
				break;
		}
		cli.run();
	}
}
