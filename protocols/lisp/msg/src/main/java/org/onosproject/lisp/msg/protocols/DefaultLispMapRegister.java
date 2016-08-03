/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.lisp.msg.protocols;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import org.onlab.util.ImmutableByteSequence;

import java.util.List;

/**
 * Default LISP map register message class.
 */
public final class DefaultLispMapRegister implements LispMapRegister {

    private final long nonce;
    private final short keyId;
    private final byte[] authenticationData;
    private final byte recordCount;
    private final List<LispMapRecord> mapRecords;
    private final boolean proxyMapReply;
    private final boolean wantMapNotify;

    /**
     * A private constructor that protects object instantiation from external.
     *
     * @param nonce              nonce
     * @param keyId              key identifier
     * @param authenticationData authentication data
     * @param recordCount        record count number
     * @param mapRecords         a collection of map records
     * @param proxyMapReply      proxy map reply flag
     * @param wantMapNotify      want map notify flag
     */
    private DefaultLispMapRegister(long nonce, short keyId,
                                   byte[] authenticationData, byte recordCount,
                                   List<LispMapRecord> mapRecords,
                                   boolean proxyMapReply, boolean wantMapNotify) {
        this.nonce = nonce;
        this.keyId = keyId;
        this.authenticationData = authenticationData;
        this.recordCount = recordCount;
        this.mapRecords = mapRecords;
        this.proxyMapReply = proxyMapReply;
        this.wantMapNotify = wantMapNotify;
    }

    @Override
    public LispType getType() {
        return LispType.LISP_MAP_REGISTER;
    }

    @Override
    public void writeTo(ByteBuf byteBuf) {
        // TODO: serialize LispMapRegister message
    }

    @Override
    public Builder createBuilder() {
        return new DefaultRegisterBuilder();
    }

    @Override
    public boolean isProxyMapReply() {
        return proxyMapReply;
    }

    @Override
    public boolean isWantMapNotify() {
        return wantMapNotify;
    }

    @Override
    public byte getRecordCount() {
        return recordCount;
    }

    @Override
    public long getNonce() {
        return nonce;
    }

    @Override
    public short getKeyId() {
        return keyId;
    }

    @Override
    public byte[] getAuthenticationData() {
        return ImmutableByteSequence.copyFrom(this.authenticationData).asArray();
    }

    @Override
    public List<LispMapRecord> getLispRecords() {
        return ImmutableList.copyOf(mapRecords);
    }

    public static final class DefaultRegisterBuilder implements RegisterBuilder {

        private long nonce;
        private short keyId;
        private byte[] authenticationData;
        private byte recordCount;
        private final List<LispMapRecord> mapRecords = Lists.newArrayList();
        private boolean proxyMapReply;
        private boolean wantMapNotify;

        @Override
        public LispType getType() {
            return LispType.LISP_MAP_REGISTER;
        }

        @Override
        public RegisterBuilder withIsProxyMapReply(boolean proxyMapReply) {
            this.proxyMapReply = proxyMapReply;
            return this;
        }

        @Override
        public RegisterBuilder withIsWantMapNotify(boolean wantMapNotify) {
            this.wantMapNotify = wantMapNotify;
            return this;
        }

        @Override
        public RegisterBuilder withRecordCount(byte recordCount) {
            this.recordCount = recordCount;
            return this;
        }

        @Override
        public RegisterBuilder withNonce(long nonce) {
            this.nonce = nonce;
            return this;
        }

        @Override
        public RegisterBuilder withKeyId(short keyId) {
            this.keyId = keyId;
            return this;
        }

        @Override
        public RegisterBuilder withAuthenticationData(byte[] authenticationData) {
            this.authenticationData = authenticationData;
            return this;
        }

        @Override
        public RegisterBuilder addRecord(LispMapRecord record) {
            this.mapRecords.add(record);
            return this;
        }

        @Override
        public LispMapRegister build() {
            return new DefaultLispMapRegister(nonce, keyId, authenticationData,
                    recordCount, mapRecords, proxyMapReply, wantMapNotify);
        }
    }
}
