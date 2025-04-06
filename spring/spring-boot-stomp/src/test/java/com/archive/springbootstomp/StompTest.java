package com.archive.springbootstomp;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class StompTest {
  @LocalServerPort private int port;

  private WebSocketStompClient stompClient;
  private StompSession stompSession;

  private static final String WEBSOCKET_ENDPOINT = "/ws-stomp";
  private static final String SUBSCRIBE_DEST = "/sub/message";
  private static final String SEND_DEST = "/pub/messages";
  

  @BeforeEach
  void setup() throws Exception {
    // WebSocket client 초기화
    stompClient =
        new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    // 연결 시도
    stompSession =
        stompClient
            .connectAsync(
                "ws://localhost:" + port + WEBSOCKET_ENDPOINT, new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);
  }

  @Test
  void testWebSocketMessageFlow() throws Exception {
    MessageFrameHandler<MessageDTO> handler = new MessageFrameHandler<>(MessageDTO.class);
    stompSession.subscribe(SUBSCRIBE_DEST, handler);

    stompSession.send(SEND_DEST, null);

    MessageDTO result = handler.completableFuture.get(3, TimeUnit.SECONDS);
    Assertions.assertEquals("hi", result.getMessage());
  }

  public static class MessageFrameHandler<T> implements StompFrameHandler {

    private final CompletableFuture<T> completableFuture = new CompletableFuture<>();

    private final Class<T> tClass;

    public MessageFrameHandler(Class<T> tClass) {
      this.tClass = tClass;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {

      return this.tClass;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {

      completableFuture.complete((T) payload);
    }
  }
}
