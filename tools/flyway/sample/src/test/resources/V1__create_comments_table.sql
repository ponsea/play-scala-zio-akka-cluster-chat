CREATE TABLE `comments` (
  `id`               BINARY(16)   NOT NULL PRIMARY KEY,
  `author_id`        VARCHAR(254) NOT NULL,
  `content`          VARCHAR(254) NOT NULL,
  `conversation_key` VARCHAR(254) NOT NULL,
  `created_at`       VARCHAR(40)  NOT NULL,
  `updated_at`       VARCHAR(40)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
