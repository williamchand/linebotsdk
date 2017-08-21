package com.example.bot.spring;

public class Databasing {

    private final int questionId;
    private final String question;
    private final String answer;

    public Greeting(String id, String content) {
    	this.questionId=questionId;
        this.question = question;
        this.answer = answer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getAnswer() {
        return answer;
    }
    
    public String getQuestion() {
        return question;
    }
}
