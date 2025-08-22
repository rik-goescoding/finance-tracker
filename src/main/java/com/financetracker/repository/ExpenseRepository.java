package com.financetracker.repository;

import com.financetracker.entity.Expense;
import com.financetracker.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Most useful real-world queries
    List<Expense> findByCategory(String category);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByPaymentMethod(PaymentMethod paymentMethod);

    List<Expense> findByLocationContaining(String location);

    List<Expense> findAllByOrderByExpenseDateDesc();

    // One combination for monthly reports
    List<Expense> findByCategoryAndExpenseDateBetween(String category, LocalDate startDate, LocalDate endDate);
}
