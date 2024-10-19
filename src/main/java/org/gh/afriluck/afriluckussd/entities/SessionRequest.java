package org.gh.afriluck.afriluckussd.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_requests")
public class SessionRequest {

    @Id
    @UuidGenerator
    private UUID id;
    public String msisdn;
    public String network;
    public String data, sequenceID, message;
    public LocalDateTime timestamp;


    public SessionRequest(String msisdn, String network, String data, String sequenceID, LocalDateTime timestamp, String message) {
        this.msisdn = msisdn;
        this.network = network;
        this.data = data;
        this.sequenceID = sequenceID;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SessionRequest{" +
                "id=" + id +
                ", msisdn='" + msisdn + '\'' +
                ", network='" + network + '\'' +
                ", data='" + data + '\'' +
                ", sequenceID='" + sequenceID + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
