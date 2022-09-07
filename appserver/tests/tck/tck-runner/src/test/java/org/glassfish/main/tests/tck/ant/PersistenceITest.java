/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import jakarta.inject.Inject;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author David Matejcek
 */
@ExtendWith(TckTestExtension.class)
public class PersistenceITest {

    @Inject
    private TckRunner tck;

    @Test
    public void appManaged() throws Exception {
        tck.start(Path.of("jpa_appmanaged"));
    }


    @Test
    public void appManagedNoTx() throws Exception {
        tck.start(Path.of("jpa_appmanagedNoTx"));
    }


    @Test
    public void pmServlet() throws Exception {
        tck.start(Path.of("jpa_pmservlet"));
    }


    @Test
    public void puServlet() throws Exception {
        tck.start(Path.of("jpa_puservlet"));
    }


    @Test
    public void stateful3() throws Exception {
        tck.start(Path.of("jpa_stateful3"));
    }


    @Test
    public void stateless3() throws Exception {
        tck.start(Path.of("jpa_stateless3"));
    }
}
