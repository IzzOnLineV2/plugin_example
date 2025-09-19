package com.smartapibox.plugin.impl;

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