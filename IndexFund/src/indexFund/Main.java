package indexFund;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {

		System.out.println("Welcome");

		try {

			// /!\ Il y a deux actifs dans les données brutes qui ne font pas
			// partie
			// du CAC40 !

			MinimizeCorrelationIndexFund.minimizeCorrelationEuristiques(40, 10,
					0.01, "Journalier_sur_un_an.csv", ";", null);

			/*
			 * IndexFundTheorique.solveMeTheorique(40, 10,
			 * "Journalier_sur_un_an.csv", ";");
			 */

			// for (int n = 100; n < 1000; n += 100) {
			// int q = n / 10; // Bof, à revoir. :p
			// FileWriter fw = new FileWriter("IndexFound_" + n + "_" + q
			// + ".csv");
			// String fileName = Utils.writeCorrelationsInFile(n);
			//
			// MinimizeCorrelationIndexFund.minimizeCorrelationEuristiques(n,
			// q, 0.01, fileName, ";", fw);
			//
			// fw.close();
			// }

			// for (int q = 10; q <= 100; q += 5) {
			// FileWriter fw = new FileWriter("IndexFund_FTSE_" + q + ".csv");
			// MinimizeCorrelationIndexFund.minimizeCorrelationEuristiques(
			// 100, q, 0.01, "FTSE.csv", ";", fw);
			// fw.flush();
			// fw.close();
			// }

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
