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
package org.onosproject.provider.lisp.mapping.util;

import com.google.common.collect.Lists;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onosproject.lisp.msg.protocols.LispLocator;
import org.onosproject.lisp.msg.protocols.LispMapRecord;
import org.onosproject.lisp.msg.types.LispAfiAddress;
import org.onosproject.lisp.msg.types.LispAsAddress;
import org.onosproject.lisp.msg.types.LispDistinguishedNameAddress;
import org.onosproject.lisp.msg.types.LispIpv4Address;
import org.onosproject.lisp.msg.types.LispIpv6Address;
import org.onosproject.lisp.msg.types.LispMacAddress;
import org.onosproject.lisp.msg.types.lcaf.LispLcafAddress;
import org.onosproject.mapping.DefaultMapping;
import org.onosproject.mapping.DefaultMappingEntry;
import org.onosproject.mapping.DefaultMappingKey;
import org.onosproject.mapping.DefaultMappingTreatment;
import org.onosproject.mapping.DefaultMappingValue;
import org.onosproject.mapping.Mapping;
import org.onosproject.mapping.MappingEntry;
import org.onosproject.mapping.MappingEntry.MappingEntryState;
import org.onosproject.mapping.MappingKey;
import org.onosproject.mapping.MappingTreatment;
import org.onosproject.mapping.MappingValue;
import org.onosproject.mapping.actions.MappingAction;
import org.onosproject.mapping.actions.MappingActions;
import org.onosproject.mapping.addresses.MappingAddress;
import org.onosproject.mapping.addresses.MappingAddresses;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Mapping entry builder class.
 */
public class MappingEntryBuilder {
    private static final Logger log =
                            LoggerFactory.getLogger(MappingEntryBuilder.class);

    private static final int IPV4_PREFIX_LENGTH = 32;
    private static final int IPV6_PREFIX_LENGTH = 128;

    private final DeviceId deviceId;

    private final MappingAddress address;
    private final MappingAction action;
    private final List<MappingTreatment> treatments;

    /**
     * Default constructor for MappingEntryBuilder.
     *
     * @param deviceId device identifier
     * @param record   LISP map record
     */
    public MappingEntryBuilder(DeviceId deviceId, LispMapRecord record) {
        this.deviceId = deviceId;
        this.address = buildAddress(record);
        this.action = buildAction(record);
        this.treatments = buildTreatments(record);
    }

    /**
     * Builds mapping entry from a specific LISP control message.
     *
     * @return mapping entry
     */
    public MappingEntry build() {
        Mapping.Builder builder;

        // we assign leastSignificantBits of UUID as the mapping identifier for now
        // id generation scheme can be changed later
        UUID uuid = UUID.randomUUID();

        builder = DefaultMapping.builder()
                .withId(uuid.getLeastSignificantBits())
                .forDevice(deviceId)
                .withKey(buildKey())
                .withValue(buildValue());

        // TODO: we assume that the mapping entry will be always
        // stored in routers without failure for now, which means
        // the mapping entry state will always be ADDED rather than
        // PENDING_ADD
        // we will revisit this part when LISP driver is finished
        return new DefaultMappingEntry(builder.build(), MappingEntryState.ADDED);
    }

    /**
     * Builds mapping key.
     *
     * @return mapping key
     */
    private MappingKey buildKey() {

        MappingKey.Builder builder = DefaultMappingKey.builder();

        builder.withAddress(address);

        return builder.build();
    }

    /**
     * Builds mapping value.
     *
     * @return mapping value
     */
    private MappingValue buildValue() {

        MappingValue.Builder builder = DefaultMappingValue.builder();
        builder.withAction(action);

        treatments.forEach(builder::add);

        return builder.build();
    }

    /**
     * Builds mapping action.
     *
     * @param record LISP map record
     * @return mapping action
     */
    private MappingAction buildAction(LispMapRecord record) {

        if (record == null) {
            return MappingActions.noAction();
        }

        switch (record.getAction()) {
            case NoAction:
                return MappingActions.noAction();
            case SendMapRequest:
                return MappingActions.forward();
            case NativelyForward:
                return MappingActions.nativeForward();
            case Drop:
                return MappingActions.drop();
            default:
                log.warn("Unsupported action type {}", record.getAction());
                return MappingActions.noAction();
        }
    }

