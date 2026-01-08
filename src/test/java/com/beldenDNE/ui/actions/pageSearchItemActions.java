package com.beldenDNE.ui.actions;

import com.beldenDNE.ui.actions.componentes.SearchPage;
import com.microsoft.playwright.*;
import org.apache.poi.ss.usermodel.*;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class pageSearchItemActions {

    private final SearchPage componente = new SearchPage();

    // ============================================================
    // PUBLIC METHOD — called by Cucumber
    // ============================================================
    public void runFourTestersInParallel() throws Exception {

        String excelPath = "src/test/resources/data/Inventory.xlsx";
        String sheetName = "2IntentGroups";

        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(() -> runTester("Tester-1", "Broadcast", excelPath, sheetName));
        executor.submit(() -> runTester("Tester-2", "Ethernet", excelPath, sheetName));
        executor.submit(() -> runTester("Tester-3", "Fiber", excelPath, sheetName));
        executor.submit(() -> runTester("Tester-4", "Control", excelPath, sheetName));

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.MINUTES);

        System.out.println("=================================================");
        System.out.println("🔥 TODOS LOS TESTERS FINALIZARON");
        System.out.println("=================================================");
    }

    // ============================================================
    // 1 TESTER = 1 browser session = 1 clientId
    // ============================================================
    private void runTester(String testerName,
                           String intentGroup,
                           String excelPath,
                           String sheetName) {

        System.out.printf("=========== Iniciando %s (%s) ===========%n", testerName, intentGroup);

        List<String> queries = loadQueries(excelPath, sheetName, intentGroup);

        if (queries.isEmpty()) {
            System.out.printf("[%s] No se encontraron queries para '%s'%n",
                    testerName, intentGroup);
            return;
        }

        Playwright pw = null;
        Browser browser = null;
        BrowserContext context = null;
        Page page = null;

        try {
            // 1) Cada tester crea su propia sesión
            pw = Playwright.create();
            browser = pw.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
            );

            // Crear carpeta HAR según tester
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String harFolder = "har_output/" + testerName + "_" + timestamp;
            new java.io.File(harFolder).mkdirs();

            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setRecordHarPath(Paths.get(harFolder + "/session.har"))
                            .setRecordHarOmitContent(true)
            );

            page = context.newPage();

            attachAnalyticsListener(page, testerName);

            // 2) Ir a la página inicial
            page.navigate("https://condor-qa.belden.com/search",
                    new Page.NavigateOptions().setTimeout(30000));

            componente.closeCookiesIfPresent(page);

            // 3) Ejecutar todas las queries del grupo
            int idx = 0;
            for (String q : queries) {
                idx++;

                System.out.printf("[%s] #%d | Searching: %s%n", testerName, idx, q);

                componente.addItemToSearch(page, q, idx);
                componente.selectFacetCategoryCable(page);
                componente.selectRandomResult(page);

                // volver al home de búsqueda
                page.navigate("https://condor-qa.belden.com/search",
                        new Page.NavigateOptions().setTimeout(30000));

                componente.closeCookiesIfPresent(page);
            }

        } catch (Exception e) {
            System.out.printf("[%s] ERROR ejecutando grupo '%s': %s%n",
                    testerName, intentGroup, e.getMessage());
        } finally {
            try { if (page != null) page.close(); } catch (Exception ignored) {}
            try { if (context != null) context.close(); } catch (Exception ignored) {}
            try { if (browser != null) browser.close(); } catch (Exception ignored) {}
            try { if (pw != null) pw.close(); } catch (Exception ignored) {}

            System.out.printf("=========== %s FINALIZADO ===========%n", testerName);
        }
    }

    // ============================================================
    // Listener para capturar clientId por tester
    // ============================================================
    private void attachAnalyticsListener(Page page, String testerName) {

        page.onRequestFinished(req -> {

            if (!req.url().contains("analytics.org.coveo.com/rest/v15/analytics/search"))
                return;

            String body = req.postData();
            if (body == null) return;

            if (body.contains("\"clientId\"")) {

                int idx = body.indexOf("\"clientId\":\"") + "\"clientId\":\"".length();
                String sub = body.substring(idx);
                String clientId = sub.substring(0, sub.indexOf("\""));

                System.out.printf("🎯 [%s] Client ID capturado: %s%n", testerName, clientId);
            }
        });
    }

    // ============================================================
    // Cargar queries del Excel según grupo
    // ============================================================
    private List<String> loadQueries(String excelPath,
                                     String sheetName,
                                     String intentGroup) {

        List<String> queries = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                System.out.println("❌ No existe la hoja '" + sheetName + "'");
                return queries;
            }

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; // encabezado

                Cell g = row.getCell(0);
                Cell q = row.getCell(1);

                if (g == null || q == null) continue;

                String groupVal = g.getStringCellValue().trim();
                String queryVal = q.getStringCellValue().trim();

                if (groupVal.equalsIgnoreCase(intentGroup) && !queryVal.isEmpty()) {
                    queries.add(queryVal);
                }
            }

            System.out.printf("📌 Grupo '%s' → %d queries cargadas%n",
                    intentGroup, queries.size());

        } catch (Exception e) {
            System.out.println("❌ Error leyendo Excel: " + e.getMessage());
        }

        return queries;
    }
}
