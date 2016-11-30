/*
(The MIT License.)

Copyright (c) 2009, Kevin Lacker.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/*
 * This is a Java port of original C code by Kevin Lacker.
 * https://github.com/lacker/ikalman
 * 
 * Ported by Andrey Novikov, 2013
 */

package ru.wtg.whereaminow.helpers;

/* To use these functions:

 1. Start with a KalmanFilter created by alloc_filter_velocity2d.
 2. At fixed intervals, call update_velocity2d with the lat/long.
 3. At any time, to get an estimate for the current position,
 bearing, or speed, use the functions:
 get_lat_long
 get_bearing
 get_mph
 */

import java.util.Date;

public class GeoTrackFilter {
	// FIXME Radius should be calculated depending on latitude instead
	// http://en.wikipedia.org/wiki/Earth_radius#Radius_at_a_given_geodetic_latitude
	private static final double EARTH_RADIUS_IN_METERS = 6371009;
    private long lastTimeStep;


    private KalmanFilter f;

	/*
	 * Create a GPS filter that only tracks two dimensions of position and
	 * velocity. The inherent assumption is that changes in velocity are
	 * randomly distributed around 0. Noise is a parameter you can use to alter
	 * the expected noise. 1.0 is the original, and the higher it is, the more a
	 * path will be "smoothed". Free with free_filter after using.
	 */

	public GeoTrackFilter(double noise) {
        lastTimeStep = new Date().getTime();
		/*
		 * The state model has four dimensions: x, y, x', y' Each time step we
		 * can only observe position, not velocity, so the observation vector
		 * has only two dimensions.
		 */
		f = new KalmanFilter(4, 2);

		/*
		 * Assuming the axes are rectilinear does not work well at the poles,
		 * but it has the bonus that we don't need to convert between lat/long
		 * and more rectangular coordinates. The slight inaccuracy of our
		 * physics model is not too important.
		 */
		f.state_transition.set_identity_matrix();
		set_seconds_per_timestep(1.0);

		/* We observe (x, y) in each time step */
		f.observation_model.set_matrix(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0);

		/* Noise in the world. */
		double pos = 0.000001;
		f.process_noise_covariance.set_matrix(pos, 0.0, 0.0, 0.0, 0.0, pos,
				0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);

		/* Noise in our observation */
		f.observation_noise_covariance.set_matrix(pos * noise, 0.0, 0.0, pos
				* noise);

		/* The start position is totally unknown, so give a high variance */
		f.state_estimate.set_matrix(0.0, 0.0, 0.0, 0.0);
		f.estimate_covariance.set_identity_matrix();
		double trillion = 1000.0 * 1000.0 * 1000.0 * 1000.0;
		f.estimate_covariance.scale_matrix(trillion);
	}

	/* Set the seconds per timestep in the velocity2d model. */
	/*
	 * The position units are in thousandths of latitude and longitude. The
	 * velocity units are in thousandths of position units per second.
	 * 
	 * So if there is one second per timestep, a velocity of 1 will change the
	 * lat or long by 1 after a million timesteps.
	 * 
	 * Thus a typical position is hundreds of thousands of units. A typical
	 * velocity is maybe ten.
	 */
	void set_seconds_per_timestep(double seconds_per_timestep) {
		/*
		 * unit_scaler accounts for the relation between position and velocity
		 * units
		 */
		double unit_scaler = 0.001;
		f.state_transition.data[0][2] = unit_scaler * seconds_per_timestep;
		f.state_transition.data[1][3] = unit_scaler * seconds_per_timestep;
	}

	/* Update the velocity2d model with new gps data. */
	public void update_velocity2d(double lat, double lon, long last_timestep) {
        double x = (last_timestep - lastTimeStep) / 1000.;
		set_seconds_per_timestep(x);
		f.observation.set_matrix(lat * 1000.0, lon * 1000.0);
		f.update();
	}

	/* Extract a lat long from a velocity2d Kalman filter. */
	public double[] get_lat_long() {
		double[] latlon = new double[2];
		latlon[0] = f.state_estimate.data[0][0] / 1000.0;
		latlon[1] = f.state_estimate.data[1][0] / 1000.0;
		return latlon;
	}

