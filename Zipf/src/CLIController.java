package zipf;
import zipf.controller.Event;
import static zipf.mindview.Print.*;
import java.util.Scanner;

public class CLIController
{
	public static void main(String[] args)
	{
		CLIControls cli = new CLIControls();
		Scanner scan = new Scanner(System.in);
		cli.addEvent(new CLIControls.StopWords(args));
		while(true)
		{
			printnb("zipf-cli> ");
			String s = scan.nextLine();
			if(s.length() == 0)
				continue;
			String[] command = s.split(" ");
			if(command.length == 0)
				continue;
			switch(command[0])
			{
				case "dir":
					cli.addEvent(cli.new DirFile(command));
					break;
				case "addfiles":
					cli.addEvent(cli.new Documents(command));
					break;
				case "dict":
					cli.addEvent(cli.new Dictionary(command));
					break;
				case "search":
					cli.addEvent(cli.new SearchFiles(command));
					break;
				case "help":
					cli.addEvent(cli.new Help(command));
					break;
				case "exit":
					cli.addEvent(cli.new Exit(command));
					break;
				case "cat":
					cli.addEvent(cli.new TextOut(command));
					break;
				default:
					cli.addEvent(cli.new CommandNotExist(command));
					break;
			}
			cli.run();
		}
	}
}