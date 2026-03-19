package engine3d;

/**
 * Camera — Extended with pitch/yaw smooth orbit, zoom, and first-person mode.
 * MEMBER 2 CONTRIBUTION: 3D Engine Upgrades
 */
public class Camera {

    public Vector3 position;
    public Vector3 target;
    public Vector3 up;

    // Orbit parameters
    public double radius = 600;
    public double yaw = 45; // horizontal angle (degrees)
    public double pitch = 30; // vertical angle (degrees)

    // First-person mode
    public boolean firstPerson = false;
    private static final double EYE_HEIGHT = 160; // 160 cm eye height

    public Camera() {
        position = new Vector3(0, 0, 0);
        target = new Vector3(0, 0, 0);
        up = new Vector3(0, 1, 0);
    }

    /**
     * Orbit camera around the center of a room (w × d).
     * Recalculates position from yaw, pitch, and radius.
     */
    public void orbitAround(double roomW, double roomD) {
        double cx = roomW / 2.0, cz = roomD / 2.0;
        target = new Vector3(cx, 0, cz);

        if (firstPerson) {
            // Eye at center-front area at eye height
            double fpYaw = Math.toRadians(yaw);
            position = new Vector3(
                    cx + Math.sin(fpYaw) * 30,
                    EYE_HEIGHT,
                    cz + Math.cos(fpYaw) * 30);
        } else {
            double pitchRad = Math.toRadians(Math.max(5, Math.min(89, pitch)));
            double yawRad = Math.toRadians(yaw);
            position = new Vector3(
                    cx + radius * Math.cos(pitchRad) * Math.sin(yawRad),
                    radius * Math.sin(pitchRad),
                    cz + radius * Math.cos(pitchRad) * Math.cos(yawRad));
        }
    }

    /** Rotate horizontally (yaw) */
    public void rotateYaw(double deltaDeg) {
        yaw = (yaw + deltaDeg) % 360;
    }

    /** Rotate vertically (pitch) — clamped 5°–89° */
    public void rotatePitch(double deltaDeg) {
        pitch = Math.max(5, Math.min(89, pitch + deltaDeg));
    }

    /** Zoom by changing radius */
    public void zoom(double delta) {
        radius = Math.max(80, Math.min(2000, radius + delta));
    }

    /** Walk forward in first-person (shifts target & position) */
    public void walkForward(double step) {
        double yawRad = Math.toRadians(yaw);
        double dx = Math.sin(yawRad) * step;
        double dz = Math.cos(yawRad) * step;
        position.x -= dx;
        position.z -= dz;
        target.x -= dx;
        target.z -= dz;
    }

    /** Strafe in first-person */
    public void walkStrafe(double step) {
        double yawRad = Math.toRadians(yaw + 90);
        position.x -= Math.sin(yawRad) * step;
        position.z -= Math.cos(yawRad) * step;
        target.x -= Math.sin(yawRad) * step;
        target.z -= Math.cos(yawRad) * step;
    }

    /** Toggle first-person / orbit mode */
    public void toggleFirstPerson() {
        firstPerson = !firstPerson;
    }
}
