package com.naveen.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer — retries failed tests once before marking as FAIL.
 *
 * Applied to smoke and CRUD tests only — NOT security tests.
 * Retrying an injection payload gives misleading results.
 *
 * Restful Booker is a public demo server that occasionally returns
 * 500 or times out — this keeps CI green on transient infra failures,
 * not genuine test failures.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            log.warn("Test '{}' failed — retrying attempt {}/{}",
                    result.getName(), retryCount, MAX_RETRY);
            return true;
        }
        log.error("Test '{}' failed after {} retry — marking as FAIL",
                result.getName(), MAX_RETRY);
        return false;
    }
}