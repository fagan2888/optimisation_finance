package indexFund;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

public final class IndexFund {

	public static IndexFund instance = null;

	public static List<Double> C = new ArrayList<Double>();

	public final static IndexFund getInstance(int n, List<Double> valeurU,
			String fichier, String csvSpliter) {

		C.clear();

		// Puis récupération des corrélations
		for (int j = 0; j < n; j++) {
			C.add(getCj(j, valeurU, fichier, csvSpliter));
		}

		if (instance == null) {
			instance = new IndexFund();
		}

		return instance;

	}

	public final static IndexFund getInstance(int n, int k,
			List<Double> valeurU, String fichier, String csvSpliter) {
		C.clear();

		// Puis récupération des corrélations
		for (int j = 0; j < n; j++) {
			C.add(getCjk(j, k, valeurU, fichier, csvSpliter));
		}

		if (instance == null) {
			instance = new IndexFund();
		}

		return instance;
	}

	private IndexFund() {
	}

	public double solveMe(int n, int q, List<Double> u, String fichier,
			String csvSpliter, FileWriter fw) throws IOException {

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
			// System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "Index Fund");
			/*******************************************************************************/
			GLPK.glp_add_cols(lp, n);

			// Set constraints
			for (int i = 1; i <= n; i++) {
				GLPK.glp_set_col_name(lp, i, "y" + (i - 1));
				GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_IV);
				GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, 0.0, 1.0); // yi
																				// vaut
																				// 0
																				// ou
																				// 1.
				GLPK.glp_set_obj_coef(lp, i, C.get(i - 1)); // La ième valeur de
															// C.
			}

			// Set the objective function
			GLPK.glp_set_obj_name(lp, "Corrélation"); // On veut maximiser la
														// corrélation globale
														// du
														// portefeuille.
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
			/********************************************************************************/
			GLPK.glp_add_rows(lp, 1);

			// Set the only constraint we have !! :)
			GLPK.glp_set_row_name(lp, 1, "c1");
			GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_FX, q, q); // On veut
																		// un
																		// portefeuille
																		// à q
																		// actifs.

			indRows = GLPK.new_intArray(1 * n);
			indCols = GLPK.new_intArray(1 * n);
			val = GLPK.new_doubleArray(1 * n);

			int counter = 1;

			for (int i = 1; i <= n; i++) {
				GLPK.intArray_setitem(indRows, counter, 1);
				GLPK.intArray_setitem(indCols, counter, i);
				GLPK.doubleArray_setitem(val, counter, 1.0); // somme des 1*yi
				counter++;
			}

			GLPK.glp_load_matrix(lp, 1 * n, indRows, indCols, val);
			// System.out.println("Loading matrix : success.");

			// Solve model
			parm = new glp_smcp();
			long startTime = System.nanoTime();

			GLPK.glp_term_out(GLPKConstants.GLP_OFF);
			// solving and time estimating
			GLPK.glp_init_smcp(parm);

			ret = GLPK.glp_simplex(lp, parm);

			// Retrieve solution
			// String name;
			double value;

			if (ret == 0) {
				// name = GLPK.glp_get_obj_name(lp);
				value = GLPK.glp_get_obj_val(lp) + sum(u);
				// System.out.println(sum(u));

				// System.out.print(name + " = ");
				// System.out.println(value);

				long endTime = System.nanoTime();
				long duration = (endTime - startTime);
				// System.out.println("Execution time = " + duration
				// + " nanoseconds");
				if (null != fw) {
					fw.append(duration + "\n");
				}

				System.out.println("NEW STEP");
				for (int i = 1; i <= n; i++) {
					System.out.print(GLPK.glp_get_col_name(lp, i));
					System.out.print("\t");
					System.out.print(GLPK.glp_get_col_prim(lp, i));
					System.out.print("\t");
				}
				System.out.println();

			} else {
				System.out.println("The problem could not be solved");
				value = 999999;
			}

			// Free memory
			GLPK.glp_delete_prob(lp);

			return value;

		} catch (GlpkException ex) {
			ex.printStackTrace();
			return 999999;
		}

	}

	private static Double getCj(int j, List<Double> u, String fichier,
			String csvSpliter) {

		Double returnValue = new Double(0.0);

		String[] correlationsLigneJ = Utils
				.getCorrelations(fichier, csvSpliter).get(j);

		for (int k = 0; k < correlationsLigneJ.length; k++) {
			if (Double.parseDouble(correlationsLigneJ[k].replace(",", ".")) > u
					.get(k)) {
				returnValue += Double.parseDouble(correlationsLigneJ[k]
						.replace(",", ".")) - u.get(k);
			} else {
				returnValue += 0.0;
			}

		}

		return returnValue;
	}

	private static Double getCjk(int j, int k, List<Double> u, String fichier,
			String csvSpliter) {
		Double returnValue = new Double(0.0);

		String[] correlationsLigneJ = Utils
				.getCorrelations(fichier, csvSpliter).get(j);
		for (int i = 0; i < k; i++) {
			if (Double.parseDouble(correlationsLigneJ[i].replace(",", ".")) > u
					.get(i)) {
				returnValue += Double.parseDouble(correlationsLigneJ[i]
						.replace(",", ".")) - u.get(i);
			} else {
				returnValue += 0.0;
			}
		}

		return returnValue;
	}

	private static Double sum(List<Double> u) {
		Double result = new Double(0.0);

		for (Double value : u) {
			result += value;
		}

		return result;
	}

}
