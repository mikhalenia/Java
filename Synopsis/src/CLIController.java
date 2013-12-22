package synopsis;
import synopsis.controller.Event;
import static synopsis.mindview.Print.*;
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
			printnb("synopsis-cli> ");
			String s = scan.nextLine();
			if(s.length() == 0)
				continue;
			String[] command = s.split(" ");
			if(command.length == 0)
				continue;
			CLIControls.sendCommand(command, cli);
		}
	}
}