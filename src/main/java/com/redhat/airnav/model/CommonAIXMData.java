package com.redhat.airnav.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonAIXMData {

    private String id;
    private String interpretation;
    private Integer sequenceNumber;
    private String scenario;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<Map<String, String>> notifications = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public List<Map<String, String>> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Map<String, String>> notifications) {
        this.notifications = notifications;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
