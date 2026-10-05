package ir.meelano.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MeelanoSqlTest {
    @Test
    public void closingLeaseTwiceReturnsConnectionOnlyOnceAndClosedLeaseCannotBeReused() throws Exception {
        AtomicBoolean physicalClosed = new AtomicBoolean(false);
        AtomicInteger releases = new AtomicInteger();
        Connection raw = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "isClosed": return physicalClosed.get();
                        case "close": physicalClosed.set(true); return null;
                        case "getAutoCommit": return true;
                        case "toString": return "test-connection";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == (args == null || args.length == 0 ? null : args[0]);
                        default: return null;
                    }
                });

        Connection first = MeelanoConnectionLease.wrap(raw, releases::incrementAndGet, failure -> { });
        assertFalse(first.isClosed());
        assertTrue(first.getAutoCommit());
        first.close();
        first.close();

        assertEquals("a pooled return must happen only once", 1, releases.get());
        assertTrue(first.isClosed());
        assertFalse("returning a pooled lease must not physically close its connection", physicalClosed.get());
        try {
            first.getAutoCommit();
            fail("using a returned lease must fail instead of touching the next borrower's connection");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage().contains("بسته شده"));
        }

        Connection second = MeelanoConnectionLease.wrap(raw, releases::incrementAndGet, failure -> { });
        assertTrue(second.getAutoCommit());
        second.close();
        assertEquals(2, releases.get());
    }

    @Test
    public void sqlParenthesisGuardIgnoresQuotedTextAndComments() throws Exception {
        MeelanoSql.checkSqlParentheses("SELECT (1 + 2), N')(' -- ) is inside a comment\n FROM dbo.items WHERE code=N'a''(' ");
    }

    @Test
    public void sqlParenthesisGuardRejectsUnbalancedGeneratedSql() throws Exception {
        try {
            MeelanoSql.checkSqlParentheses("SELECT (1 + 2))");
            fail("an extra closing parenthesis must be rejected");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage().contains("پرانتز"));
        }
    }
}
