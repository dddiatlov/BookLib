-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema bookLib
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema bookLib
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `bookLib` DEFAULT CHARACTER SET utf8 ;
USE `bookLib` ;

-- -----------------------------------------------------
-- Table `bookLib`.`reader`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`reader` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                  `name` VARCHAR(100) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;

INSERT INTO `bookLib`.`reader` (`id`, `name`, `password_hash`)
VALUES (1, 'Default Reader', 'dev')
    ON DUPLICATE KEY UPDATE
                         `name` = VALUES(`name`),
                         `password_hash` = VALUES(`password_hash`);


-- -----------------------------------------------------
-- Table `bookLib`.`book`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`book` (
                                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                `title` VARCHAR(255) NOT NULL,
    `author` VARCHAR(255) NULL,
    `pages` INT NOT NULL,
    `genre` VARCHAR(50) NOT NULL,
    `language` VARCHAR(10) NULL DEFAULT 'en',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`reading_goal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`reading_goal` (
                                                        `reader_id` BIGINT NOT NULL,
                                                        `month` TINYINT NOT NULL,
                                                        `target_hours` INT NULL,
                                                        `target_pages` INT NULL,
                                                        `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                                        PRIMARY KEY (`month`),
    INDEX `fk_reading_goal_reader1_idx` (`reader_id` ASC) VISIBLE,
    CONSTRAINT `fk_reading_goal_reader1`
    FOREIGN KEY (`reader_id`)
    REFERENCES `bookLib`.`reader` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`favorite_books`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`favorite_books` (
                                                          `reader_id` BIGINT NOT NULL,
                                                          `book_id` BIGINT NOT NULL,
                                                          PRIMARY KEY (`reader_id`, `book_id`),
    INDEX `fk_fav_book_idx` (`book_id` ASC) VISIBLE,
    CONSTRAINT `fk_fav_reader`
    FOREIGN KEY (`reader_id`)
    REFERENCES `bookLib`.`reader` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_fav_book`
    FOREIGN KEY (`book_id`)
    REFERENCES `bookLib`.`book` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`book_status`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`book_status` (
                                                       `book_id` BIGINT NOT NULL,
                                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                       `reader_id` BIGINT NOT NULL,
                                                       `status` ENUM('WANT_TO_READ', 'READING', 'FINISHED') NOT NULL DEFAULT 'WANT_TO_READ',
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `fk_book_status_reader1_idx` (`reader_id` ASC) VISIBLE,
    INDEX `fk_book_status_book1_idx` (`book_id` ASC) VISIBLE,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_book_status_reader1`
    FOREIGN KEY (`reader_id`)
    REFERENCES `bookLib`.`reader` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_book_status_book1`
    FOREIGN KEY (`book_id`)
    REFERENCES `bookLib`.`book` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`reading_session`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`reading_session` (
                                                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                           `reader_id` BIGINT NOT NULL,
                                                           `book_id` BIGINT NOT NULL,
                                                           `pages_read` INT NOT NULL,
                                                           `duration_minutes` INT NOT NULL,
                                                           `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                                           PRIMARY KEY (`id`),
    INDEX `fk_reading_session_reader1_idx` (`reader_id` ASC) VISIBLE,
    INDEX `fk_reading_session_book1_idx` (`book_id` ASC) VISIBLE,
    CONSTRAINT `fk_reading_session_reader1`
    FOREIGN KEY (`reader_id`)
    REFERENCES `bookLib`.`reader` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_reading_session_book1`
    FOREIGN KEY (`book_id`)
    REFERENCES `bookLib`.`book` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`review`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`review` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                  `book_id` BIGINT NOT NULL,
                                                  `reader_id` BIGINT NOT NULL,
                                                  `rating` DECIMAL(2,1) NOT NULL,
    `review_text` TEXT NULL,
    `date_finished` DATE NOT NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `fk_review_book1_idx` (`book_id` ASC) VISIBLE,
    INDEX `fk_review_reader1_idx` (`reader_id` ASC) VISIBLE,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_review_book1`
    FOREIGN KEY (`book_id`)
    REFERENCES `bookLib`.`book` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_review_reader1`
    FOREIGN KEY (`reader_id`)
    REFERENCES `bookLib`.`reader` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`author`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`author` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                  `name` VARCHAR(150) NOT NULL,
    `country` VARCHAR(100) NULL,
    `birth_year` YEAR NULL,
    `death_year` YEAR NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `bookLib`.`book_author`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookLib`.`book_author` (
                                                       `author_id` BIGINT NOT NULL,
                                                       `book_id` BIGINT NOT NULL,
                                                       INDEX `fk_author_has_book_book1_idx` (`book_id` ASC) VISIBLE,
    INDEX `fk_author_has_book_author1_idx` (`author_id` ASC) VISIBLE,
    PRIMARY KEY (`author_id`, `book_id`),
    CONSTRAINT `fk_author_has_book_author1`
    FOREIGN KEY (`author_id`)
    REFERENCES `bookLib`.`author` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_author_has_book_book1`
    FOREIGN KEY (`book_id`)
    REFERENCES `bookLib`.`book` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
