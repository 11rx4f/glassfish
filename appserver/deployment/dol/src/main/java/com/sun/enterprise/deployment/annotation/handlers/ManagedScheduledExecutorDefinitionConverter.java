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

package com.sun.enterprise.deployment.annotation.handlers;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.config.support.TranslatedConfigView;
import org.jvnet.hk2.annotations.Service;

/**
 * @author David Matejcek
 */
@Service
class ManagedScheduledExecutorDefinitionConverter {
    private static final Logger LOG = System.getLogger(ManagedScheduledExecutorDefinitionConverter.class.getName());

    public Set<ManagedScheduledExecutorDefinitionData> convert(ManagedScheduledExecutorDefinition[] definitions) {
        LOG.log(Level.TRACE, "convert(definitions={0})", (Object) definitions);
        if (definitions == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(definitions).map(this::convert).collect(Collectors.toSet());
    }


    ManagedScheduledExecutorDefinitionData convert(ManagedScheduledExecutorDefinition definition) {
        LOG.log(Level.DEBUG, "convert(definition={0})", definition);
        ManagedScheduledExecutorDefinitionData msedd = new ManagedScheduledExecutorDefinitionData();
        msedd.setName(TranslatedConfigView.expandValue(definition.name()));
        msedd.setContext(TranslatedConfigView.expandValue(definition.context()));

        if (definition.hungTaskThreshold() < 0) {
            msedd.setHungTaskThreshold(0);
        } else {
            msedd.setHungTaskThreshold(definition.hungTaskThreshold());
        }

        if (definition.maxAsync() < 0) {
            msedd.setMaxAsync(Integer.MAX_VALUE);
        } else {
            msedd.setMaxAsync(definition.maxAsync());
        }
        return msedd;
    }


    void merge(ManagedScheduledExecutorDefinitionData annotationData, ManagedScheduledExecutorDefinitionData descriptorData) {
        LOG.log(Level.DEBUG, "merge(annotationData={0}, descriptorData={1})", annotationData, descriptorData);
        if (!annotationData.getName().equals(descriptorData.getName())) {
            throw new IllegalArgumentException("Cannot merge managed executors with different names: "
                + annotationData.getName() + " x " + descriptorData.getName());
        }

        if (descriptorData.getHungTaskThreshold() <= 0 && annotationData.getHungTaskThreshold() != 0) {
            descriptorData.setHungTaskThreshold(annotationData.getHungTaskThreshold());
        }

        if (descriptorData.getMaxAsync() <= 0) {
            descriptorData.setMaxAsync(annotationData.getMaxAsync());
        }

        if (descriptorData.getContext() == null && annotationData.getContext() != null
            && !annotationData.getContext().isBlank()) {
            descriptorData.setContext(TranslatedConfigView.expandValue(annotationData.getContext()));
        }
    }
}