	/*
	 * Extract velocity with lat-long-per-second units from a velocity2d Kalman
	 * filter.
	 */
	public double[] get_velocity() {
		double[] delta_latlon = new double[2];
		delta_latlon[0] = f.state_estimate.data[2][0] / (1000.0 * 1000.0);
		delta_latlon[1] = f.state_estimate.data[3][0] / (1000.0 * 1000.0);
		return delta_latlon;
	}

	/*
	 * Extract a bearing from a velocity2d Kalman filter. 0 = north, 90 = east,
	 * 180 = south, 270 = west
	 */
	/*
	 * See http://www.movable-type.co.uk/scripts/latlong.html for formulas
	 */
    public	double get_bearing() {
		double x, y;
		double[] latlon = get_lat_long();
		double[] delta_latlon = get_velocity();

		/* Convert to radians */
		latlon[0] = Math.toRadians(latlon[0]);
		latlon[1] = Math.toRadians(latlon[1]);
		delta_latlon[0] = Math.toRadians(delta_latlon[0]);
		delta_latlon[1] = Math.toRadians(delta_latlon[1]);

		/* Do math */
		double lat1 = latlon[0] - delta_latlon[0];
		y = Math.sin(delta_latlon[1]) * Math.cos(latlon[0]);
		x = Math.cos(lat1) * Math.sin(latlon[0]) - Math.sin(lat1)
				* Math.cos(latlon[0]) * Math.cos(delta_latlon[1]);
		double bearing = Math.atan2(y, x);

		/* Convert to degrees */
		bearing = Math.toDegrees(bearing);
		return bearing;
	}

	/* Extract speed in meters per second from a velocity2d Kalman filter. */
	public double get_speed(double altitude) {
		double[] latlon = get_lat_long();
		double[] delta_latlon = get_velocity();
		/*
		 * First, let's calculate a unit-independent measurement - the radii of
		 * the earth traveled in each second. (Presumably this will be a very
		 * small number.)
		 */

		/* Convert to radians */
		latlon[0] = Math.toRadians(latlon[0]);
		latlon[1] = Math.toRadians(latlon[1]);
		delta_latlon[0] = Math.toRadians(delta_latlon[0]);
		delta_latlon[1] = Math.toRadians(delta_latlon[1]);

		/* Haversine formula */
		double lat1 = latlon[0] - delta_latlon[0];
		double sin_half_dlat = Math.sin(delta_latlon[0] / 2.0);
		double sin_half_dlon = Math.sin(delta_latlon[1] / 2.0);
		double a = sin_half_dlat * sin_half_dlat + Math.cos(lat1) * Math.cos(latlon[0]) * sin_half_dlon * sin_half_dlon;
		double radians_per_second = 2 * Math.atan2(1000.0 * Math.sqrt(a), 1000.0 * Math.sqrt(1.0 - a));

		/* Convert units */
		double meters_per_second = radians_per_second * (EARTH_RADIUS_IN_METERS + altitude);
		return meters_per_second;
	}
	



/* Refer to http://en.wikipedia.org/wiki/Kalman_filter for
 mathematical details. The naming scheme is that variables get names
 that make sense, and are commented with their analog in
 the Wikipedia mathematical notation.
 This Kalman filter implementation does not support controlled
 input.
 (Like knowing which way the steering wheel in a car is turned and
 using that to inform the model.)
 Vectors are handled as n-by-1 matrices.
 TODO: comment on the dimension of the matrices */

	public class KalmanFilter {
		/* k */
		int timestep;

		/* These parameters define the size of the matrices. */
		int state_dimension, observation_dimension;

		/* This group of matrices must be specified by the user. */
	/* F_k */
		Matrix state_transition;
		/* H_k */
		Matrix observation_model;
		/* Q_k */
		Matrix process_noise_covariance;
		/* R_k */
		Matrix observation_noise_covariance;

		/* The observation is modified by the user before every time step. */
	/* z_k */
		Matrix observation;

		/* This group of matrices are updated every time step by the filter. */
	/* x-hat_k|k-1 */
		Matrix predicted_state;
		/* P_k|k-1 */
		Matrix predicted_estimate_covariance;
		/* y-tilde_k */
		Matrix innovation;
		/* S_k */
		Matrix innovation_covariance;
		/* S_k^-1 */
		Matrix inverse_innovation_covariance;
		/* K_k */
		Matrix optimal_gain;
		/* x-hat_k|k */
		Matrix state_estimate;
		/* P_k|k */
		Matrix estimate_covariance;

