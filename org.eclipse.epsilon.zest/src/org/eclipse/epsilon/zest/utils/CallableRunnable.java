package org.eclipse.epsilon.zest.utils;

import java.util.concurrent.Callable;

import org.eclipse.swt.widgets.Display;

/**
 * Simple utility class for running code in the UI thread, while capturing and
 * rethrowing exceptions (unlike the regular
 * {@link Display#syncExec(Runnable)}).
 */
public abstract class CallableRunnable<T> implements Callable<T>, Runnable {

	private T result;
	private Exception exception;

	@Override
	public void run() {
		try {
			result = call();
		} catch (Exception e) {
			exception = e;
		}
	}

	public T getResult() {
		return result;
	}

	public Exception getException() {
		return exception;
	}

	public static <T> T syncExec(CallableRunnable<T> loadModels) throws Exception {
		Display.getDefault().syncExec(loadModels);
		if (loadModels.getException() != null) {
			throw loadModels.getException();
		} else {
			return loadModels.getResult();
		}
	}
}