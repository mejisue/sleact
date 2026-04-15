#!/bin/bash
# 3개 프로젝트 DB를 단일 MySQL 인스턴스에 생성
# MYSQL_USER, MYSQL_ROOT_PASSWORD 는 compose env_file(.env.prod)에서 주입됨

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
  CREATE DATABASE IF NOT EXISTS sleact CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE DATABASE IF NOT EXISTS mywebsite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE DATABASE IF NOT EXISTS retweet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

  GRANT ALL PRIVILEGES ON sleact.*   TO '${MYSQL_USER}'@'%';
  GRANT ALL PRIVILEGES ON mywebsite.* TO '${MYSQL_USER}'@'%';
  GRANT ALL PRIVILEGES ON retweet.*  TO '${MYSQL_USER}'@'%';
  FLUSH PRIVILEGES;
EOSQL