		/* This group is used for meaningless intermediate calculations */
		Matrix vertical_scratch;
		Matrix small_square_scratch;
		Matrix big_square_scratch;

		public KalmanFilter(int state_dimension, int observation_dimension) {
			timestep = 0;
			this.state_dimension = state_dimension;
			this.observation_dimension = observation_dimension;

			state_transition = new Matrix(state_dimension, state_dimension);
			observation_model = new Matrix(observation_dimension, state_dimension);
			process_noise_covariance = new Matrix(state_dimension, state_dimension);
			observation_noise_covariance = new Matrix(observation_dimension, observation_dimension);

			observation = new Matrix(observation_dimension, 1);

			predicted_state = new Matrix(state_dimension, 1);
			predicted_estimate_covariance = new Matrix(state_dimension,	state_dimension);
			innovation = new Matrix(observation_dimension, 1);
			innovation_covariance = new Matrix(observation_dimension, observation_dimension);
			inverse_innovation_covariance = new Matrix(observation_dimension, observation_dimension);
			optimal_gain = new Matrix(state_dimension, observation_dimension);
			state_estimate = new Matrix(state_dimension, 1);
			estimate_covariance = new Matrix(state_dimension, state_dimension);

			vertical_scratch = new Matrix(state_dimension, observation_dimension);
			small_square_scratch = new Matrix(observation_dimension,observation_dimension);
			big_square_scratch = new Matrix(state_dimension, state_dimension);
		}

		/*
         * Runs one timestep of prediction + estimation.
         *
         * Before each time step of running this, set f.observation to be the next
         * time step's observation.
         *
         * Before the first step, define the model by setting: f.state_transition
         * f.observation_model f.process_noise_covariance
         * f.observation_noise_covariance
         *
         * It is also advisable to initialize with reasonable guesses for
         * f.state_estimate f.estimate_covariance
         */
		void update() {
			predict();
			estimate();
		}

		/* Just the prediction phase of update. */
		void predict() {
			timestep++;

		/* Predict the state */
			MatrixUtil.multiply_matrix(state_transition, state_estimate, predicted_state);

		/* Predict the state estimate covariance */
			MatrixUtil.multiply_matrix(state_transition, estimate_covariance, big_square_scratch);
			MatrixUtil.multiply_by_transpose_matrix(big_square_scratch, state_transition, predicted_estimate_covariance);
			MatrixUtil.add_matrix(predicted_estimate_covariance, process_noise_covariance, predicted_estimate_covariance);
		}

		/* Just the estimation phase of update. */
		void estimate() {
		/* Calculate innovation */
			MatrixUtil.multiply_matrix(observation_model, predicted_state, innovation);
			MatrixUtil.subtract_matrix(observation, innovation, innovation);

		/* Calculate innovation covariance */
			MatrixUtil.multiply_by_transpose_matrix(predicted_estimate_covariance, observation_model, vertical_scratch);
			MatrixUtil.multiply_matrix(observation_model, vertical_scratch, innovation_covariance);
			MatrixUtil.add_matrix(innovation_covariance, observation_noise_covariance, innovation_covariance);

		/*
		 * Invert the innovation covariance. Note: this destroys the innovation
		 * covariance. TODO: handle inversion failure intelligently.
		 */
			MatrixUtil.destructive_invert_matrix(innovation_covariance, inverse_innovation_covariance);

		/*
		 * Calculate the optimal Kalman gain. Note we still have a useful
		 * partial product in vertical scratch from the innovation covariance.
		 */
			MatrixUtil.multiply_matrix(vertical_scratch, inverse_innovation_covariance, optimal_gain);

		/* Estimate the state */
			MatrixUtil.multiply_matrix(optimal_gain, innovation, state_estimate);
			MatrixUtil.add_matrix(state_estimate, predicted_state, state_estimate);

		/* Estimate the state covariance */
			MatrixUtil.multiply_matrix(optimal_gain, observation_model, big_square_scratch);
			MatrixUtil.subtract_from_identity_matrix(big_square_scratch);
			MatrixUtil.multiply_matrix(big_square_scratch, predicted_estimate_covariance, estimate_covariance);
		}
	}

