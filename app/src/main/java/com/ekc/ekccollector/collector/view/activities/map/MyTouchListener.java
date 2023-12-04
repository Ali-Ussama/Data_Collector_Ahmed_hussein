package com.ekc.ekccollector.collector.view.activities.map;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MyTouchListener implements MapView.OnTouchListener {

    private String type = "POLYGON";
    private Point startPoint = null;
    private MapView mapView;
    private MapActivity mCurrent;
    private ArcGISMap basemap;
    private GraphicsOverlay drawGraphicLayer;
    private Polygon poly;

    MyTouchListener(MapActivity context, MapView view, ArcGISMap map, GraphicsOverlay drawGraphicLayer) {
        super();
        this.drawGraphicLayer = drawGraphicLayer;
        this.mapView = view;
        this.mCurrent = context;
        this.basemap = map;
    }
//
//    public boolean onSingleTap(MotionEvent e) {
//        if (type.length() > 1 && type.equalsIgnoreCase("POINT")) {
//            drawGraphicLayer.removeAll();
//            Graphic graphic = new Graphic(mapView.toMapPoint(new Point(e.getX(), e.getY())), new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE));
//            //graphic.setGeometry();
//            drawGraphicLayer.addGraphic(graphic);
//
//            return true;
//        }
//        return false;
//
//    }
//
//    public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
//
//        if (type.length() > 1 && type.equalsIgnoreCase("POLYGON")) {
//
//            Point mapPt = mapView.toMapPoint(to.getX(), to.getY());
//
//            if (startPoint == null) {
//                drawGraphicLayer.removeAll();
//
//                poly = new Polygon();
//
//                startPoint = mapView.toMapPoint(from.getX(), from.getY());
//
//                poly.startPath((float) startPoint.getX(), (float) startPoint.getY());
//
//            }
//
//            poly.lineTo((float) mapPt.getX(), (float) mapPt.getY());
//
//
//            Graphic graphic = new Graphic(poly, new SimpleMarkerSymbol(Color.parseColor("#90000000"), 10, SimpleMarkerSymbol.STYLE.CIRCLE));
//
//            drawGraphicLayer.addGraphic(graphic);
//            return true;
//
//        }
//        return super.onDragPointerMove(from, to);
//    }
//
//    @Override
//    public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
//        if (type.length() > 1 && type.equalsIgnoreCase("POLYGON")) {
//            if (type.equalsIgnoreCase("POLYGON")) {
//                poly.lineTo((float) startPoint.getX(), (float) startPoint.getY());
//                drawGraphicLayer.removeAll();
//                drawGraphicLayer.addGraphic(new Graphic(poly, new SimpleFillSymbol(Color.parseColor("#88000000"))));
//            }
//
//            Graphic graphic = new Graphic(poly, new SimpleLineSymbol(Color.BLUE, 1));
//            drawGraphicLayer.addGraphic(graphic);
//            startPoint = null;
//
////                Envelope env = new Envelope();
////                Envelope NewEnv = new Envelope();
////                for (int i : drawGraphicLayer.getGraphicIDs()) {
////                    Polygon p = (Polygon) drawGraphicLayer.getGraphic(i).getGeometry();
////                    p.queryEnvelope(env);
////                    NewEnv.merge(env);
////                }
////
////                Log.d("Test", "Graphic Extent = " + NewEnv.getXMin());
////                Log.d("Test", "Map Extent = " + mapView.getExtent().getPoint(0).getX());
////                mapView.setExtent(NewEnv);
//
//            mapView.setExtent(poly);
//
//            return true;
//        }
//        return super.onDragPointerUp(from, to);
//    }

    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (type.length() > 1 && type.equalsIgnoreCase("POINT")) {
            drawGraphicLayer.getGraphics().clear();
            Point point = new Point(e.getX(), e.getY());

            Graphic graphic = new Graphic(point, new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10));
            //graphic.setGeometry();
            drawGraphicLayer.getGraphics().add(graphic);

            return true;
        }
        return false;
    }

    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param from      The first down motion event that started the scrolling.
     * @param to        The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent from, MotionEvent to, float distanceX, float distanceY) {

        if (type.length() > 1 && type.equalsIgnoreCase("POLYGON")) {
//
            Point mapPt = new Point(to.getX(), to.getY());
            Polyline polyline = null;
            if (startPoint == null) {
                drawGraphicLayer.getGraphics().clear();
                PointCollection points = new PointCollection(mapView.getSpatialReference());
                startPoint = new Point(from.getX(), from.getY());

                points.add(startPoint);
                poly = new Polygon(points);

                polyline = poly.toPolyline();
//                poly.startPath((float) startPoint.getX(), (float) startPoint.getY());
            }

            Graphic graphic = new Graphic(polyline, new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.DKGRAY, 10));

            drawGraphicLayer.getGraphics().add(graphic);
            return true;

        }
        return false;
    }

    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1        The first down motion event that started the fling.
     * @param e2        The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second
     *                  along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second
     *                  along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onMultiPointerTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTouchDrag(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onRotate(MotionEvent motionEvent, double v) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    /**
     * Notified when a double-tap occurs.
     *
     * @param e The down motion event of the first tap of the double-tap.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    /**
     * Notified when an event within a double-tap gesture occurs, including
     * the down, move, and up events.
     *
     * @param e The motion event that occurred during the double-tap gesture.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /**
     * Responds to scaling events for a gesture in progress.
     * Reported by pointer motion.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should consider this event
     * as handled. If an event was not handled, the detector
     * will continue to accumulate movement until an event is
     * handled. This can be useful if an application, for example,
     * only wants to update scaling factors if the change is
     * greater than 0.01.
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    /**
     * Responds to the beginning of a scaling gesture. Reported by
     * new pointers going down.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     * @return Whether or not the detector should continue recognizing
     * this gesture. For example, if a gesture is beginning
     * with a focal point outside of a region where it makes
     * sense, onScaleBegin() may return false to ignore the
     * rest of the gesture.
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    /**
     * Responds to the end of a scale gesture. Reported by existing
     * pointers going up.
     * <p>
     * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
     * and {@link ScaleGestureDetector#getFocusY()} will return focal point
     * of the pointers remaining on the screen.
     *
     * @param detector The detector reporting the event - use this to
     *                 retrieve extended info about event state.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
