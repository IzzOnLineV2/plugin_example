# 🧩 SmartApiBox Plugin Development Guide

This guide explains how to build a **valid plugin JAR** for the [SmartApiBox](https://smartapibox.com) platform using the `plugin-api-sdk`.

Plugins are standard JARs that expose **REST endpoints** and interact with the host via a lightweight SDK. Plugins **do not require** Spring Boot at compile time; the host provides the runtime Spring context. Plugins should be built so they can be dynamically loaded by SmartApiBox.

---

## 🚀 Quick summary of the runtime contract

- SmartApiBox **dynamically loads plugin JARs** and registers beans/controllers into the host `GenericApplicationContext`.
- The host **exposes plugin endpoints under the global prefix**:  
  ```
  /api/v1/plugin/external
  ```
  regardless of what the plugin author declares.  
  Example: a controller annotated with `@RequestMapping("/myplugin/v2")` will be exposed by the host as:
  ```
  /api/v1/plugin/external/myplugin/v2
  ```
- Plugin authors are free to choose any base path in their controllers. The host will mount the controller methods under the global prefix at registration time.

---

## ✅ How to build a plugin — minimal steps

You can also download a plugin scaffold:

```bash
curl --location 'https://sandboxapi.smartapibox.com/api/public/plugin/download?pluginName=YourPluginName' \
  --output YourPluginNameFile.zip
```

Unzip and import the Maven project in your IDE.

---

### 1) Recommended `pom.xml` basics

- Keep `plugin-api-sdk` as a normal dependency (compile-scope) so developers can build locally.
- Keep Spring libraries (`spring-web`, `spring-context`) as `provided` — they are supplied by the host at runtime.
- Provide an optional `local-test` Maven profile that imports the Spring Boot BOM so local builds/tests can run without the host.

Minimal example:

```xml
<project ...>
  <modelVersion>4.0.0</modelVersion>

  <properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- SDK required for compilation -->
    <dependency>
      <groupId>com.smartapibox</groupId>
      <artifactId>plugin-api-sdk</artifactId>
      <version>0.0.4</version>
    </dependency>

    <!-- Provided by host (do not bundle these in the plugin jar) -->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
      <version>6.1.5</version>
      <scope>provided</scope>
    </dependency>

    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-web</artifactId>
      <version>6.1.5</version>
      <scope>provided</scope>
    </dependency>

    <!-- Annotations (provided) -->
    <dependency>
      <groupId>io.swagger.core.v3</groupId>
      <artifactId>swagger-annotations</artifactId>
      <version>2.2.20</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>

  <!-- Optional: import Spring Boot BOM for local-test profile -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>3.2.5</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <profiles>
    <profile>
      <id>local-test</id>
      <dependencies>
        <!-- With the BOM you can omit explicit version -->
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
      </dependencies>
    </profile>
  </profiles>
</project>
```

Use `mvn clean install -Plocal-test` to compile & test locally without the host.

---

### 2) Create your REST controller

You may annotate your controller with any paths; the host will mount them under `/api/v1/plugin/external`.

```java
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
```

**At runtime** the host will expose the method at:  
`/api/v1/plugin/external/hello`

### 2b) Create your Service class (optional)

If your controller requires business logic, you can create a service class and inject it using constructor injection. This also shows how to access the `GPTClient` provided by the host:

```java
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
```

The `GPTClient` is available automatically in the host environment and lets you run prompts directly from your plugin.

---

### 3) Implement `SmartApiPlugin`

Implement the plugin entry point and use the provided `PluginRegistrar` to register beans/controllers.

**Important:** do **not** manually `new` the controller if it has dependencies — register the classes as beans so the host Spring context can perform DI.

Example plugin skeleton:

```java
package com.example.test;

import com.example.test.controller.HelloWorldController;
import com.example.test.service.HelloWorldService;
import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.PluginRegistrar;
import com.smartapibox.plugin.SmartApiPlugin;

import java.util.List;

public class HelloWorldPlugin implements SmartApiPlugin {

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
            "HelloWorldPlugin",
            "A simple Hello World plugin",
            "1.0.0",
            "Your Name",
            "/api/v1/plugin/external/hello",
            PluginMetadata.HttpMethod.GET
        );
    }

    /**
     * Called by SmartApiBox when loading the plugin.
     * Use the registrar to register beans or controllers so that the host
     * can instantiate them and perform dependency injection.
     *
     * Typical registrar methods:
     * - registrar.registerBean(Class<?> beanClass)
     *      Register a class as a Spring bean in the host context. The host will instantiate it,
     *      perform constructor injection and make it available for controller registration.
     *
     * - registrar.registerController(Object controllerInstance)
     *      (optional) Register an already created controller instance.
     *      Prefer registerBean(Class) when your controller/service needs DI.
     *
     * (See the PluginRegistrar javadoc in plugin-api-sdk for the exact method signatures.)
     */
    @Override
    public void onLoad(final PluginRegistrar registrar) {
        // Register service and controller as Spring-managed beans
        registrar.registerBean(HelloWorldService.class);
        registrar.registerBean(HelloWorldController.class);
    }

    @Override
    public List<Class<?>> getRestControllers() {
        // Used by the host for metadata/OpenAPI extraction and validation
        return List.of(HelloWorldController.class);
    }
}
```

---

### 4) `META-INF/services` discovery

Add `META-INF/services/com.smartapibox.plugin.SmartApiPlugin` containing:

```
com.example.test.HelloWorldPlugin
```

This allows the host `ServiceLoader` to discover your plugin inside the JAR.

---

### 5) Local test & build

- Local compile & test (uses `local-test` profile if present):

```bash
mvn clean install -Plocal-test
```

- Final package:

```bash
mvn clean package
```

The built JAR will be in `target/hello-world-plugin-<version>.jar`.

---

### 6) Register & deploy the plugin to SmartApiBox (sandbox)

Developers can access the **SmartApiBox Sandbox Environment** to upload, register and test their plugins with integrated GPT features.  
This allows you to verify both the REST logic and GPT prompt responses before publishing your plugin to production.

### 7) Publishing process

- Sandbox → validation and automated approval in `sandbox`.
- Production → `PENDING_APPROVAL` and manual review by SmartApiBox team before publishing to the public catalogue.

---

## Implementation notes for plugin authors

- Prefer to register classes (`registrar.registerBean(Class)`), not instances, when you need DI.
- Do not bundle Spring core libs inside the plugin JAR (they are `provided`).
- The host will always mount controllers under `/api/v1/plugin/external` — leave the developer path as their "logical" path; the host prepends the global prefix during registration.
- If you need a plugin-local configuration file, keep it under `src/main/resources` and read via classpath.