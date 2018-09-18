/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.JobLocator;
import javax.inject.Singleton;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 * This service will scan for all jobs.xml
 * @author Bhakti Mehta
 */
@Service (name="job-filescanner")
@Singleton
public class JobFileScanner implements PostConstruct {

    @Inject
    private ServiceLocator serviceLocator;

    HashSet<File> persistedJobFiles ;

    @Override
    public void postConstruct() {

        Collection<JobLocator> services = serviceLocator.getAllServices(JobLocator.class);
        persistedJobFiles = new HashSet<File>() ;
        for (JobLocator locator: services) {
            persistedJobFiles.addAll(locator.locateJobXmlFiles());
        }
    }

    public HashSet<File> getJobFiles() {
        return persistedJobFiles;
    }
}
