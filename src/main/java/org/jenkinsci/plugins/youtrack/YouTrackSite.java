package org.jenkinsci.plugins.youtrack;

import hudson.model.*;
import hudson.scm.ChangeLogSet;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YouTrackSite {
    @Getter @Setter private String name;
    @Getter @Setter private String url;
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private transient boolean pluginEnabled;
    @Getter @Setter private transient boolean runAsEnabled;
    @Getter @Setter private transient boolean commandsEnabled;
    @Getter @Setter private transient boolean commentEnabled;
    @Getter @Setter private transient SecureGroovyScript commentTextSecure;
    @Getter @Setter private transient boolean annotationsEnabled;
    @Getter @Setter private transient String linkVisibility;
    @Getter @Setter private transient String stateFieldName;
    @Getter @Setter private transient String fixedValues;
    @Getter @Setter private transient boolean silentCommands;
    @Getter @Setter private transient boolean silentLinks;
    @Getter @Setter private transient String project;
    @Getter @Setter private transient String executeProjectLimits;
    @Getter @Setter private transient List<PrefixCommandPair> prefixCommandPairs;
    @Getter @Setter private boolean trackCommits;
    @Getter @Setter private YoutrackBuildFailureMode failureMode;

    @DataBoundConstructor
    public YouTrackSite(String name, String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.name = name;
    }

    public static YouTrackSite get(Job<?, ?> project) {
        YouTrackProjectProperty ypp = project.getProperty(YouTrackProjectProperty.class);
        if (ypp != null) {
            YouTrackSite site = ypp.getSite();
            if (site != null) {
                return site;
            }
        }

        return null;
    }

    public List<? extends ChangeLogSet.Entry> getChangeLogEntries(WorkflowRun run) {
        List<ChangeLogSet.Entry> changeLogEntries = new ArrayList<>();
        for (ChangeLogSet cs : run.getChangeSets()) {
            Iterator<? extends ChangeLogSet.Entry> changeLogIterator = cs.iterator();
            while (changeLogIterator.hasNext()) {
                changeLogEntries.add(changeLogIterator.next());
            }
        }

        return changeLogEntries;
    }

    /**
     * Updates the result for build, depending on the failure mode.
     * @param run the build to update the result for.
     */
    public void failed(Run<?, ?> run) {
        if (failureMode != null) {
            switch (failureMode) {
                case NONE:
                    break;
                case UNSTABLE:
                    run.setResult(Result.UNSTABLE);
                    break;
                case FAILURE:
                    run.setResult(Result.FAILURE);
                    break;
            }
        }
    }

    public YouTrackServer createServer() {
        return new YouTrackServer(getUrl());
    }

    public User getUser() { return getUser(null); }

    public User getUser(YouTrackServer server) {
        if(server==null)
            server = createServer();

        return server.login(getUsername(), getPassword());
    }
}
