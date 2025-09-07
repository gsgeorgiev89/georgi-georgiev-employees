package com.ggeorgiev.employees.entities;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeePair {
    private final Long emp1;
    private final Long emp2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeePair)) return false;
        EmployeePair other = (EmployeePair) o;
        return emp1.equals(other.emp1) && emp2.equals(other.emp2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emp1, emp2);
    }
}