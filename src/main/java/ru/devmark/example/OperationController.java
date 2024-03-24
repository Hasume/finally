package ru.devmark.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OperationController {

    private final DatabaseManager dbManager;

    public OperationController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @GetMapping("/operations")
    public List<String> getOperationList(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must be provided");
        }
        return dbManager.getOperationList(userId, startDate, endDate);
    }
}