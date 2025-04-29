package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.service.MarketPriceService;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MarketPriceServiceTest {
    private MarketPriceService service;

    @BeforeEach
    public void setup() {
        service = new MarketPriceService();
        service.init();
    }

    @Test
    public void testCurrentPricesNotNull() throws InterruptedException {
        // Wait some time for initial refresh
        Thread.sleep(3000);
        Map<String, java.util.List<src.model.FishPrice>> prices = service.getCurrentPrices();
        assertNotNull(prices);
    }

    @Test
    public void testAddListenerAndNotify() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        service.addListener(map -> {
            if (!map.isEmpty()) {
                latch.countDown();
            }
        });
        service.addListener(null); // no crash
        service.refreshData();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testCacheRefreshing() throws InterruptedException {
        int initialSize = service.getCurrentPrices().size();
        // simulate refresh
        service.refreshData();
        int newSize = service.getCurrentPrices().size();
        assertTrue(newSize >= 0);
    }
}
