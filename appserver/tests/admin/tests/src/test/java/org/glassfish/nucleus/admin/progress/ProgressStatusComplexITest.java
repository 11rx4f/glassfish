/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin.progress;

import java.util.List;

import org.glassfish.nucleus.test.tool.asadmin.Asadmin;
import org.glassfish.nucleus.test.tool.asadmin.AsadminResult;
import org.glassfish.nucleus.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.Test;

import static org.glassfish.nucleus.admin.progress.ProgressMessage.grepProgressMessages;
import static org.glassfish.nucleus.admin.progress.ProgressMessage.isIncreasing;
import static org.glassfish.nucleus.admin.progress.ProgressMessage.uniqueMessages;
import static org.glassfish.nucleus.test.tool.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
public class ProgressStatusComplexITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    public void executeCommandFromCommand() {
        AsadminResult result = ASADMIN.exec(30_000, false, "progress-exec-other");
        assertThat(result, asadminOK());
        assertArrayEquals(new String[] {
            "Starting", "Preparing", "Parsing", "Working on main part",
            "Cleaning", "Finished", "Finishing outer command", "Finished outer command" },
            uniqueMessages(grepProgressMessages(result.getStdOut())));
    }

    @Test
    public void executeCommandWithSupplements() {
        AsadminResult result = ASADMIN.exec("progress-supplement");
        assertThat(result, asadminOK());

        List<ProgressMessage> prgs = grepProgressMessages(result.getStdOut());
        assertArrayEquals(new String[] {
            "Starting", "2 seconds supplemental command", "Parsing",
            "Working on main part", "Finished", "3 seconds supplemental" },
            uniqueMessages(prgs));

        assertThat(prgs, hasSize(21));
        assertThat(prgs, isIncreasing());
        assertFalse(prgs.get(4).isPercentage());
        assertTrue(prgs.get(10).isPercentage());
    }

    @Test
    public void executeVeryComplexCommand() {
        AsadminResult result = ASADMIN.exec("progress-complex");
        assertThat(result, asadminOK());

        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertThat(prgs, hasSize(greaterThanOrEqualTo(100)));
        assertThat(prgs, isIncreasing());
        assertEquals(5, scopeCount(prgs, "complex:"));
        assertEquals(0, scopeCount(prgs, "complex.ch1:"));
        assertThat(scopeCount(prgs, "complex.ch2-paral:"), anyOf(equalTo(5), equalTo(6)));
        assertEquals(4, scopeCount(prgs, "complex.ch3:"));
        assertEquals(5, scopeCount(prgs, "complex.ch1.ch11:"));
        assertEquals(6, scopeCount(prgs, "complex.ch1.ch12:"));
        assertEquals(25, scopeCount(prgs, "complex.ch2-paral.ch21:"));
        assertEquals(25, scopeCount(prgs, "complex.ch2-paral.ch22:"));
        assertEquals(25, scopeCount(prgs, "complex.ch2-paral.ch23:"));
        assertEquals(25, scopeCount(prgs, "complex.ch2-paral.ch24:"));
        assertEquals(5, scopeCount(prgs, "complex.ch3.ch31:"));
        assertEquals(5, scopeCount(prgs, "complex.ch3.ch32:"));
    }

    private int scopeCount(List<ProgressMessage> prgs, String scope) {
        int result = 0;
        for (ProgressMessage prg : prgs) {
            if (scope.equals(prg.getScope())) {
                result++;
            }
        }
        return result;
    }
}
