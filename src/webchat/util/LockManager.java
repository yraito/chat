package webchat.util;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows locking based on equals() rather than ==. <br />
 * 
 * Useful for ensuring two threads don't create an object with the same name, e.g. 
 * when creating chat rooms, as you can lock on the name. Also when different modules 
 * have different instances representing the same thing, e.g. two separate ClientSession
 * objects representing the same connection
 * 
 * @author Nick
 *
 */
public class LockManager {

	private final static Logger logger = LoggerFactory.getLogger(LockManager.class);
	
	private final Map<Object, Queue<Thread>> lockCount = new ConcurrentHashMap<>();
	//Map<Object, Object> lockObjs =  new ConcurrentHashMap<>();
	
	public void acquireLock(Object obj) {
		acquireLock(obj, 0);
		
	}
	
	public boolean acquireLock(Object obj, long waitMs) {
		
		logger.debug("acquireLock: attempt to get lock for {}", obj);
		
		if (hasLockFor(obj)) {
			logger.debug("acquireLock: already have lock for {}", obj);
			return true;
		}
		
		if (waitMs < 0) {
			throw new IllegalArgumentException("negative wait");
		}
		
		Object lockObj = null;
		
		synchronized(this) {
			if (!lockCount.containsKey(obj)) {
				logger.debug("acquireLock: No queue for {}, creating new one", obj);
				
				//lockObjs.put(obj, obj);
				Queue<Thread> queue = new ConcurrentLinkedQueue<>();
				queue.add(Thread.currentThread());
				lockCount.put(obj, queue);
				lockObj = queue;
				//hasLock = true;
			} else {
				logger.debug("acquireLock: queue exists for {}, fetching", obj);
				Queue<Thread> queue = lockCount.get(obj);
				if (!queue.contains(Thread.currentThread())) {
					queue.add(Thread.currentThread());
				}
				//lockCount.
				lockObj = queue;
				//hasLock = false;
			}
		
		}

		if (!hasLockFor(obj)) { 
			synchronized(lockObj) {
				long t0 = System.currentTimeMillis();
				long deltaWaitMs = waitMs;
				while (!hasLockFor(obj)) {

					logger.debug("Another thread has lock for {}. Going to wait", obj);
					try {
						lockObj.wait(deltaWaitMs);
					} catch (InterruptedException e) {
						logger.debug("Wait for {} was interrupted. Checking time left", obj);
						if (waitMs != 0) {
							long t1 = System.currentTimeMillis();
							deltaWaitMs = waitMs - (t1 - t0);
						}
						if (deltaWaitMs < 0) {
							break;
						} 
					}
				}
			}
		} 
		
		boolean hasLock = hasLockFor(obj);
		logger.debug("Got lock for {}? : {}", obj, hasLock);
		return hasLock;
		
	}
	
	public void releaseLock(Object obj) {
		
		logger.debug("releaseLock: preparing to release for {}", obj);
		
		if (!hasLockFor(obj)) {
			logger.debug("releaseLock: already released lock for {}", obj);
			return;
		}
		
		Object lockObj = lockCount.get(obj);
		synchronized(lockObj) {
			logger.debug("releaseLock: removing self from queue and notifying waiting threads for {}", obj);
			lockCount.get(obj).remove();
			lockObj.notifyAll();
		}
		
		synchronized(this) {
			synchronized(lockObj) {
				if (lockCount.get(obj).isEmpty()) {
					logger.debug("releaseLock: no threads waiting for {}. Destroying queue", obj);
					lockCount.remove(obj);
					//lockObjs.remove(obj);
				}
			}
		}
		
		logger.debug("Lock for {} released", obj);
	}
	
	private boolean hasLockFor(Object obj) {
		Queue<Thread> tQueue = lockCount.get(obj);
		if (tQueue == null) {
			logger.debug("Don't have lock for {}: No queue", obj);
			return false;
		} else {
			Thread lockOwner = tQueue.peek();
			logger.debug("Lock for {} owned by: {}", obj, lockOwner.getName());
			return lockOwner.equals(Thread.currentThread());
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		final LockManager lm = new LockManager();
		final String s = "hello";
		
		Runnable r = ()->{
			System.out.println("A: getting lock");
			lm.acquireLock("hello");
			System.out.println("A: got lock");
			try {
				System.in.read();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lm.releaseLock(s);
			System.out.println("A: Released lock");
		};
		Runnable r2 = ()->{
			System.out.println("B: getting lock");
			lm.acquireLock(s);
			System.out.println("B: got lock");
			try {
				System.in.read();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lm.releaseLock(s);
			System.out.println("B: Released lock");
		};
		Thread t = new Thread(r);
		Thread t2 = new Thread(r2);
		t.start();
		t2.start();
	}
}
