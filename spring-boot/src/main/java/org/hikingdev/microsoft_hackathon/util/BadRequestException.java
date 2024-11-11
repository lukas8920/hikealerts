package org.hikingdev.microsoft_hackathon.util;

import lombok.Getter;

@Getter
public class BadRequestException extends Exception {
    private Integer code = null;

    public BadRequestException(String message){super(message);}

    public BadRequestException(String message, int code){
        super(message);
        this.code = code;
    }
}
