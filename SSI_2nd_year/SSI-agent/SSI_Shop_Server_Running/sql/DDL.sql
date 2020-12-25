CREATE TABLE `User` (
    `phone_id` VARCHAR(500) NOT NULL COMMENT '핸드폰 UUID' COLLATE 'utf8_general_ci',
    `did` VARCHAR(2000) NOT NULL COMMENT 'DID' COLLATE 'utf8_general_ci',
	`username` VARCHAR(50) NOT NULL COMMENT '아이디' COLLATE 'utf8_general_ci',
	`password` VARCHAR(250) NOT NULL COMMENT '비밀번호' COLLATE 'utf8_general_ci',
	`name` VARCHAR(50) NULL DEFAULT NULL COMMENT '이름' COLLATE 'utf8_general_ci',
	`role` VARCHAR(20) NOT NULL COMMENT '권한' COLLATE 'utf8_general_ci',
	`phone` VARCHAR(13) NULL DEFAULT NULL COMMENT '전화번호' COLLATE 'utf8_general_ci',
	`address` VARCHAR(255) NULL DEFAULT NULL COMMENT '주소' COLLATE 'utf8_general_ci',
	`token` VARCHAR(255) NULL DEFAULT NULL COMMENT 'FCM 토큰' COLLATE 'utf8_general_ci',
	PRIMARY KEY (`username`) USING BTREE
)
COMMENT='사용자'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `Product` (
    `productId` INT(11) NOT NULL AUTO_INCREMENT,
    `productName` VARCHAR(50) NOT NULL COMMENT '상품명' COLLATE 'utf8_general_ci',
    `description` MEDIUMTEXT NULL DEFAULT NULL COMMENT '상품 설명' COLLATE 'utf8_general_ci',
    `price` BIGINT(20) NULL DEFAULT NULL COMMENT '가격',
    `type` VARCHAR(50) NULL DEFAULT NULL COMMENT '새제품,중고품' COLLATE 'utf8_general_ci',
    `did` VARCHAR(255) NULL DEFAULT NULL COMMENT '로그인 did' COLLATE 'utf8_general_ci',
    `didSelected` VARCHAR(512) NULL COMMENT 'DID선택 버튼으로 선택한 DID',
    `address` VARCHAR(255) NULL DEFAULT NULL COMMENT '판매자 주소' COLLATE 'utf8_general_ci',
    `createUser` VARCHAR(50) NULL DEFAULT NULL COMMENT '등록자 아이디' COLLATE 'utf8_general_ci',
    `createDate` DATETIME NULL DEFAULT NULL COMMENT '등록일',
    `productDid` VARCHAR(512) NULL COMMENT '물품정보DID',
    `manufacturerDid` VARCHAR(512) NULL COMMENT '제조사DID',
    `manufacturer` VARCHAR(50) NULL COMMENT '제조사',
    `madeDate` DATETIME NULL COMMENT '제조일',
    `serialNum` VARCHAR(512) NULL COMMENT '물품 serial number',
    `productWallet` VARCHAR(1024) NULL COMMENT '물품정보 wallet',
    `manufacturerWallet` VARCHAR(1024) NULL COMMENT '제조사 wallet',
    `buyCnt` INT(11) default 0 NULL COMMENT '구매수량',
    `reviewCnt` INT(11) default 0 NULL COMMENT '리뷰수',
    PRIMARY KEY (`productId`) USING BTREE
)
COMMENT='상품'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `Product_img` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `productId` INT(11) NOT NULL COMMENT '상품 아이디',
    `img` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '이미지 파일명' COLLATE 'utf8_general_ci',
    PRIMARY KEY (`id`) USING BTREE
)
COMMENT='상품이미지'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `Deal` (
    `dealId` INT(11) NOT NULL AUTO_INCREMENT,
    `productId` INT(11) NOT NULL COMMENT '상품아이디',
    `buyer` VARCHAR(2000) NOT NULL COMMENT '구매자 아이디' COLLATE 'utf8_general_ci',
    `buyerName` VARCHAR(50) NOT NULL COMMENT '구매자 이름' COLLATE 'utf8_general_ci',
    `request` VARCHAR(200) NULL DEFAULT NULL COMMENT '요청사항' COLLATE 'utf8_general_ci',
    `paymentMethod` VARCHAR(50) NULL DEFAULT NULL COMMENT '결제 방식' COLLATE 'utf8_general_ci',
    `paymentCard` VARCHAR(50) NULL DEFAULT NULL COMMENT '카드결제 회사' COLLATE 'utf8_general_ci',
    `state` VARCHAR(10) NULL DEFAULT NULL COMMENT '거래 진행 상태' COLLATE 'utf8_general_ci',
    `dealDate` DATETIME NOT NULL COMMENT '거래 일자',
    `count` INT(11) NULL DEFAULT NULL COMMENT '거래 개수',
    `pricePerOne` INT(11) DEFAULT 0 NULL COMMENT '개당 구매 가격',
    `totalPrice` INT(11) DEFAULT 0 NULL COMMENT '최종 구매한 전체 가격',
    `address` VARCHAR(255) NULL DEFAULT NULL COMMENT '배송지주소' COLLATE 'utf8_general_ci',
    `phone` VARCHAR(13) NULL DEFAULT NULL COMMENT '배송지 전화번호' COLLATE 'utf8_general_ci',
    `publicKey` VARCHAR(255) NULL DEFAULT NULL COMMENT '구매자 public key' COLLATE 'utf8_general_ci',
    `did` VARCHAR(255) NULL DEFAULT NULL COMMENT '로그인 did' COLLATE 'utf8_general_ci',
    `didSeller` VARCHAR(512) NULL DEFAULT NULL COMMENT '판매자 did. 2020.11.16 기준=물건등록당시 등록자 DID' COLLATE 'utf8_general_ci',
    `didSelected` VARCHAR(512) NULL COMMENT 'DID선택 버튼으로 선택한 DID. 2020.11.16 기준=물건등록당시 선택한DID',
    `didBuyer` VARCHAR(512) NULL COMMENT 'DID선택 버튼으로 선택한 DID. SSI APP에서 넘겨주는 값. didSelected는 등록시 선택한 DID. 구매자 DID. 2020.11.16 기준=물품보증서요청시 선택한 DID',
    `vcState` VARCHAR(10) NULL DEFAULT NULL COMMENT 'VC 발급 상태' COLLATE 'utf8_general_ci',
    `buyNSell` CHAR DEFAULT 'B' NULL COMMENT '구매 or 판매 여부. B=구매, S=판매',
    `etc` VARCHAR(4000) NULL DEFAULT NULL COMMENT 'VC, VP. 의미없음' COLLATE 'utf8_general_ci',
	`sign` VARCHAR(512) NULL COMMENT '거래증명VC 발급을 위한 signature',
    PRIMARY KEY (`dealId`) USING BTREE
)
COMMENT='거래내역'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


create table `PushMngr`
(
	`did` varchar(512) null comment '사용자 DID',
	`token` varchar(512) null comment 'FCM 푸시용 token',
	`create_date` datetime default current_timestamp null comment '생성일',
	`update_date` varchar(512) null
)
comment 'FCM 알람 정보(DID, Token 맵핑)';
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
