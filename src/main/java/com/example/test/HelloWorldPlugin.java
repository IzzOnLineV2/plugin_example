package com.example.test;

import com.example.test.controller.HelloWorldController;
import com.example.test.service.HelloWorldService;
import com.smartapibox.plugin.PluginMetadata;
import com.smartapibox.plugin.PluginRegistrar;
import com.smartapibox.plugin.SmartApiPlugin;

import java.util.List;

/**
 * Esempio di implementazione di un plugin per SmartApiBox.
 * Questo plugin espone un'API REST semplice che restituisce un messaggio "Hello" tramite GPT.
 */
public class HelloWorldPlugin implements SmartApiPlugin {

    /**
     * Restituisce i metadati del plugin, utilizzati da SmartApiBox per:
     * - descrivere il plugin nel catalogo
     * - mostrare il nome, descrizione, versione, autore
     * - documentare l'endpoint di esempio principale (path e metodo HTTP)
     *
     * @return metadata del plugin
     */
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                "HelloWorldPlugin",                         // Nome identificativo
                "A simple Hello World plugin",              // Descrizione breve
                "v1",                                       // Versione
                "Stefania",                                 // Autore o contributor
                "/api/v1/plugin/external/hello",            // Endpoint principale (path)
                PluginMetadata.HttpMethod.GET               // Metodo HTTP dell'endpoint principale
        );
    }

    /**
     * Metodo chiamato da SmartApiBox al caricamento del plugin.
     * Qui è possibile:
     * - registrare bean (es. service, controller, utilità)
     * - inizializzare risorse o configurazioni
     *
     * I bean devono essere registrati con il registrar per permettere a Spring
     *     di gestire automaticamente l'injection delle dipendenze.
     *
     * @param registrar oggetto fornito da SmartApiBox per registrare componenti
     */
    @Override
    public void onLoad(final PluginRegistrar registrar) {
        registrar.registerBean(HelloWorldService.class);     // Service con logica GPT
        registrar.registerBean(HelloWorldController.class);  // Controller REST esposto
    }

    /**
     * Restituisce la lista dei controller REST esposti dal plugin.
     * Serve per:
     * - la registrazione dinamica del controller
     * - la generazione automatica della documentazione OpenAPI
     *
     * I controller devono essere annotati con @RestController e @RequestMapping/@GetMapping ecc.
     *
     * @return lista delle classi controller
     */
    @Override
    public List<Class<?>> getRestControllers() {
        return List.of(HelloWorldController.class);
    }
}