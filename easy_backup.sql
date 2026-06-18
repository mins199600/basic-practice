/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.5.2-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: easy
-- ------------------------------------------------------
-- Server version	11.5.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '게시글 고유 식별자',
  `title` varchar(200) NOT NULL COMMENT '게시글 제목',
  `content` text NOT NULL COMMENT '게시글 내용',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '작성일시',
  `modified_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `view_count` int(11) DEFAULT 0 COMMENT '조회수',
  `category` varchar(50) DEFAULT NULL COMMENT '게시글 카테고리',
  `is_notice` tinyint(1) DEFAULT 0 COMMENT '공지사항 여부',
  `file_path` varchar(255) DEFAULT NULL COMMENT '첨부파일 경로',
  `member_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_notice` (`is_notice`),
  KEY `member_id` (`member_id`),
  CONSTRAINT `board_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board`
--

LOCK TABLES `board` WRITE;
/*!40000 ALTER TABLE `board` DISABLE KEYS */;
INSERT INTO `board` VALUES
(3,'qwe','qwe','2026-04-13 11:44:56',NULL,1,'자유',0,NULL,0),
(5,'qwe','qwe','2026-04-13 11:49:35',NULL,0,'자유',0,NULL,0),
(7,'123','123','2026-04-13 11:57:40',NULL,0,'정보',0,NULL,0),
(8,'1','3','2026-04-13 12:38:06',NULL,1,'질문',0,NULL,0),
(9,'45','44','2026-04-13 12:39:49',NULL,0,'자유',0,NULL,0),
(13,'33','gfgfgf','2026-04-17 22:32:41','2026-04-18 00:03:06',26,'정보',0,NULL,0),
(18,'wqe','qweqweqweqwe','2026-05-16 22:30:03','2026-05-16 22:30:03',0,'일반',0,NULL,29),
(20,'123','12312312','2026-05-19 13:11:18','2026-05-19 13:11:18',0,'문의',0,NULL,29),
(24,'234234','234234234234423','2026-05-19 14:32:53','2026-05-19 14:32:53',0,'일반',0,NULL,29),
(25,'234234ttrrttr','erterterdfgdgdgdddgg','2026-05-19 14:33:02','2026-05-19 14:33:02',0,'정보',0,NULL,29),
(26,'werwedfgdfgfgdfgdfgdg','gdfgdfggfgdfgvbvcbccbvbvc','2026-05-19 14:33:28','2026-05-19 14:33:28',0,'정보',0,NULL,29),
(27,'cvbbcbvccbvvcbvcbhhjjkjkjhkhkmn','bjalkjaljflaldjlsajflaldjldfjkdfjlkajflkjdslkjadsfkdsdsfsdf','2026-05-19 14:33:45','2026-05-19 14:33:45',0,'일반',0,NULL,29),
(28,'test23','23','2026-05-19 14:33:51','2026-05-19 14:33:51',0,'일반',0,NULL,29),
(29,'test2333','123123132','2026-05-19 14:34:00','2026-05-19 14:34:00',0,'일반',0,NULL,29),
(30,'test23','23123','2026-05-19 14:34:07','2026-05-19 14:34:07',0,'일반',0,NULL,29),
(31,'test2313123','erewwrwerbvvcvbcbcvbcvcvbcvbcbvcbcbvcbvcvbcvbc','2026-05-19 14:34:20','2026-05-19 14:34:20',0,'일반',0,NULL,29),
(32,'tstsdfsdfsdsdfsdfdsf','rdtertertakfa;sfk;sd;lkfkfa;\'sa[oewr[qfa[fo[e[row[edsfk;lk;ak;lkfd','2026-05-19 14:34:48','2026-05-19 14:34:48',0,'일반',0,NULL,29),
(33,'teset','twerwerwerretertretertertgdfgdgfgdgf','2026-05-19 14:35:21','2026-05-19 14:35:21',0,'일반',0,NULL,29),
(34,'test01','test01 글쓰기','2026-05-19 14:36:18','2026-05-19 14:36:18',0,'일반',0,NULL,29),
(35,'test02','인정인정','2026-05-19 14:36:32','2026-05-19 14:36:32',0,'일반',0,NULL,29),
(36,'test03','02020300320230302','2026-05-19 14:36:46','2026-05-19 14:36:46',0,'일반',0,NULL,29),
(37,'test04','test040404','2026-05-19 14:37:22','2026-05-19 14:37:22',0,'일반',0,NULL,29),
(38,'test03040404','234errteertdgddgfdgdgxvxvcxcvxadazcxxzcetrretgfhghnbvnbvnv  gddfgg','2026-05-19 14:37:48','2026-05-19 14:37:48',0,'일반',0,NULL,29),
(39,'test09090','23ewrwer','2026-05-19 14:38:00','2026-05-19 14:38:00',0,'일반',0,NULL,29),
(40,'test','33444gg','2026-05-19 14:38:07','2026-05-19 14:38:07',0,'일반',0,NULL,29),
(41,'1231231','123123123123123','2026-05-19 18:25:28','2026-05-19 18:25:28',0,'문의',0,NULL,29),
(42,'1231231','123123123123123','2026-05-19 18:25:42','2026-05-19 18:25:42',0,'문의',0,NULL,29),
(45,'asdasdadsasdasdasdsadㅈㄷㄱㅈㄷㄱㅈㄷㄱ','alkjflsajflkjsalsjdflk ljlsajldsjfl jflajflkajdflkkjiowreowiurojjflkasjlfal ldjflasjflasjfdlasdfsafds\r\n\r\n수정저수어저어수정\r\n수정이요~~ 내가 왕이 될 상인가>','2026-05-19 18:56:06','2026-05-21 19:26:38',0,'일반',0,NULL,29),
(46,'weqeqewq','qewqeqeqwe','2026-05-21 19:28:14','2026-05-21 19:28:14',0,'문의',0,NULL,30),
(47,'test1','122','2026-05-21 19:28:20','2026-05-21 19:28:20',0,'일반',0,NULL,30),
(48,'test2','werwer','2026-05-21 19:28:25','2026-05-21 19:28:25',0,'일반',0,NULL,30),
(49,'test4','wwer','2026-05-21 19:28:30','2026-05-21 19:28:30',0,'일반',0,NULL,30),
(50,'123','123213123','2026-05-30 16:11:01','2026-05-30 16:11:01',0,'문의',0,NULL,32),
(51,'123','123123123123','2026-05-30 16:11:38','2026-05-30 16:11:38',0,'일반',0,NULL,31),
(52,'1``12`2``1wdasdasd','qsadada','2026-05-30 16:12:08','2026-05-30 16:12:08',0,'일반',0,NULL,31),
(53,'324234rrrrrrrrrrrrrrrr','erwerwerrrrrrrrrrrrrrr','2026-06-04 15:17:03','2026-06-04 15:17:26',0,'일반',0,NULL,32),
(54,'3223423234234','234234fdgdgfdfgdfgdgdfgdgfdfgdfg','2026-06-06 14:55:26','2026-06-06 14:55:26',0,'문의',0,NULL,34);
/*!40000 ALTER TABLE `board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `board_id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `board_id` (`board_id`),
  KEY `member_id` (`member_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES
(3,27,32,NULL,'댓글 대댓글 기능 추가','2026-06-04 07:23:10','2026-06-04 07:23:10'),
(4,27,32,3,'내가 함ㅋ','2026-06-04 07:23:17','2026-06-04 07:23:17'),
(9,45,31,NULL,'왕이될상??','2026-06-04 07:50:04','2026-06-04 07:50:04'),
(10,45,32,9,'pretender 노래 개조음ㅋㅋㅋㅋㅋ늙크크ㅋㅋ','2026-06-04 07:50:30','2026-06-04 09:24:22'),
(14,52,32,NULL,'lkjlkjlk','2026-06-04 09:51:26','2026-06-04 09:51:26'),
(15,52,32,14,'nmbnm','2026-06-04 09:51:32','2026-06-04 09:51:32');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL COMMENT '회원 이메일',
  `nickname` varchar(50) DEFAULT NULL COMMENT '회원 닉네임',
  `password` varchar(100) NOT NULL COMMENT '암호화된 비밀번호',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '회원 권한',
  `reg_date` datetime DEFAULT current_timestamp() COMMENT '가입일시',
  `address` varchar(255) DEFAULT NULL COMMENT '주소',
  `detail_address` varchar(255) DEFAULT NULL COMMENT '상세주소',
  `postcode` varchar(10) DEFAULT NULL COMMENT '우편번호',
  `update_date` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '회원정보 수정일시',
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `member_unique` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES
(1,'mins199600@naver.com',NULL,'1234','ADMIN',NULL,NULL,NULL,NULL,'2026-06-04 07:42:14',0),
(2,'mins199600@nate.com',NULL,'1234','USER',NULL,NULL,NULL,NULL,NULL,0),
(3,'mins199600@cuk.edu',NULL,'1234','USER',NULL,NULL,NULL,NULL,NULL,0),
(4,'mins@naver.com',NULL,'1234','USER',NULL,NULL,NULL,NULL,NULL,0),
(5,'123@naver.com',NULL,'123','USER',NULL,NULL,NULL,NULL,NULL,0),
(6,'mi@naver.com',NULL,'123','USER',NULL,NULL,NULL,NULL,NULL,0),
(7,'m@test.com',NULL,'1234','USER',NULL,NULL,NULL,NULL,NULL,0),
(8,'mi19@gmail.com',NULL,'chzh12!@','USER','2026-04-06 05:20:23','부산 기장군 장안읍 판곡길 2','2층','46031',NULL,0),
(9,'test@test.com',NULL,'1234','USER',NULL,NULL,NULL,NULL,NULL,0),
(10,'ex@t.com',NULL,'123','USER',NULL,NULL,NULL,NULL,NULL,0),
(11,'t@test.com',NULL,'213','USER',NULL,NULL,NULL,NULL,NULL,0),
(12,'e@t.com',NULL,'$2a$10$K2m9lgfYbz2rbo2KOWGqxuS2zMVQale/kAFl7oWVy2ZQXjAUeTOcW','USER',NULL,NULL,NULL,NULL,NULL,0),
(13,'l@gmail.com',NULL,'$2a$10$hDvvuYZe5R5y7aJVA/nPxOhz1hAFXKlIRdlrVycvbTfTuffnI3r.G','USER',NULL,NULL,NULL,NULL,NULL,0),
(14,'test@com',NULL,'$2a$10$4R/DI0hwvvUUAI1sCzFWiO5EUpnpSLlMtOj8qT9iVb6oLUY6ripYi','USER',NULL,NULL,NULL,NULL,NULL,0),
(15,'e@v.com',NULL,'$2a$10$5.pIucrgnJArZNbdd/umMuCMuNFfVlGMMKC/3LGR2BKuF.68y4aYO','USER',NULL,NULL,NULL,NULL,NULL,0),
(16,'1234@v.com',NULL,'$2a$10$yOhKD5GcFZvvGN.LJE/RJun57McASoQVfRMezbO7TqStUON0KEWl2','USER',NULL,NULL,NULL,NULL,NULL,0),
(17,'user@v.com',NULL,'$2a$10$bFVSzFvyC5I5os3m4CMsNehWiaN.RJjp/wn5TPEKn/pgEHJy0dNlm','USER',NULL,NULL,NULL,NULL,NULL,0),
(18,'eeee@v.com',NULL,'$2a$10$1LxOd93QISD0Mw6f6xVbbeZDM/w0v0auawexDenJ9AIdvPHLWo/FG','USER',NULL,NULL,NULL,NULL,NULL,0),
(20,'example@test.com',NULL,'$2a$10$owVBYuUtI.Vyn.GUZ5V/tek.niolLtHCFxUpoS4AugNTcQOQKLyLK','USER','2026-05-05 21:05:20',NULL,NULL,NULL,'2026-05-05 12:05:20',0),
(21,'ee@c.com',NULL,'$2a$10$JnxV22XXw/d0b0gASNxcuOJ0KLH2OIzrcVBQNkHbHoJ0j8/8cUOMK','USER','2026-05-05 21:17:34',NULL,NULL,NULL,'2026-05-05 12:17:34',0),
(22,'anstn@test.com',NULL,'$2a$10$WSwY996A.yKb7puffebWUOwhBmSE4AyZ7bf4WtsRW7KU8/82WnvHa','USER','2026-05-08 13:37:10',NULL,NULL,NULL,'2026-05-08 04:37:10',0),
(23,'l@test.com',NULL,'$2a$10$c2LO4mTVtN3jcTN3IvJhPOwHNXvAm8EC0T1roqVQ/jVzafF0auZPy','USER','2026-05-08 13:38:43',NULL,NULL,NULL,'2026-05-08 04:38:43',0),
(24,'lee@test.com',NULL,'$2a$10$124SZVqHckbMRZR.stydH./HRlk2JeBi/czNRIZkLVhbRz/dmw85u','USER','2026-05-08 13:41:12',NULL,NULL,NULL,'2026-05-08 04:41:12',0),
(25,'tttt@test.com',NULL,'$2a$10$seVnUYku7TY7vfaHNWmrVeKmYE1SLgM.JoJFJQz5dzo6yKVczoDFG','USER','2026-05-08 13:50:59',NULL,NULL,NULL,'2026-05-08 04:50:59',0),
(26,'example1@test.com',NULL,'$2a$10$S0K8D6.qrTUD8GY3amPBN.vcRnMSWNlyBvKjS27uTXPHurHFOv.XK','USER','2026-05-08 13:55:38',NULL,NULL,NULL,'2026-05-08 04:55:38',0),
(27,'bbong@test.com','이뽕이','$2a$10$cAZycVfb2qYGoD/OgPdtL.S96MNRmyOIWtg4aNdIiMJjCj16ZqRcu','USER','2026-05-08 17:58:37',NULL,NULL,NULL,'2026-05-08 11:04:23',0),
(29,'qwer@test.com','이초코','$2a$10$LrVqq2.nr1//NcHaVMEjqOCB8dyoRyzQczodbAk71G.ouKwRD1Nte','USER','2026-05-16 12:51:26',NULL,NULL,NULL,'2026-05-21 10:27:13',1),
(30,'asdf@test.com','이뽕장군','$2a$10$4/J3/4u1U9pkIDM20s4fReMisIB6wCndbzgyM0Jfnh/2HtvBdv6BG','USER','2026-05-21 19:27:58',NULL,NULL,NULL,'2026-05-21 10:42:12',0),
(31,'ex@test.com','뽕장군초코핑','$2a$10$HdPCJES3ZnQ8jwzv3fCFAORj1vwjfbQxPZGxPqs4BAQ.6S3WXLfTO','USER','2026-05-21 22:28:15',NULL,NULL,NULL,'2026-05-21 13:34:56',0),
(32,'gggg@test.com','이수민맛쿠키','$2a$10$wPiYYtIBCE7CukqgMZFUQOeIz97pe6ix6Tr0ABPFYAzdDKbhOVm2S','USER','2026-05-30 16:10:32',NULL,NULL,NULL,'2026-06-04 07:49:14',0),
(33,'e@test.com','123','$2a$10$HqVmQVte/emAYQ2nhmr9GuJtGVIdrEm0aWIEpEtQ/OA4nQ.EUs906','USER','2026-06-04 16:23:54',NULL,NULL,NULL,'2026-06-04 07:23:54',0),
(34,'dltnals@test.com','초코맛문수핑','$2a$10$bh7e.B8f18pT1C8mprbtCOKiqay7i44MoUxYd/hhXspsJamwWlLyK','USER','2026-06-06 14:54:43',NULL,NULL,NULL,'2026-06-06 05:54:43',0),
(35,'Admin1@test.com','이초코','$2a$10$0UFpvcHbcui/h9XVMSmRIeHsCLj1yxHPEDpW2jP6McKMk5WctTjTS','ADMIN','2026-06-09 17:03:32',NULL,NULL,NULL,'2026-06-16 05:40:32',0),
(37,'Admin2@test.com','이뽕이','1234','ADMIN','2026-06-09 17:05:31',NULL,NULL,NULL,'2026-06-09 08:05:31',0),
(38,'Admin3@test.com',NULL,'$2a$10$ZsCX4RB0NJClAlEFkfaOGO2qSxNIZFuALDrfkq4XQKtZw71rUCKSm','ADMIN','2026-06-11 22:37:23',NULL,NULL,NULL,'2026-06-11 13:37:23',0),
(39,'Admin4@test.com',NULL,'$2a$10$N00Q2ayQd.Rz.Y5ikqEA2uIg53VatP7ZmIp5ilkIngDlaLkKo.Qhi','ADMIN','2026-06-12 22:25:31',NULL,NULL,NULL,'2026-06-12 13:25:31',0);
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `popup`
--

DROP TABLE IF EXISTS `popup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `popup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `start_date` varchar(20) DEFAULT NULL,
  `end_date` varchar(20) DEFAULT NULL,
  `use_yn` char(1) DEFAULT 'Y',
  `reg_date` datetime DEFAULT current_timestamp(),
  `update_date` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `popup`
--

LOCK TABLES `popup` WRITE;
/*!40000 ALTER TABLE `popup` DISABLE KEYS */;
/*!40000 ALTER TABLE `popup` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-06-18 15:40:49
