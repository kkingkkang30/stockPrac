package com.example.stock.facade;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissionLockStockFacadeTest {
    @Autowired
    private RedissionLockStockFacade redissionLockStockFacade ;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void insert(){
        Stock stock = new Stock(1L,1L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void delete(){
        stockRepository.deleteAll();
    }

    @Test
    public void all_request() throws InterruptedException {
        int threadCnt = 100;
        ExecutorService executorService= Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCnt);
        for(int i=0;i<threadCnt;i++){
            executorService.submit(()->{
                try{
                    redissionLockStockFacade.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0L, stock.getQuantity());
    }
}