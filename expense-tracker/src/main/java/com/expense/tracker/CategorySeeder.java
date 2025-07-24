package com.expense.tracker;

import com.expense.tracker.model.Category;
import com.expense.tracker.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args){
        List<String> categories = List.of(
                "🍕Food", "🛫Travel", "🩺Health", "🛒Shopping",
                "📃Bills & Utilities", "🎬Entertainment", "🎓Education",
                "🎁Gifts & Donations", "💹Investment", "Others"
        );

        for (String categoryName : categories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category saved = categoryRepository.save(new Category(categoryName));
                System.out.println("Saved Category: ID=" + saved.getId() + ", Name=" + saved.getName());

            }
        }
    }
}
