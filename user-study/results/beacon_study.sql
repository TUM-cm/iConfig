-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 09. Mrz 2017 um 09:37
-- Server Version: 5.5.53-0ubuntu0.14.04.1
-- PHP-Version: 5.5.9-1ubuntu4.20

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `beacon_study`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `beacon assignment`
--

CREATE TABLE IF NOT EXISTS `beacon assignment` (
  `id` int(11) NOT NULL,
  `sBeacon` varchar(16) NOT NULL,
  `mac` varchar(17) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `beacon assignment`
--

INSERT INTO `beacon assignment` (`id`, `sBeacon`, `mac`) VALUES
(3, '2F0446B54A6B9998', 'D6:82:27:05:E4:0B'),
(2, 'C28C3BC4AE8EAC43', 'EB:26:C9:E4:DC:5B'),
(1, '8EC968106F272A76', 'C9:ED:20:D2:6A:5F');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `result`
--

CREATE TABLE IF NOT EXISTS `result` (
  `user_id` int(11) NOT NULL DEFAULT '0',
  `gender` varchar(6) NOT NULL,
  `education` varchar(5) NOT NULL,
  `age` varchar(5) NOT NULL,
  `iot_experience` char(1) NOT NULL,
  `iot_configuration_difficulty` tinyint(1) NOT NULL,
  `iot_configuration` varchar(9) NOT NULL,
  `start_manual_config_1` int(11) NOT NULL,
  `end_manual_config_1` int(11) NOT NULL,
  `success_rate_manual_config_1` double NOT NULL,
  `error_rate_manual_config_1` double NOT NULL,
  `error_fields_manual_config_1` varchar(500) NOT NULL,
  `start_automatic_config_1` int(11) NOT NULL,
  `end_automatic_config_1` int(11) NOT NULL,
  `success_rate_automatic_config_1` double NOT NULL,
  `error_rate_automatic_config_1` double NOT NULL,
  `error_fields_automatic_config_1` varchar(500) NOT NULL,
  `start_manual_config_2` int(11) NOT NULL,
  `end_manual_config_2` int(11) NOT NULL,
  `success_rate_manual_config_2` double NOT NULL,
  `error_rate_manual_config_2` double NOT NULL,
  `error_fields_manual_config_2` varchar(500) NOT NULL,
  `start_automatic_config_2` int(11) NOT NULL,
  `end_automatic_config_2` int(11) NOT NULL,
  `success_rate_automatic_config_2` double NOT NULL,
  `error_rate_automatic_config_2` double NOT NULL,
  `error_fields_automatic_config_2` varchar(500) NOT NULL,
  `start_manual_config_3` int(11) NOT NULL,
  `end_manual_config_3` int(11) NOT NULL,
  `success_rate_manual_config_3` double NOT NULL,
  `error_rate_manual_config_3` double NOT NULL,
  `error_fields_manual_config_3` varchar(500) NOT NULL,
  `start_automatic_config_3` int(11) NOT NULL,
  `end_automatic_config_3` int(11) NOT NULL,
  `success_rate_automatic_config_3` double NOT NULL,
  `error_rate_automatic_config_3` double NOT NULL,
  `error_fields_automatic_config_3` varchar(500) NOT NULL,
  `beacon_config_order` varchar(20) NOT NULL,
  `iot_test_manual_configuration` char(1) NOT NULL,
  `iot_test_automatic_configuration` char(1) NOT NULL,
  `iot_manual_configuration_difficulty` tinyint(1) NOT NULL,
  `iot_manual_configuration_meaning` varchar(500) NOT NULL,
  `iot_automatic_configuration_difficulty` tinyint(1) NOT NULL,
  `iot_automatic_configuration_meaning` varchar(500) NOT NULL,
  `web_app_feature_monitor_config_status` tinyint(1) NOT NULL,
  `web_app_feature_localization` tinyint(1) NOT NULL,
  `web_app_feature_monitor_health` tinyint(1) NOT NULL,
  `web_app_feature_monitor_broken_status` tinyint(1) NOT NULL,
  `iconfig_use_cases_applications` varchar(500) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `result`
--

INSERT INTO `result` (`user_id`, `gender`, `education`, `age`, `iot_experience`, `iot_configuration_difficulty`, `iot_configuration`, `start_manual_config_1`, `end_manual_config_1`, `success_rate_manual_config_1`, `error_rate_manual_config_1`, `error_fields_manual_config_1`, `start_automatic_config_1`, `end_automatic_config_1`, `success_rate_automatic_config_1`, `error_rate_automatic_config_1`, `error_fields_automatic_config_1`, `start_manual_config_2`, `end_manual_config_2`, `success_rate_manual_config_2`, `error_rate_manual_config_2`, `error_fields_manual_config_2`, `start_automatic_config_2`, `end_automatic_config_2`, `success_rate_automatic_config_2`, `error_rate_automatic_config_2`, `error_fields_automatic_config_2`, `start_manual_config_3`, `end_manual_config_3`, `success_rate_manual_config_3`, `error_rate_manual_config_3`, `error_fields_manual_config_3`, `start_automatic_config_3`, `end_automatic_config_3`, `success_rate_automatic_config_3`, `error_rate_automatic_config_3`, `error_fields_automatic_config_3`, `beacon_config_order`, `iot_test_manual_configuration`, `iot_test_automatic_configuration`, `iot_manual_configuration_difficulty`, `iot_manual_configuration_meaning`, `iot_automatic_configuration_difficulty`, `iot_automatic_configuration_meaning`, `web_app_feature_monitor_config_status`, `web_app_feature_localization`, `web_app_feature_monitor_health`, `web_app_feature_monitor_broken_status`, `iconfig_use_cases_applications`) VALUES
(1, 'male', 'msc', '20-30', 'Y', 3, 'automatic', 1488969118, 1488969735, 1, 0, '', 1488971507, 1488971572, 1, 0, '', 1488970064, 1488970419, 0.8709677419354839, 0.12903225806451613, 'I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER', 1488971638, 1488971697, 1, 0, '', 1488970536, 1488970891, 0.8709677419354839, 0.12903225806451613, 'I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER', 1488971744, 1488971798, 1, 0, '', '1,2,3', 'Y', 'Y', 4, 'time consuming and error prone', 2, 'sometimes I had to write the s beacon id.', 3, 5, 4, 4, 'tba'),
(5, 'male', 'msc', '20-30', 'Y', 3, 'automatic', 1488547446, 1488550658, 0.8064516, 0.19354838, 'EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_CONNECTABLE_RATE, EDDYSTONE_UID_NON_CONNECTABLE_RATE', 1488552211, 1488552320, 1, 0, '', 1488548123, 1488549052, 0.9032258, 0.09677419, 'I_BEACON_UUID, EDDYSTONE_UID_INSTANCE, EDDYSTONE_UID_NAMESPACE', 1488551912, 1488552045, 1, 0, '', 1488549131, 1488551417, 0.83870965, 0.16129032, 'I_BEACON_UUID, I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER', 1488552090, 1488552174, 1, 0, '', '2,1,3', 'Y', 'Y', 5, 'everything about it.', 1, 'no', 3, 5, 5, 3, 'wifi auto configuration\r\napplication user trust'),
(6, 'male', 'phd', '30-40', 'Y', 4, 'automatic', 1488813203, 1488813685, 1, 0, '', 1488814054, 1488814303, 1, 0, '', 1488810605, 1488811980, 1, 0, '', 1488814358, 1488814497, 1, 0, '', 1488812749, 1488813192, 1, 0, '', 1488814541, 1488814636, 1, 0, '', '2,3,1', 'Y', 'Y', 5, 'bad ui, time consuming, error prone', 1, 'registration requires still some manual input', 5, 4, 5, 5, 'proximate services'),
(7, 'male', 'msc', '20-30', 'N', 3, 'automatic', 1488801852, 1488802534, 0.7741935, 0.22580644, 'I_BEACON_UUID, EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_CONNECTABLE_RATE, EDDYSTONE_UID_NON_CONNECTABLE_RATE', 1488804133, 1488804210, 1, 0, '', 1488802563, 1488803366, 0.83870965, 0.16129032, 'I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER, PASSWORD', 1488804285, 1488804330, 1, 0, '', 1488803420, 1488803804, 1, 0, '', 1488804404, 1488804456, 1, 0, '', '1,2,3', 'Y', 'Y', 4, 'It is quite clear where to enter which information but the whole process is really annoying, takes a lot of time and little mistakes can happen all the time.', 1, '-', 5, 5, 5, 5, '-'),
(8, 'male', 'msc', '20-30', 'Y', 3, 'manual', 1488888635, 1488889249, 1, 0, '', 1488890686, 1488890800, 1, 0, '', 1488889396, 1488889903, 0.967741935483871, 0.03225806451612903, 'PASSWORD', 1488890830, 1488890917, 1, 0, '', 1488889967, 1488890438, 1, 0, '', 1488890954, 1488891053, 1, 0, '', '1,2,3', 'Y', 'Y', 5, '´Time consuming, counter-intuitive', 2, 'Manually writing the sbeacon id is annoying', 4, 5, 4, 4, '.'),
(9, 'male', 'msc', '30-40', 'N', 3, 'automatic', 1488893911, 1488895255, 0.967741935483871, 0.03225806451612903, 'EDDYSTONE_URL', 1488897796, 1488897969, 1, 0, '', 1488895414, 1488896614, 0.9354838709677419, 0.06451612903225806, 'I_BEACON_UUID, PASSWORD', 1488898029, 1488898113, 1, 0, '', 1488896802, 1488897537, 0.9032258064516129, 0.0967741935483871, 'EDDYSTONE_URL_DAY_ADVERTISEMENT_RATE, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_URL_NIGHT_ADVERTISEMENT_RATE', 1488898181, 1488898291, 1, 0, '', '1,2,3', 'Y', 'Y', 4, 'Time consuming and error prone', 2, 'Typing the sensor id is time consuming. Adding a confirmation after taking the image is good', 5, 5, 5, 5, 'NA'),
(10, 'male', 'msc', '30-40', 'N', 3, 'automatic', 1488900261, 1488901348, 0.8709677419354839, 0.12903225806451613, 'I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER', 1488902720, 1488902897, 1, 0, '', 1488901363, 1488901908, 0.967741935483871, 0.03225806451612903, 'I_BEACON_UUID', 1488902937, 1488903056, 1, 0, '', 1488901923, 1488902407, 0.967741935483871, 0.03225806451612903, 'EDDYSTONE_UID_INSTANCE', 1488903094, 1488903191, 1, 0, '', '1,2,3', 'Y', 'Y', 4, '-', 2, '-', 3, 5, 4, 2, '-'),
(11, 'male', 'bsc', '20-30', 'N', 2, 'automatic', 1488957815, 1488958400, 1, 0, '', 1488960288, 1488960382, 1, 0, '', 1488958436, 1488959114, 1, 0, '', 1488961483, 1488961575, 1, 0, '', 1488959122, 1488959717, 1, 0, '', 1488960707, 1488960803, 1, 0, '', '1,2,3', 'Y', 'Y', 5, 'boring repetitive task,\r\neasy to make mistakes,\r\nmore than two beacons very time consuming', 1, 'very easy, one minor issue is the input of the sBeacon ID', 3, 4, 5, 4, 'tba'),
(12, 'male', 'msc', '20-30', 'N', 3, 'automatic', 1488964623, 1488965069, 0.6451612903225806, 0.3548387096774194, 'I_BEACON_DAY_ADVERTISEMENT_RATE, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_ADVERTISEMENT_RATE, I_BEACON_NIGHT_TRANSMISSION_POWER, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_CONNECTABLE_RATE, EDDYSTONE_UID_NON_CONNECTABLE_RATE', 1488966785, 1488966906, 1, 0, '', 1488965425, 1488965809, 0.7741935483870968, 0.22580645161290322, 'EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_CONNECTABLE_RATE, EDDYSTONE_UID_NON_CONNECTABLE_RATE, PASSWORD', 1488966924, 1488966986, 1, 0, '', 1488965864, 1488966465, 0.5806451612903226, 0.41935483870967744, 'EDDYSTONE_URL_DAY_ADVERTISEMENT_RATE, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_URL_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER, EDDYSTONE_URL_CONNECTABLE_RATE, EDDYSTONE_URL_NON_CONNECTABLE_RATE, EDDYSTONE_UID_DAY_ADVERTISEMENT_RATE, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_ADVERTISEMENT_RATE, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_CONNECTABLE_RATE, EDDYSTONE_UID_NON_CONNECTABLE_RATE, PASSWORD', 1488967006, 1488967080, 1, 0, '', '1,2,3', 'Y', 'Y', 4, 'Definitely time consuming! Too many steps before getting the configuration done.', 1, 'None', 4, 4, 5, 5, 'As use case I´m thinking about emergency situations, e.g. localization of people'),
(13, 'female', 'msc', '20-30', 'N', 4, 'automatic', 1488976299, 1488977167, 0.6774193548387096, 0.3225806451612903, 'S_BEACON_DAY_TRANSMISSION_POWER, S_BEACON_NIGHT_TRANSMISSION_POWER, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_TRANSMISSION_POWER, EDDYSTONE_TLM_DAY_TRANSMISSION_POWER, EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER', 1488978735, 1488978818, 1, 0, '', 1488977227, 1488977753, 0.6129032258064516, 0.3870967741935484, 'S_BEACON_DAY_TRANSMISSION_POWER, S_BEACON_NIGHT_TRANSMISSION_POWER, I_BEACON_MINOR, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_TRANSMISSION_POWER, EDDYSTONE_TLM_DAY_TRANSMISSION_POWER, EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER, EDDYSTONE_URL, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER', 1488978881, 1488978990, 1, 0, '', 1488977761, 1488978375, 0.6451612903225806, 0.3548387096774194, 'S_BEACON_DAY_TRANSMISSION_POWER, S_BEACON_NIGHT_TRANSMISSION_POWER, I_BEACON_DAY_TRANSMISSION_POWER, I_BEACON_NIGHT_TRANSMISSION_POWER, EDDYSTONE_TLM_DAY_TRANSMISSION_POWER, EDDYSTONE_TLM_NIGHT_TRANSMISSION_POWER, EDDYSTONE_URL, EDDYSTONE_URL_DAY_TRANSMISSION_POWER, EDDYSTONE_URL_NIGHT_TRANSMISSION_POWER, EDDYSTONE_UID_DAY_TRANSMISSION_POWER, EDDYSTONE_UID_NIGHT_TRANSMISSION_POWER', 1488979042, 1488979141, 1, 0, '', '1,2,3', 'Y', 'Y', 4, 'A lot of bugs\r\ntime consuming\r\nNo easy value check', 2, 'Why enter image, comments, location?\r\nWhat if I want to change the other values?\r\nBeacons jump around\r\nText fields not easy to click (too small)\r\ntText input always in the way of what i want to click next', 2, 3, 5, 5, 'Map with all registered beacon locations - e.g. for indoor localization');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `participation_date` date NOT NULL,
  `study_start` time NOT NULL,
  `study_end` time NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=14 ;

--
-- Daten für Tabelle `user`
--

INSERT INTO `user` (`id`, `email`, `full_name`, `participation_date`, `study_start`, `study_end`, `completed`) VALUES
(1, 'haus@in.tum.de', 'Michael Haus', '2017-03-08', '11:40:00', '12:20:00', 1),
(5, 'tonetto@in.tum.de', 'Leonardo Tonetto', '2017-03-03', '14:00:00', '15:55:00', 1),
(6, 'ding@in.tum.de', 'Aaron Yi Ding', '2017-03-06', '15:20:00', '16:45:00', 1),
(7, 'herzogd@in.tum.de', 'Daniel Herzog', '2017-03-06', '12:55:00', '13:54:00', 1),
(8, 'vittorio.cozzolino@in.tum.de', 'Vittorio Cozzolino', '2017-03-07', '13:00:00', '14:00:00', 1),
(9, 'ermishoo@gmail.com', 'Ermias Walelgne', '2017-03-07', '14:30:00', '15:55:00', 1),
(10, 'hauffa@in.tum.de', 'Jan Hauffa', '2017-03-07', '16:20:00', '17:15:00', 1),
(11, 'paulth@in.tum.de', 'Thomas Paul', '2017-03-08', '08:15:00', '09:20:00', 1),
(12, 'roberto.morabito@live.com', 'Roberto Morabito', '2017-03-08', '10:02:00', '11:05:00', 1),
(13, 'hanna.schaefer@tum.de', 'Hanna Schäfer', '2017-03-08', '13:26:00', '14:26:00', 1);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
