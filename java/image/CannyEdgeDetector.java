package image;

import java.awt.Point;
import java.util.Stack;

/**
 * @author "Sergey Edunov"
 * @version 2/18/11
 */
public class CannyEdgeDetector {

    private final static double GAUSSIAN_CUT_OFF = 0.005;
    private int W;
    private int H;
    private int[] r, g, b;
    private double yConv[];
    private double xConv[];

    private double xGradient[];
    private double yGradient[];
    private double vMagnitude[];

    private int[] data;


    private double[] magnitude;

    double gaussianKernelRadius = 1;
    int gaussianKernelWidth = 16;
    private static final double lowThreshold = 0.1;
    private static final double highThreshold = 8;

    public CannyEdgeDetector(int W, int H, int[] r, int[] g, int[] b){
        this.W = W;
        this.H = H;
        this.r = r;
        this.g = g;
        this.b = b;
        yConv = new double[r.length];
        xConv = new double[r.length];

        xGradient = new double[r.length];
        yGradient = new double[r.length];
        vMagnitude = new double[r.length];

        data = new int[r.length];
        magnitude = new double[r.length];

    }


    public int[] findEdges() {
        computeColorGradients(gaussianKernelRadius, gaussianKernelWidth, luminance(r, g, b));
        double low = lowThreshold;
        double high = highThreshold;
        hysterezis(low, high);
        threshold();
        return data;
    }

    private int[] luminance(int[] r, int[] g, int[] b) {
        int[] res = new int[r.length];
        for(int i=0; i<r.length; i++){
            res[i] = (int) (0.299 * r[i] + 0.587 * g[i] + 0.114 * b[i]);
        }
        return res;
    }

    private void computeColorGradients(double kernelRadius, int kernelWidth, int[] data) {
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
                double sumX = data[x*H+y] * kernel[0];
                double sumY = sumX;
                int offset = 1;
                for (; offset < kWidth;) {
                    sumY += kernel[offset] * (data[x*H + y - offset] + data[x*H + y + offset]);
                    sumX += kernel[offset] * (data[(x - offset)*H + y] + data[(x + offset)*H + y]);
                    offset++;
                }

                yConv[x*H+y] = sumY;
                xConv[x*H+y] = sumX;
            }

        }

        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y++) {
                double sumX = 0;
                double sumY = 0;

                for (int i = 1; i < kWidth; i++) {
                    sumX += diffKernel[i] * (yConv[(x-i)*H+y] - yConv[(x+i)*H+y]);
                    sumY += diffKernel[i] * (xConv[x*H+y - i] - xConv[x*H+y + i]);
                }

                xGradient[x*H+y] = sumX;
                yGradient[x*H+y] = sumY;
                vMagnitude[x * H + y] = Math.hypot(xGradient[x*H+y], yGradient[x*H+y]);
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
                        data[x*H+y] = (int) Math.ceil(magnitude[x * H + y]);
                        stack.push(new Point(x, y));
                    }
                }
            }
        }
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
