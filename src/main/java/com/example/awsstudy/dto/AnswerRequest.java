package com.example.awsstudy.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AnswerRequest(@NotBlank String questionId, @NotBlank @Size(max = 2000) String answer) {}
