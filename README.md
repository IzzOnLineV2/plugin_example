# 🧩 SmartApiBox Plugin Development Guide

This guide explains how to build a **valid plugin JAR** for the [SmartApiBox](https://smartapibox.com) platform using the `plugin-api-sdk`.

Plugins are standard JARs that expose **REST endpoints** and interact with the host via a lightweight SDK, without needing Spring Boot or GPT integration on the plugin side.

---

## 🚀 How to Build a Plugin

Follow these steps to create a compatible plugin or download a ready-to-use plugin from [here](https://github.com/IzzOnLineV2/plugin_example).
This is a minimal configuration, you can add more dependencies to your plugin as you wish.
---

### 1. Add the SDK to your plugin `pom.xml`

```xml
 <dependencies>
    <!-- Main project SDK -->
    <dependency>
        <groupId>com.smartapibox</groupId>
        <artifactId>plugin-api-sdk</artifactId>
        <version>0.0.1-SNAPSHOT</version> <!-- Please choose the correct version -->
        <scope>provided</scope>
    </dependency>

    <!-- Spring Core (for ApplicationContext) -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.5</version>
        <scope>provided</scope>
    </dependency>

    <!-- Spring Web (for @RestController, @GetMapping) -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>6.1.5</version>
        <scope>provided</scope>
    </dependency>

    <!-- Swagger Annotations -->
    <dependency>
        <groupId>io.swagger.core.v3</groupId>
        <artifactId>swagger-annotations</artifactId>
        <version>2.2.20</version>
        <scope>provided</scope>
    </dependency>

    <!-- SpringDoc OpenAPI (only annotations, not runtime) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
        <scope>provided</scope>
    </dependency>

</dependencies>
```
### 2. Implement the SmartApiPlugin interface

```java
package com.smartapibox.plugin.impl;

import com.example.test.HelloWorldController;
import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.SmartApiPlugin;
import org.springframework.context.support.GenericApplicationContext;

public class HelloWorldPlugin implements SmartApiPlugin {

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata("HelloWorldPlugin", "A simple Hello World plugin", "1.0.0", "Stefania");
    }

    @Override
    public void onLoad(Object context) {
        HelloWorldController controller = new HelloWorldController();

        ((GenericApplicationContext) context)
                .registerBean(HelloWorldController.class, () -> controller);

        System.out.println("HelloWorldPlugin loaded and controller registered!");
    }
}

```
### 3. Implement the SmartApiPlugin interface
```java
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
```
### 4. Create META-INF/services/com.smartapibox.plugin.SmartApiPlugin
This file is required for dynamic discovery of your plugin:
```text
com.example.test.HelloWorldPlugin
```

### 5. Build the plugin
```mvn
mvn clean package
```

### 6. Deploy & Test your plugin
Once the JAR is built, you can test it in the SmartApiBox sandbox environment before submitting it for publication.

You can upload your plugin JAR at:

```code
POST https://develop.smartapibox.com/api/plugins/upload-plugin
Content-Type: multipart/form-data
Field: file = your-plugin.jar
```
Or use Swagger UI:

👉 https://devapi.smartapibox.com/swagger-ui/index.html#/Plugins/uploadPlugin

Once uploaded, your plugin will be available immediately, and its endpoints will be registered dynamically.

### 7. 📤 Publish your plugin
When you’re ready, submit your plugin JAR for review.

If approved, it will be published to the official SmartApiBox production platform and made available to all users.

Thanks for your contribution! 🎉