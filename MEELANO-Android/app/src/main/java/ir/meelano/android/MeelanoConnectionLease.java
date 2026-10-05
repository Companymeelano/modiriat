package ir.meelano.android;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A single borrow of a pooled JDBC connection. Closing the lease returns the underlying connection
 * exactly once; a previously closed borrower cannot accidentally use a connection after it has been
 * checked out by another thread.
 */
final class MeelanoConnectionLease {
    interface FailureListener { void onFailure(Throwable failure); }

    private MeelanoConnectionLease() { }

    static Connection wrap(Connection raw, Runnable onClose, FailureListener failures) {
        final AtomicBoolean closed = new AtomicBoolean(false);
        return (Connection) Proxy.newProxyInstance(MeelanoConnectionLease.class.getClassLoader(),
                new Class<?>[]{Connection.class}, new InvocationHandler() {
                    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("close".equals(name)) {
                            if (closed.compareAndSet(false, true) && onClose != null) onClose.run();
                            return null;
                        }
                        if ("isClosed".equals(name)) {
                            if (closed.get() || raw == null) return true;
                            try { return raw.isClosed(); } catch (Exception ignored) { return true; }
                        }
                        if ("toString".equals(name)) return "meelano-pooled-connection";
                        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                        if ("equals".equals(name)) return proxy == (args == null || args.length == 0 ? null : args[0]);
                        if (closed.get()) throw new SQLException("این اجاره اتصال قبلاً بسته شده است.");

                        // Catch unbalanced generated SQL locally before it reaches Atiran.
                        if (("prepareStatement".equals(name) || "createStatement".equals(name) || "prepareCall".equals(name))
                                && args != null && args.length > 0 && args[0] instanceof String) {
                            MeelanoSql.checkSqlParentheses((String) args[0]);
                        }
                        try {
                            return method.invoke(raw, args);
                        } catch (InvocationTargetException ex) {
                            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                            if (failures != null) {
                                try { failures.onFailure(cause); } catch (RuntimeException ignored) { }
                            }
                            throw cause;
                        }
                    }
                });
    }
}
