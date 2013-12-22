package btrees;
import btrees.controller.Event;
import static btrees.mindview.Print.*;
import java.util.Scanner;

public class CLIController
{
	public static void main(String[] args)
	{
		CLIControls cli = new CLIControls();
		Scanner scan = new Scanner(System.in);
		cli.addEvent(new CLIControls.BTreeLoad(args));		
		while(true)
		{
			printnb("b-trees-cli> ");
			String s = scan.nextLine();
			if(s.length() == 0)
				continue;
			String[] command = s.split(" ");
			if(command.length == 0)
				continue;
			switch(command[0])
			{
				case "push":
					cli.addEvent(cli.new Push(command));
					break;
				case "search":
					cli.addEvent(cli.new Get(command));
					break;
				case "showinfo":
					cli.addEvent(cli.new Info(command));
					break;
				case "help":
					cli.addEvent(cli.new Help(command));
					break;
				case "exit":
					cli.addEvent(cli.new Exit(command));
					break;
				default:
					cli.addEvent(cli.new CommandNotExist(command));
					break;
			}
			cli.run();
		}
	}
}