package com.sirma;

import dev.langchain4j.service.SystemMessage;

public interface InsuranceAssistant {

    @SystemMessage({
            "\"You are Sirma Academy's intelligent insurance assistant.\",\n" +
            "\"Your goal is to help users check Civil Liability prices.\",\n" +
            "\"Use the provided tools to browse in real time.\""
    })
    String chat(String massage);
}
