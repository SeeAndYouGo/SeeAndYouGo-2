package com.SeeAndYouGo.SeeAndYouGo.aop.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ApiFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMessage().contains("API"))
            return FilterReply.ACCEPT;
        else
            return FilterReply.DENY;
    }
}