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
package org.onosproject.drivers.lisp.extensions.codec;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.drivers.lisp.extensions.LispAsAddress;
import org.onosproject.mapping.addresses.MappingAddress;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.nullIsIllegal;

/**
 * LISP AS address codec.
 */
public final class LispAsAddressCodec extends JsonCodec<LispAsAddress> {

    protected static final String AS_NUMBER = "asNumber";
    protected static final String ADDRESS = "address";
    private static final String MISSING_MEMBER_MESSAGE =
                                " member is required in LispAsAddress";

    @Override
    public ObjectNode encode(LispAsAddress address, CodecContext context) {
        checkNotNull(address, "LispAsAddress cannot be null");

        final ObjectNode result = context.mapper().createObjectNode()
                                    .put(AS_NUMBER, address.getAsNumber());

        if (address.getAddress() != null) {
            final JsonCodec<MappingAddress> addressCodec =
                    context.codec(MappingAddress.class);
            ObjectNode addressNode = addressCodec.encode(address.getAddress(), context);
            result.set(ADDRESS, addressNode);
        }

        return result;
    }

    @Override
    public LispAsAddress decode(ObjectNode json, CodecContext context) {
        if (json == null || !json.isObject()) {
            return null;
        }

        int asNumber = nullIsIllegal(json.get(AS_NUMBER),
                AS_NUMBER + MISSING_MEMBER_MESSAGE).asInt();

        ObjectNode addressJson = get(json, ADDRESS);
        MappingAddress mappingAddress = null;

        if (addressJson != null) {
            final JsonCodec<MappingAddress> addressCodec =
                    context.codec(MappingAddress.class);
            mappingAddress = addressCodec.decode(addressJson, context);
        }

        return new LispAsAddress.Builder()
                        .withAsNumber(asNumber)
                        .withAddress(mappingAddress)
                        .build();
    }
}
