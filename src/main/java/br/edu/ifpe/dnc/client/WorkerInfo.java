package br.edu.ifpe.dnc.client;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WorkerInfo {

    private final String id;
    private final String url;
    private final boolean dynamic;
    private final AtomicReference<Status> status;
    private final AtomicReference<Instant> lastHealthCheck;
    private final AtomicInteger consecutiveFailures;
    private final AtomicInteger activeRequests;

    public WorkerInfo(String url) {
        this(url, false);
    }

    public WorkerInfo(String url, boolean dynamic) {
        this.id = extractId(url);
        this.url = normalizeUrl(url);
        this.dynamic = dynamic;
        this.status = new AtomicReference<>(Status.UNKNOWN);
        this.lastHealthCheck = new AtomicReference<>(Instant.EPOCH);
        this.consecutiveFailures = new AtomicInteger(0);
        this.activeRequests = new AtomicInteger(0);
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Status getStatus() {
        return status.get();
    }

    public Instant getLastHealthCheck() {
        return lastHealthCheck.get();
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public int getActiveRequests() {
        return activeRequests.get();
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void markOnline() {
        this.status.set(Status.ONLINE);
        this.lastHealthCheck.set(Instant.now());
        this.consecutiveFailures.set(0);
    }

    public void markOffline() {
        this.status.set(Status.OFFLINE);
        this.lastHealthCheck.set(Instant.now());
        this.consecutiveFailures.incrementAndGet();
    }

    public void incrementActiveRequests() {
        this.activeRequests.incrementAndGet();
    }

    public void decrementActiveRequests() {
        this.activeRequests.decrementAndGet();
    }

    public boolean isOnline() {
        return status.get() == Status.ONLINE;
    }

    public boolean isOffline() {
        return status.get() == Status.OFFLINE;
    }

    private static String extractId(String url) {
        String normalized = normalizeUrl(url);
        return normalized
                .replaceFirst("^https?://", "")
                .replaceFirst("/$", "");
    }

    private static String normalizeUrl(String url) {
        if (url == null) throw new IllegalArgumentException("Worker URL cannot be null");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public String toString() {
        return String.format("WorkerInfo{id='%s', status=%s, dynamic=%s, failures=%d, active=%d, lastCheck=%s}",
                id, status.get(), dynamic, consecutiveFailures.get(), activeRequests.get(), lastHealthCheck.get());
    }

    public enum Status {
        ONLINE,
        OFFLINE,
        UNKNOWN
    }
}
