package com.hit.spectrum.test;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.util.Pair;


public class OptimizeTest5 {

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

        /**
         * Solve the linear least squares problem Jx=r.
         *
         * @param jacobian  the Jacobian matrix, J. the number of rows >= the number or
         *                  columns.
         * @param residuals the computed residuals, r.
         * @return the solution x, to the linear least squares problem Jx=r.
         * @throws ConvergenceException if the matrix properties (e.g. singular) do not
         *                              permit a solution.
         */
        protected abstract RealVector solve(RealMatrix jacobian,
                                            RealVector residuals);
    }

    public static double[] optimize(double[][] A1, double[] y1) {
        RealMatrix A = MatrixUtils.createRealMatrix(A1);
        RealVector y = MatrixUtils.createRealVector(y1);

        // 定义模型函数和雅可比矩阵
        MultivariateJacobianFunction model = new MultivariateJacobianFunction() {
            @Override
            public Pair<RealVector, RealMatrix> value(final RealVector point) {
                return new Pair<>(A.preMultiply(point).subtract(y), A.transpose());
            }
        };


        LeastSquaresProblem problem = new LeastSquaresBuilder()
                .start(new double[A1.length])
                .target(new double[A1[0].length])
                .maxEvaluations(200000)
                .maxIterations(200000)
                .model(model)
                .checker(new EvaluationRmsChecker(1e-3))
                .build();


        LeastSquaresProblem.Evaluation optimize = optimize(problem);

        return optimize.getPoint().toArray();
    }

    public static LeastSquaresProblem.Evaluation optimize(final LeastSquaresProblem lsp) {
        OptimizeTest5.Decomposition decomposition = OptimizeTest5.Decomposition.SVD;
        //create local evaluation and iteration counts
        final Incrementor evaluationCounter = lsp.getEvaluationCounter();
        final Incrementor iterationCounter = lsp.getIterationCounter();
        final ConvergenceChecker<LeastSquaresProblem.Evaluation> checker
                = lsp.getConvergenceChecker();

        // Computation will be useless without a checker (see "for-loop").
        if (checker == null) {
            throw new NullArgumentException();
        }

        RealVector currentPoint = lsp.getStart();

        // iterate until convergence is reached
        LeastSquaresProblem.Evaluation current = null;
        while (true) {
            iterationCounter.incrementCount();

            // evaluate the objective function and its jacobian
            LeastSquaresProblem.Evaluation previous = current;
            // Value of the objective function at "currentPoint".
            evaluationCounter.incrementCount();
            current = lsp.evaluate(currentPoint);
            final RealVector currentResiduals = current.getResiduals();
            final RealMatrix weightedJacobian = current.getJacobian();
            currentPoint = current.getPoint();

            // Check convergence.
            if (previous != null &&
                    checker.converged(iterationCounter.getCount(), previous, current)) {
                return current;
            }

            // solve the linearized least squares problem
            final RealVector dX = decomposition.solve(weightedJacobian, currentResiduals);
            // update the estimated parameters
            currentPoint = currentPoint.add(dX);
        }
    }

}
