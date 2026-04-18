package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.CategoryDto;
import OopProject.AiPoweredEcommerceSystem.dto.CategoryRequest;

import java.util.List;

abstract public class CategoryServiceAbstraction {
    abstract public CategoryDto createCategory(CategoryRequest req);
    abstract public CategoryDto updateCategory(Long id, CategoryRequest req);
    abstract public void deleteCategory(Long id);
    abstract  public List<CategoryDto> getAllCategories();
    abstract public CategoryDto getCategoryById(Long id);
}
