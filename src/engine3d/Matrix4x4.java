package engine3d;

public class Matrix4x4 {
    public double[][] m = new double[4][4];

    public Matrix4x4() {
        // Identity by default
        for (int i = 0; i < 4; i++)
            m[i][i] = 1;
    }

    public static Matrix4x4 getIdentity() {
        return new Matrix4x4();
    }

    public Matrix4x4 multiply(Matrix4x4 other) {
        Matrix4x4 res = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res.m[i][j] = m[i][0] * other.m[0][j] +
                        m[i][1] * other.m[1][j] +
                        m[i][2] * other.m[2][j] +
                        m[i][3] * other.m[3][j];
            }
        }
        return res;
    }

    public Vector3 multiply(Vector3 v) {
        double x = v.x * m[0][0] + v.y * m[0][1] + v.z * m[0][2] + m[0][3];
        double y = v.x * m[1][0] + v.y * m[1][1] + v.z * m[1][2] + m[1][3];
        double z = v.x * m[2][0] + v.y * m[2][1] + v.z * m[2][2] + m[2][3];
        double w = v.x * m[3][0] + v.y * m[3][1] + v.z * m[3][2] + m[3][3];

        if (w != 0 && w != 1) {
            return new Vector3(x / w, y / w, z / w);
        }
        return new Vector3(x, y, z);
    }

    public static Matrix4x4 getTranslation(double x, double y, double z) {
        Matrix4x4 res = new Matrix4x4();
        res.m[0][3] = x;
        res.m[1][3] = y;
        res.m[2][3] = z;
        return res;
    }

    public static Matrix4x4 getScale(double x, double y, double z) {
        Matrix4x4 res = new Matrix4x4();
        res.m[0][0] = x;
        res.m[1][1] = y;
        res.m[2][2] = z;
        return res;
    }

    public static Matrix4x4 getRotationY(double angleDegrees) {
        Matrix4x4 res = new Matrix4x4();
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        res.m[0][0] = cos;
        res.m[0][2] = sin;
        res.m[2][0] = -sin;
        res.m[2][2] = cos;
        return res;
    }

    public static Matrix4x4 getRotationX(double angleDegrees) {
        Matrix4x4 res = new Matrix4x4();
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad); // Note: Sign depends on coordinate system (Right Handed vs Left)

        res.m[1][1] = cos;
        res.m[1][2] = -sin;
        res.m[2][1] = sin;
        res.m[2][2] = cos;
        return res;
    }

    public static Matrix4x4 getProjection(double fov, double aspectRatio, double near, double far) {
        Matrix4x4 res = new Matrix4x4();
        // Clear identity (projection is different)
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                res.m[i][j] = 0;

        double tanHalfFov = Math.tan(Math.toRadians(fov / 2));
        double range = far - near;

        res.m[0][0] = 1.0 / (tanHalfFov * aspectRatio);
        res.m[1][1] = 1.0 / tanHalfFov;
        res.m[2][2] = -(far + near) / range;
        res.m[2][3] = -2 * far * near / range;
        res.m[3][2] = -1.0;

        return res;
    }

    public static Matrix4x4 getLookAt(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 zAxis = eye.sub(target).normalize(); // Forward
        Vector3 xAxis = up.cross(zAxis).normalize(); // Right
        Vector3 yAxis = zAxis.cross(xAxis).normalize(); // Up

        Matrix4x4 res = new Matrix4x4();
        res.m[0][0] = xAxis.x;
        res.m[0][1] = xAxis.y;
        res.m[0][2] = xAxis.z;
        res.m[0][3] = -xAxis.dot(eye);
        res.m[1][0] = yAxis.x;
        res.m[1][1] = yAxis.y;
        res.m[1][2] = yAxis.z;
        res.m[1][3] = -yAxis.dot(eye);
        res.m[2][0] = zAxis.x;
        res.m[2][1] = zAxis.y;
        res.m[2][2] = zAxis.z;
        res.m[2][3] = -zAxis.dot(eye);
        res.m[3][0] = 0;
        res.m[3][1] = 0;
        res.m[3][2] = 0;
        res.m[3][3] = 1;

        return res;
    }
}
