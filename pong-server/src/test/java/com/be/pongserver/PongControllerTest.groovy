package com.be.pongserver


import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@WebFluxTest
class PongControllerTest extends Specification {

    private WebTestClient webTestClient2



    void setup() {
        webTestClient2 = WebTestClient.bindToController(PongController).build();
        Thread.sleep(1000)
    }


    void "should respond with OK when message is 'Hello'"() {
        given:
        def message = "Hello"

        when:
        def responseSpec = webTestClient2.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/pong").queryParam("message", message).build()
                }
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType("text/plain;charset=UTF-8") // 明确指定预期的内容类型
                .returnResult(String.class) // 指定返回类型为 String

        def responseBody = responseSpec.responseBody

        then:
        assert responseBody.blockFirst() == "World"
    }


    void "should respond with BAD_REQUEST when message is empty"() {
        given:
        def message = ""

        when:
        def response = webTestClient2.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/pong").queryParam("message", message).build()
                }
                .exchange()
                .expectStatus().is4xxClientError()
                .returnResult(String.class)

        then:
        response.responseBody.blockFirst() == "Invalid parameter: message is empty or null"
    }


    void "should respond with BAD_REQUEST when message is not 'Hello'"() {
        given:
        def message = "Hi"

        when:
        def response = webTestClient2.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/pong").queryParam("message", message).build()
                }
                .exchange()
                .expectStatus().is4xxClientError()
                .returnResult(String.class)

        then:
        response.responseBody.blockFirst() == "Invalid parameter: message must be 'Hello'"
    }


    void "should respond with TOO_MANY_REQUESTS when rate limit is exceeded"() {
        given:
        // 假设桶已空
        PongController.bucket.poll()
        def message = "Hello"

        when:
        def response = webTestClient2.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/pong").queryParam("message", message).build()
                }
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .returnResult(String.class)

        then:
        response.responseBody.blockFirst() == "Too many requests"
    }
}
