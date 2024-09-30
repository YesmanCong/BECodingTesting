package com.be.pingserver


import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class PingControllerTest extends Specification {



    PingSchedule pingSchedule
    String testingLockFilPath = "F:\\testLockfile.lock"

    @MockBean
    WebClient.Builder webClientBuilder


    

    def setup() {
        pingSchedule = new PingSchedule()
        if(Files.exists(Paths.get(testingLockFilPath))){
            Files.delete(Paths.get(testingLockFilPath))
        }
        pingSchedule.LOCK_FILE_PATH = testingLockFilPath
        // Create a Mock WebClient instance
        WebClient webClient = Mock()
        webClientBuilder.build() >> webClient

        // Set up the response chain for the mocked WebClient
        def requestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def responseSpec = Mock(WebClient.ResponseSpec)
        def mockMono = Mock(Mono)
        // mock webClient行为
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri("?message=Hello") >> requestHeadersUriSpec
        requestHeadersUriSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> mockMono
        mockMono.block() >> {
            Thread.sleep(500)
            "World"
        }

        pingSchedule.webClient = webClient

    }

    def "test ping call"() {
        given:

        when:
        def r1 = pingSchedule.doPing()


        then: "验证返回是否为World"
        r1 == "World"
    }

    @Unroll
    def "test limited rate ping call"() {
        given:


        when: "并发执行多次doPing()"
        List<CompletableFuture<String>> futures = (1..4).collect {CompletableFuture.supplyAsync({return pingSchedule.doPing()})}
        List<String> results = futures.collect{it.get()}
        then: "验证返回是否为limited"
        results.any { it == "limited" }
    }


    def "test ping call while pong has too many request"() {
        given:
        // Create a Mock WebClient instance
        WebClient webClient = Mock()
        webClientBuilder.build() >> webClient
        // Set up the response chain for the mocked WebClient
        def requestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def responseSpec = Mock(WebClient.ResponseSpec)
        // mock webClient行为
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri("?message=Hello") >> requestHeadersUriSpec
        requestHeadersUriSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> {
            Mono.error(new WebClientResponseException(429, "Too Many Requests", null, null, null))
        }
        pingSchedule.webClient = webClient


        when: "并发执行多次doPing()"
        def r1 = pingSchedule.doPing()

        then: "验证返回是否为limited"
        r1 == "error"
    }



}
