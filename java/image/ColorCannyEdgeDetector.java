package image;

import java.awt.Point;
import java.util.Stack;

/**
 * @author "Sergey Edunov"
 * @version 2/18/11
 */
public class ColorCannyEdgeDetector {

    private final static double GAUSSIAN_CUT_OFF = 0.005;
    private int W;
    private int H;
    private int[] r, g, b;
    private double yrConv[];
    private double xrConv[];
    private double ygConv[];
    private double xgConv[];
    private double ybConv[];
    private double xbConv[];


    private double xGradient[];
    private double yGradient[];
    private double vMagnitude[];

    private int[] data;


    private double[] magnitude;

    double gaussianKernelRadius = 1;
    int gaussianKernelWidth = 16;
    private static final double lowThreshold = 3;
    private static final double highThreshold = 8;

    public ColorCannyEdgeDetector(int W, int H, int[] r, int[] g, int[] b){
        this.W = W;
        this.H = H;
        this.r = r;
        this.g = g;
        this.b = b;
        yrConv = new double[r.length];
        xrConv = new double[r.length];
        ygConv = new double[r.length];
        xgConv = new double[r.length];
        ybConv = new double[r.length];
        xbConv = new double[r.length];

        xGradient = new double[r.length];
        yGradient = new double[r.length];
        vMagnitude = new double[r.length];

        data = new int[r.length];
        magnitude = new double[r.length];

    }


    public int[] findEdges() {
        computeColorGradients(gaussianKernelRadius, gaussianKernelWidth);
        double low = lowThreshold;
        double high = highThreshold;
        hysterezis(low, high);
        threshold();
        return data;
    }

