package org.truenewx.test.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * junit规则扩展语句执行过程
 * 
 * @author jianglei
 * @since JDK 1.8
 */
public interface StatementProcedure {
    /**
     * Modifies the method-running {@link Statement} to implement this
     * test-running rule
     * 
     * @param base
     *            The {@link Statement} to be modified
     * @param description
     *            A {@link Description} of the test implemented in {@code base}
     */
    void evaluate(Statement base, Description description) throws Throwable;
}
