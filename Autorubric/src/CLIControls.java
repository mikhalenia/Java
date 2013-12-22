package autorubric;
import autorubric.controller.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import static autorubric.mindview.Print.*;
import static autorubric.mindview.GetHash.*;

public class CLIControls extends Controller
{
	private Map<String, List<String>> samples = new HashMap<String, List<String>>();	
	private List<String> fatureVectorKeys = new ArrayList<String>();
	private Map<String, Map<String, Double>> rubrics = new HashMap<String, Map<String, Double>>();
	private final int COUNTWORDS = 10;
	private static List<String> stopWords = new ArrayList<String>();
	private Map<String, Map<String, Double>> dictionary = new HashMap<String, Map<String, Double>>();

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
	public class Rubric extends Event
	{
		private Map<String, Double> words = new HashMap<String, Double>();
		public Rubric(String[] args){super(args);}
		private void getFatureVectors()
		{			
			for(String rubric : samples.keySet())
			{
				List<String> files = samples.get(rubric);
				for(String file: files)
				{
					Map<String, Double> wordsList = dictionary.get(file);					
					for(String word: wordsList.keySet())
					{
						String key = md5(rubric + "," + file + "," + word);
						if(!fatureVectorKeys.contains(key))
							fatureVectorKeys.add(key);
					}
				}
			}			
		}
		private Map<String, Map<String, Double>> getCommonFatureVector()
		{
			getFatureVectors();
			Map<String, Map<String, Double>> commonFatureVector = new HashMap<String, Map<String, Double>>();
			for(String file : dictionary.keySet())
			{
				for(String rubric : samples.keySet())
				{
					List<String> files = samples.get(rubric);
					for(String fileKey: files)
					{
						Map<String, Double> wordsList = dictionary.get(file);
						Map<String, Double> vector = commonFatureVector.get(file);
						if(vector == null)
							vector = new HashMap<String, Double>();
						for(String word: wordsList.keySet())
						{			
							String key = md5(rubric + "," + file + "," + word);
							vector.put(key, wordsList.get(word));							
						}
						for(String hash: fatureVectorKeys)
						{								
							if(!vector.containsKey(hash))
								vector.put(hash, 0d);
						}
						commonFatureVector.put(file, vector);
					}
				}				
			}
			return commonFatureVector;
		}
		private void perceptronLearn(Map<String, Map<String, Double>> commonFatureVector)
		{			
			for(String rubric: samples.keySet())
			{
				List<String> files = samples.get(rubric);				
				Map<String, Double> rubricInfo = rubrics.get(rubric);
				if(rubricInfo == null)
					rubricInfo = new HashMap<String, Double>();
				for(String file: files)
				{
					Map<String, Double> vector = commonFatureVector.get(file);					
					for(String hash: fatureVectorKeys)
					{
						if(!rubricInfo.containsKey(hash))
							rubricInfo.put(hash, vector.get(hash));
						else
							rubricInfo.put(hash, rubricInfo.get(hash) + vector.get(hash));
					}					
				}
				rubrics.put(rubric, rubricInfo);
			}
		}
		private void addRubric(String filename, String rubric)
		{
			if(dictionary.containsKey(filename))
				return;			
			List<String> files = samples.get(rubric);
			if(files != null)
			{
				if(files.contains(filename))
					return;
			}
			else
				files = new ArrayList<String>();
			Zipf zipf = new Zipf(COUNTWORDS);
			try{				
				words = zipf.readWords(filename, stopWords);
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
			Map<String, Double> wordsList = dictionary.get(filename);			
			if(wordsList != null && wordsList.size() < COUNTWORDS)
			{
				print("Document contains very few keywords");
				dictionary.remove(filename);
				return;
			}
			files.add(filename);
			samples.put(rubric, files);
			Map<String, Map<String, Double>> commonFatureVector = getCommonFatureVector();			
			perceptronLearn(commonFatureVector);
		}
		public void action()
		{
			if(args.length <= 2)
			{
				print("Select the file and rubric");
				return;
			}
			String fileName = args[1];
			String rubric = args[2];
			File f = new File(fileName);
			if(!f.isFile())
			{
				print("File " + fileName + " not exists");
				return;
			}
			addRubric(fileName, rubric);
			words = new HashMap<String, Double>();
		}
	}
	public class Categorization extends Event
	{
		private Map<String, Double> words = new HashMap<String, Double>();
		public Categorization(String[] args){super(args);}
		private Map<String, Map<String, Double>> getCommonFatureVector(String file, Map<String, Map<String, Double>> fileDictionary)
		{
			Map<String, Map<String, Double>> commonFatureVector = new HashMap<String, Map<String, Double>>();
			Map<String, Double> wordsList = fileDictionary.get(file);
			for(String rubric : samples.keySet())
			{
				List<String> files = samples.get(rubric);
				for(String fileKey: files)
				{
					Map<String, Double> vector = new HashMap<String, Double>();
					for(String word: wordsList.keySet())
					{			
						String key = md5(rubric + "," + fileKey + "," + word);
						if(fatureVectorKeys.contains(key))
							vector.put(key, wordsList.get(word));
					}
					for(String hash: fatureVectorKeys)
					{								
						if(!vector.containsKey(hash))
							vector.put(hash, 0d);
					}
					commonFatureVector.put(file, vector);
				}
			}			
			return commonFatureVector;
		}
		private void getRubric(Map<String, Map<String, Double>> commonFatureVector, String filename)
		{
			Map<String, Double> vector = commonFatureVector.get(filename);
			double maxValue = 0d;
			String rubricName = new String();
			for(String rubric: rubrics.keySet())
			{
				Map<String, Double> setClassCoeff = rubrics.get(rubric);
				double curValue = 0d;
				for(String hash: setClassCoeff.keySet())
					curValue += vector.get(hash) * setClassCoeff.get(hash);
				if(curValue >= maxValue)
				{
					maxValue = curValue;
					rubricName = rubric;
					curValue = 0d;
				}
			}
			List<String> files = samples.get(rubricName);
			files.add(filename);
			samples.put(rubricName, files);
		}
		private void addFile(String filename)
		{
			if(dictionary.containsKey(filename))
				return;
			Zipf zipf = new Zipf(COUNTWORDS);
			try{				
				words = zipf.readWords(filename, stopWords);
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
			Map<String, Double> wordsList = dictionary.get(filename);			
			if(wordsList != null && wordsList.size() < COUNTWORDS)
			{
				print("Document contains very few keywords");
				return;
			}
			Map<String, Map<String, Double>> commonFatureVector = getCommonFatureVector(filename, dictionary); 
			getRubric(commonFatureVector, filename);
		}
		public void action()
		{
			if(args.length <= 1)
			{
				print("Select the file and rubric");
				return;
			}
			int i = 0;
			String fileName = args[1];
			File f = new File(fileName);
			if(!f.isFile())
			{
				print("File " + fileName + " not exists");
				return;
			}
			addFile(fileName);
			words = new HashMap<String, Double>();
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
			commands.add(new String[] {"addrubric", "Add rubric"});
			commands.add(new String[] {"addfile", "Add file"});
			commands.add(new String[] {"print", "Print rubrics"});
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
	public class Show extends Event
	{
		public Show(String[] args){super(args);}
		public void action()
		{
			printTable(samples);
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
			case "addrubric":
				cli.addEvent(cli.new Rubric(command));
				break;
			case "addfile":
				cli.addEvent(cli.new Categorization(command));
				break;
			case "print":
				cli.addEvent(cli.new Show(command));
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
