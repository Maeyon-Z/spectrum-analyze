package com.hit.spectrum.algo;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Optimize {

    public enum Decomposition {
        SVD {
            @Override
            protected RealVector solve(final RealMatrix jacobian,
                                       final RealVector residuals) {
                return new QRDecomposition(jacobian)
                        .getSolver()
                        .solve(residuals);
            }
        };

        protected abstract RealVector solve(RealMatrix jacobian,
                                            RealVector residuals);
    }

    public static double[] optimize(double[][] A1, double[] y1) {
        Optimize.Decomposition decomposition = Optimize.Decomposition.SVD;
        double[] x1 = new double[A1.length];
        Arrays.fill(x1, 1.0);
        RealVector previousResiduals = null;
        Integer maxIterations = 200000, iter = 0;
        double tol = 1e-2;

        List<Integer> l = new ArrayList<>();
        List<Integer> t = new ArrayList<>();

        while (true) {
            //remove
            if(x1.length > 10 && iter != 0){
                List<Integer> ids = new ArrayList<>();
                for(int i = 0; i < x1.length; i++){
                    if(x1[i] < 1e-3){
                        ids.add(i);
                        l.add(i);
                    }
                }
                if(ids.size() != 0){
                    t.add(ids.size());
                    A1 = remove2(A1, ids);
                    x1 = remove1(x1, ids);
                }
            }


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
            while (check(x.toArray(), dX.toArray())){
                dX.mapMultiplyToSelf(0.5);
            }

            x1 = x.add(dX).toArray();
            previousResiduals = currentResiduals;
            iter ++;
        }


        List<List<Integer>> ll = new ArrayList<>();
        int begin = 0;
        for(int i = 0; i < t.size(); i++){
            int end = t.get(i) + begin;
            ll.add(l.subList(begin, end));
            begin = end;
        }
        for(int i = ll.size() - 1; i >= 0; i--){
            List<Integer> lll = ll.get(i);
            double[] d = new double[x1.length + lll.size()];
            Arrays.fill(d, 1.0);
            for (int j = 0; j < lll.size(); j++){
                d[lll.get(j)] = 0.0;
            }
            int k = 0, j = 0;
            while(j < x1.length){
                while (d[k] == 0.0) k++;
                d[k] = x1[j];
                k++;
                j++;
            }
            x1 = d;
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
