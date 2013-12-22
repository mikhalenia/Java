package myworker;
import java.util.*;
import java.util.concurrent.*;

public class TaskQueue
{
	private Worker[] workers;
	private ExecutorService executors;
	private ConcurrentLinkedQueue<Task> queue;
	public TaskQueue(int countWorkers)
	{
		queue=new ConcurrentLinkedQueue<Task>();
		workers=new Worker[countWorkers];
		executors=Executors.newFixedThreadPool(countWorkers);
		for(int i=0;i<countWorkers;i++)
		{
			workers[i]=new Worker(i);
			executors.execute(workers[i]);
		}
		System.out.println("Run workers...");
	}
	public void addTask(Task job)
	{
		if(job!=null)
			queue.add(job);
	}
	public class Worker implements Runnable
	{
		private int id;
		private Long timeLastTask;
		public Worker(int id)
		{
			this.id=id;
		}
		private boolean isShutDown()
		{
			long tm=System.currentTimeMillis();
			if(timeLastTask==null)
				timeLastTask=tm;
			long delta=tm-timeLastTask;
			if(delta>=RateLimit.ONESECOND*5)
				return true;
			return false;
		}
		public void run()
		{
			while(!Thread.interrupted())
			{
				if(queue.peek()==null)
				{
					if(isShutDown())
						Thread.currentThread().interrupt();
					continue;
				}
				Task oTask=queue.poll();
				oTask.setWorkerId(id);
				oTask.run();
				oTask=null;
				timeLastTask=System.currentTimeMillis();
			}
			System.out.println("Worker#"+id+" shutdown...");
			executors.shutdown();
		}
	}
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
}