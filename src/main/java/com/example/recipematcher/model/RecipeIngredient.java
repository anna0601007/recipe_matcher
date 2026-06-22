package com.example.recipematcher.model;

import com.example.recipematcher.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(
        name = "recipe_ingredients",
        schema = "recipe_matcher",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_recipe_ingredient",
                        columnNames = {"recipe_id", "ingredient_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "unit", columnDefinition = "recipe_matcher.measurement_unit")
    private MeasurementUnit unit;

    @Column(columnDefinition = "TEXT")
    private String note;
}