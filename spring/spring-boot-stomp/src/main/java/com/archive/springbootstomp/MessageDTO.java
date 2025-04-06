package com.archive.springbootstomp;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageDTO {

  private String message;

  @Builder
  public MessageDTO(String message) {
    this.message = message;
  }
}
