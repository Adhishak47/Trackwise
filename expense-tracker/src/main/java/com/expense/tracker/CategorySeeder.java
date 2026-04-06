package com.expense.tracker;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        Map<String, EntryType> categories = new LinkedHashMap<>();

        // Expense categories
        categories.put("🍕 Food",                EntryType.EXPENSE);
        categories.put("🛫 Travel",              EntryType.EXPENSE);
        categories.put("🩺 Health",              EntryType.EXPENSE);
        categories.put("🛒 Shopping",            EntryType.EXPENSE);
        categories.put("📃 Bills & Utilities",   EntryType.EXPENSE);
        categories.put("🎬 Entertainment",       EntryType.EXPENSE);
        categories.put("🎓 Education",           EntryType.EXPENSE);
        categories.put("🎁 Gifts & Donations",   EntryType.EXPENSE);
        categories.put("💹 Investment",          EntryType.EXPENSE);
        categories.put("📦 Others",              EntryType.EXPENSE);

        // Income categories
        categories.put("💼 Salary",                       EntryType.INCOME);
        categories.put("🧑‍💻 Freelance & Side Hustles",    EntryType.INCOME);
        categories.put("📈 Business Income",              EntryType.INCOME);
        categories.put("💹 Investments & Returns",        EntryType.INCOME);
        categories.put("🏠 Rental Income",                EntryType.INCOME);
        categories.put("🎁 Gifts Received",               EntryType.INCOME);
        categories.put("💸 Other Income",                 EntryType.INCOME);

        categories.forEach((name, type) -> {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(new Category(name, type));
                System.out.println("✅ Seeded category [" + type + "]: " + name);
            }
        });
    }
}
