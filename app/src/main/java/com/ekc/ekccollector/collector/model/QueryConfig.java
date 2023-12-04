package com.ekc.ekccollector.collector.model;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;

public class QueryConfig {

    public static QueryParameters getQuery(Point point, SpatialReference sp, boolean returnGeometry) {

        QueryParameters query = new QueryParameters();

        Envelope geometry = new Envelope(point, 5.0, 5.0);
        // make search case insensitive
        query.setWhereClause("1=1");
        // call select features
        query.setMaxFeatures(0);
        query.setReturnGeometry(returnGeometry);
        query.setOutSpatialReference(sp);
        query.setGeometry(geometry);

        return query;
    }

    public static QueryParameters getSurveyorAreaQuery(SpatialReference sp, String surveyorCode, boolean returnGeometry) {

        QueryParameters query = new QueryParameters();

        // make search case insensitive
        query.setWhereClause("Name = " + surveyorCode);
        // call select features
        query.setMaxFeatures(0);
        query.setReturnGeometry(returnGeometry);
        query.setOutSpatialReference(sp);

        return query;
    }
}
