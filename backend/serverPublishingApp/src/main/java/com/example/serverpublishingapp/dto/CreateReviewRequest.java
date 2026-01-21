package com.example.serverpublishingapp.dto;

public class CreateReviewRequest {
        private String comment;
        private String decision; // "approve", "reject", "revision"
        private String orderStatusAfterReview;

        public CreateReviewRequest() {}

        public CreateReviewRequest(String comment, String decision, String orderStatusAfterReview) {
            this.comment = comment;
            this.decision = decision;
            this.orderStatusAfterReview = orderStatusAfterReview;
        }

        // Геттеры и сеттеры
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }

        public String getOrderStatusAfterReview() { return orderStatusAfterReview; }
        public void setOrderStatusAfterReview(String orderStatusAfterReview) {
            this.orderStatusAfterReview = orderStatusAfterReview;
        }
}