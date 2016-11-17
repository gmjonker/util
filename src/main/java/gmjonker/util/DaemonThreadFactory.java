package gmjonker.util;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptation of DefaultThreadFactory.
 */
public class DaemonThreadFactory implements ThreadFactory
{
    public static final int DAEMON_PRIORITY = 3;

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public DaemonThreadFactory()
    {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-deamon-";
    }

    public Thread newThread(@Nonnull Runnable r)
    {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if ( ! t.isDaemon())
            t.setDaemon(true);
        if (t.getPriority() != DAEMON_PRIORITY)
            t.setPriority(DAEMON_PRIORITY);
        return t;
    }
}
