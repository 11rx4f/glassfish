/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.uberjar.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import com.sun.enterprise.glassfish.bootstrap.Constants.Platform;
import com.sun.enterprise.glassfish.bootstrap.osgi.OSGiFrameworkLauncher;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.launch.Framework;
import org.osgi.util.tracker.ServiceTracker;

import static com.sun.enterprise.glassfish.bootstrap.Constants.PLATFORM_PROPERTY_KEY;
import static com.sun.enterprise.util.io.FileUtils.USER_HOME;

/**
 * @author bhavanishankar@dev.java.net
 */
public class UberJarOSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    private static Logger logger = Logger.getLogger("embedded-glassfish");

    private static final String AUTO_START_BUNDLES_PROP = "org.glassfish.embedded.osgimain.autostartBundles";

    private static final String UBER_JAR_URI = "org.glassfish.embedded.osgimain.jarURI";

    private Framework framework;

    @Override
    public boolean handles(BootstrapProperties bsOptions) {
        Platform platform = Platform.valueOf(bsOptions.getProperty(Constants.PLATFORM_PROPERTY_KEY));
        if (platform == null) {
            platform = Platform.Felix;
        }
        logger.log(Level.FINER, "platform = {0}", platform);
        // TODO(Sahoo): Add support for generic OSGi platform
        switch (platform) {
            case Felix:
            case Equinox:
            case Knopflerfish:
                return true;
            default:
                return false;
        }
    }

    public void destroy() throws GlassFishException {
        if (framework == null) {
            logger.finer("EmbeddedOSGIRuntimeBuilder.destroy called, but framework is null.");
        } else {
            try {
                framework.stop();
                framework.waitForStop(0);
                logger.info("EmbeddedOSGIRuntimeBuilder.destroy, stopped framework " + framework);
            } catch (InterruptedException ex) {
                throw new GlassFishException(ex);
            } catch (BundleException ex) {
                throw new GlassFishException(ex);
            }
        }
    }

    @Override
    public GlassFishRuntime build(BootstrapProperties bsOptions) throws GlassFishException {
        String uberJarURI = bsOptions.getProperty(UBER_JAR_URI);
        logger.log(Level.FINER, "EmbeddedOSGIRuntimeBuilder.build, uberJarUri={0}", uberJarURI);

        URI jar = null;
        try {
            jar = uberJarURI != null ? new URI(uberJarURI) : Util.whichJar(GlassFishRuntime.class);
        } catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        // XXX : Commented out by Prasad , we are again looking for instance root here. Why ?
        // String instanceRoot = props.getProperty(Constants.INSTALL_ROOT_PROP_NAME);
        String installRoot = bsOptions.getInstallRoot();

        if (installRoot == null) {
            installRoot = getDefaultInstallRoot();
            bsOptions.setInstallRoot(installRoot);
            bsOptions.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, new File(installRoot).toURI().toString());
        }

        // XXX : Assuming that this property will be set along with Bootstrap options.
        // This is a temporary hack, we need to separate the properties out between bootstrap and newGlassfish methods clearly
        // and not mix them in the code.
        String instanceRoot = bsOptions.getProperty(Constants.INSTANCE_ROOT_PROP_NAME);
        if (instanceRoot == null) {
            instanceRoot = getDefaultInstanceRoot();
            bsOptions.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot);
            bsOptions.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME, new File(instanceRoot).toURI().toString());
        }
        FileUtils.ensureWritableDir(new File(instanceRoot));
        try {
            copyConfigFile(bsOptions.getProperty(GlassFishProperties.CONFIG_FILE_URI_PROP_NAME), instanceRoot);
        } catch (Exception ex) {
            throw new GlassFishException(ex);
        }

        String platform = bsOptions.getProperty(PLATFORM_PROPERTY_KEY);
        if (platform == null) {
            platform = Platform.Felix.toString();
            bsOptions.setProperty(PLATFORM_PROPERTY_KEY, platform);
        }

       // readConfigProperties(installRoot, props);

        System.setProperty(UBER_JAR_URI, jar.toString()); // embedded-osgi-main module will need this to extract the modules.

        String osgiMainModule = "jar:" + jar.toString() + "!/uber-osgi-main.jar";
        bsOptions.setProperty("glassfish.auto.start", osgiMainModule);

        String autoStartBundleLocation = "jar:" + jar.toString() + "!/modules/installroot-builder_jar/," +
                "jar:" + jar.toString() + "!/modules/instanceroot-builder_jar/," +
                "jar:" + jar.toString() + "!/modules/kernel_jar/"; // TODO :: was modules/glassfish_jar

        if (isOSGiEnv()) {
            autoStartBundleLocation = autoStartBundleLocation +
                    ",jar:" + jar.toString() + "!/modules/osgi-modules-uninstaller_jar/";
        }

        bsOptions.setProperty(AUTO_START_BUNDLES_PROP, autoStartBundleLocation);
        System.setProperty(AUTO_START_BUNDLES_PROP, autoStartBundleLocation);

        System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot);
        System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot);

        bsOptions.setProperty("org.osgi.framework.system.packages.extra",
                "org.glassfish.simpleglassfishapi; version=3.1");

