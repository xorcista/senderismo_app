package com.example.senderismo;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class DirectionsResponse {

    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public static class Route {
        @SerializedName("overview_polyline")
        private OverviewPolyline overviewPolyline;

        @SerializedName("legs")
        private List<Leg> legs;

        public OverviewPolyline getOverviewPolyline() {
            return overviewPolyline;
        }

        public List<Leg> getLegs() {
            return legs;
        }
    }

    public static class Leg {
        @SerializedName("distance")
        private TextValueObject distance;

        @SerializedName("duration")
        private TextValueObject duration;

        public TextValueObject getDistance() {
            return distance;
        }

        public TextValueObject getDuration() {
            return duration;
        }
    }

    public static class TextValueObject {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value;

        public String getText() {
            return text;
        }

        public int getValue() {
            return value;
        }
    }

    public static class OverviewPolyline {
        @SerializedName("points")
        private String points;

        public String getPoints() {
            return points;
        }
    }
}
    