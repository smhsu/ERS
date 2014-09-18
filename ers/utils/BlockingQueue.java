package ers.utils;

import java.util.LinkedList;

/**
 * BlockingQueue.java<br>
 * A queue that holds objects of type T.  Has a maximum capacity.
 * Blocks when trying to enqueue elements on a full queue and blocks
 * when trying to dequeue on an empty queue.
 * 
 * BlockingQueues are thread safe.
 *
 * @param <T> the type of object placed on the queue
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class BlockingQueue<T> {

	private static int DEFAULT_SIZE = 10;
	
	private LinkedList<T> queue;
	private int maxSize;

	/**
	 * A queue that causes its caller to wait if
	 * the queue is empty for dequeue or
	 * the queue is full for enqueue.
	 * Initializes with a default max size of 10.
	 */
	public BlockingQueue()
	{
		this(DEFAULT_SIZE);
	}
	
	/**
	 * A queue that causes its caller to wait if
	 * the queue is empty for dequeue or
	 * the queue is full for enqueue
	 * @param maxSize - maximum size of the queue
	 */
	public BlockingQueue(int maxSize) {
		this.maxSize = maxSize;
		queue = new LinkedList<T>();
	}

	/**
	 * Return the next element from the queue, 
	 * waiting until the queue is not empty.
	 * @return first element in the queue
	 */
	public synchronized T dequeue() {
		while (queue.isEmpty())
		{
			try { wait(); }
			catch (InterruptedException e)
			{
				return null;
			}
		}
		T ans = queue.removeFirst();
		notifyAll();
		return ans;

	}

	/**
	 * Add an element to the queue,
	 * blocking until the queue is not full
	 * @param thing - added to end of the queue
	 */
	public synchronized void enqueue(T thing) {
		while ( queue.size() >= maxSize )
		{
			try { wait(); }
			catch (InterruptedException e)
			{
				return;
			}
		}
		queue.addLast(thing);
		notifyAll();
	}

	private static void sleep(int secs) {
		try {
			Thread.sleep(secs*1000);
		}
		catch (Throwable t) {

		}
	}

	/**
	 * Test code
	 */
	public static void main(String[] args) {
		final BlockingQueue<Integer> queue = new BlockingQueue<Integer>(10);

		//
		// Add 20 things to the queue
		//
		new Thread() {
			public void run() {
				BlockingQueue.sleep(3);
				for (int i=0; i < 20; ++i) {
					queue.enqueue(new Integer(i));
					System.out.println("Enqueued " + i);
				}
			}
		}.start();
		System.out.println("Started enqueue thread");

		//
		// while at the same time...
		//

		System.out.println("Trying to dequeue - should wait a bit");
		//
		// Pull 20 things off the queue
		//
		for (int i=0; i < 20; ++i) {
			System.out.println("dequeue " + queue.dequeue().toString());
			sleep(1);
		}

	}

}
