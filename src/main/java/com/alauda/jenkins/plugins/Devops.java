package com.alauda.jenkins.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Devops extends AbstractDescribableImpl<Devops> {

    public static final String DEFAULT_LOGLEVEL = "0";

    @Extension
    @Symbol("alaudaDevOpsClientConfiguration")
    public static class DescriptorImpl extends Descriptor<Devops> {

        // Store a config version so we're able to migrate config.
        public Long configVersion;

        public List<ClusterConfig> clusterConfigs;

        public String tool = "kubectl";

        public DescriptorImpl() {
            configVersion = 1L;
            load();
        }

        @Override
        public String getDisplayName() {
            return "Devops Configuration";
        }

        public String getClientToolName() {
            return tool;
        }

        public void removeClusterConfig(ClusterConfig clusterConfig) throws IllegalArgumentException {
            if (clusterConfigs == null || clusterConfigs.size() <= 0) {
                throw new IllegalArgumentException("ClusterConfigs is null or empty");
            }

            clusterConfigs.remove(clusterConfig);
        }

        public void addClusterConfig(ClusterConfig clusterConfig) {
            if (clusterConfigs == null) {
                clusterConfigs = new ArrayList<>(1);
            }

            clusterConfigs.add(clusterConfig);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json)
                throws FormException {

            /**
             * If all cluster configurations are deleted in the UI and saved,
             * binJSON does not set the list. So clear out the list before bind.
             */
            clusterConfigs = null;

            req.bindJSON(this, json.getJSONObject("alaudaDevops"));
            save();
            return true;
        }

        // Creates a model that fills in logLevel options in configuration UI
        public ListBoxModel doFillLogLevelItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("0 - Least Logging", "0");
            for (int i = 1; i < 10; i++) {
                items.add("" + i, "" + i);
            }
            items.add("10 - Most Logging", "10");
            return items;
        }

        public List<ClusterConfig> getClusterConfigs() {
            if (clusterConfigs == null) {
                return new ArrayList<>(0);
            }
            return Collections.unmodifiableList(clusterConfigs);
        }

        @DataBoundSetter
        public void setClusterConfigs(List<ClusterConfig> clusterConfigs) {
            this.clusterConfigs = clusterConfigs;
        }

        /**
         * Determines if a cluster has been configured with a given name. If a
         * cluster has been configured with the name, its definition is
         * returned.
         * 
         * @param name
         *            The name of the cluster config to find
         * @return A ClusterConfig for the supplied parameters OR null.
         */
        public ClusterConfig getClusterConfig(String name) {
            if (clusterConfigs == null) {
                return null;
            }

            name = Util.fixEmptyAndTrim(name);
            for (ClusterConfig cc : clusterConfigs) {
                if (cc.getName().equalsIgnoreCase(name)) {
                    return cc;
                }
            }
            return null;
        }

    }

}
