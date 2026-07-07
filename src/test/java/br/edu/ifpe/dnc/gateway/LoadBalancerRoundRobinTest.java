package br.edu.ifpe.dnc.gateway;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerRoundRobinTest {

    @Test
    void shouldDistributeAcrossWorkersInRoundRobin() {
        List<String> urls = List.of("http://box1:8080", "http://box2:8080", "http://box3:8080");
        AtomicInteger counter = new AtomicInteger(0);

        String first = nextUrl(urls, counter);
        String second = nextUrl(urls, counter);
        String third = nextUrl(urls, counter);
        String fourth = nextUrl(urls, counter);

        assertEquals("http://box1:8080", first);
        assertEquals("http://box2:8080", second);
        assertEquals("http://box3:8080", third);
        assertEquals("http://box1:8080", fourth);
    }

    @Test
    void shouldHandleSingleWorker() {
        List<String> urls = List.of("http://localhost:8080");
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            assertEquals("http://localhost:8080", nextUrl(urls, counter));
        }
    }

    @Test
    void shouldNotProduceNegativeIndexOnOverflow() {
        List<String> urls = List.of("http://box1:8080", "http://box2:8080");
        AtomicInteger counter = new AtomicInteger(Integer.MAX_VALUE - 1);

        String a = nextUrl(urls, counter);
        String b = nextUrl(urls, counter);
        String c = nextUrl(urls, counter);

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);
    }

    private String nextUrl(List<String> urls, AtomicInteger counter) {
        int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % urls.size();
        return urls.get(index);
    }
}
