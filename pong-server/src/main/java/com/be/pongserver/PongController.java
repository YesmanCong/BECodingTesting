package com.be.pongserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
@RestController
@RequestMapping("/pong")
public class PongController {

    private static final Integer RATE_LIMIT = 1;
    private static final Integer REQUEST_PRE_SECOND = 1;

    private static final LinkedBlockingQueue<String> bucket = new LinkedBlockingQueue<>(RATE_LIMIT);

    static {
        //初始化容量
        bucket.add("");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(REQUEST_PRE_SECOND * 1000);
                    if (bucket.size() != RATE_LIMIT) {
                        bucket.put("");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @GetMapping("")
    public Mono<ResponseEntity<String>> ponging(@RequestParam String message) {
        if (tryConsume()) {
            if (message == null || message.trim().isEmpty()) {
                log.info("Invalid parameter: message is empty or null");
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameter: message is empty or null"));
            }
            if (! "Hello".equals(message)) {
                log.info("Invalid parameter: message must be 'Hello'");
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameter: message must be 'Hello'"));
            }
            log.info("Ponging: {}", message);
            return Mono.just(ResponseEntity.status(HttpStatus.OK).body("World"));
        } else {
            log.info("Too many requests");
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests"));
        }
    }


    private boolean tryConsume() {
        synchronized (bucket) {
            if (! bucket.isEmpty()) {
                return bucket.poll() != null;
            } else {
                return false;
            }
        }
    }
}
