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