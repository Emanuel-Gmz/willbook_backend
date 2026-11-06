package edu.willbook_backend.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}