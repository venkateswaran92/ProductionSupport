package com.psyncopate.rag.dto;

import java.util.List;

public record RagResponse(String answer, List<String> relevantDocuments) {
}
