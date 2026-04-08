package com.sirma;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class Main {
    public static void main(String[] args) {
        String apiKey = System.getenv("GROQ_API_KEY");

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName("llama-3.3-70b-versatile")
                .build();

        SDITool tool = new SDITool();

        System.out.println("--- Agent Starting ---");

        // Step 1: Run Selenium automation
        String result = tool.openAndFillInsuranceForm();
        System.out.println("Browser Result: " + result);

        // Step 2: Check for "Success" from the tool
        if (result.contains("Success")) {
            System.out.println("--- Processing data and sending email ---");

            // Get the formatted table as a string
            String formattedTable = ResultParser.getFormattedOffers();

            // Print to console
            System.out.println(formattedTable);

            // Send Email
            EmailService.sendEmail(formattedTable);

        } else {
            System.out.println("Parser skipped - no data found.");
        }
    }
}