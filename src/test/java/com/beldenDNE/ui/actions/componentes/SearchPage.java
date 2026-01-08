package com.beldenDNE.ui.actions.componentes;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.Random;
import java.util.regex.Pattern;

public class SearchPage {

    // ==============================
    // 🔥 COOKIES
    // ==============================
    public void closeCookiesIfPresent(Page page) {
        Locator closeBtn = page.locator("#onetrust-banner-sdk button.onetrust-close-btn-handler[aria-label='Close']");

        if (closeBtn.count() == 0)
            closeBtn = page.locator("button.onetrust-close-btn-handler[aria-label='Close']:not(.save-preference-btn-handler)");

        if (closeBtn.count() == 0)
            closeBtn = page.locator("#onetrust-banner-sdk button.onetrust-close-btn-handler:not(.save-preference-btn-handler)");

        try {
            closeBtn.first().waitFor(new Locator.WaitForOptions().setTimeout(2500).setState(WaitForSelectorState.VISIBLE));
            closeBtn.first().click();
            System.out.println("🍪 Cookies closed");
        } catch (Exception ignore) {}
    }

    // ==============================
    // 🔥 READY STATE
    // ==============================
    public void waitUntilReady(Page page) {
        page.waitForLoadState();

        page.locator("atomic-search-box textarea[placeholder='Search']")
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    // ==============================
    // 🔥 SEND QUERY
    // ==============================
    public void addItemToSearch(Page page, String item, int index) {
        try {
            Locator box = page.locator("atomic-search-box textarea[placeholder='Search']");
            if (box.count() == 0)
                box = page.locator("textarea[placeholder='Search'][aria-controls*='atomic-search-box']");

            box.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            box.first().click();
            box.first().fill(item);

            System.out.println("#" + index + " | 🔎 Searching: " + item);

            page.keyboard().press("Enter");

        } catch (Exception e) {
            System.out.println("⚠️ Error writing search: " + e.getMessage());
        }
    }

    // ==============================
    // 🔥 FACET = "Cable"
    // ==============================
    public void selectFacetCategoryCable(Page page) {

        // 1️⃣ Caso A: Cable YA está seleccionado (DNE o búsqueda previa)
        Locator selectedCable = page.locator(
                "button[part='parent-button'] span:has-text('Cable')"
        );

        if (selectedCable.count() > 0) {
            System.out.println("ℹ️ Cable facet already selected — skipping selection");
            return;
        }

        // 2️⃣ Asegurar que Product Category esté visible
        Locator productCategoryButton = page.locator(
                "button[aria-label*='Product Category']"
        ).first();

        try {
            productCategoryButton.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000)
            );
        } catch (Exception e) {
            System.out.println("⚠️ Product Category facet not found — reloading");
            page.reload();
            page.waitForLoadState();
        }

        // 3️⃣ Expandir Product Category si está colapsado
        String expanded = productCategoryButton.getAttribute("aria-expanded");
        if (!"true".equals(expanded)) {
            System.out.println("📂 Expanding Product Category...");
            productCategoryButton.click();
            page.waitForTimeout(300);
        }

        // 4️⃣ Clickear el label visible "Cable"
        Locator cableLabel = page.locator(
                "span[part='value-label'][title='Cable']"
        );

        try {
            cableLabel.first().waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000)
            );

            cableLabel.first().click();
            System.out.println("✔ Cable facet selected");

        } catch (Exception e) {
            System.out.println("❌ Could not select Cable facet: " + e.getMessage());
        }
    }


    // ==============================
    // 🔥 RANDOM RESULT
    // ==============================
    public void selectRandomResult(Page page) {

        Locator results = page.locator("atomic-result:visible a:visible");

        try {
            results.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10000));
        } catch (Exception e) {
            System.out.println("❌ No results visible");
            return;
        }

        int total = results.count();
        if (total == 0) {
            System.out.println("❌ 0 results found");
            return;
        }

        // Elegir índice random
        int index = new Random().nextInt(total);
        Locator chosen = results.nth(index);

        try {
            System.out.println("🎲 Clicking random result #" + (index + 1) + "/" + total);
            chosen.click();
            System.out.println("✔ Result clicked successfully");
        } catch (Exception e) {
            System.out.println("❌ Error clicking result: " + e.getMessage());
        }
    }

}