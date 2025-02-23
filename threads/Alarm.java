package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
import java.util.PriorityQueue;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	
	private PriorityQueue<Long> wakeTimeQueue;
	private LinkedList<KThread> sleepingThreads;
	
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	
	public Alarm() {
		wakeTimeQueue = new PriorityQueue<>();
		sleepingThreads = new LinkedList<>();
        
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}
	
    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
	
	public void timerInterrupt() {
		boolean intStatus = Machine.interrupt().disable(); // Disable interrupts
		long currentTime = Machine.timer().getTime();
		
		while (!wakeTimeQueue.isEmpty() && wakeTimeQueue.peek() <= currentTime) {
			
			wakeTimeQueue.poll(); // Remove from wakeTimeQueue
			
			KThread qFrontThread = sleepingThreads.poll(); // Get thread
			
			if (qFrontThread != null) {
				qFrontThread.ready(); // Move to ready queue
			}
		}

        KThread.currentThread().yield(); // Yield the current thread
        Machine.interrupt().restore(intStatus); // Restore interrupts
    }
	
    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
	
	public void waitUntil(long x) {
		if (x <= 0) return; // No need to sleep if x is zero or negative
		
		boolean intStatus = Machine.interrupt().disable(); // Disable interrupts
		long wakeTime = Machine.timer().getTime() + x;
		
		wakeTimeQueue.add(wakeTime); // Add the wake time to the queue
		sleepingThreads.add(KThread.currentThread()); // Store the thread at the same index
		
		KThread.currentThread().sleep(); // Put the current thread to sleep
		Machine.interrupt().restore(intStatus); // Restore interrupts
	}
}
