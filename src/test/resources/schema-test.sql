-- schema-test.sql (clean for tests)
-- Same structure as your init.sql, but WITHOUT reading_goal
-- and without Workbench @OLD_* variables.

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;

-- Drop in correct FK order (also drop reading_goal if it exists in your dev DB)
DROP TABLE IF EXISTS reading_session;
DROP TABLE IF EXISTS book_status;
DROP TABLE IF EXISTS favorite_books;
DROP TABLE IF EXISTS reading_goal;   -- IMPORTANT: in case it exists in the DB already
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;

-- -----------------------------------------------------
-- Table `reader`
-- -----------------------------------------------------
CREATE TABLE reader (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_reader_name (name)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table `book`
-- -----------------------------------------------------
CREATE TABLE book (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      title VARCHAR(255) NOT NULL,
                      author VARCHAR(255) NULL,
                      pages INT NOT NULL,
                      genre VARCHAR(50) NOT NULL,
                      language VARCHAR(10) NULL DEFAULT 'en',
                      created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (id)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table `favorite_books`
-- -----------------------------------------------------
CREATE TABLE favorite_books (
                                reader_id BIGINT NOT NULL,
                                book_id BIGINT NOT NULL,
                                PRIMARY KEY (reader_id, book_id),
                                INDEX fk_fav_book_idx (book_id ASC),
                                CONSTRAINT fk_fav_reader
                                    FOREIGN KEY (reader_id)
                                        REFERENCES reader (id)
                                        ON DELETE CASCADE
                                        ON UPDATE NO ACTION,
                                CONSTRAINT fk_fav_book
                                    FOREIGN KEY (book_id)
                                        REFERENCES book (id)
                                        ON DELETE CASCADE
                                        ON UPDATE NO ACTION
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table `book_status`
-- -----------------------------------------------------
CREATE TABLE book_status (
                             book_id BIGINT NOT NULL,
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             reader_id BIGINT NOT NULL,
                             status ENUM('WANT_TO_READ', 'READING', 'FINISHED') NOT NULL DEFAULT 'WANT_TO_READ',
                             created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                             INDEX fk_book_status_reader1_idx (reader_id ASC),
                             INDEX fk_book_status_book1_idx (book_id ASC),

                             PRIMARY KEY (id),
                             UNIQUE KEY uk_book_status_reader_book (reader_id, book_id),

                             CONSTRAINT fk_book_status_reader1
                                 FOREIGN KEY (reader_id)
                                     REFERENCES reader (id)
                                     ON DELETE NO ACTION
                                     ON UPDATE NO ACTION,

                             CONSTRAINT fk_book_status_book1
                                 FOREIGN KEY (book_id)
                                     REFERENCES book (id)
                                     ON DELETE NO ACTION
                                     ON UPDATE NO ACTION
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table `reading_session`
-- -----------------------------------------------------
CREATE TABLE reading_session (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 reader_id BIGINT NOT NULL,
                                 book_id BIGINT NOT NULL,

                                 pages_read INT NOT NULL DEFAULT 0,
                                 duration_minutes INT NOT NULL DEFAULT 0,

                                 created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id),

                                 INDEX fk_reading_session_reader_idx (reader_id ASC),
                                 INDEX fk_reading_session_book_idx (book_id ASC),

                                 CONSTRAINT fk_reading_session_reader
                                     FOREIGN KEY (reader_id)
                                         REFERENCES reader (id)
                                         ON DELETE CASCADE
                                         ON UPDATE NO ACTION,

                                 CONSTRAINT fk_reading_session_book
                                     FOREIGN KEY (book_id)
                                         REFERENCES book (id)
                                         ON DELETE CASCADE
                                         ON UPDATE NO ACTION
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
