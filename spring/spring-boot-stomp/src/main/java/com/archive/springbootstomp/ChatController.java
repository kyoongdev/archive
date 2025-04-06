package com.archive.springbootstomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ChatController {

  private final SimpMessagingTemplate template;

  @Autowired
  public ChatController(SimpMessagingTemplate template) {
    this.template = template;
  }

  @MessageMapping("/messages")
  public void send2() {
    log.info("ChatController.send2() - send2() called");

    template.convertAndSend("/sub/message", MessageDTO.builder().message("hi").build());
  }
}
