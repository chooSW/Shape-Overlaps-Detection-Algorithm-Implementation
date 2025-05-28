package knu.lsy.shapes;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class RegularPolygon extends Shape {
    private int sides;
    private double rotationAngle;
    private List<Point> vertices;

    public RegularPolygon(Point center, double radius, int sides, double rotationAngle) {
        super(center, radius);
        this.sides = sides;
        this.rotationAngle = rotationAngle;
        this.vertices = generateVertices();
    }

    private List<Point> generateVertices() {
        List<Point> points = new ArrayList<>();
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            double angle = angleStep * i + rotationAngle;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            points.add(new Point(x, y));
        }

        return points;
    }

    @Override
    public boolean overlaps(Shape other) {
        // 원과의 충돌은 Circle.overlaps()에 위임
        if (other instanceof Circle) {
            return other.overlaps(this);
        }

        // 두 다각형 간 SAT 충돌 검사
        List<Point> vertsA = this.getVertices();
        List<Point> vertsB = other.getVertices();

        // 모든 후보 축 생성 (A와 B 양쪽)
        List<double[]> axes = new ArrayList<>();
        axes.addAll(getAxes(vertsA));
        axes.addAll(getAxes(vertsB));

        // 각 축에 대해 투영 검사
        for (double[] axis : axes) {
            double[] projA = project(vertsA, axis);
            double[] projB = project(vertsB, axis);
            // 투영 간 겹침이 없으면 분리 축 발견 → 충돌 없음
            if (projA[1] < projB[0] || projB[1] < projA[0]) {
                return false;
            }
        }

        // 모든 축에서 겹침 → 충돌함
        return true;
    }

    /** 각 변에 수직인 축(법선) 벡터 목록을 생성 */
    private List<double[]> getAxes(List<Point> verts) {
        List<double[]> axes = new ArrayList<>();
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            Point p1 = verts.get(i);
            Point p2 = verts.get((i + 1) % n);
            // 변 벡터
            double edgeX = p2.getX() - p1.getX();
            double edgeY = p2.getY() - p1.getY();
            // 법선 벡터 (–edgeY, edgeX)
            axes.add(new double[]{ -edgeY, edgeX });
        }
        return axes;
    }

    /** 주어진 축에 다각형을 투영하여 [min, max] 범위를 반환 */
    private double[] project(List<Point> verts, double[] axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Point p : verts) {
            double projection = p.getX() * axis[0] + p.getY() * axis[1];
            if (projection < min) min = projection;
            if (projection > max) max = projection;
        }
        return new double[]{ min, max };
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "regularPolygon");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("sides", sides);
        json.put("rotationAngle", rotationAngle);
        json.put("color", color);

        JSONArray verticesArray = new JSONArray();
        for (Point vertex : vertices) {
            verticesArray.put(vertex.toJSON());
        }
        json.put("vertices", verticesArray);

        return json;
    }

    @Override
    public String getShapeType() {
        return "regularPolygon";
    }

    @Override
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }
}
