package com.example.test.service;

import com.smartapibox.sdk.GPTClient;
import org.springframework.stereotype.Service;


@Service
public class HelloWorldService {

    private final GPTClient gptClient;

    public HelloWorldService(final GPTClient gptClient) {
        this.gptClient = gptClient;
    }

    public String sayHello() {
        return gptClient.ask("Say Hello in funny mode");
    }

}
