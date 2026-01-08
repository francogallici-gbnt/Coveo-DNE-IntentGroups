package com.beldenDNE.ui.steps;

import com.beldenDNE.ui.actions.pageSearchItemActions;
import io.cucumber.java.en.When;

public class searchDNE_steps {

    private final pageSearchItemActions searchItemActions = new pageSearchItemActions();

    @When("the automation runs 4 testers in parallel")
    public void runParallelTesters() throws Exception {
        searchItemActions.runFourTestersInParallel();
    }
}
