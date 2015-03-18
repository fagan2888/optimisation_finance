package calculCVaR;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {

		System.out.println("Welcome");
		int n = 200;
		int S = 2000;
		double CVaRMax = 25.0;
		double a = 0.95;

		FileWriter fw = null;

		try {
			// Exercice2.solveMe(true, 50, 500, 35.0, 0.90);
			// fwTemps = new FileWriter("temps.csv", true);

			fw = new FileWriter("statistiques.csv", true);
			// fw.append("n;S;CVaRMax;alpha;Rendement;CVaR optimisee;VaR optimisee;Duree (en ms)\n");

			// for (int k = 100; k <= 500; k += 100) {
			// for (int i = 0; i < 50; i++) {
			// fw.append(k + ";" + S + ";" + CVaRMax + ";" + a + ";");
			// Exercice2_simu.solveMe(fw, k, S, CVaRMax, a);
			// fw.append("\n");
			// fw.flush();
			// }
			// }

			// for (int k = 4500; k <= 5000; k += 500) {
			// for (int i = 0; i < 50; i++) {
			// fw.append(n + ";" + k + ";" + CVaRMax + ";" + a + ";");
			// Exercice2_simu.solveMe(fw, n, k, CVaRMax, a);
			// fw.append("\n");
			// fw.flush();
			// }
			// }

			// for (double k = 48; k <= 60; k += 4) {
			// for (int i = 0; i < 50; i++) {
			// fw.append(n + ";" + S + ";" + k + ";" + a + ";");
			// Exercice2_simu.solveMe(fw, n, S, k, a);
			// fw.append("\n");
			// fw.flush();
			// }
			// }

			for (double k = 0.89; k <= 0.99; k += 0.02) {
				for (int i = 0; i < 50; i++) {
					fw.append(n + ";" + S + ";" + CVaRMax + ";" + k + ";");
					Exercice2_simu.solveMe(fw, n, S, CVaRMax, k);
					fw.append("\n");
					fw.flush();
				}
			}

			System.out.println("Simulations terminated.");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
