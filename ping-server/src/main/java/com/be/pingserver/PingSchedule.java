package com.be.pingserver;

import io.netty.handler.codec.http.HttpResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;

@Service
@EnableScheduling
@Slf4j
public class PingSchedule {

    private final static String PONG_SERVER_URL = "http://localhost:8070/pong";

    @Value("${lock.file.path:F:\\lockfile.lock}")
    private String LOCK_FILE_PATH;

    private static final int ACTIVE_PROCESS_COUNT = 2;
    private static final int FILE_SIZE = 1024;
    private static final int LOCK_SIZE = FILE_SIZE / ACTIVE_PROCESS_COUNT;

    private static WebClient webClient = WebClient.builder().baseUrl(PONG_SERVER_URL).defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE).build();


    @Scheduled(cron = "* * * * * *")
    public String doPing() {
        Path filePath = Paths.get(LOCK_FILE_PATH);
        String resultMessage = "";
        if (! Files.exists(filePath, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                log.info("create file error: {}", e.getMessage());
            }
        }

        try (FileChannel channel = new FileOutputStream(LOCK_FILE_PATH).getChannel()) {
            // 尝试获取锁
            for (int i = 0; i < ACTIVE_PROCESS_COUNT; i++) {
                FileLock tryLock = channel.tryLock(i * LOCK_SIZE, (i + 1) * LOCK_SIZE, false);
                if (tryLock != null) {
                    try {
                        log.info("requesting pong server");
                        var response = webClient.get()
                                .uri("?message=Hello")
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info("response: {}", response);
                        resultMessage = response;
                        break;
                    }catch (Exception e){
                        log.info("request pong server error: {}", e.getMessage());
                        resultMessage = "error";
                    }finally {
                        tryLock.release();
                    }
                }else{
                    log.info("limited");
                    return "limited";
                }
            }
        } catch (OverlappingFileLockException lockE) {
            log.info("limited");
            return "limited";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMessage;
    }


}
