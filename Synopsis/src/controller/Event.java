package synopsis.controller;

public abstract class Event
{
	private long eventTime;
	protected final String[] args;
	public Event(String[] args)
	{
		this.args = args;
	}
	public abstract void action();
}