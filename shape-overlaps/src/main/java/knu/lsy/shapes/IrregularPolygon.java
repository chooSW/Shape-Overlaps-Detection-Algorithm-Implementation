package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IrregularPolygon extends Shape {
    private List<Point> vertices;

    public IrregularPolygon(Point center, double radius, int numVertices) {
        super(center, radius);
        this.vertices = generateIrregularVertices(numVertices);
    }

    private List<Point> generateIrregularVertices(int numVertices) {
        List<Point> points = new ArrayList<>();
        // 1. 무작위 각도로 점들 생성
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            angles.add(Math.random() * 2 * Math.PI);
        }
        Collections.sort(angles);
        // 2. 무작위 반경 적용
        for (double angle : angles) {
            double r = radius * (0.5 + Math.random() * 0.5);
            points.add(new Point(
                    center.getX() + r * Math.cos(angle),
                    center.getY() + r * Math.sin(angle)
            ));
        }
        // 간단한 컨벡스 헐 생성
        return createSimpleConvexHull(points);
    }

    private List<Point> createSimpleConvexHull(List<Point> points) {
        if (points.size() <= 3) return points;
        points.sort(Comparator.comparingDouble(Point::getX));
        List<Point> hull = new ArrayList<>();
        // 하부
        for (Point p : points) {
            while (hull.size() >= 2 &&
                    orientation(hull.get(hull.size()-2), hull.get(hull.size()-1), p) <= 0) {
                hull.remove(hull.size()-1);
            }
            hull.add(p);
        }
        // 상부
        int lower = hull.size();
        for (int i = points.size()-2; i >= 0; i--) {
            Point p = points.get(i);
            while (hull.size() > lower &&
                    orientation(hull.get(hull.size()-2), hull.get(hull.size()-1), p) <= 0) {
                hull.remove(hull.size()-1);
            }
            hull.add(p);
        }
        hull.remove(hull.size()-1);
        return hull;
    }

    private double orientation(Point p, Point q, Point r) {
        return (q.getX() - p.getX()) * (r.getY() - p.getY())
                - (q.getY() - p.getY()) * (r.getX() - p.getX());
    }

    @Override
    public boolean overlaps(Shape other) {
        // 1) 원과의 충돌이면 Circle 로직에 위임
        if (other instanceof Circle) {
            return other.overlaps(this);
        }
        // 2) 다각형–다각형 SAT 검사
        List<Point> va = this.getVertices();
        List<Point> vb = other.getVertices();
        // 후보 축 생성 (양쪽 변의 법선)
        List<double[]> axes = new ArrayList<>();
        axes.addAll(getAxes(va));
        axes.addAll(getAxes(vb));
        // 모든 축에서 투영 겹침 확인
        for (double[] axis : axes) {
            double[] projA = project(va, axis);
            double[] projB = project(vb, axis);
            if (projA[1] < projB[0] || projB[1] < projA[0]) {
                // 분리 축 발견 → 겹침 아님
                return false;
            }
        }
        // 모든 축에서 겹침 → 충돌함
        return true;
    }

    /** 각 변에 수직인 법선 벡터 리스트 생성 */
    private List<double[]> getAxes(List<Point> verts) {
        List<double[]> axes = new ArrayList<>();
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            Point p1 = verts.get(i);
            Point p2 = verts.get((i+1)%n);
            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            // 법선: (-dy, dx)
            axes.add(new double[]{ -dy, dx });
        }
        return axes;
    }

    /** 축(axis)에 다각형을 투영하여 [min, max] 반환 */
    private double[] project(List<Point> verts, double[] axis) {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (Point p : verts) {
            double proj = p.getX() * axis[0] + p.getY() * axis[1];
            if (proj < min) min = proj;
            if (proj > max) max = proj;
        }
        return new double[]{ min, max };
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "irregularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);

        JSONArray arr = new JSONArray();
        for (Point v : vertices) {
            arr.put(v.toJSON());
        }
        json.put("vertices", arr);
        return json;
    }

    @Override
    public String getShapeType() {
        return "irregularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
