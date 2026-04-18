package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.CategoryDto;
import OopProject.AiPoweredEcommerceSystem.dto.CategoryRequest;
import OopProject.AiPoweredEcommerceSystem.entity.Category;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for product category management.
 * Category creation / update / deletion is restricted to ADMINs
 * (enforced at the controller level via {@code @PreAuthorize}).
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(CategoryRequest req) {
        if (categoryRepository.existsByName(req.getName())) {
            throw new BadRequestException("Category already exists: " + req.getName());
        }
        Category category = new Category(req.getName(), req.getDescription());
        return CategoryDto.from(categoryRepository.save(category));
    }

    public CategoryDto updateCategory(Long id, CategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        return CategoryDto.from(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        return CategoryDto.from(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }
}
