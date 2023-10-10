package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResponse {
    private Integer capacity;
    private Integer connected;
    private String dateTime;
}
