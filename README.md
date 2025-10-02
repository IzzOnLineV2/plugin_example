# 🧩 SmartApiBox Plugin Development Guide

This guide explains how to build a **valid plugin JAR** for the [SmartApiBox](https://smartapibox.com) platform using the `plugin-api-sdk`.

Plugins are standard JARs that expose **REST endpoints** and interact with the host via a lightweight SDK, without requiring Spring Boot or GPT integration on the plugin side.

---

## 🚀 How to Build a Plugin

Follow these steps to create a compatible plugin, or download a ready-to-use plugin from [here](https://github.com/IzzOnLineV2/plugin_example).

This is a minimal configuration — you can add more dependencies to your plugin as needed.

If you prefer, you can **download a plugin scaffold** using the following API call:  
<br>

📦 Download the plugin scaffold
```bash
curl --location 'https://sandboxapi.smartapibox.com/api/public/plugin/download?pluginName=YourPluginName' \
--output YourPluginNameFile.zip
```
Replace `YourPluginName` with your desired plugin name


---

### 1. Add the SDK to your plugin `pom.xml`

```xml
 <dependencies>
    
    <!-- Main project SDK -->
    <dependency>
        <groupId>com.smartapibox</groupId>
        <artifactId>plugin-api-sdk</artifactId>
        <version>0.0.4</version>
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
### 2. Create your REST controller
The exposed REST endpoints must be annotated with the `@RestController` and `@RequestMapping` must start with `/api/plugin/external`.

```java
package com.example.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugin/external")
@Tag(name = "Hello Plugin", description = "API exposed by HelloWorld plugin")
public class HelloWorldController {

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
        return "Hello from dynamically loaded plugin!";
    }
}

```
### 3. Implement the `SmartApiPlugin` class
```java
package com.example.test;

import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.PluginRegistrar;
import com.smartapibox.plugin.SmartApiPlugin;

import java.util.List;

public class HelloWorldPlugin implements SmartApiPlugin {


    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata("HelloWorldPlugin", "A simple Hello World plugin", "1.0.0", "Stefania", "/api/plugin/external/hello", PluginMetadata.HttpMethod.GET);
    }

    @Override
    public void onLoad(PluginRegistrar registrar) {
        registrar.registerController(new HelloWorldController());
    }


    @Override
    public List<Class<?>> getRestControllers() {
        return List.of(HelloWorldController.class);
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

### 6. Register & Deploy your plugin via API

To test your plugin in the **sandbox** environment, you must upload it together with the endpoint metadata via the SmartApiBox `/api/private/catalogue/endpoint` API.

This endpoint expects a `multipart/form-data` request with two parts:

- `data` — the JSON payload representing your API endpoint (see `ApiEndpointRequest`)
- `pluginJar` — your plugin JAR file

🧾 Example using `curl`:

```bash
curl --location 'https://sandboxapi.smartapibox.com/api/private/catalogue/endpoint' \
--header 'x-api-key: YOUR-SMARTAPIBOX-API-KEY' \
--header 'Authorization: Bearer YOUR-SMARTAPIBOX-JWT-TOKEN' \
--form 'pluginJar=@"../HelloWorld-plugin/target/hello-world-plugin-1.0.0.jar"' \
--form 'data="{\"method\":\"GET\",\"path\":\"/api/plugin/external/hello\",\"headers\":[{\"name\":\"x-api-key\",\"value\":\"REQUIRED\",\"description\":null}],\"example\":\"example string\",\"name\":\"name string\",\"requiresAuth\":false,\"consumes\":\"application/json\",\"tags\":[\"NO GPT REQUIRED\"],\"categoryIds\":[6],\"description\":\"descrizione\"}";type=application/json'
```

✅ If the metadata matches the plugin, it will be:
- automatically set to **APPROVED** if you're in the `sandbox` or `develop` environment
- set to **PENDING_APPROVAL** in `production`, and reviewed manually

Once validated, the plugin is copied into the internal `plugins/` directory and dynamically loaded.

🧪 You can then test your plugin with:

```bash
curl --location 'https://sandboxapi.smartapibox.com/api/plugin/external/hello' \
--header 'x-api-key: YOUR-SMARTAPIBOX-API-KEY'
```

### 7. 📤 Publish your plugin

When you're ready to make your plugin available on the **SmartApiBox production environment**, follow these steps:

1. **Make sure your plugin is validated** using the sandbox upload endpoint `/api/private/catalogue/endpoint` with `PENDING_APPROVAL` status (this is the default in production).

2. A SmartApiBox team member will manually review your submission and, if approved, your plugin will be published on the **official API catalogue**.

3. Once approved, the plugin will be dynamically available to all SmartApiBox users, and you'll be eligible to earn usage-based revenue.

📨 Plugin monetization, documentation, and review features will soon be available in your personal developer dashboard on [smartapibox.com](https://smartapibox.com).

Thanks for contributing to the SmartApiBox ecosystem! 🎉