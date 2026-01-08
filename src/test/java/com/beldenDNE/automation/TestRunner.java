package com.beldenDNE.automation;

import java.util.stream.Stream;

public class TestRunner {
    private static final String[] defaultOptions = {
            "--glue", "com.beldenDNE.ui.steps.searchDNE_steps",
            "--plugin", "pretty", // Para salida en consola
            "--plugin", "json:target/cucumber-reports/Cucumber.json", // Reporte en formato JSON
            "--plugin", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm", // Plugin de Allure
            "classpath:features"
    };

    public static void main(String[] args) {
        Stream<String> cucumberOptions = Stream.concat(Stream.of(defaultOptions), Stream.of(args));
        io.cucumber.core.cli.Main.main(cucumberOptions.toArray(String[]::new));
    }
}