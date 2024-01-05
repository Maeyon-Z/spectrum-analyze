create database spectrum default character set utf8mb4 collate utf8mb4_general_ci;
CREATE USER 'spectrum'@'%' IDENTIFIED BY 'xgGRt2152GBnmVbPKjh';
GRANT ALL ON spectrum.* TO 'spectrum'@'%';
flush privileges;