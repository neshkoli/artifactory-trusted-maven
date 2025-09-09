package org.jfrog.buildinfo;

import lombok.experimental.Delegate;
import org.jfrog.build.api.util.NullLog;
import org.jfrog.build.extractor.clientConfiguration.ArtifactoryClientConfiguration;
import org.jfrog.build.extractor.clientConfiguration.PrefixPropertyHandler;

/**
 * Represents the Artifactory Maven plugin configuration in the pom.xml file.
 * The configuration is automatically injected to this class.
 *
 * @author yahavi
 */
public class Config {

    private static final ArtifactoryClientConfiguration CLIENT_CONFIGURATION = new ArtifactoryClientConfiguration(new NullLog());

    /**
     * Represents the 'artifactory' configuration in the pom.xml.
     */
    public static class Artifactory {
        @Delegate
        ArtifactoryClientConfiguration delegate = CLIENT_CONFIGURATION;

        // Explicit bridge methods for XML mapping
        public void setIncludeEnvVars(Boolean includeEnvVars) {
            if (includeEnvVars != null) {
                delegate.setIncludeEnvVars(includeEnvVars);
            }
        }
        public Boolean getIncludeEnvVars() {
            return delegate.isIncludeEnvVars();
        }
        public void setEnvVarsExcludePatterns(String patterns) {
            delegate.setEnvVarsExcludePatterns(patterns);
        }
        public String getEnvVarsExcludePatterns() {
            return delegate.getEnvVarsExcludePatterns();
        }
        public void setEnvVarsIncludePatterns(String patterns) {
            delegate.setEnvVarsIncludePatterns(patterns);
        }
        public String getEnvVarsIncludePatterns() {
            return delegate.getEnvVarsIncludePatterns();
        }
        public void setTimeoutSec(Integer timeoutSec) {
            if (timeoutSec != null) {
                delegate.setTimeoutSec(timeoutSec);
            }
        }
    }

    /**
     * Represents the 'resolver' configuration in the pom.xml.
     */
    public static class Resolver {
        @Delegate(types = {
                ArtifactoryClientConfiguration.ResolverHandler.class,
                ArtifactoryClientConfiguration.RepositoryConfiguration.class,
                ArtifactoryClientConfiguration.AuthenticationConfiguration.class,
                PrefixPropertyHandler.class})
        ArtifactoryClientConfiguration.ResolverHandler delegate = CLIENT_CONFIGURATION.resolver;
    }

    /**
     * Represents the 'publisher' configuration in the pom.xml.
     */
    public static class Publisher {
        @Delegate(types = {
                ArtifactoryClientConfiguration.PublisherHandler.class,
                ArtifactoryClientConfiguration.RepositoryConfiguration.class,
                ArtifactoryClientConfiguration.AuthenticationConfiguration.class,
                PrefixPropertyHandler.class})
        ArtifactoryClientConfiguration.PublisherHandler delegate = CLIENT_CONFIGURATION.publisher;
    }

    /**
     * Represents the 'buildInfo' configuration in the pom.xml.
     */
    public static class BuildInfo {
        @Delegate(types = {ArtifactoryClientConfiguration.BuildInfoHandler.class, PrefixPropertyHandler.class})
        ArtifactoryClientConfiguration.BuildInfoHandler delegate = CLIENT_CONFIGURATION.info;

        // Explicit bridge methods for XML mapping
        public void setBuildName(String buildName) { delegate.setBuildName(buildName); }
        public String getBuildName() { return delegate.getBuildName(); }

        public void setBuildNumber(String buildNumber) { delegate.setBuildNumber(buildNumber); }
        public String getBuildNumber() { return delegate.getBuildNumber(); }

        public void setBuildUrl(String buildUrl) { delegate.setBuildUrl(buildUrl); }
        public String getBuildUrl() { return delegate.getBuildUrl(); }

        public void setProject(String project) { delegate.setProject(project); }
        public String getProject() { return delegate.getProject(); }

        public void setBuildNumbersNotToDelete(String value) { delegate.setBuildNumbersNotToDelete(value); }
        public String getBuildNumbersNotToDelete() { return delegate.getBuildNumbersNotToDelete(); }

        public void setBuildRetentionMaxDays(Integer days) { if (days != null) delegate.setBuildRetentionMaxDays(days); }
        public Integer getBuildRetentionMaxDays() { return delegate.getBuildRetentionMaxDays(); }

        public void setBuildRetentionCount(Integer count) { if (count != null) delegate.setBuildRetentionCount(count); }
        public Integer getBuildRetentionCount() { return delegate.getBuildRetentionCount(); }

        public void setPrincipal(String principal) { delegate.setPrincipal(principal); }
        public String getPrincipal() { return delegate.getPrincipal(); }
    }

    /**
     * Represents the 'proxy' configuration in the pom.xml.
     */
    public static class Proxy {
        @Delegate(types = {ArtifactoryClientConfiguration.ProxyHandler.class, PrefixPropertyHandler.class})
        ArtifactoryClientConfiguration.ProxyHandler delegate = CLIENT_CONFIGURATION.proxy;
    }
}
