CREATE SCHEMA IF NOT EXISTS recipe_matcher;

CREATE TYPE recipe_matcher.recipe_category AS ENUM (
    'BREAKFAST',
    'LUNCH',
    'DINNER',
    'DESSERT',
    'SNACK',
    'DRINK',
    'SOUP',
    'SALAD',
    'SIDE_DISH'
    );

CREATE TYPE recipe_matcher.recipe_difficulty AS ENUM (
    'EASY',
    'MEDIUM',
    'HARD'
    );

CREATE TYPE recipe_matcher.ingredient_category AS ENUM (
    'VEGETABLE',
    'FRUIT',
    'MEAT',
    'FISH',
    'DAIRY',
    'GRAIN',
    'SPICE',
    'OIL',
    'SAUCE',
    'OTHER'
    );

CREATE TYPE recipe_matcher.measurement_unit AS ENUM (
    'GRAM',
    'KILOGRAM',
    'MILLILITER',
    'LITER',
    'PIECE',
    'TEASPOON',
    'TABLESPOON',
    'CUP',
    'PINCH'
    );

CREATE TABLE IF NOT EXISTS recipe_matcher.recipes
(
    id                   BIGSERIAL PRIMARY KEY,
    title                VARCHAR(255) NOT NULL,
    description          TEXT,
    instructions         TEXT         NOT NULL,
    cooking_time_minutes INT CHECK (cooking_time_minutes IS NULL OR cooking_time_minutes > 0),
    category             recipe_matcher.recipe_category,
    difficulty           recipe_matcher.recipe_difficulty,
    created_at           TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS recipe_matcher.ingredients
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    category   recipe_matcher.ingredient_category,
    created_at TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS recipe_matcher.recipe_ingredients
(
    id            BIGSERIAL PRIMARY KEY,
    recipe_id     BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    amount        NUMERIC(10, 2) CHECK (amount IS NULL OR amount > 0),
    unit          recipe_matcher.measurement_unit,
    note          TEXT,

    CONSTRAINT fk_recipe
    FOREIGN KEY (recipe_id)
    REFERENCES recipe_matcher.recipes (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_ingredient
    FOREIGN KEY (ingredient_id)
    REFERENCES recipe_matcher.ingredients (id)
    ON DELETE CASCADE,

    CONSTRAINT unique_recipe_ingredient
    UNIQUE (recipe_id, ingredient_id)
    );