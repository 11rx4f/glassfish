/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author David Matejcek
 */
public class ServerUtils {

    /**
     * Tries to alocate a free local port.
     *
     * @return a free local port number.
     * @throws IllegalStateException if it fails for 20 times
     */
    public static int getFreePort() throws IllegalStateException {
        int counter = 0;
        while (true) {
            counter++;
            try {
                final ServerSocket socket = new ServerSocket(0);
                final int port = socket.getLocalPort();
                socket.setSoTimeout(1);
                socket.setReuseAddress(true);
                socket.close();
                return port;
            } catch (IOException e) {
                if (counter >= 20) {
                    throw new IllegalStateException("Cannot open random port, tried 20 times.", e);
                }
            }
        }
    }


    /**
     * @return the IP address of the localhost.
     */
    public static String getLocalIP4Address() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot determine the local address.", e);
        }
    }


    /**
     * Creates a simple war file made of provided classes.
     *
     * @param warFile
     * @param classes
     * @return file usable for deployment.
     */
    public static File createWar(final File warFile, final Class<?>... classes) {
        try {
            final WebArchive war = ShrinkWrap.create(WebArchive.class).addClasses(classes);
            war.as(ZipExporter.class).exportTo(warFile, true);
            return warFile;
        } catch (Exception e) {
            return fail(e);
        }
    }


    /**
     * Downloads content from the url.
     * Expects there is a service listening and returning textual response.
     * Therefore this is usable just for simple servlets.
     *
     * @param url
     * @return content from the url.
     * @throws IOException
     */
    public static String download(final URL url) throws IOException {
        final Object object = url.getContent();
        if (object instanceof InputStream) {
            final InputStream input = (InputStream) object;
            try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())) {
                return scanner.nextLine();
            } finally {
                input.close();
            }
        }
        return fail("Expected input stream, but received this: " + object);
    }
}
