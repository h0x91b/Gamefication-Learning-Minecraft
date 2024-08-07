package org.h0x91b.mcTestAi1.models;

import java.util.List;

public class Question {
    private final String question;
    private final List<String> answers;
    private final int correctAnswerIndex;

    public Question(String question, List<String> answers, int correctAnswerIndex) {
        this.question = question;
        this.answers = List.copyOf(answers);  // Create an immutable copy
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public boolean isCorrectAnswer(int index) {
        return index == correctAnswerIndex;
    }
}