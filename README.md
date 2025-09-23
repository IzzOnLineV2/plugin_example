# 🧩 SmartApiBox Plugin Development Guide

This guide explains how to build a **valid plugin JAR** for the [SmartApiBox](https://smartapibox.com) platform using the `plugin-api-sdk`.

Plugins are standard JARs that expose **REST endpoints** and interact with the host via a lightweight SDK, without requiring Spring Boot or GPT integration on the plugin side.

---

## 🚀 How to Build a Plugin

Follow these steps to create a compatible plugin, or download a ready-to-use plugin from [here](https://github.com/IzzOnLineV2/plugin_example).

This is a minimal configuration — you can add more dependencies to your plugin as needed.

If you prefer, you can **download a plugin scaffold** using the following API calls:  
<br>

🧾 Step 1 — Get your API key (free plan)
```bash
curl -i --location --request POST 'https://sandboxapi.smartapibox.com/api/keys/generate?email=youremail@example.com'
```
If your email is already verified, you will receive your API key directly in the response.  
If it’s your first time, you’ll receive an email with a verification link. Didn’t get the email? Make sure to check your spam or promotions folder.
Once verified, your API key will be sent to your email.
<br>

📦 Step 2 — Download the plugin scaffold
```bash
curl --location 'https://sandboxapi.smartapibox.com/api/plugins/download?pluginName=YourPluginName' \
--header 'x-api-key: YOUR-SMARTAPIBOX-API-KEY' \
--output YourPluginNameFile.zip
```
Replace `YourPluginName` with your desired plugin name, and include the header `x-api-key: YOUR-SMARTAPIBOX-API-KEY` in the request.
Make sure to generate your API key first.


---

### 1. Add the SDK to your plugin `pom.xml`

```xml
 <dependencies>
    
    <!-- Main project SDK -->
    <dependency>
        <groupId>com.smartapibox</groupId>
        <artifactId>plugin-api-sdk</artifactId>
        <version>0.0.1</version>
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
The exposed REST endpoints must be annotated with the `@RestController` and `@GetMapping` annotations and must start with `/api/external`.

```java
package com.example.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello Plugin", description = "API exposed by HelloWorld plugin")
public class HelloWorldController {

    @GetMapping("/api/external/hello")
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
### 3. Implement the SmartApiPlugin interface
```java
package com.example.test;

import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.SmartApiPlugin;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class HelloWorldPlugin implements SmartApiPlugin {

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata("HelloWorldPlugin", "A simple Hello World plugin", "1.0.0", "Stefania");
    }

    @Override
    public void onLoad(Object context) {
        if (context instanceof GenericApplicationContext gac) {
            HelloWorldController controller = new HelloWorldController();
            gac.registerBean(HelloWorldController.class, () -> controller);

            try {
                Map<String, RequestMappingHandlerMapping> mappings = gac.getBeansOfType(RequestMappingHandlerMapping.class);
                RequestMappingHandlerMapping handlerMapping = mappings.get("requestMappingHandlerMapping");

                Method detectMethod = handlerMapping.getClass()
                        .getSuperclass()
                        .getSuperclass()
                        .getDeclaredMethod("detectHandlerMethods", Object.class);
                detectMethod.setAccessible(true);
                detectMethod.invoke(handlerMapping, controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

### 6. Deploy & Test your plugin
Once the JAR is built, you can test it in the SmartApiBox **sandbox** environment in the [Sandbox Site](https://sandbox.smartapibox.com) before submitting it for publication.

You can upload your plugin JAR at:

```code
Header: x-api-key: YOUR-SMARTAPIBOX-API-KEY
POST https://develop.smartapibox.com/api/plugins/upload-plugin
Content-Type: multipart/form-data
Field: file = your-plugin.jar
```

Once uploaded, your plugin will be available immediately, and its endpoints will be registered dynamically.
For security reasons, you can only upload plugins from the SmartApiBox sandbox environment.
**Your plugin could be automatically disabled randomly. Simply re-upload it to enable it again.**

You can test your plugin by sending a request to the `/api/external/hello` endpoint like this:
```bash
curl --location 'https://sandboxapi.smartapibox.com/api/external/hello' \
--header 'x-api-key: YOUR-SMARTAPIBOX-API-KEY'
```

### 7. 📤 Publish your plugin
When you’re ready for the **production** environment, submit us your plugin JAR for review at https://www.smartapibox.com/plugins/submit. (NOT AVAILABLE YET)

If approved, it will be published to the official SmartApiBox production platform and made available to all users and **you can start earning money**!

Thanks for your contribution! 🎉