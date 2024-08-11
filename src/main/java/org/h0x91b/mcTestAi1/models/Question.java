package org.h0x91b.mcTestAi1.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    private final String question;
    private final List<String> answers;
    private final int correctAnswerIndex;
    private final List<Integer> randomizedIndices;

    public Question(String question, List<String> answers, int correctAnswerIndex) {
        this.question = question;
        this.answers = List.copyOf(answers);  // Create an immutable copy
        this.correctAnswerIndex = correctAnswerIndex;
        this.randomizedIndices = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            randomizedIndices.add(i);
        }
        Collections.shuffle(randomizedIndices);
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        List<String> randomizedAnswers = new ArrayList<>();
        for (int index : randomizedIndices) {
            randomizedAnswers.add(answers.get(index));
        }
        return randomizedAnswers;
    }

    public boolean isCorrectAnswer(int index) {
        return randomizedIndices.get(index) == correctAnswerIndex;
    }

    public int getOriginalIndex(int randomizedIndex) {
        return randomizedIndices.get(randomizedIndex);
    }
}