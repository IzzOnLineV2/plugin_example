package com.example.test;

import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.SmartApiPlugin;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

public class HelloWorldPlugin implements SmartApiPlugin {

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata("HelloWorldPlugin", "A simple Hello World plugin", "1.0.0", "Stefania", "/api/plugin/external/hello", PluginMetadata.HttpMethod.GET);
    }

    @Override
    public void onLoad(Object context) {
        if (context instanceof GenericApplicationContext gac) {
            // Registra il controller nel context (Spring si occupa di instanziarlo e mappare l'endpoint)
            gac.registerBean(HelloWorldController.class);
        }
    }


    @Override
    public List<Class<?>> getRestControllers() {
        return List.of(HelloWorldController.class);
    }
}