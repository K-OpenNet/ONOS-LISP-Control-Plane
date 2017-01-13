/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.provider.lisp.mapping.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.lisp.ctl.LispController;
import org.onosproject.lisp.ctl.LispMessageListener;
import org.onosproject.lisp.ctl.LispRouterId;
import org.onosproject.lisp.ctl.LispRouterListener;
import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider which uses a LISP controller to manage EID-RLOC mapping.
 */
@Component(immediate = true)
public class LispMappingProvider extends AbstractProvider {

    private static final Logger log = LoggerFactory.getLogger(LispMappingProvider.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LispController controller;

    private static final String SCHEME_NAME = "lisp";
    private static final String MAPPING_PROVIDER_PACKAGE =
                                "org.onosproject.lisp.provider.mapping";

    private final InternalMappingProvider listener = new InternalMappingProvider();

    /**
     * Creates a LISP mapping provider with the supplier identifier.
     */
    public LispMappingProvider() {
        super(new ProviderId(SCHEME_NAME, MAPPING_PROVIDER_PACKAGE));
    }

    @Activate
    public void activate() {

        // listens all LISP router related events
        controller.addRouterListener(listener);

        // listens all LISP control message
        controller.addMessageListener(listener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {

        // stops listening all LISP router related events
        controller.removeRouterListener(listener);

        // stops listening all LISP control messages
        controller.removeMessageListener(listener);

        log.info("Stopped");
    }

    /**
     * A listener for LISP router events and control messages.
     */
    private class InternalMappingProvider implements LispRouterListener,
                                                     LispMessageListener {

        @Override
        public void routerAdded(LispRouterId routerId) {

        }

        @Override
        public void routerRemoved(LispRouterId routerId) {

        }

        @Override
        public void routerChanged(LispRouterId routerId) {

        }

        @Override
        public void handleIncomingMessage(LispRouterId routerId, LispMessage msg) {

        }

        @Override
        public void handleOutgoingMessage(LispRouterId routerId, LispMessage msg) {

        }
    }
}
