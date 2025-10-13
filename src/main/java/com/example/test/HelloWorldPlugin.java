package com.example.test;

import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.PluginRegistrar;
import com.smartapibox.plugin.SmartApiPlugin;

import java.util.List;

public class HelloWorldPlugin implements SmartApiPlugin {


    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata("HelloWorldPlugin", "A simple Hello World plugin", "1.0.0", "Stefania", "/api/v1/plugin/external/hello", PluginMetadata.HttpMethod.GET);
    }

    @Override
    public void onLoad(final PluginRegistrar registrar) {
        registrar.registerController(new HelloWorldController());
    }


    @Override
    public List<Class<?>> getRestControllers() {
        return List.of(HelloWorldController.class);
    }
}