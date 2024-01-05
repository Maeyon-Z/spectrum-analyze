package com.hit.spectrum.test;

import com.hit.spectrum.algo.Normalization;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.List;


public class OptimizeTest6_1 {

    public enum Decomposition {
        SVD {
            @Override
            protected RealVector solve(final RealMatrix jacobian,
                                       final RealVector residuals) {
                return new SingularValueDecomposition(jacobian)
                        .getSolver()
                        .solve(residuals);
            }
        };

        protected abstract RealVector solve(RealMatrix jacobian,
                                            RealVector residuals);
    }

    public static double[] optimize(double[][] A1, double[] y1) {
        y1 = Normalization.normalize(y1);
        for (int i = 0; i < A1.length; i++) {
            A1[i] = Normalization.normalize(A1[i]);
        }

        OptimizeTest6_1.Decomposition decomposition = OptimizeTest6_1.Decomposition.SVD;
        double[] x1 = new double[A1.length];
        Arrays.fill(x1, 1.0);
        RealVector previousResiduals = null;
        Integer maxIterations = 200000, iter = 0;
        double tol = 1e-3;

        while (true) {

            RealMatrix A = new Array2DRowRealMatrix(A1);
            RealVector y = new ArrayRealVector(y1);
            RealVector x = new ArrayRealVector(x1);

            RealVector currentResiduals = A.preMultiply(x).subtract(y).mapMultiply(-1.0);
            RealMatrix weightedJacobian = A.transpose();

            // Check convergence.
            if (previousResiduals != null && FastMath.abs(getRMS(currentResiduals) - getRMS(previousResiduals)) <= tol) {
                break;
            }

            RealVector dX = decomposition.solve(weightedJacobian, currentResiduals);

            x1 = x.add(dX).toArray();
            previousResiduals = currentResiduals;
            iter ++;
        }


        return x1;
    }

    public static double getRMS(RealVector residuals){
        double cost = FastMath.sqrt(residuals.dotProduct(residuals));
        return FastMath.sqrt(cost * cost / residuals.getDimension());
    }

    private static boolean check(double[] cur, double[] dx){
        for (int i = 0; i < cur.length; i++){
            if(cur[i] + dx[i] < 0){
                return true;
            }
        }
        return false;
    }

    private static double[][] remove2(double[][] aTemp, List<Integer> ids) {
        double[][] res = new double[aTemp.length - ids.size()][aTemp[0].length];
        int k = 0;
        for(int i = 0; i < aTemp.length; i++){
            if(!ids.contains(i))
                res[k++] = aTemp[i];
        }
        return res;
    }

    private static double[] remove1(double[] xTemp, List<Integer> ids) {
        double[] res = new double[xTemp.length - ids.size()];
        int k = 0;
        for(int i = 0; i < xTemp.length; i++){
            if(!ids.contains(i))
                res[k++] = xTemp[i];
        }
        return res;
    }

}