    private void computeColorGradients(double kernelRadius, int kernelWidth) {
        double kernel[] = new double[kernelWidth];
        double diffKernel[] = new double[kernelWidth];
        int kWidth;
        for (kWidth = 0; kWidth < kernelWidth; kWidth++) {
            double g1 = gaussian(kWidth, kernelRadius);
            if (g1 <= GAUSSIAN_CUT_OFF && kWidth >= 2) break;
            double g2 = gaussian(kWidth - 0.5, kernelRadius);
            double g3 = gaussian(kWidth + 0.5, kernelRadius);
            kernel[kWidth] = (g1 + g2 + g3) / 3. / (2 * Math.PI * kernelRadius * kernelRadius);
            diffKernel[kWidth] = g3 - g2;
        }

        int initX = kWidth - 1;
        int maxX = W - (kWidth - 1);
        int initY = (kWidth - 1);
        int maxY = (H - (kWidth - 1));

        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y++) {
                double sumrX = r[x * H + y] * kernel[0];
                double sumrY = r[x * H + y] * kernel[0];
                double sumgX = g[x * H + y] * kernel[0];
                double sumgY = g[x * H + y] * kernel[0];
                double sumbX = b[x * H + y] * kernel[0];
                double sumbY = b[x * H + y] * kernel[0];

                for (int ri = 1; ri < kWidth; ri++) {
                    sumrY += kernel[ri] * (r[x * H + y - ri] + r[x * H + y + ri]);
                    sumrX += kernel[ri] * (r[(x - ri) * H + y] + r[(x + ri) * H + y]);
                    sumgY += kernel[ri] * (g[x * H + y - ri] + g[x * H + y + ri]);
                    sumgX += kernel[ri] * (g[(x - ri) * H + y] + g[(x + ri) * H + y]);
                    sumbY += kernel[ri] * (b[x * H + y - ri] + b[x * H + y + ri]);
                    sumbX += kernel[ri] * (b[(x - ri) * H + y] + b[(x + ri) * H + y]);

                }

                yrConv[x * H + y] = sumrY;
                xrConv[x * H + y] = sumrX;
                ygConv[x * H + y] = sumgY;
                xgConv[x * H + y] = sumgX;
                ybConv[x * H + y] = sumbY;
                xbConv[x * H + y] = sumbX;
            }

        }


        initX = kWidth;
        maxX = W - kWidth;
        initY = kWidth;
        maxY = (H - kWidth);
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y++) {
                double rx = (xrConv[(x + 1) * H + y] - xrConv[(x - 1) * H + y]) / 2.;
                double gx = (xgConv[(x + 1) * H + y] - xgConv[(x - 1) * H + y]) / 2.;
                double bx = (xbConv[(x + 1) * H + y] - xbConv[(x - 1) * H + y]) / 2.;

                double ry = (yrConv[x * H + y - 1] - yrConv[x * H + y + 1]) / 2.;
                double gy = (ygConv[x * H + y - 1] - ygConv[x * H + y + 1]) / 2.;
                double by = (ybConv[x * H + y - 1] - ybConv[x * H + y + 1]) / 2.;


                double q1 = rx * rx + gx * gx + bx * bx;
                double q2 = rx * ry + gx * gy + bx * by;
                double q4 = ry * ry + gy * gy + by * by;

                double[] eigen = eigen(q1, q2, q2, q4);
                double fe1 = eigen[0];
                double fe2 = eigen[1];
                double fv = eigen[2];

                xGradient[x * H + y] = fe1;
                yGradient[x * H + y] = fe2;
                vMagnitude[x * H + y] = Math.sqrt(fv);
            }

        }


        for (int x = initX+1; x < maxX-1; x++) {
            for (int y = initY+1; y < maxY-1; y++) {

                double a1 = vMagnitude[(x - 1) * H + y - 1];
                double a2 = vMagnitude[x * H + y - 1];
                double a3 = vMagnitude[(x + 1) * H + y - 1];

                double a4 = vMagnitude[(x - 1) * H + y];
                double a5 = vMagnitude[x * H + y];
                double a6 = vMagnitude[(x + 1) * H + y];

                double a7 = vMagnitude[(x - 1) * H + y + 1];
                double a8 = vMagnitude[x * H + y + 1];
                double a9 = vMagnitude[(x + 1) * H + y + 1];

                double gx = xGradient[x * H + y];
                double gy = yGradient[x * H + y];

                double angle = Math.atan2(gy, gx);
                double h, v;

                if (ishorizonatal(angle)) {
                    h = a4;
                    v = a6;
                } else if (isvertical(angle)) {
                    h = a2;
                    v = a8;
                } else if (isdiag1(angle)) {
                    h = a3;
                    v = a7;
                } else if (isdiag2(angle)) {
                    h = a1;
                    v = a9;
                } else {
                    h = 1000000;
                    v = 1000000;
                }

                double val = 0;
                if (a5 > h && a5 > v)
                    val = a5;

                magnitude[x * H + y] = val;
            }
        }
    }

    private void hysterezis(double low, double high) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (data[x * H + y] == 0 && magnitude[x * H + y] >= high) {
                    track(x, y, low);
                }
            }
        }
    }

    private void threshold() {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                data[x * H + y] = data[x * H + y] > 0 ? 255 : 0;
            }
        }
    }

    private void track(int x_, int y_, double threshold) {
        Stack<Point> stack = new Stack<Point>();
        stack.push(new Point(x_, y_));
        data[x_*H+y_] = (int) magnitude[x_*H+y_];
        while(!stack.empty()) {
            Point n = stack.pop();
            int x1 = n.x;
            int y1 = n.y;
            int x0 = x1 == 0 ? 0 : x1 - 1;
            int x2 = x1 == W-1 ? W-1 : x1 + 1;
            int y0 = y1 == 0 ? 0 : y1 - 1;
            int y2 = y1 == H-1 ? H-1 : y1 + 1;

            for (int x = x0; x <= x2; x++) {
                for (int y = y0; y <= y2; y++) {
                    if ((y != y1 || x != x1) && data[x * H + y] == 0 && magnitude[x * H + y] >= threshold) {
                        data[x*H+y] = (int) magnitude[x*H+y];
                        stack.push(new Point(x, y));
                    }
                }
            }
        }
    }

    private double[] eigen(double q1, double q2, double q3, double q4) {

        double a = 1;
        double b = -(q1 + q4);
        double c = q1 * q4 - q2 * q3;
        double v = ((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
        double e1 = -q2;
        double e2 = q1 - v;

        if (v < 0) {
            v = -v;
            e1 = -e1;
            e2 = -e2;
        }

        return new double[]{e1, e2, v};
    }

    private boolean isdiag2(double angle) {
        return ((angle >= -3 * Math.PI / 8) && (angle < -(Math.PI / 8)))
                || ((angle >= 5 * Math.PI / 8) && (angle < 7 * Math.PI / 8));
    }

    private boolean isdiag1(double angle) {
        return ((angle >= -7 * Math.PI / 8) && (angle < -5 * Math.PI / 8))
                || ((angle >= Math.PI / 8) && (angle < 3 * Math.PI / 8));
    }

    private boolean isvertical(double angle) {
        return ((angle >= -5 * Math.PI / 8) && (angle < -3 * Math.PI / 8))
                || ((angle >= 3 * Math.PI / 8) && (angle < 5 * Math.PI / 8));
    }

    private boolean ishorizonatal(double angle) {
        return (angle < -7 * Math.PI / 8) || ((angle >= -(Math.PI / 8))
                && (angle < Math.PI / 8)) || (angle >= 7 * Math.PI / 8);
    }


    private double gaussian(double x, double sigma) {
        return Math.exp(-(x * x) / (2f * sigma * sigma));
    }
}
