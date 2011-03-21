/*
 * #%L
 * Service Activity Monitoring :: Agent
 * %%
 * Copyright (C) 2011 Talend Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.talend.esb.sam.agent.serviceclient;

import java.util.ArrayList;
import java.util.List;

import org.talend.esb.sam._2011._03.common.EventType;
import org.talend.esb.sam.common.event.Event;
import org.talend.esb.sam.common.event.MonitoringException;
import org.talend.esb.sam.monitoringservice.v1.MonitoringService;

/**
 * Wraps business logic to web service logic. So web service should be changeable.
 * 
 */
public class MonitoringServiceWrapper implements org.talend.esb.sam.common.service.MonitoringService {
    private MonitoringService monitoringService;

    private int numberOfRetries;
    private long delayBetweenRetry;

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public void setDelayBetweenRetry(long delayBetweenRetry) {
        this.delayBetweenRetry = delayBetweenRetry;
    }

    /**
     * Set by Spring. Sets the web service implementation.
     * 
     * @param monitoringService
     */
    public void setMonitoringService(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Set by Spring. Sets the dozer mapper for transforming objects.
     * 
     * @param mapper
     */
    /*
     * public void setMapper(DozerBeanMapper mapper) { this.mapper = mapper; }
     */

    /**
     * Sends all events to the web service. Events will be transformed with mapper before sending.
     */
    public void putEvents(List<Event> events) {
        Exception lastException;
        List<EventType> eventTypes = new ArrayList<EventType>();
        for (Event event : events) {
            EventType eventType = EventMapper.map(event);
            eventTypes.add(eventType);
        }

        int i = 0;
        if (numberOfRetries == 0)
            numberOfRetries = 5;
        lastException = null;
        while (i < numberOfRetries) {
            try {
                monitoringService.putEvents(eventTypes);
                break;
            } catch (Exception e) {
                lastException = e;
                i++;
                if (delayBetweenRetry <= 0) {
                    delayBetweenRetry = 1000;
                }
                try {
                    Thread.sleep(delayBetweenRetry);
                } catch (Exception e1) {
                }
            }
        }

        if (i == numberOfRetries) {
            throw new MonitoringException("1104", "Could not send events to monitoring service after "
                                                  + numberOfRetries + " retries.", lastException, events);
        }
    }


}
