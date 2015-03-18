package indexFund;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MinimizeCorrelationIndexFund {

	private static void initU(int n, List<Double> vecteurU, Double valeurInit) {
		if (null == vecteurU) {
			vecteurU = new ArrayList<Double>();
		}
		for (int i = 0; i < n; i++) {
			vecteurU.add(valeurInit);
		}
	}

	public static void minimizeCorrelationEuristiques(int n, int q,
			double epsillon, String fileName, String csvSpliter, FileWriter fw)
			throws IOException {
		List<Double> vecteurU = new ArrayList<Double>();

		MinimizeCorrelationIndexFund.initU(n, vecteurU, 1.0);

		double valeurTmp = 0.0;
		List<Double> valeursCorrelations = new ArrayList<Double>();
		List<Double> valeursCorrelationsI = new ArrayList<Double>();
		List<List<Double>> valeursValeursCorrelationsI = new ArrayList<List<Double>>();

		// Premier calcul
		valeurTmp = IndexFund.getInstance(n, vecteurU, fileName, csvSpliter)
				.solveMe(n, q, vecteurU, fileName, csvSpliter, fw);

		valeursCorrelationsI.add(valeurTmp);

		for (int i = 0; i < vecteurU.size(); i++) {
			valeurTmp = IndexFund
					.getInstance(n, vecteurU, fileName, csvSpliter).solveMe(n,
							q, vecteurU, fileName, csvSpliter, fw);

			while (valeurTmp <= Collections.min(valeursCorrelationsI)
					+ epsillon) { // Fucking erreurs d'arrondi...
				vecteurU.set(i, vecteurU.get(i) - 0.01);

				valeurTmp = IndexFund.getInstance(n, vecteurU, fileName,
						csvSpliter).solveMe(n, q, vecteurU, fileName,
						csvSpliter, fw);

				valeursCorrelationsI.add(valeurTmp);
			}
			// On a atteint un minimum local et on vient de remonter : il faut
			// donc revenir à la valeur précédente.
			vecteurU.set(i, vecteurU.get(i) + 0.01);

			// On ajoute les valeurs dans une liste plus globale
			valeursValeursCorrelationsI.add(valeursCorrelationsI);
			valeursCorrelations.add(Collections.min(valeursCorrelationsI));

		}

		System.out.println("Fin de la simulation avec heuristiques.");
		System.out.println("Valeur minimale trouvée : "
				+ Collections.min(valeursCorrelations));

	}

	public static void minimizeCorrelationAllUi(int n, int q, double epsillon,
			String fileName, String csvSpliter, FileWriter fw)
			throws IOException {
		// Objectif minimiser au maximum la corrélation

		double correlation = 30.0; // Faire ici un appel à la méthode de
									// résolution exacte...
		double valeurTmp = 0.0;
		// double previousValue = 0.0;
		int nombreAppelsResolution = 0;
		List<Double> vecteurU = new ArrayList<Double>();

		MinimizeCorrelationIndexFund.initU(n, vecteurU, 1.0);

		List<Double> valeursCorrelations = new ArrayList<Double>();

		do {
			valeurTmp = IndexFund
					.getInstance(n, vecteurU, fileName, csvSpliter).solveMe(n,
							q, vecteurU, fileName, csvSpliter, fw);

			valeursCorrelations.add(valeurTmp);

			for (int i = 0; i < n; i++) {
				vecteurU.set(i, vecteurU.get(i) - 0.001);
			}
			System.out.println("Nombre d'appels résolution : "
					+ ++nombreAppelsResolution);
		} while (Math.abs(correlation - valeurTmp) > epsillon
				&& vecteurU.get(0) > -1
				&& valeurTmp <= Collections.min(valeursCorrelations)
				&& valeurTmp > 0);

		// On supprime le dernier test qui n'a pas répondu à la condition while.
		valeursCorrelations.remove(valeursCorrelations.size() - 1);

		System.out.println("Fin de la simulation uniforme.");
		System.out.println("Valeur minimale trouvée : "
				+ Collections.min(valeursCorrelations));
	}

	public static void minimizeCorrelationAllDecreaseAndAlea(int n, int q,
			double epsillon, String fileName, String csvSpliter, FileWriter fw)
			throws IOException {

		// Objectif minimiser au maximum la corrélation

		double correlation = 30.0; // Faire ici un appel à la méthode de
									// résolution exacte...
		double valeurTmp = 0.0;
		// double previousValue = 0.0;
		int nombreAppelsResolution = 0;
		List<Double> vecteurU = new ArrayList<Double>();

		MinimizeCorrelationIndexFund.initU(n, vecteurU, 1.0);

		List<Double> valeursCorrelations = new ArrayList<Double>();

		do {
			valeurTmp = IndexFund
					.getInstance(n, vecteurU, fileName, csvSpliter).solveMe(n,
							q, vecteurU, fileName, csvSpliter, fw);

			valeursCorrelations.add(valeurTmp);

			for (int i = 0; i < n; i++) {
				vecteurU.set(i, vecteurU.get(i) - 0.001);
			}
			System.out.println("Nombre d'appels résolution : "
					+ ++nombreAppelsResolution);
		} while (Math.abs(correlation - valeurTmp) > epsillon
				&& vecteurU.get(0) > -1
				&& valeurTmp <= Collections.min(valeursCorrelations)
				&& valeurTmp > 0);

		// On supprime le dernier test qui n'a pas répondu à la condition while.
		valeursCorrelations.remove(valeursCorrelations.size() - 1);

		/********* Mise en place d'aléatoire pour la suite *******************/

		final Double uMax = vecteurU.get(0) + 0.001;

		for (int i = 0; i < n; i++) {
			vecteurU.set(i, uMax);
		}

		List<Double> valeursCorrelationsAleatoires = new ArrayList<Double>();
		int nombreAppelsResolutionAleatoire = 0;
		List<Double> vecteurUAleatoire = new ArrayList<Double>();

		for (int i = 0; i < n; i++) {
			// Mise en place d'aléatoire !!!!!
			Random randomGen = new Random();
			vecteurUAleatoire.add(i, randomGen.nextDouble() * vecteurU.get(i));
		}

		do {

			valeurTmp = IndexFund.getInstance(n, vecteurUAleatoire, fileName,
					csvSpliter).solveMe(n, q, vecteurUAleatoire, fileName,
					csvSpliter, fw);

			valeursCorrelationsAleatoires.add(valeurTmp);

			System.out.println("Nombre d'appels résolution aléatoire : "
					+ ++nombreAppelsResolutionAleatoire);

			for (int i = 0; i < n; i++) {
				// Mise en place d'aléatoire !!!!!
				Random randomGen = new Random();
				vecteurUAleatoire.set(i, randomGen.nextDouble());// *
				// vecteurU.get(i)
				// * 2);
			}

		} while (Math.abs(correlation - valeurTmp) > epsillon
				&& vecteurU.get(0) > 0
				// && valeurTmp <=
				// Collections.min(valeursCorrelationsAleatoires)
				&& valeurTmp > 0 && nombreAppelsResolutionAleatoire < 10000);

		System.out.println("Fin de la simulation alétoire.");
		System.out.println("Valeur minimale trouvée : "
				+ Collections.min(valeursCorrelationsAleatoires));

	}
}
