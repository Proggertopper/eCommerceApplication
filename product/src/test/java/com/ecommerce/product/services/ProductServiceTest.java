package com.ecommerce.product.services;

import com.ecommerce.product.dtos.ProductRequest;
import com.ecommerce.product.dtos.ProductResponse;
import com.ecommerce.product.models.Product;
import com.ecommerce.product.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {


    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProductPersistsActiveProductAndReturnsResponse() {
        ProductRequest request = productRequest();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(1L);
            return product;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Keyboard");
        assertThat(response.getPrice()).isEqualByComparingTo("99.99");
        assertThat(response.getActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void fetchByIdReturnsOnlyActiveProduct() {
        Product product = product();
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        Optional<ProductResponse> response = productService.fetchById(1L);

        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
    }

    @Test
    void deleteProductMarksProductInactive() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        boolean deleted = productService.deleteProduct(1L);

        assertThat(deleted).isTrue();
        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void searchProductsMapsRepositoryResult() {
        when(productRepository.searchProducts("key")).thenReturn(List.of(product()));

        List<ProductResponse> result = productService.searchProducts("key");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Keyboard");
    }

    private ProductRequest productRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Keyboard");
        request.setDescription("Mechanical keyboard");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(10);
        request.setCategory("Accessories");
        request.setImageUrl("https://example.com/keyboard.png");
        return request;
    }

    private Product product() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Keyboard");
        product.setDescription("Mechanical keyboard");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setCategory("Accessories");
        product.setImageUrl("https://example.com/keyboard.png");
        product.setActive(true);
        return product;
    }
}
