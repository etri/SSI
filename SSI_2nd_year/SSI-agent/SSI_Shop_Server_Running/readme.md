## Table Schema

```sql
CREATE TABLE `user` (
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_general_ci',
	`password` VARCHAR(250) NOT NULL COLLATE 'utf8mb4_general_ci',
	`name` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`role` VARCHAR(20) NOT NULL COLLATE 'utf8mb4_general_ci',
	`phone` VARCHAR(13) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	`address` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci',
	PRIMARY KEY (`username`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `product` (
	`productId` INT(11) NOT NULL AUTO_INCREMENT,
	`productName` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`description` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`price` BIGINT(20) NULL DEFAULT NULL,
	`type` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`did` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`address` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`createUser` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`createDate` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`productId`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
AUTO_INCREMENT=4
;

CREATE TABLE `Product_img` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`productId` INT(11) NOT NULL,
	`img` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_0900_ai_ci',
	PRIMARY KEY (`id`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
AUTO_INCREMENT=5
;

CREATE TABLE `deal` (
	`dealId` INT(11) NOT NULL AUTO_INCREMENT,
	`productId` INT(11) NOT NULL,
	`buyer` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`request` VARCHAR(200) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`paymentMethod` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`state` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`dealDate` DATETIME NOT NULL,
	`count` INT(11) NULL DEFAULT NULL,
	`address` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`phone` VARCHAR(13) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	PRIMARY KEY (`dealId`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
AUTO_INCREMENT=16
;

```

## 테스트 user 생성 방법
UserRepositoryTest.testSave 유닛테스트 실행해서 user1 생성