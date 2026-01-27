package com.mabotjatr.taskflow.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskQueryParams {
    private int page = 0;
    private int size = 20;
    private String sortBy = "dueDate";
    private String sortDirection = "asc";

    public void validate() {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        if (!List.of("asc", "desc").contains(sortDirection.toLowerCase())) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
    }
}
