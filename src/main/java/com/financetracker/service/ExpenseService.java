package com.financetracker.service;

import com.financetracker.entity.Expense;
import com.financetracker.entity.PaymentMethod;
import com.financetracker.exception.ExpenseNotFoundException;
import com.financetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

// TODO: Phase 2 - Part of Day Analytics
// Requires: Change expenseDate from LocalDate to LocalDateTime
// Morning: 06:00-11:59, Afternoon: 12:00-17:59, Evening: 18:00-23:59, Night: 00:00-05:59

// public BigDecimal getTotalPerPartOfDay(String partOfDay) { ... }
// public BigDecimal getAveragePerPartOfDay(String partOfDay) { ... }


@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    // maybe a  variable for the minimum required columns?

    // CRUD methods
    public Expense saveExpense(Expense expense) {
        log.info("Saving expense {}", expense.getDescription());
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        log.info("Getting all expenses");
        return expenseRepository.findAll();
    }

    public Expense getExpenseById(Long id) {
        log.info("Getting expense by id {}", id);
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with id: " + id));
    }

    public Expense updateExpense(Long id, Expense expenseDetails) {
        log.info("Updating expense with id: {}", id);

        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setDescription(expenseDetails.getDescription());
                    expense.setAmount(expenseDetails.getAmount());
                    expense.setCategory(expenseDetails.getCategory());
                    expense.setExpenseDate(expenseDetails.getExpenseDate());
                    expense.setLocation(expenseDetails.getLocation());
                    expense.setPaymentMethod(expenseDetails.getPaymentMethod());
                    // updatedAt is handled automatically by @PreUpdate
                    return expenseRepository.save(expense);
                })
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with id: " + id));
    }

    public void deleteExpense(Long id) {
        log.info("Deleting expense {}", id);
        Expense existingExpense = getExpenseById(id);
        expenseRepository.delete(existingExpense);
    }

    /**
     * Private helper method to calculate total from list of expenses
     */
    private BigDecimal calculateTotal(List<Expense> expenses, String context) {
        if (expenses.isEmpty()) {
            log.warn("No expenses found to calculate total, for {}", context);
            return BigDecimal.ZERO;
        }

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Private helper method to calculate average from list of expenses
     */
    private BigDecimal calculateAverage(List<Expense> expenses, String context) {
        if (expenses.isEmpty()) {
            log.warn("No expenses found to calculate average,for {}", context);
            return BigDecimal.ZERO;
        }

        BigDecimal total = calculateTotal(expenses, context);
        return total.divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalExpenseByCategory(String category) {
        log.info("Calculating total expenses for category: {}", category);
        List<Expense> expenses = expenseRepository.findByCategory(category);
        return calculateTotal(expenses, "category: " + category);
    }

    public BigDecimal getTotalExpenseInMonth(int year, int month) {
        log.info("Calculating total expenses for {}/{}", month, year);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        return calculateTotal(expenses, "month: " + month + "/" + year);
    }

    public BigDecimal getTotalPerPaymentMethod(PaymentMethod paymentMethod) {
        log.info("Calculating total expenses for payment method: {}", paymentMethod);
        List<Expense> expenses = expenseRepository.findByPaymentMethod(paymentMethod);
        return calculateTotal(expenses, "payment method: " + paymentMethod);
    }

    public BigDecimal getTotalPerLocation(String location) {
        log.info("Calculating total expenses for location: {}", location);
        List<Expense> expenses = expenseRepository.findByLocationContaining(location);
        return calculateTotal(expenses, "location: " + location);
    }

    public BigDecimal getAverageByCategory(String category) {
        log.info("Calculating average expense for category: {}", category);
        List<Expense> expenses = expenseRepository.findByCategory(category);
        return calculateAverage(expenses, "category: " + category);
    }

    public BigDecimal getAverageExpenseInMonth(int year, int month) {
        log.info("Calculating average expenses for {}/{}", month, year);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        return calculateAverage(expenses, "month: " + month + "/" + year);
    }

    public BigDecimal getAverageExpenseByPaymentMethod(PaymentMethod paymentMethod) {
        log.info("Calculating average expense for payment method: {}", paymentMethod);
        List<Expense> expenses = expenseRepository.findByPaymentMethod(paymentMethod);
        return calculateAverage(expenses, "payment method: " + paymentMethod);
    }

    public BigDecimal getAveragePerLocation(String location) {
        log.info("Calculating average expense for location: {}", location);
        List<Expense> expenses = expenseRepository.findByLocationContaining(location);
        return calculateAverage(expenses, "location: " + location);
    }

    // Search/filter methods
    public List<Expense> getExpensesByCategory(String category) {
        log.info("Fetching expenses for category: {}", category);
        return expenseRepository.findByCategory(category);
    }

    public List<Expense> getExpensesInDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching expenses between {} and {}", startDate, endDate);
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);
    }

    public List<Expense> getExpensesByPaymentMethod(PaymentMethod paymentMethod) {
        log.info("Fetching expenses for payment method: {}", paymentMethod);
        return expenseRepository.findByPaymentMethod(paymentMethod);
    }

    public List<Expense> getExpensesByLocation(String location) {
        log.info("Fetching expenses for location containing: {}", location);
        return expenseRepository.findByLocationContaining(location);
    }

    public List<Expense> getAllExpensesSortedByDate() {
        log.info("Fetching all expenses ordered by date (newest first)");
        return expenseRepository.findAllByOrderByExpenseDateDesc();
    }

}
