-- BookLib minimal init.sql (MySQL 8+)

CREATE DATABASE IF NOT EXISTS booklib
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bookLib;

-- ----------------------------
-- reader
-- ----------------------------
CREATE TABLE IF NOT EXISTS reader (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reader_name (name)
    ) ENGINE=InnoDB;

-- (optional) default user: name=, password=dev (sha-256 hex)
INSERT INTO reader (id, name, password_hash)
VALUES (1, 'Default Reader', 'ef260e9aa3c673af240d17a2660480361a8e081d1ffeca2a5ed0e3219fc18567')
    ON DUPLICATE KEY UPDATE
                         name = VALUES(name),
                         password_hash = VALUES(password_hash);

-- ----------------------------
-- book (global catalog)
-- ----------------------------
CREATE TABLE IF NOT EXISTS book (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NULL,
    pages INT NOT NULL,
    genre VARCHAR(50) NOT NULL,
    language VARCHAR(10) NULL DEFAULT 'en',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- helps CSV import avoid duplicates (optional but recommended)
    UNIQUE KEY uk_book_title_author (title, author)
    ) ENGINE=InnoDB;

-- ----------------------------
-- book_status (books owned by a reader + status)
-- ----------------------------
CREATE TABLE IF NOT EXISTS book_status (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           reader_id BIGINT NOT NULL,
                                           book_id BIGINT NOT NULL,
                                           status ENUM('WANT_TO_READ','READING','FINISHED') NOT NULL DEFAULT 'WANT_TO_READ',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_status_reader_book (reader_id, book_id),
    INDEX idx_book_status_reader (reader_id),
    INDEX idx_book_status_book (book_id),
    CONSTRAINT fk_book_status_reader
    FOREIGN KEY (reader_id) REFERENCES reader(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_book_status_book
    FOREIGN KEY (book_id) REFERENCES book(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- ----------------------------
-- reading_session (per reader & book)
-- ----------------------------
CREATE TABLE IF NOT EXISTS reading_session (
                                               id BIGINT NOT NULL AUTO_INCREMENT,
                                               reader_id BIGINT NOT NULL,
                                               book_id BIGINT NOT NULL,
                                               pages_read INT NOT NULL DEFAULT 0,
                                               duration_minutes INT NOT NULL DEFAULT 0,
                                               created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                               PRIMARY KEY (id),
    INDEX idx_reading_session_reader (reader_id),
    INDEX idx_reading_session_book (book_id),
    CONSTRAINT fk_reading_session_reader
    FOREIGN KEY (reader_id) REFERENCES reader(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_reading_session_book
    FOREIGN KEY (book_id) REFERENCES book(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- ----------------------------
-- review (one review per reader per book)
-- ----------------------------
CREATE TABLE IF NOT EXISTS review (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      reader_id BIGINT NOT NULL,
                                      book_id BIGINT NOT NULL,
                                      rating INT NOT NULL,
                                      comment TEXT NULL,
                                      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id),
    UNIQUE KEY uk_review_reader_book (reader_id, book_id),
    INDEX idx_review_reader (reader_id),
    INDEX idx_review_book (book_id),
    CONSTRAINT fk_review_reader
    FOREIGN KEY (reader_id) REFERENCES reader(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_review_book
    FOREIGN KEY (book_id) REFERENCES book(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;
