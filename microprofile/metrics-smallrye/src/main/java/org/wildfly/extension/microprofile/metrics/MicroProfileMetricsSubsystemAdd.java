/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.microprofile.metrics;

import static org.jboss.as.controller.OperationContext.Stage.RUNTIME;
import static org.jboss.as.server.deployment.Phase.DEPENDENCIES;
import static org.jboss.as.server.deployment.Phase.DEPENDENCIES_MICROPROFILE_METRICS;
import static org.jboss.as.server.deployment.Phase.INSTALL;
import static org.jboss.as.server.deployment.Phase.POST_MODULE_MICROPROFILE_METRICS;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.microprofile.metrics._private.MicroProfileMetricsLogger;
import org.wildfly.extension.microprofile.metrics.deployment.DependencyProcessor;
import org.wildfly.extension.microprofile.metrics.deployment.DeploymentMetricProcessor;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2018 Red Hat inc.
 */
class MicroProfileMetricsSubsystemAdd extends AbstractBoottimeAddStepHandler {


    MicroProfileMetricsSubsystemAdd() {
        super(MicroProfileMetricsSubsystemDefinition.ATTRIBUTES);
    }

    static final MicroProfileMetricsSubsystemAdd INSTANCE = new MicroProfileMetricsSubsystemAdd();

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performBoottime(context, operation, model);

        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(MicroProfileMetricsExtension.SUBSYSTEM_NAME, DEPENDENCIES, DEPENDENCIES_MICROPROFILE_METRICS, new DependencyProcessor());
                processorTarget.addDeploymentProcessor(MicroProfileMetricsExtension.SUBSYSTEM_NAME, INSTALL, POST_MODULE_MICROPROFILE_METRICS, new DeploymentMetricProcessor());
            }
        }, RUNTIME);

        final boolean securityEnabled = MicroProfileMetricsSubsystemDefinition.SECURITY_ENABLED.resolveModelAttribute(context, model).asBoolean();
        List<String> exposedSubsystems = MicroProfileMetricsSubsystemDefinition.EXPOSED_SUBSYSTEMS.unwrap(context, model);
        String prefix = MicroProfileMetricsSubsystemDefinition.PREFIX.resolveModelAttribute(context, model).asStringOrNull();

        MetricsContextService.install(context, securityEnabled);
        MetricsRegistrationService.install(context, exposedSubsystems, prefix);

        MicroProfileMetricsLogger.LOGGER.activatingSubsystem();
    }
}