	static class MatrixUtil {

		/* Copy a matrix. */
		static void copy_matrix(Matrix source, Matrix destination) {
			assert (source.rows == destination.rows);
			assert (source.cols == destination.cols);
			for (int i = 0; i < source.rows; ++i) {
				for (int j = 0; j < source.cols; ++j) {
					destination.data[i][j] = source.data[i][j];
				}
			}
		}
		/* Add matrices a and b and put the result in c. */
		static void add_matrix(Matrix a, Matrix b, Matrix c) {
			assert (a.rows == b.rows);
			assert (a.rows == c.rows);
			assert (a.cols == b.cols);
			assert (a.cols == c.cols);
			for (int i = 0; i < a.rows; ++i) {
				for (int j = 0; j < a.cols; ++j) {
					c.data[i][j] = a.data[i][j] + b.data[i][j];
				}
			}
		}

		/* Subtract matrices a and b and put the result in c. */
		static void subtract_matrix(Matrix a, Matrix b, Matrix c) {
			assert (a.rows == b.rows);
			assert (a.rows == c.rows);
			assert (a.cols == b.cols);
			assert (a.cols == c.cols);
			for (int i = 0; i < a.rows; ++i) {
				for (int j = 0; j < a.cols; ++j) {
					c.data[i][j] = a.data[i][j] - b.data[i][j];
				}
			}
		}

		/* Subtract from the identity matrix in place. */
		static void subtract_from_identity_matrix(Matrix a) {
			assert (a.rows == a.cols);
			for (int i = 0; i < a.rows; ++i) {
				for (int j = 0; j < a.cols; ++j) {
					if (i == j) {
						a.data[i][j] = 1.0 - a.data[i][j];
					} else {
						a.data[i][j] = 0.0 - a.data[i][j];
					}
				}
			}
		}

		/* Multiply matrices a and b and put the result in c. */
		static void multiply_matrix(Matrix a, Matrix b, Matrix c) {
			assert (a.cols == b.rows);
			assert (a.rows == c.rows);
			assert (b.cols == c.cols);
			for (int i = 0; i < c.rows; ++i) {
				for (int j = 0; j < c.cols; ++j) {
				/*
				 * Calculate element c.data[i][j] via a dot product of one row
				 * of a with one column of b
				 */
					c.data[i][j] = 0.0;
					for (int k = 0; k < a.cols; ++k) {
						c.data[i][j] += a.data[i][k] * b.data[k][j];
					}
				}
			}
		}

		/* Multiply matrix a by b-transpose and put the result in c. */
	/*
	 * This is multiplying a by b-tranpose so it is like multiply_matrix but
	 * references to b reverse rows and cols.
	 */
		static void multiply_by_transpose_matrix(Matrix a, Matrix b, Matrix c) {
			assert (a.cols == b.cols);
			assert (a.rows == c.rows);
			assert (b.rows == c.cols);
			for (int i = 0; i < c.rows; ++i) {
				for (int j = 0; j < c.cols; ++j) {
				/*
				 * Calculate element c.data[i][j] via a dot product of one row
				 * of a with one row of b
				 */
					c.data[i][j] = 0.0;
					for (int k = 0; k < a.cols; ++k) {
						c.data[i][j] += a.data[i][k] * b.data[j][k];
					}
				}
			}
		}

		/* Transpose input and put the result in output. */
		static void transpose_matrix(Matrix input, Matrix output) {
			assert (input.rows == output.cols);
			assert (input.cols == output.rows);
			for (int i = 0; i < input.rows; ++i) {
				for (int j = 0; j < input.cols; ++j) {
					output.data[j][i] = input.data[i][j];
				}
			}
		}

		/* Whether two matrices are approximately equal. */
		static boolean equal_matrix(Matrix a, Matrix b, double tolerance) {
			assert (a.rows == b.rows);
			assert (a.cols == b.cols);
			for (int i = 0; i < a.rows; ++i) {
				for (int j = 0; j < a.cols; ++j) {
					if (Math.abs(a.data[i][j] - b.data[i][j]) > tolerance)
						return false;
				}
			}
			return true;
		}

			/*
	 * Invert a square matrix. Returns whether the matrix is invertible. input
	 * is mutated as well by this routine.
	 */

