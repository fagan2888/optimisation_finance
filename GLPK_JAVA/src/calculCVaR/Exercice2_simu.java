package calculCVaR;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

/**
 * Maximisation du rendement
 * 
 * @author Yoann
 *
 */
public class Exercice2_simu {

	public static void solveMe(FileWriter fw, int n, int s, double CVaRmax,
			double a) throws IOException {

		int[] b = new int[n]; // prix des actifs
		int[][] y = new int[n][s]; // prix des actifs selon les scénarios
		double[] m = new double[n];// rendement des actifs
		double sumInvestment = 0.0;

		// Assigning the initial values for our assets
		Random randomGeneratorb = new Random();
		Random randomGeneratory = new Random();
		Random randomGeneratorm = new Random();
		// fw.append("Here are the initial prices of your actifs");
		for (int i = 0; i < n; i++) {
			b[i] = 100 - randomGeneratorb.nextInt(100); // Les prix initiaux
			// des actifs varient entre 50 et 150
			// b[i] = randomGeneratorb.nextInt(100);
			sumInvestment = sumInvestment + b[i];
		}
		sumInvestment = sumInvestment / n; // implique que sumInvestment doit
											// être un double

		// Assigning initial expected returns
		// fw.append("Expected returns: ");
		for (int i = 0; i < n; i++) {
			m[i] = ((double) randomGeneratorm.nextInt(30)) / 100; // On suppose
																	// que les
																	// actifs
																	// ont un
																	// rendement
																	// de 15 %

		}

		// Assigning scenarios for our assets
		// fw.append("Different scenarios: ");
		for (int j = 0; j < s; j++) {
			// System.out.println("Scenario " + j);
			for (int i = 0; i < n; i++) {
				y[i][j] = (int) Math
						.round(Math.min(b[i],
								Math.abs(randomGeneratory.nextGaussian() + 0.5)
										* b[i]));
			}
		}

		// debut du code GLPK
		// declaration de l'objet de programmation linéaire
		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int indRows;
		SWIGTYPE_p_int indCols;
		SWIGTYPE_p_double val;
		int ret;
		try {
			// Create problem
			lp = GLPK.glp_create_prob();
			System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "Maximisation du rendement");

			// Define columns and objective function coefficients
			GLPK.glp_add_cols(lp, 1 + s + n);

			GLPK.glp_set_col_name(lp, 1, "VaR");
			GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_CV);
			GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_LO, 0.0, 0.0);
			GLPK.glp_set_obj_coef(lp, 1, 0.0);
			for (int i = 2; i <= s + 1; i++) {
				GLPK.glp_set_col_name(lp, i, "z" + (i - 1));
				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0.0, 0.0);
				GLPK.glp_set_obj_coef(lp, i, 0); // Modifié
			}
			for (int i = s + 2; i <= 1 + n + s; i++) {
				GLPK.glp_set_col_name(lp, i, "x" + (i - (s + 1)));
				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV);
				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, -1.0, 1.0); // Arbitrage
																				// ok
				GLPK.glp_set_obj_coef(lp, i, m[i - (s + 2)]);
			}

			// Define objective
			GLPK.glp_set_obj_name(lp, "Rendement");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);

			// Create constraints
			GLPK.glp_add_rows(lp, s + 3);
			for (int i = 1; i <= s; i++) {
				GLPK.glp_set_row_name(lp, i, "cZ" + i);
				GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_UP, 0.0, 0.0);
			}

			// a garder
			GLPK.glp_set_row_name(lp, s + 1, "Markovitz");
			GLPK.glp_set_row_bnds(lp, s + 1, GLPKConstants.GLP_FX, 1.0, 1.0);

			GLPK.glp_set_row_name(lp, s + 2, "CVaR");
			GLPK.glp_set_row_bnds(lp, s + 2, GLPKConstants.GLP_UP, CVaRmax,
					CVaRmax); // contrainte
								// sur
								// la
								// CVaR
			GLPK.glp_set_row_name(lp, s + 3, "Allocation");
			GLPK.glp_set_row_bnds(lp, s + 3, GLPKConstants.GLP_FX,
					sumInvestment, sumInvestment); // Allocation fixée !

			indRows = GLPK.new_intArray((s + 3) * (s + n + 1));
			indCols = GLPK.new_intArray((s + 3) * (s + n + 1));
			val = GLPK.new_doubleArray((s + 3) * (s + n + 1));

			// set coefs for VaR
			// double matrice[][] = new double[s + 3][s + n + 1];
			int counter = 1;

			// gamma
			for (int i = 1; i <= s; i++) {
				GLPK.intArray_setitem(indRows, counter, i);
				GLPK.intArray_setitem(indCols, counter, 1);
				GLPK.doubleArray_setitem(val, counter, -1);
				counter++;
				// matrice[i - 1][1 - 1] = -1; // matrice[i-1][0]
			}
			for (int i = s + 1; i <= s + 3; i++) {
				GLPK.intArray_setitem(indRows, counter, i);
				GLPK.intArray_setitem(indCols, counter, 1);
				if (i == s + 2) {
					GLPK.doubleArray_setitem(val, counter, 1);
					counter++;
					// matrice[i - 1][1 - 1] = 1; // matrice[i-1][0]
				} else {
					GLPK.doubleArray_setitem(val, counter, 0);
					counter++;
					// matrice[i - 1][1 - 1] = 0; // matrice[i-1][0]
				}

			}

			// set constraints for Zs
			// Ne change pas
			for (int i = 1; i <= s; i++) {
				for (int j = 2; j <= s + 1; j++) {
					GLPK.intArray_setitem(indRows, counter, i);
					GLPK.intArray_setitem(indCols, counter, j);
					if (i + 1 == j) {
						GLPK.doubleArray_setitem(val, counter, -1);
						// matrice[i - 1][j - 1] = -1.0;
					} else {
						GLPK.doubleArray_setitem(val, counter, 0);
						// matrice[i - 1][j - 1] = 0.0;
					}
					counter++;
				}
			}
			for (int i = s + 1; i <= s + 3; i++) {
				for (int j = 2; j <= s + 1; j++) {
					GLPK.intArray_setitem(indRows, counter, i);
					GLPK.intArray_setitem(indCols, counter, j);
					if (i == s + 2) { // Contrainte sur la CVaR
						GLPK.doubleArray_setitem(val, counter,
								(double) (1 / (s * (1 - a))));
						// matrice[i - 1][j - 1] = (double) (1 / (s * (1 - a)));
					} else {
						GLPK.doubleArray_setitem(val, counter, 0);
						// matrice[i - 1][j - 1] = 0.0;
					}
					counter++;
				}
			}

			// set constraints for X
			for (int i = 1; i <= s; i++) {
				for (int j = s + 2; j <= s + n + 1; j++) {
					GLPK.intArray_setitem(indRows, counter, i);
					GLPK.intArray_setitem(indCols, counter, j);
					double inter = b[j - s - 2] - (double) y[j - s - 2][i - 1];
					GLPK.doubleArray_setitem(val, counter, inter);
					counter++;
					// matrice[i - 1][j - 1] = inter;
				}
			}
			for (int j = s + 2; j <= s + n + 1; j++) {
				GLPK.intArray_setitem(indRows, counter, s + 1);
				GLPK.intArray_setitem(indCols, counter, j);
				GLPK.doubleArray_setitem(val, counter, 1.0); // Markovitz
				counter++;
				// matrice[s][j - 1] = 1.0;
			}
			for (int j = s + 2; j <= s + n + 1; j++) {
				GLPK.intArray_setitem(indRows, counter, s + 2);
				GLPK.intArray_setitem(indCols, counter, j);
				GLPK.doubleArray_setitem(val, counter, 0.0); // CVaR (modifié)
				counter++;
				// matrice[s + 1][j - 1] = 0.0;
			}
			for (int j = s + 2; j <= s + n + 1; j++) {
				GLPK.intArray_setitem(indRows, counter, s + 3);
				GLPK.intArray_setitem(indCols, counter, j);
				GLPK.doubleArray_setitem(val, counter, b[j - s - 2]); // Allocation
																		// constante
				counter++;
				// matrice[s + 2][j - 1] = (double) b[j - s - 2];
			}

			// Chargement de la matrice des contraintes
			GLPK.glp_load_matrix(lp, (s + 3) * (1 + n + s), indRows, indCols,
					val);

			// Solve model
			parm = new glp_smcp();
			long startTime = System.nanoTime();

			// solving and time estimating
			GLPK.glp_init_smcp(parm);

			ret = GLPK.glp_simplex(lp, parm);
			// Retrieve solution
			if (ret == 0) {
				write_lp_solution(lp, fw);
				long endTime = System.nanoTime();
				long duration = (endTime - startTime);
				System.out.println();
				System.out.println("Execution time = " + duration / 1000000
						+ " milliseconds");
				if (null != fw) {
					fw.append("" + (double) (duration / 1000000));
				}
			} else {
				System.out.println("The problem could not be solved");
			}
			// Free memory
			GLPK.glp_delete_prob(lp);
		} catch (GlpkException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * write simplex solution
	 * 
	 * @param lp
	 *            problem
	 */
	static void write_lp_solution(glp_prob lp, FileWriter fw)
			throws IOException {
		int i;
		int n;
		int s;
		String name;
		double val;
		name = GLPK.glp_get_obj_name(lp);
		val = GLPK.glp_get_obj_val(lp);
		// System.out.print(name + " = ");
		// System.out.println(val);
		if (null != fw) {
			fw.append(val + ";");
		}
		n = GLPK.glp_get_num_cols(lp);
		s = GLPK.glp_get_num_rows(lp);

		for (i = 1; i <= s; i++) {
			name = GLPK.glp_get_row_name(lp, i);
			val = GLPK.glp_get_row_prim(lp, i);

			if (name.toLowerCase().contains("var")) {
				System.out.print("CVaR" + " = ");
				System.out.println(val);

				if (null != fw) {
					fw.append(val + ";");
				}
			}

		}

		for (i = 1; i <= n; i++) {
			name = GLPK.glp_get_col_name(lp, i);
			val = GLPK.glp_get_col_prim(lp, i);
			if (name.toLowerCase().contains("var")) {
				System.out.print("VaR" + " = ");
				System.out.println(val);

				if (null != fw) {
					fw.append(val + ";");
				}

			}
		}
	}
}