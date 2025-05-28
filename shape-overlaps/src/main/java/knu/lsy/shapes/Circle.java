package knu.lsy.shapes;

import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class Circle extends Shape {

    public Circle(Point center, double radius) {
        super(center, radius);
    }

    @Override
    public boolean overlaps(Shape other) {
        // 1) 다른 도형이 원인 경우: 두 원의 중심 거리가 반지름의 합보다 작은지 확인
        if (other instanceof Circle) {
            Circle c2 = (Circle) other;
            double distance = this.center.distanceTo(c2.center);
            return distance < (this.radius + c2.radius);
        }

        // 2) 다른 도형이 다각형인 경우
        List<Point> verts = other.getVertices();

        // 2-1) 다각형의 모든 정점이 원 안에 있는지 확인
        for (Point v : verts) {
            if (v.distanceTo(this.center) < this.radius) {
                return true;
            }
        }

        // 2-2) 다각형의 모든 변이 원과 교차하는지 확인
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            Point a = verts.get(i);
            Point b = verts.get((i + 1) % n);
            if (segmentIntersectsCircle(a, b, this.center, this.radius)) {
                return true;
            }
        }

        return false;
    }

    // 선분 AB와 원(C: center, r: radius)의 교차 검사
    private boolean segmentIntersectsCircle(Point a, Point b, Point center, double radius) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double lengthSq = dx * dx + dy * dy;
        if (lengthSq == 0) {
            // A와 B가 동일한 점일 때
            return a.distanceTo(center) < radius;
        }
        // AB 선분 위의 투영 파라미터 t 계산
        double t = ((center.getX() - a.getX()) * dx + (center.getY() - a.getY()) * dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));
        // 투영점 좌표
        double projX = a.getX() + t * dx;
        double projY = a.getY() + t * dy;
        // 투영점과 원 중심 사이 거리 비교
        return new Point(projX, projY).distanceTo(center) < radius;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "circle");
        json.put("id", id);
        json.put("center", center.toJSON());
        json.put("radius", radius);
        json.put("color", color);
        return json;
    }

    @Override
    public String getShapeType() {
        return "circle";
    }

    @Override
    public List<Point> getVertices() {
        List<Point> vertices = new ArrayList<>();
        int numPoints = 32;
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.getX() + radius * Math.cos(angle);
            double y = center.getY() + radius * Math.sin(angle);
            vertices.add(new Point(x, y));
        }
        return vertices;
    }
}
