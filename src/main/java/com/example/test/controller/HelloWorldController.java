package com.example.test.controller;

import com.example.test.service.HelloWorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plugin/external")
@Tag(name = "Hello Plugin", description = "API exposed by HelloWorld plugin")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    public HelloWorldController(final HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("/hello")
    @Operation(
            summary = "Say Hello",
            description = "Returns a greeting from the dynamically loaded plugin",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "500", description = "Internal error")
            }
    )
    public String sayHello() {
        return helloWorldService.sayHello();
    }
}