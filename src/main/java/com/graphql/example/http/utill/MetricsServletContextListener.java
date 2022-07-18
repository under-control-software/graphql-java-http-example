package com.graphql.example.http.utill;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    private MetricRegistry metricRegistry;
    private Histogram histogram;

    public MetricsServletContextListener() {
        metricRegistry = new MetricRegistry();
        histogram = metricRegistry.histogram("latency-analysis");
    }

    public void update(long length) {
        double timeD = Double.parseDouble(Utility.formatTime(length));
        long timeL = Math.round(timeD);
        histogram.update(timeL);
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

}