		/*
         * Uses Gauss-Jordan elimination.
         *
         * The elimination procedure works by applying elementary row operations to
         * our input matrix until the input matrix is reduced to the identity
         * matrix. Simultaneously, we apply the same elementary row operations to a
         * separate identity matrix to produce the inverse matrix. If this makes no
         * sense, read wikipedia on Gauss-Jordan elimination.
         *
         * This is not the fastest way to invert matrices, so this is quite possibly
         * the bottleneck.
         */
		static boolean destructive_invert_matrix(Matrix input, Matrix output) {
			assert (input.rows == input.cols);
			assert (input.rows == output.rows);
			assert (input.rows == output.cols);

			output.set_identity_matrix();

		/*
		 * Convert input to the identity matrix via elementary row operations.
		 * The ith pass through this loop turns the element at i,i to a 1 and
		 * turns all other elements in column i to a 0.
		 */
			for (int i = 0; i < input.rows; ++i) {
				if (input.data[i][i] == 0.0) {
				/* We must swap rows to get a nonzero diagonal element. */
					int r;
					for (r = i + 1; r < input.rows; ++r) {
						if (input.data[r][i] != 0.0)
							break;
					}
					if (r == input.rows) {
					/*
					 * Every remaining element in this column is zero, so this
					 * matrix cannot be inverted.
					 */
						return false;
					}
					input.swap_rows(i, r);
					output.swap_rows(i, r);
				}

			/*
			 * Scale this row to ensure a 1 along the diagonal. We might need to
			 * worry about overflow from a huge scalar here.
			 */
				double scalar = 1.0 / input.data[i][i];
				input.scale_row(i, scalar);
				output.scale_row(i, scalar);

			/* Zero out the other elements in this column. */
				for (int j = 0; j < input.rows; ++j) {
					if (i == j)
						continue;
					double shear_needed = -input.data[j][i];
					input.shear_row(j, i, shear_needed);
					output.shear_row(j, i, shear_needed);
				}
			}

			return true;
		}
	}

	public class Matrix {
		/* Dimensions */
		int rows;
		int cols;

		/* Contents of the matrix */
		double[][] data;

		/*
         * Allocate memory for a new matrix. Zeros out the matrix. Assert-fails if
         * we are out of memory.
         */
		Matrix(int rows, int cols) {
			this.rows = rows;
			this.cols = cols;
			this.data = new double[rows][cols];
		}

		/* Set values of a matrix, row by row. */
		void set_matrix(double... arg) {
			assert (arg.length == rows * cols);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					data[i][j] = arg[i * cols + j];
				}
			}
		}

		/* Turn m into an identity matrix. */
		void set_identity_matrix() {
			assert (rows == cols);
			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < cols; ++j) {
					if (i == j) {
						data[i][j] = 1.0;
					} else {
						data[i][j] = 0.0;
					}
				}
			}
		}

		/* Pretty-print a matrix. */
		void print_matrix() {
			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < cols; ++j) {
					if (j > 0)
						System.out.print(" ");
					System.out.format("%6.2f", data[i][j]);
				}
				System.out.print("\n");
			}
		}


		/* Multiply a matrix by a scalar. */
		void scale_matrix(double scalar) {
			assert (scalar != 0.0);
			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < cols; ++j) {
					data[i][j] *= scalar;
				}
			}
		}

		/*
         * Swap rows r1 and r2 of a matrix. This is one of the three
         * "elementary row operations".
         */
		void swap_rows(int r1, int r2) {
			assert (r1 != r2);
			double[] tmp = data[r1];
			data[r1] = data[r2];
			data[r2] = tmp;
		}

		/*
         * Multiply row r of a matrix by a scalar. This is one of the three
         * "elementary row operations".
         */
		void scale_row(int r, double scalar) {
			assert (scalar != 0.0);
			for (int i = 0; i < cols; ++i)
				data[r][i] *= scalar;
		}

		/*
         * Add a multiple of row r2 to row r1. Also known as a "shear" operation.
         * This is one of the three "elementary row operations".
         */
	/* Add scalar * row r2 to row r1. */
		void shear_row(int r1, int r2, double scalar) {
			assert (r1 != r2);
			for (int i = 0; i < cols; ++i)
				data[r1][i] += scalar * data[r2][i];
		}

	}

}
