package com.smartapibox.plugin.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello Plugin", description = "API exposed by HelloWorld plugin")
public class HelloWorldController {

    @GetMapping("/api/plugin/hello")
    @Operation(
            summary = "Say Hello",
            description = "Returns a greeting from the dynamically loaded plugin",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "500", description = "Internal error")
            }
    )
    public String sayHello() {
        return "Hello from dynamically loaded plugin!";
    }
}