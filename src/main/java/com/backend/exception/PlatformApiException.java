package com.backend.exception;

/**
 * Thrown when an upstream platform API (LeetCode, Codeforces, etc.) fails.
 */
public class PlatformApiException extends RuntimeException {

    private final String platform;

    public PlatformApiException(String platform, String message) {
        super(message);
        this.platform = platform;
    }

    public PlatformApiException(String platform, String message, Throwable cause) {
        super(message, cause);
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }
}
