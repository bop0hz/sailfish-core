/******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
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
 ******************************************************************************/
package com.exactpro.sf.common.messages;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.exactpro.sf.common.services.ServiceInfo;
import com.exactpro.sf.configuration.suri.SailfishURI;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class MsgMetaData implements Cloneable {
    // unique (during SailFish run time). Mapped to 'StoredId' in StoredMessage
    private final long id;
    private final Date msgTimestamp;
    private final String msgNamespace;
    private final String msgName;

	private String fromService;
	private String toService;
	private boolean isAdmin;
	private boolean isRejected;
	private String rejectReason;
    private boolean isDirty;

	private byte[] rawMessage;
	private ServiceInfo serviceInfo;
	private SailfishURI dictionaryURI;
    private String protocol;

	@JsonCreator
	public MsgMetaData(@JsonProperty("namespace") String namespace, @JsonProperty("name") String name, @JsonProperty("msgTimestamp") Date msgTimestamp, @JsonProperty("id") long id) {
	    this.msgNamespace = namespace;
	    this.msgName = name;
	    this.msgTimestamp = msgTimestamp;
        this.id = id;
	}

    public MsgMetaData(String namespace, String name, Date msgTimestamp) {
        this(namespace, name, msgTimestamp, MessageUtil.generateId());
    }

    public MsgMetaData(String namespace, String name) {
		this(namespace, name, new Date(), MessageUtil.generateId());
	}

    public MsgMetaData(String namespace, String name, long id) {
        this(namespace, name, new Date(), id);
    }

	public long getId() {
		return id;
	}

	public String getFromService() {
		return fromService;
	}

	public void setFromService(String fromService) {
		this.fromService = fromService;
	}

	public String getToService() {
		return toService;
	}

	public void setToService(String toService) {
		this.toService = toService;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	@JsonSetter("isAdmin")
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

    public boolean isRejected() {
        return isRejected;
    }

    @Deprecated
    /**
     * @deprecated please use more userfriendly setRejectReason, it automatically set isRejected when reason non-null
     */
    @JsonSetter("isRejected")
    public void setRejected(boolean isRejected) {
        this.isRejected = isRejected;
    }

    public boolean isDirty() {
        return isDirty;
    }

    @JsonSetter("isDirty")
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public Date getMsgTimestamp() {
		return msgTimestamp;
	}

	public String getMsgNamespace() {
		return msgNamespace;
	}

	public String getMsgName() {
		return msgName;
	}

	public byte[] getRawMessage() {
        return rawMessage;
	}

	public void setRawMessage(byte[] value) {
		this.rawMessage = value;
	}

	public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public SailfishURI getDictionaryURI() {
        return dictionaryURI;
    }

    public void setDictionaryURI(SailfishURI dictionaryURI) {
        this.dictionaryURI = dictionaryURI;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    // FIXME: find all the usages of MsgMetaData.clone() and copy id as well if it doesn't break anything
    @Override
    public MsgMetaData clone() {
	    MsgMetaData metaData = new MsgMetaData(msgNamespace, msgName, msgTimestamp, MessageUtil.generateId());

        metaData.setAdmin(isAdmin);
        metaData.setRejected(isRejected);
        metaData.setDirty(isDirty);
	    metaData.setFromService(fromService);
	    metaData.setToService(toService);
	    metaData.setServiceInfo(serviceInfo);
        metaData.setDictionaryURI(dictionaryURI);
        metaData.setProtocol(protocol);
        metaData.setRejectReason(rejectReason);

	    if(rawMessage != null) {
	        metaData.setRawMessage(Arrays.copyOf(rawMessage, rawMessage.length));
	    }

	    return metaData;
	}

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(!(obj instanceof MsgMetaData)) {
            return false;
        }

        MsgMetaData that = (MsgMetaData)obj;
        EqualsBuilder builder = new EqualsBuilder();

        builder.append(msgName, that.msgName);
        builder.append(msgNamespace, that.msgNamespace);
        builder.append(msgTimestamp, that.msgTimestamp);
        builder.append(fromService, that.fromService);
        builder.append(toService, that.toService);
        builder.append(isAdmin, that.isAdmin);
        builder.append(isRejected, that.isRejected);
        builder.append(isDirty, that.isDirty);
        builder.append(rawMessage, that.rawMessage);
        builder.append(serviceInfo, that.serviceInfo);
        builder.append(dictionaryURI, that.dictionaryURI);
        builder.append(rejectReason, that.rejectReason);


        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(msgName);
        builder.append(msgNamespace);
        builder.append(msgTimestamp);
        builder.append(fromService);
        builder.append(toService);
        builder.append(isAdmin);
        builder.append(isRejected);
        builder.append(isDirty);
        builder.append(rawMessage);
        builder.append(serviceInfo);
        builder.append(dictionaryURI);
        builder.append(rejectReason);

        return builder.toHashCode();
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
        if (rejectReason != null) {
            setRejected(true);
        }
    }
}
