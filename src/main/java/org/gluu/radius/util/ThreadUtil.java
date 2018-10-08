package org.gluu.radius.util;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;


public class ThreadUtil {

	private static ThreadGroup rootgroup = null;

	public static final Thread findRunningThread(String name) {

		if(name == null)
			return null;
		final Thread [] threads = getAllThreads(Thread.State.RUNNABLE);
		for(Thread thread: threads)
			if(thread.getName().equalsIgnoreCase(name))
				return thread;
		return null;
	}

	private static final Thread [] getAllThreads(final Thread.State state) {
		
		initRootThreadGroup();
		final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
		int allocsize = thbean.getThreadCount();
		int n = 0;
		Thread [] threads;
		do {
			allocsize *= 2;
			threads = new Thread[allocsize];
			n = rootgroup.enumerate(threads,true);
		}while(n == allocsize);
		return java.util.Arrays.copyOf(threads,n);
	}

	private static void initRootThreadGroup() {

		if(rootgroup!=null)
			return;

		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		ThreadGroup ptg = null;
		while((ptg = tg.getParent()) != null)
			tg = ptg;
		rootgroup = tg;
	}
}