    /**
     * Builds mapping address.
     *
     * @param record LISP map record
     * @return mapping address
     */
    private MappingAddress buildAddress(LispMapRecord record) {

        return record == null ? null :
                getAddress(record.getEidPrefixAfi());
    }

    /**
     * Converts LispAfiAddress into abstracted mapping address.
     *
     * @param address LispAfiAddress
     * @return abstracted mapping address
     */
    private MappingAddress getAddress(LispAfiAddress address) {

        if (address == null) {
            log.warn("Address is not specified.");
            return null;
        }

        switch (address.getAfi()) {
            case IP4:
                return afi2MappingAddress(address);
            case IP6:
                return afi2MappingAddress(address);
            case AS:
                int asNum = ((LispAsAddress) address).getASNum();
                return MappingAddresses.asMappingAddress(String.valueOf(asNum));
            case DISTINGUISHED_NAME:
                String dn = ((LispDistinguishedNameAddress)
                                                address).getDistinguishedName();
                return MappingAddresses.dnMappingAddress(dn);
            case MAC:
                MacAddress macAddress = ((LispMacAddress) address).getAddress();
                return MappingAddresses.ethMappingAddress(macAddress);
            case LCAF:
                LispLcafAddress lcafAddress = (LispLcafAddress) address;
                return lcaf2Extension(lcafAddress);
            default:
                log.warn("Unsupported address type {}", address.getAfi());
                break;
        }

        return null;
    }

    /**
     * Converts LCAF address to extension mapping address.
     *
     * @param lcaf LCAF address
     * @return extension mapping address
     */
    private MappingAddress lcaf2Extension(LispLcafAddress lcaf) {

        // TODO: move LCAF to extension mapping to LISP Extension Interpreter
        return null;
    }

    /**
     * Converts AFI address to generalized mapping address.
     *
     * @param afiAddress IP typed AFI address
     * @return generalized mapping address
     */
    private MappingAddress afi2MappingAddress(LispAfiAddress afiAddress) {
        switch (afiAddress.getAfi()) {
            case IP4:
                IpAddress ipv4Address = ((LispIpv4Address) afiAddress).getAddress();
                IpPrefix ipv4Prefix = IpPrefix.valueOf(ipv4Address, IPV4_PREFIX_LENGTH);
                return MappingAddresses.ipv4MappingAddress(ipv4Prefix);
            case IP6:
                IpAddress ipv6Address = ((LispIpv6Address) afiAddress).getAddress();
                IpPrefix ipv6Prefix = IpPrefix.valueOf(ipv6Address, IPV6_PREFIX_LENGTH);
                return MappingAddresses.ipv6MappingAddress(ipv6Prefix);
            default:
                log.warn("Only support to convert IP address type");
                break;
        }
        return null;
    }

    /**
     * Builds a collection of mapping treatments.
     *
     * @param record LISP map record
     * @return a collection of mapping treatments
     */
    private List<MappingTreatment> buildTreatments(LispMapRecord record) {

        List<LispLocator> locators = record.getLocators();
        List<MappingTreatment> treatments = Lists.newArrayList();
        for (LispLocator locator : locators) {
            MappingTreatment.Builder builder = DefaultMappingTreatment.builder();
            LispAfiAddress address = locator.getLocatorAfi();

            final MappingAddress mappingAddress = getAddress(address);
            if (mappingAddress != null) {
                builder.withAddress(mappingAddress);
            }

            builder.setUnicastWeight(locator.getWeight())
                    .setUnicastPriority(locator.getPriority())
                    .setMulticastWeight(locator.getMulticastWeight())
                    .setMulticastPriority(locator.getMulticastPriority());

            // TODO: need to convert specific properties to
            // abstracted extension properties

            treatments.add(builder.build());
        }

        return treatments;
    }
}
