package hudson.plugins.blazemeter;

import hudson.*;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.blazemeter.utils.BuildResult;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;

public class PerformanceBuilder extends Builder {

    private String jobApiKey = "";

    private String serverUrl = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;

    /**
     * @deprecated as of 1.3. for compatibility
     */
    private transient String filename;

    /**
     * Configured report parsers.
     */

    @DataBoundConstructor
    public PerformanceBuilder(String jobApiKey,
                              String serverUrl,
                              String testId,
                              String notes,
                              String sessionProperties,
                              String jtlPath,
                              String junitPath,
                              boolean getJtl,
                              boolean getJunit
    ) {
        this.jobApiKey = JobUtility.selectUserKeyOnId(DESCRIPTOR, jobApiKey);
        this.serverUrl = serverUrl;
        this.testId = testId;
        this.jtlPath = jtlPath;
        this.junitPath = junitPath;
        this.getJtl = getJtl;
        this.getJunit = getJunit;
        this.notes = notes;
        this.sessionProperties = sessionProperties;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        Result r = null;
        try {
            BlazeMeterBuild b = new BlazeMeterBuild();
            b.setJobApiKey(this.jobApiKey);
            b.setServerUrl(this.serverUrl);
            b.setTestId(this.testId);
            b.setNotes(this.notes);
            b.setSessionProperties(this.sessionProperties);
            b.setJtlPath(this.jtlPath);
            b.setJunitPath(this.junitPath);
            b.setGetJtl(this.getJtl);
            b.setGetJunit(this.getJunit);
            FilePath ws = build.getWorkspace();
            b.setWs(ws);
            b.setBuildId(build.getId());
            String jobName = build.getLogFile().getParentFile().getParentFile().getParentFile().getName();
            b.setJobName(jobName);
            VirtualChannel c = launcher.getChannel();
            if (c instanceof LocalChannel) {
                String logDir = build.getLogFile().getParent();
                b.setLogDir(logDir);
            }
            EnvVars ev=EnvVars.getRemote(c);
            b.setEv(ev);
            r = c.call(b);
        } catch (InterruptedException e) {
            r=Result.ABORTED;
        } catch (Exception e) {
            listener.getLogger().print("Failed to run blazemeter test: " + e);
            r = Result.FAILURE;
        } finally {
            BuildResult rstr = BuildResult.valueOf(r.toString());
            build.setResult(r);
            switch (rstr) {
                case ABORTED:
                    return false;
                case FAILURE:
                    return false;
                case NOT_BUILT:
                    return false;
                case UNSTABLE:
                    return false;
                default:
                    return true;
            }
        }
    }


    public String getJobApiKey() {
        return jobApiKey;
    }

    public void setJobApiKey(String jobApiKey) {
        this.jobApiKey = jobApiKey;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isGetJtl() {
        return getJtl;
    }

    public boolean isGetJunit() {
        return getJunit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSessionProperties() {
        return sessionProperties;
    }

    public String getJtlPath() {
        return jtlPath;
    }

    public void setJtlPath(String jtlPath) {
        this.jtlPath = jtlPath;
    }

    public String getJunitPath() {
        return junitPath;
    }

    public void setJunitPath(String junitPath) {
        this.junitPath = junitPath;
    }

    public void setSessionProperties(String sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    @Override
    public BlazeMeterPerformanceBuilderDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final BlazeMeterPerformanceBuilderDescriptor DESCRIPTOR = new BlazeMeterPerformanceBuilderDescriptor();

    // The descriptor has been moved but we need to maintain the old descriptor for backwards compatibility reasons.
    @SuppressWarnings({"UnusedDeclaration"})
    public static final class DescriptorImpl
            extends BlazeMeterPerformanceBuilderDescriptor {

        @Override
        public boolean configure(StaplerRequest req, net.sf.json.JSONObject formData) throws FormException {
            return super.configure(req, formData);
        }
    }
}
