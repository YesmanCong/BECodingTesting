package com.be.pongserver;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication

public class PongServerApplication {



    public static void main(String[] args) {
        SpringApplication.run(PongServerApplication.class, args);
    }

}
