package uk.ac.ebi.pride.toolsuite.ols.dialog.task.impl;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.toolsuite.ols.dialog.util.Util;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;

import javax.swing.*;
import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 09/07/2017.
 */
public class GetOntologiesTask extends AbstractTask{


    public OLSDialog olsDialog;

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(GetOntologiesTask.class);

    String ontologyToSelect = "";


    /**
     * Detaul Constructor
     * @param progressBar
     * @param olsClient
     */
    private GetOntologiesTask(JProgressBar progressBar, OLSClient olsClient) {
        super(progressBar, olsClient);
    }


    public GetOntologiesTask(OLSDialog olsDialog, JProgressBar progressBar, OLSClient olsClient){
        super(progressBar, olsClient);
        this.olsDialog = olsDialog;
    }

    @Override
    protected Object doInBackground() throws Exception {

        boolean error = false;
        Vector ontologyNamesAndKeys = new Vector();
        olsDialog.setPreselectedNames2Ids(new HashMap());
        List<Ontology> ontologies = olsClient.getOntologies();
        ontologies = Util.refineOntologyNames(ontologies);

        for (Ontology ontology : ontologies) {
            String key = ontology.getConfig().getPreferredPrefix();
            String temp = ontology.getName() + " [" + key + "]";
            if (olsDialog.getPreselectedOntologies().isEmpty()) {
                ontologyNamesAndKeys.add(temp);
            } else {
                if (olsDialog.getPreselectedOntologies().keySet().contains(key.toLowerCase())) {
                    if (olsDialog.getPreselectedOntologies().get(key.toUpperCase()) == null) {
                        ontologyNamesAndKeys.add(temp);
                    }
                }
            }
            if (olsDialog.getSelectedOntology().equalsIgnoreCase(temp) || olsDialog.getSelectedOntology().equalsIgnoreCase(key)) {
                ontologyToSelect = temp;
            }
        }
        if (!olsDialog.getPreselectedOntologies().isEmpty()) {
            if (olsDialog.getPreselectedOntologies().size() != ontologyNamesAndKeys.size()) {
                logger.error("Warning: One or more of your preselected ontologies have not been found in OLS");
            }
        }
        Collections.sort(ontologyNamesAndKeys);

        return ontologyNamesAndKeys;
    }

    @Override
    protected void done() {

        Vector ontologyNamesAndKeys = null;
        try {
            ontologyNamesAndKeys = (Vector) get();
            if (!olsDialog.isOnlyListPreselectedOntologies()) {
                ontologyNamesAndKeys.add(0, OLSDialog.SEARCH_IN_ALL_ONTOLOGIES_AVAILABLE_IN_THE_OLS_REGISTRY);
                if (olsDialog.getPreselectedOntologies().size() > 1) {
                    ontologyNamesAndKeys.add(1, OLSDialog.SEARCH_IN_THESE_PRESELECTED_ONTOLOGIES);
                }
            }
            olsDialog.getOntologyJComboBox().setModel(new DefaultComboBoxModel(ontologyNamesAndKeys));
            //default selected ontology. Has to be the same name shown in the menu
            olsDialog.getOntologyJComboBox().setSelectedItem(ontologyToSelect);
            olsDialog.setLastSelectedOntology( (String) olsDialog.getOntologyJComboBox().getSelectedItem());
            olsDialog.hideOrShowNewtLinks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(olsDialog, olsDialog.DEFAULT_OLS_CONNECTION_ERROR, "Failed to Contact the OLS", JOptionPane.ERROR_MESSAGE);
            logger.error("Error when trying to access OLS: ");
        }
    }
}
