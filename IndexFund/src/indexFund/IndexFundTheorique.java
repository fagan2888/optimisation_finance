package indexFund;

import java.io.IOException;
import java.util.List;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

public class IndexFundTheorique {

	public static double solveMeTheorique(int n, int q, String fichier,
			String csvSpliter) throws IOException {
		double value = 0.0;

		List<String[]> correlations = Utils
				.getCorrelations(fichier, csvSpliter);
		/*******************************************************************/

		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int indRows;
		SWIGTYPE_p_int indCols;
		SWIGTYPE_p_double val;

		int ret;

		try {
			// Create problem
			lp = GLPK.glp_create_prob();
			// System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "IndexFund theorique");

			// Define columns and objective function coefficients
			GLPK.glp_add_cols(lp, n * n + n);

			for (int i = 1; i <= n; i++) {
				for (int j = 1; j <= n; j++) {
					GLPK.glp_set_col_name(lp, i + j, "x" + (i - 1) + (j - 1));
					GLPK.glp_set_col_kind(lp, i + j, GLPKConstants.GLP_IV);
					GLPK.glp_set_col_bnds(lp, i + j, GLPKConstants.GLP_DB, 0.0,
							1.0);
					GLPK.glp_set_obj_coef(lp, i + j, Double
							.parseDouble((correlations.get(i - 1))[j - 1]
									.replace(",", "."))); // A modifier
				}
			}

			// Define objective
			GLPK.glp_set_obj_name(lp, "Corrélation");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);

			// Create constraints
			GLPK.glp_add_rows(lp, n * n + n + 1);
			for (int i = 1; i <= n; i++) {
				GLPK.glp_set_row_name(lp, i, "c" + i);
				GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_FX, 1.0, 1.0); // 1*
																				// xij
			}

			for (int i = n + 1; i <= 2 * n; i++) {
				GLPK.glp_set_row_name(lp, i, "c" + i);
				GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_UP, 0.0, 1.0); // yj
																				// pour
																				// le
																				// 1
			}

			GLPK.glp_set_row_name(lp, 2 * n + 1, "somme sur j xij = 1");
			GLPK.glp_set_row_bnds(lp, 2 * n + 1, GLPKConstants.GLP_FX, 1.0, 1.0);

			GLPK.glp_set_row_name(lp, 2 * n + 2, "somme sur j yj = q");
			GLPK.glp_set_row_bnds(lp, 2 * n + 2, GLPKConstants.GLP_FX, q, q);

			indRows = GLPK.new_intArray(((n * n) + n) * ((n * n) + (n + 1)));
			indCols = GLPK.new_intArray(((n * n) + n) * ((n * n) + (n + 1)));
			val = GLPK.new_doubleArray(((n * n) + n) * ((n * n) + (n + 1)));

			int counter = 1;

			for (int i = 1; i <= n; i++) {
				for (int j = 1; j <= n; j++) {
					GLPK.intArray_setitem(indRows, counter, i);
					GLPK.intArray_setitem(indCols, counter, j);
					GLPK.doubleArray_setitem(val, counter, 1);
					counter++;
				}
			}

			// Chargement de la matrice des contraintes
			GLPK.glp_load_matrix(lp, ((n * n) + n) * ((n * n) + (n + 1)),
					indRows, indCols, val);
			System.out.println("Loading matrix : success.");

			// Solve model
			parm = new glp_smcp();
			long startTime = System.nanoTime();

			// solving and time estimating
			GLPK.glp_init_smcp(parm);

			ret = GLPK.glp_simplex(lp, parm);
			// Retrieve solution
			if (ret == 0) {
				// write_lp_solution(lp, fw);
				long endTime = System.nanoTime();
				long duration = (endTime - startTime);
				System.out.println();
				System.out.println("Execution time = " + duration / 1000000
						+ " milliseconds");
				// if (null != fw) {
				// fw.append(duration / 1000000 + ";milliseconds");
				// fw.append("\n");
				// }
			} else {
				System.out.println("The problem could not be solved");
			}
			// Free memory
			GLPK.glp_delete_prob(lp);
		} catch (GlpkException ex) {
			ex.printStackTrace();
		}

		return value;
	}
}
