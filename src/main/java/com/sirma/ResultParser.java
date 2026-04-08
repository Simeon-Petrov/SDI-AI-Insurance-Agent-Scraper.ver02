package com.sirma;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultParser {

    static class InsuranceOffer {
        String company;
        double singlePaymentEur;
        String fourInstallmentsFormula;
        double totalFourEur;

        InsuranceOffer(String company, double singlePaymentEur, String fourInstallmentsFormula, double totalFourEur) {
            this.company = company;
            this.singlePaymentEur = singlePaymentEur;
            this.fourInstallmentsFormula = fourInstallmentsFormula;
            this.totalFourEur = totalFourEur;
        }
    }

    /**
     * Parses the CSV and returns a formatted table as a String
     */
    public static String getFormattedOffers() {
        String filePath = "GO_Results.csv";
        List<InsuranceOffer> offers = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        // Regex patterns for parsing data
        Pattern singleEurPattern = Pattern.compile("^([А-Яа-я\\s\\w]+?)\\s+.*?/\\s*(\\d+,\\d{2})\\s*€");
        Pattern installmentsPattern = Pattern.compile("(\\d+,\\d{2})\\s*€\\s*\\+\\s*(\\d+,\\d{2})\\s*€\\s*\\+\\s*(\\d+,\\d{2})\\s*€\\s*\\+\\s*(\\d+,\\d{2})\\s*€\\s*=\\s*(\\d+,\\d{2})\\s*€");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip delivery text and empty lines
                if (line.startsWith("Ако") || line.isEmpty()) continue;

                Matcher mSingle = singleEurPattern.matcher(line);
                if (mSingle.find()) {
                    String name = mSingle.group(1).trim();
                    if (name.equalsIgnoreCase("Ако") || name.length() < 2) continue;

                    double singleEur = Double.parseDouble(mSingle.group(2).replace(",", "."));
                    String formula = "Not available";
                    double totalFour = 0;

                    Matcher mInst = installmentsPattern.matcher(line);
                    if (mInst.find()) {
                        formula = String.format("%s + 3 x %s = %s", mInst.group(1), mInst.group(2), mInst.group(5));
                        totalFour = Double.parseDouble(mInst.group(5).replace(",", "."));
                    }

                    if (offers.stream().noneMatch(o -> o.company.equalsIgnoreCase(name))) {
                        offers.add(new InsuranceOffer(name, singleEur, formula, totalFour));
                    }
                }
            }

            // Sort by single payment price
            offers.sort(Comparator.comparingDouble(o -> o.singlePaymentEur));

            // Build the table string
            sb.append("\n").append("=".repeat(85)).append("\n");
            sb.append("          INSURANCE OFFERS COMPARISON (EUR) - SORTED BY PRICE\n");
            sb.append("=".repeat(85)).append("\n");
            sb.append(String.format("%-18s | %-15s | %-40s\n", "INSURER", "SINGLE PAYMENT", "SCHEME: 4 INSTALLMENTS"));
            sb.append("-".repeat(85)).append("\n");

            if (offers.isEmpty()) {
                sb.append("No valid data found.\n");
            } else {
                for (InsuranceOffer offer : offers) {
                    sb.append(String.format("%-18s | %10.2f €  | %-40s\n",
                            offer.company, offer.singlePaymentEur, offer.fourInstallmentsFormula));
                }
            }
            sb.append("=".repeat(85)).append("\n");

        } catch (Exception e) {
            return "Error parsing file: " + e.getMessage();
        }
        return sb.toString();
    }
}