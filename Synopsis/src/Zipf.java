package synopsis;
import java.io.*;
import java.util.*;

public class Zipf
{
	private Map<String, Double> words = new HashMap<String, Double>();
	private Map<String, Map<String, Double>> dictionary = new HashMap<String, Map<String, Double>>();
	private String text = new String();
	private String splitRegexp = "[ ,.!();:1234567890-]";
	private final double RANK_START = 0.3;
	private final double RANK_END = 0.7;
	public class ValueComparator implements Comparator<String>
	{
		Map<String, Double> base;
		public ValueComparator(Map<String, Double> base) 
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
	public Map<String, Double> readFile(String filename, List<String> stopWords) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String s;
		StringBuilder sb = new StringBuilder();		
		while((s = in.readLine()) != null)
		{
			sb.append(s + "\n");
			String[] newWords = s.split(splitRegexp);
			for(String word : newWords)
			{
				word = word.trim();
				if(word.length() <= 3 || stopWords.contains(word))
					continue;
				word = word.toLowerCase();
				if(words.containsKey(word))
				{
					double value = words.get(word);
					words.put(word, ++value);
				}
				else
					words.put(word, 1d);
			}
		}
		in.close();
		text = sb.toString();
		return words;
	}
	public String getText(){return text;}
	public Map<Integer, List<String>> getRankWords()
	{
		ValueComparator comparator =  new ValueComparator(words);
		Map<String, Double> sortedWords = new TreeMap<String, Double>(comparator);
		sortedWords.putAll(words);
		double frequency = -1;
		int rank = 0;
		Map<Integer, List<String>> rankWords = new HashMap<Integer, List<String>>();
		List<String> curentRankWords = new ArrayList<String>();
		for(Map.Entry<String, Double> entry : sortedWords.entrySet())
		{
			double currentFrequency = entry.getValue();
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
	public Map<String, Map<String, Double>> filterZipfs(Map<Integer, List<String>> rankWords, String filename, Map<String, Map<String, Double>> dictionary)
	{
		this.dictionary = dictionary;
		List<String> curentRankWords = new ArrayList<String>();
		int rank, 
			rankStart = new Double(rankWords.size() * RANK_START).intValue(), 
			rankEnd = new Double(rankWords.size() * RANK_END).intValue();
		for(Map.Entry<Integer, List<String>> entry : rankWords.entrySet())
		{
			rank = entry.getKey();
			if(!(rank >= rankStart && rank <= rankEnd))
				continue;
			curentRankWords = entry.getValue();
			
			for(String word : curentRankWords)
				addWord(word, words.get(word), filename);
		}
		return this.dictionary;
	}
	public void addWord(String word, Double frequency, String filename)
	{
		Map<String, Double> wordsList = dictionary.get(filename);			
		if(wordsList == null)
			wordsList = new HashMap<String, Double>();
		wordsList.put(word, frequency);
		dictionary.put(filename, wordsList);
	}	
	public void getFrequency()
	{
		for(String word: words.keySet())
		{
			double frequency = words.get(word) / words.size();
			words.put(word, frequency);
		}
	}
}