//        props.setProperty(org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT,
//                org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
//        props.setProperty("org.osgi.framework.bootdelegation", "org.jvnet.hk2.component, " +
//                "org.jvnet.hk2.component.*," +
//                "org.jvnet.hk2.annotations," +
//                "org.jvnet.hk2.annotations.*");
//        props.setProperty("org.osgi.framework.bootdelegation", "*");

        bsOptions.setProperty("org.osgi.framework.storage", instanceRoot + "/osgi-cache/Felix");
//        }

        logger.logp(Level.FINER, "EmbeddedOSGIRuntimeBuilder", "build", "Building file system {0}", bsOptions);

        try {
            if (!isOSGiEnv()) {
                final OSGiFrameworkLauncher fwLauncher = new OSGiFrameworkLauncher(bsOptions.getProperties());
                framework = fwLauncher.launchOSGiFrameWork();
                return fwLauncher.getService(GlassFishRuntime.class);
            }
            BundleContext context = ((BundleReference) getClass().getClassLoader()).getBundle().getBundleContext();
            Bundle autostartBundle = context.installBundle(bsOptions.getProperty("glassfish.auto.start"));
            autostartBundle.start(Bundle.START_TRANSIENT);
            logger.log(Level.FINER, "Started autostartBundle {0}", autostartBundle);
            return getService(GlassFishRuntime.class, context);
        } catch (Throwable t) {
            throw new GlassFishException(new Exception(t));
        }
    }

    private String getDefaultInstallRoot() {
        return new File(USER_HOME, ".glassfish7-embedded").getAbsolutePath();
    }

    private String getDefaultInstanceRoot() {
        return USER_HOME.toPath().resolve(Path.of(".glassfish7-embedded", "domains", "domain1")).toFile()
            .getAbsolutePath();
    }

    private boolean isOSGiEnv() {
        return (getClass().getClassLoader() instanceof BundleReference);
    }

    public <T> T getService(Class<T> type, BundleContext context) throws Exception {
        ServiceTracker tracker = new ServiceTracker(context, type.getName(), null);
        try {
            tracker.open(true);
            return type.cast(tracker.waitForService(0));
        } finally {
            tracker.close(); // no need to track further
        }
    }

    private void copyConfigFile(String configFileURI, String instanceRoot) throws Exception {
        if (configFileURI != null && instanceRoot != null) {
            URI configFile = URI.create(configFileURI);
            InputStream stream = configFile.toURL().openConnection().getInputStream();
            File domainXml = new File(instanceRoot, "config/domain.xml");
            logger.finer("domainXML uri = " + configFileURI + ", size = " + stream.available());
            if (!domainXml.toURI().equals(configFile)) {
                domainXml.getParentFile().mkdirs();
                Util.copy(stream, new FileOutputStream(domainXml), stream.available());
                logger.finer("Created " + domainXml);
            } else {
                logger.finer("Skipped creation of " + domainXml);
            }

        }
    }

}
